package hr.tvz.njakopcic.zavrsnijakopcic.game;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.*;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.*;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.lights.DirectionalLight;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle.FlowParticleEmitter;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle.IParticleEmitter;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle.Particle;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.sound.SoundManager;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hr.tvz.njakopcic.zavrsnijakopcic.engine.GameItem.createItem;
import static org.lwjgl.glfw.GLFW.*;

public class SpaceGame implements IGameLogic {

    private final Renderer renderer;
    private final SoundManager soundManager;
    private final Camera camera;
    private Scene scene;
    private Hud hud;

    private boolean gameOver = false;
    private int score = 0;
    private int playerInc = 0;
    private boolean playerFiring = false;
    private long lastTimePlayerShot = 0;
    private GameItem playerItem;
    private Mesh bulletMesh;
    private Particle explosionParticle;
    private Particle trailParticle;
    private static final long TIME_BETWEEN_SHOTS = 300;
    private static final float PLAYER_POS_STEP = 0.4f;
    private static final float ENEMY_SIZE = 2f;
    private static final float BULLET_SPEED = 0.4f;
    private static final float ENEMY_SPEED = 0.05f;
    private static int ENEMY_MOVE_COUNTER = 0;

    private List<GameItem> bullets;
    private List<GameItem> enemies;
    private List<GameItem> gameItems;

    private List<FlowParticleEmitter> explosionEmitters;
    private Map<GameItem, FlowParticleEmitter> trailEmitters;

    private enum Sounds { MUSIC, PLAYER_FIRE, ENEMY_FIRE, EXPLOSION }

    public SpaceGame() {
        renderer = new Renderer();
        soundManager = new SoundManager();
        camera = new Camera();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);
        scene = new Scene();
        camera.setPosition(0, 0, 50.0f);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        gameItems = new ArrayList<>();
        explosionEmitters = new ArrayList<>();
        trailEmitters = new HashMap<>();

        // player spaceship setup
        playerItem = createItem("/models/spaceship.obj", "textures/spaceship.png", 1.0f);
        playerItem.setPosition(0, -20, 0);
        playerItem.setRotation(0, 0, 180);
        gameItems.add(playerItem);

        // background setup
        GameItem background = createItem("/models/plane.obj", "textures/back.png", 1.0f);
        background.setPosition(0, 0, -80);
        background.setRotation(-90, 0, 0);
        background.setScale(80);
        gameItems.add(background);

