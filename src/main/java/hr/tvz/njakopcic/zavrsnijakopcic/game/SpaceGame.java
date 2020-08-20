package hr.tvz.njakopcic.zavrsnijakopcic.game;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.*;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.*;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.sound.SoundManager;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
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

    private int playerInc = 0;
    private boolean playerFiring = false;
    private long lastTimePlayerShot = 0;
    private long timeBetweenShots = 300;
    private GameItem playerItem;
    private Mesh bulletMesh;
    private static final float PLAYER_POS_STEP = 0.4f;
    private static final float ENEMY_SIZE = 2f;

    private List<GameItem> bullets;
    private List<GameItem> enemies;
    private List<GameItem> gameItems;
    private int score = 0;

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
                enemyItem.setPosition(-31.5f + j*7, 20 - i*6, 0);
                enemies.add(enemyItem);
                gameItems.add(enemyItem);
            }
        }

        // bullet mesh setup (not initially added to the scene)
        bulletMesh = OBJLoader.loadMesh("/models/bullet.obj");
        bulletMesh.setMaterial(new Material(new Vector4f(1.0f, 0.5f, 0, 1), 1.0f));

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

    private void setupSounds() {
        soundManager.init();
        // TODO: add sounds
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

        // player movement
        float playerX = playerPosition.x;
        if (playerX < 35.0f && playerInc > 0 || playerX > -35.0f && playerInc < 0) {
            playerItem.movePosition(playerInc * PLAYER_POS_STEP, 0, 0);
        }

        // player firing
        playerFire(playerPosition);

        // bullet update
        List<GameItem> toRemove = new ArrayList<>();
        for (GameItem bullet : bullets) {
            // move bullet
            bullet.movePosition(0, 0.4f, 0);

            // remove off-screen bullets
            if (bullet.getPosition().y > 30f) {
                toRemove.add(bullet);
            }

            // check bullet and enemy collisions
            for (GameItem enemy : enemies) {
                Vector3f bulletPos = bullet.getPosition();
                Vector3f enemyPos = enemy.getPosition();
                if (bulletPos.x < enemyPos.x + ENEMY_SIZE &&
                    bulletPos.x > enemyPos.x - ENEMY_SIZE &&
                    bulletPos.y < enemyPos.y + ENEMY_SIZE &&
                    bulletPos.y > enemyPos.y - ENEMY_SIZE) {
                    // collision occured

                    // score increase
                    score += 100;
                    hud.setStatusText("Score: " + score);

                    toRemove.add(bullet);
                    toRemove.add(enemy);
                }
            }
        }
        bullets.removeAll(toRemove);
        enemies.removeAll(toRemove);
        gameItems.removeAll(toRemove);

        scene.setGameItems(gameItems);
    }

    private void playerFire(Vector3f position) {
        long now = System.currentTimeMillis();
        if (playerFiring && now - lastTimePlayerShot >= timeBetweenShots) {
            GameItem bulletItem = new GameItem(bulletMesh);
            bulletItem.setPosition(position.x, position.y, position.z);
            bullets.add(bulletItem);
            gameItems.add(bulletItem);

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
