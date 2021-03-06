package hr.tvz.njakopcic.zavrsnijakopcic.game;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.*;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.*;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.lights.DirectionalLight;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.lights.PointLight;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.lights.SpotLight;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle.FlowParticleEmitter;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle.Particle;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.sound.SoundManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class DemoGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.2f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final SoundManager soundMgr;
    private final Camera camera;
    private Scene scene;
    private List<GameItem> gameItems;
    private Hud hud;
    private FlowParticleEmitter particleEmitter;
    private float lightAngle;
    private float spotAngle = 0;
    private float spotInc = 1;

    private enum Sounds { MUSIC }

    public DemoGame() {
        renderer = new Renderer();
        soundMgr = new SoundManager();
        camera = new Camera();
        cameraInc = new Vector3f();
        lightAngle = -90;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        scene = new Scene();

        gameItems = new ArrayList<>();

        camera.setPosition(0, 0, 40.0f);

        float reflectance = 1f;

        // spaceship mesh
        Mesh spaceshipMesh = OBJLoader.loadMesh("/models/spaceship.obj");
        Texture spaceshipTexture = new Texture("textures/enemy.png");
        Material spaceshipMaterial = new Material(spaceshipTexture, reflectance);
        spaceshipMesh.setMaterial(spaceshipMaterial);

        // spaceship item
        GameItem gameItem = new GameItem(spaceshipMesh);
        gameItem.setPosition(5, 0, 0);
        gameItems.add(gameItem);

        // bunny mesh
        Mesh bunnyMesh = OBJLoader.loadMesh("/models/bunny.obj");
        Vector4f bunnyColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        Material bunnyMaterial = new Material(bunnyColor, reflectance);
        bunnyMesh.setMaterial(bunnyMaterial);

        // bunny item
        GameItem bunnyItem = new GameItem(bunnyMesh);
        bunnyItem.setPosition(0, 0, -5);
        gameItems.add(bunnyItem);

        // plane mesh
        Mesh planeMesh = OBJLoader.loadMesh("/models/plane.obj");
        Texture planeTexture = new Texture("textures/back.png");
        Material planeMaterial = new Material(planeTexture, reflectance);
        planeMesh.setMaterial(planeMaterial);

        // plane item
        GameItem planeItem = new GameItem(planeMesh);
        planeItem.setPosition(0, 0, -80);
        planeItem.setRotation(-90, 0, 0);
        planeItem.setScale(80);
        gameItems.add(planeItem);

        scene.setGameItems(gameItems);

        Vector3f particleSpeed = new Vector3f(0, 1, 0);
        particleSpeed.mul(2.5f);
        long lifespan = 1500;
        int maxParticles = 200;
        long creationPeriodMillis = 800;
        float range = 0.2f;
        float scale = 1.0f;
        Mesh partMesh = OBJLoader.loadMesh("/models/particle.obj");
        Texture texture = new Texture("textures/explosion_anim.png", 8, 6);
        Material partMaterial = new Material(texture, reflectance);
        partMesh.setMaterial(partMaterial);
        Particle particle = new Particle(partMesh, particleSpeed, lifespan, 20);
        particle.setScale(scale);
        particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
        particleEmitter.setActive(true);
        particleEmitter.setPositionRndRange(range);
        particleEmitter.setSpeedRndRange(range);
        scene.getParticleEmitters().add(particleEmitter);

        setupLights();

        hud = new Hud("DEMO");

        // sound
        setupSounds();
    }

    private void setupSounds() throws Exception {
        soundMgr.init();

        soundMgr.addSound(Sounds.MUSIC.ordinal(), "/sounds/music.ogg", true, true);
        soundMgr.playSoundSource(Sounds.MUSIC.ordinal()); // play background music
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();

        scene.setSceneLight(sceneLight);

        sceneLight.setAmbientLight(new Vector3f(0.7f, 0.7f, 0.7f));

        // point Light
        Vector3f lightColor = new Vector3f(1, 0, 0);
        Vector3f lightPosition = new Vector3f(0, 0.5f, -3.5f);
        float lightIntensity = 1.0f;
        PointLight pointLight = new PointLight(lightColor, lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);
        sceneLight.setPointLightList(new ArrayList<>());
        sceneLight.getPointLightList().add(pointLight);

        // spot light
        lightPosition = new Vector3f(0, 0.5f, 10f);
        lightColor = new Vector3f(0, 1, 0);
        PointLight sl_pointLight = new PointLight(lightColor, lightPosition, lightIntensity);
        att = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
        sl_pointLight.setAttenuation(att);
        Vector3f coneDir = new Vector3f(0, 0, -1);
        float cutoff = (float) Math.cos(Math.toRadians(20));
        SpotLight spotLight = new SpotLight(sl_pointLight, coneDir, cutoff);
        sceneLight.setSpotLightList(new SpotLight[] { spotLight, new SpotLight(spotLight) });

        // directional light
        lightPosition = new Vector3f(-1, 0, 0);
        lightColor = new Vector3f(0, 0, 1);
        sceneLight.setDirectionalLight(new DirectionalLight(lightColor, lightPosition, 1.0f));
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            cameraInc.y = 1;
        } else if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
            cameraInc.y = -1;
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        camera.movePosition(
                cameraInc.x * CAMERA_POS_STEP,
                cameraInc.y * CAMERA_POS_STEP,
                cameraInc.z * CAMERA_POS_STEP
        );

        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        particleEmitter.update((long)(interval * 1000));

        soundMgr.updateListenerPosition(camera);
    }

    @Override
    public void render(Window window) {
        hud.updateSize(window);
        renderer.render(window, camera, scene, hud);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        soundMgr.cleanup();
        Map<Mesh, List<GameItem>> meshMap = scene.getMeshMap();
        for (Mesh mesh : meshMap.keySet()) {
            mesh.cleanup();
        }
        hud.cleanup();
    }
}