        // enemy setup
        Mesh enemyMesh = OBJLoader.loadMesh("/models/spaceship.obj");
        enemyMesh.setMaterial(new Material(new Texture("textures/enemy.png"), 1.0f));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 10; j++) {
                GameItem enemyItem = new GameItem(enemyMesh);
                enemyItem.setPosition(-28.5f + j*7, 20 - i*6, 0);
                enemies.add(enemyItem);
                gameItems.add(enemyItem);
            }
        }

        // bullet mesh setup (not initially added to the scene)
        bulletMesh = OBJLoader.loadMesh("/models/bullet.obj");
        bulletMesh.setMaterial(new Material(new Vector4f(1.0f, 0.3f, 0, 1), 1.0f));

        // explosion particle (template for explosion emitters)
        Mesh explosionMesh = OBJLoader.loadMesh("/models/explosion.obj");
        Texture explosionTexture = new Texture("textures/explosion_anim.png", 8, 6);
        explosionMesh.setMaterial(new Material(explosionTexture, 1.0f));
        explosionParticle = new Particle(explosionMesh, new Vector3f(0f), 1500, 20);

        // trail particle (template for bullet trail emitters)
        Mesh trailMesh = OBJLoader.loadMesh("/models/particle.obj");
        Texture trailTexture = new Texture("textures/particle_anim.png", 4, 4);
        trailMesh.setMaterial(new Material(trailTexture, 1.0f));
        trailParticle = new Particle(trailMesh, new Vector3f(0f, 0f, 0f), 250, 20);

        // add items to scene
        scene.setGameItems(gameItems);

        setupLights();
        hud = new Hud("Score: 0");
        setupSounds();
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        sceneLight.setAmbientLight(new Vector3f(0.7f, 0.7f, 0.7f));
        sceneLight.setDirectionalLight(new DirectionalLight(
                new Vector3f(1, 1, 1),          // color
                new Vector3f(0.3f, 0.5f, 0.7f), // position
                1.0f
        ));
    }

    private void setupSounds() throws Exception {
        soundManager.init();

        soundManager.addSound(Sounds.EXPLOSION.ordinal(), "/sounds/explosion.ogg", false, true);
        soundManager.getSoundSource(Sounds.EXPLOSION.ordinal()).setGain(0.4f);

        soundManager.addSound(Sounds.PLAYER_FIRE.ordinal(), "/sounds/player_fire.ogg", false, true);
        soundManager.getSoundSource(Sounds.PLAYER_FIRE.ordinal()).setGain(0.1f);

        soundManager.addSound(Sounds.MUSIC.ordinal(), "/sounds/music.ogg", true, true);
        soundManager.getSoundSource(Sounds.MUSIC.ordinal()).setGain(0.2f);
        soundManager.playSoundSource(Sounds.MUSIC.ordinal());
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        playerInc = 0;
        playerFiring = false;
        if (window.isKeyPressed(GLFW_KEY_A) || window.isKeyPressed(GLFW_KEY_LEFT)) {
            playerInc -= 1;
        }
        if (window.isKeyPressed(GLFW_KEY_D) || window.isKeyPressed(GLFW_KEY_RIGHT)) {
            playerInc += 1;
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            playerFiring = true;
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        Vector3f playerPosition = playerItem.getPosition();
        List<IParticleEmitter> emittersToRemove = new ArrayList<>();

        // player movement
        float playerX = playerPosition.x;
        if (!gameOver && playerX < 35.0f && playerInc > 0 || !gameOver && playerX > -35.0f && playerInc < 0) {
            playerItem.movePosition(playerInc * PLAYER_POS_STEP, 0, 0);
        }

        // player firing
        playerFire(playerPosition);

        // bullet update
        List<GameItem> toRemove = new ArrayList<>();
        for (GameItem bullet : bullets) {
            // move bullet
            bullet.movePosition(0, BULLET_SPEED, 0);

            // remove off-screen bullets
            if (bullet.getPosition().y > 30f) {
                toRemove.add(bullet);
                emittersToRemove.add(trailEmitters.get(bullet));
                trailEmitters.remove(bullet);
            }

            // check bullet and enemy collisions
            for (GameItem enemy : enemies) {
                Vector3f bulletPos = bullet.getPosition();
                Vector3f enemyPos = enemy.getPosition();
                if (bulletPos.x < enemyPos.x + ENEMY_SIZE &&
                    bulletPos.x > enemyPos.x - ENEMY_SIZE &&
                    bulletPos.y < enemyPos.y + ENEMY_SIZE &&
                    bulletPos.y > enemyPos.y - ENEMY_SIZE) { // collision occurred

                    // score increase
                    score += 100;
                    hud.setStatusText("Score: " + score);

                    // explosion effect
                    FlowParticleEmitter emitter = new FlowParticleEmitter(new Particle(explosionParticle), 1, 10);
                    emitter.getBaseParticle().setPosition(enemyPos.x, enemyPos.y, enemyPos.z);
                    explosionEmitters.add(emitter);
                    scene.getParticleEmitters().add(emitter);

                    // remove trail
                    emittersToRemove.add(trailEmitters.get(bullet));
                    trailEmitters.remove(bullet);

                    // explosion sound
                    soundManager.playSoundSource(Sounds.EXPLOSION.ordinal());

                    toRemove.add(bullet);
                    toRemove.add(enemy);
                }
            }
        }

        // move enemies
        if (!gameOver) {
            for (GameItem enemy : enemies) {
                enemy.movePosition(enemyMove(), -ENEMY_SPEED / 5, 0);
            }
            ENEMY_MOVE_COUNTER++;
        }

        // update position of trail emitters
        for (FlowParticleEmitter e : trailEmitters.values()) {
            e.getBaseParticle().movePosition(0f, BULLET_SPEED, 0f);
        }

        // update particle emitters
        for (IParticleEmitter e : scene.getParticleEmitters()) {
            ((FlowParticleEmitter)e).update((long)(interval * 1000));
        }

        // delete emitters whose particle has completed its lifespan
        for (IParticleEmitter emitter : explosionEmitters) {
            if (emitter.getParticles().size() > 0 && ((Particle) emitter.getParticles().get(0)).getLifespan() < 100) {
                emittersToRemove.add(emitter);
            }
        }

        scene.getParticleEmitters().removeAll(emittersToRemove);
        bullets.removeAll(toRemove);
        enemies.removeAll(toRemove);
        gameItems.removeAll(toRemove);

        for (GameItem enemy : enemies) {
            if (enemy.getPosition().y < -15) {
                gameOver = true;
                hud.setStatusText("Press ESC to exit.");
            }
        }

        if (enemies.isEmpty()) {
            gameOver = true;
            hud.setStatusText("Press ESC to exit.");
        }

        scene.setGameItems(gameItems);
    }

    private float enemyMove() {
        int orientation;
        if (ENEMY_MOVE_COUNTER % 200 > 100) {
            orientation = 1;
        } else {
            orientation = -1;
        }

        return ENEMY_SPEED * orientation;
    }

    private void playerFire(Vector3f position) {
        long now = System.currentTimeMillis();
        if (!gameOver && playerFiring && now - lastTimePlayerShot >= TIME_BETWEEN_SHOTS) {
            // create bullet
            GameItem bulletItem = new GameItem(bulletMesh);
            bulletItem.setPosition(position.x, position.y, position.z);

            // create trail particle emitter
            FlowParticleEmitter emitter = new FlowParticleEmitter(new Particle(trailParticle), 20, 10);
            emitter.getBaseParticle().setPosition(position.x, position.y, position.z);
            trailEmitters.put(bulletItem, emitter);
            scene.getParticleEmitters().add(emitter);

            bullets.add(bulletItem);
            gameItems.add(bulletItem);

            // play firing sound
            soundManager.playSoundSource(Sounds.PLAYER_FIRE.ordinal());

            lastTimePlayerShot = now;
        }
    }

    @Override
    public void render(Window window) {
        hud.updateSize(window);
        renderer.render(window, camera, scene, hud);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        soundManager.cleanup();
        Map<Mesh, List<GameItem>> meshMap = scene.getMeshMap();
        for (Mesh mesh : meshMap.keySet()) {
            mesh.cleanup();
        }
        hud.cleanup();
    }
}
