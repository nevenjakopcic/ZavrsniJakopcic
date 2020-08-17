package hr.tvz.njakopcic.zavrsnijakopcic.game;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.*;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.*;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle.FlowParticleEmitter;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle.Particle;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private Scene scene;
    private Hud hud;
    private FlowParticleEmitter particleEmitter;
    private float lightAngle;
    private float spotAngle = 0;
    private float spotInc = 1;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f();
        lightAngle = -90;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        scene = new Scene();

        float reflectance = 1f;

        // spaceship mesh
        Mesh spaceshipMesh = OBJLoader.loadMesh("/models/spaceship.obj");
        Texture spaceshipTexture = new Texture("textures/spaceship.png");
        Material spaceshipMaterial = new Material(spaceshipTexture, reflectance);
        spaceshipMesh.setMaterial(spaceshipMaterial);

        // spaceship item
        GameItem gameItem = new GameItem(spaceshipMesh);
        gameItem.setScale(0.25f);
        gameItem.setPosition(0, 0, -2);


        // bunny mesh
        Mesh bunnyMesh = OBJLoader.loadMesh("/models/bunny.obj");
        Vector4f bunnyColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        Material bunnyMaterial = new Material(bunnyColor, reflectance);
        bunnyMesh.setMaterial(bunnyMaterial);

        // bunny item
        GameItem bunnyItem = new GameItem(bunnyMesh);
        bunnyItem.setPosition(0, 0, -5);

        // plane mesh
        Mesh planeMesh = OBJLoader.loadMesh("/models/plane.obj");
        Texture planeTexture = new Texture("textures/back.png");
        Material planeMaterial = new Material(planeTexture, reflectance);
        planeMesh.setMaterial(planeMaterial);

        // plane item
        GameItem planeItem = new GameItem(planeMesh);
        planeItem.setPosition(0, 0, -20);
        planeItem.setRotation(-90, 0, 0);
        planeItem.setScale(20);

        scene.setGameItems(new GameItem[] { gameItem, bunnyItem, planeItem });

        Vector3f particleSpeed = new Vector3f(0, 1, 0);
        particleSpeed.mul(2.5f);
        long lifespan = 4000;
        int maxParticles = 200;
        long creationPeriodMillis = 300;
        float range = 0.2f;
        float scale = 1.0f;
        Mesh partMesh = OBJLoader.loadMesh("/models/particle.obj");
        Texture texture = new Texture("textures/particle_anim.png", 4, 4);
        Material partMaterial = new Material(texture, reflectance);
        partMesh.setMaterial(partMaterial);
        Particle particle = new Particle(partMesh, particleSpeed, lifespan, 100);
        particle.setScale(scale);
        particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
        particleEmitter.setActive(true);
        particleEmitter.setPositionRndRange(range);
        particleEmitter.setSpeedRndRange(range);
        scene.setParticleEmitters(new FlowParticleEmitter[] { particleEmitter });

        setupLights();

        hud = new Hud("DEMO");
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();

        scene.setSceneLight(sceneLight);

        sceneLight.setAmbientLight(new Vector3f(0.7f, 0.7f, 0.7f));

        // point Light
        Vector3f lightColor = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntensity = 1.0f;
        PointLight pointLight = new PointLight(lightColor, lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);
        sceneLight.setPointLightList(new PointLight[] { pointLight });

        // spot light
        lightPosition = new Vector3f(0, 0.0f, 10f);
        PointLight sl_pointLight = new PointLight(lightColor, lightPosition, lightIntensity);
        att = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
        sl_pointLight.setAttenuation(att);
        Vector3f coneDir = new Vector3f(0, 0, -1);
        float cutoff = (float) Math.cos(Math.toRadians(140));
        SpotLight spotLight = new SpotLight(sl_pointLight, coneDir, cutoff);
        sceneLight.setSpotLightList(new SpotLight[] { spotLight, new SpotLight(spotLight) });

        // directional light
        lightPosition = new Vector3f(-1, 0, 0);
        sceneLight.setDirectionalLight(new DirectionalLight(lightColor, lightPosition, lightIntensity));
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

        SpotLight[] spotLightList = scene.getSceneLight().getSpotLightList();
        float lightPos = spotLightList[0].getPointLight().getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            spotLightList[0].getPointLight().getPosition().z = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_M)) {
            spotLightList[0].getPointLight().getPosition().z = lightPos - 0.1f;
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

        // update spot light direction
        spotAngle += spotInc * 0.05f;
        if (spotAngle > 2) {
            spotInc = -1;
        } else if (spotAngle < -2) {
            spotInc = 1;
        }
        double spotAngleRad = Math.toRadians(spotAngle);
        SpotLight[] spotLightList = scene.getSceneLight().getSpotLightList();
        Vector3f coneDir = spotLightList[0].getConeDirection();
        coneDir.y = (float) Math.sin(spotAngleRad);

        // update directional light direction, intensity and color
        DirectionalLight directionalLight = scene.getSceneLight().getDirectionalLight();
        lightAngle += 1.1f;
        if (lightAngle > 90) {
            directionalLight.setIntensity(0);
            if (lightAngle >= 360) {
                lightAngle = -90;
            }
        } else if (lightAngle <= -80 || lightAngle >= 80) {
            float factor = 1 - (Math.abs(lightAngle) - 80) / 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);

        particleEmitter.update((long)(interval * 1000));
    }

    @Override
    public void render(Window window) {
        hud.updateSize(window);
        renderer.render(window, camera, scene, hud);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        Map<Mesh, List<GameItem>> meshMap = scene.getMeshMap();
        for (Mesh mesh : meshMap.keySet()) {
            mesh.cleanup();
        }
        hud.cleanup();
    }
}
