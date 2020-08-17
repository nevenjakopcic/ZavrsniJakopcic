package hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.*;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle.IParticleEmitter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;
    private final Transformation transformation;
    private ShaderProgram sceneShaderProgram;
    private ShaderProgram hudShaderProgram;
    private ShaderProgram particlesShaderProgram;
    private final float specularPower;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
    }

    public void init(Window window) throws Exception {
        setupSceneShader();
        setupParticlesShader();
        setupHudShader();

        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    private void setupSceneShader() throws Exception {
        // create shader
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/vertex.glsl"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/fragment.glsl"));
        sceneShaderProgram.link();
        // uniforms
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("modelViewMatrix");
        sceneShaderProgram.createUniform("textureSampler");
        sceneShaderProgram.createMaterialUniform("material");
        // lighting uniforms
        sceneShaderProgram.createUniform("specularPower");
        sceneShaderProgram.createUniform("ambientLight");
        sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        sceneShaderProgram.createDirectionalLightUniform("directionalLight");
    }

    private void setupParticlesShader() throws Exception {
        particlesShaderProgram = new ShaderProgram();
        particlesShaderProgram.createVertexShader(Utils.loadResource("/shaders/particle_vertex.glsl"));
        particlesShaderProgram.createFragmentShader(Utils.loadResource("/shaders/particle_fragment.glsl"));
        particlesShaderProgram.link();

        particlesShaderProgram.createUniform("projectionMatrix");
        particlesShaderProgram.createUniform("modelViewMatrix");
        particlesShaderProgram.createUniform("textureSampler");
    }

    private void setupHudShader() throws Exception {
        hudShaderProgram = new ShaderProgram();
        hudShaderProgram.createVertexShader(Utils.loadResource("/shaders/hud_vertex.glsl"));
        hudShaderProgram.createFragmentShader(Utils.loadResource("/shaders/hud_fragment.glsl"));
        hudShaderProgram.link();

        // create uniforms for ortographic-model projection matrix and base color
        hudShaderProgram.createUniform("projModelMatrix");
        hudShaderProgram.createUniform("color");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, Scene scene, IHud hud) {

        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        transformation.updateProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        transformation.updateViewMatrix(camera);

        renderScene(window, camera, scene);
        renderParticles(window, camera, scene);
        renderHud(window, hud);
    }

    private void renderScene(Window window, Camera camera, Scene scene) {

        sceneShaderProgram.bind();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix();
        SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        sceneShaderProgram.setUniform("textureSampler", 0);

        // render each mesh with the associated game items
        Map<Mesh, List<GameItem>> meshMap = scene.getMeshMap();
        for (Mesh mesh : meshMap.keySet()) {
            sceneShaderProgram.setUniform("material", mesh.getMaterial());
            mesh.renderList(meshMap.get(mesh), gameItem -> {
                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(gameItem, viewMatrix);
                sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            });
        }

        sceneShaderProgram.unbind();
    }

    private void renderLights(Matrix4f viewMatrix, SceneLight sceneLight) {

        // update light uniforms
        sceneShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
        sceneShaderProgram.setUniform("specularPower", specularPower);

        // process point lights
        PointLight[] pointLightList = sceneLight.getPointLightList();
        int numLights = pointLightList != null ? pointLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // get a copy of the light object and transform its position to view coordinates
            PointLight currPointLight = new PointLight(pointLightList[i]);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            sceneShaderProgram.setUniform("pointLights", currPointLight, i);
        }

        // process spot lights
        SpotLight[] spotLightList = sceneLight.getSpotLightList();
        numLights = spotLightList != null ? spotLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // get a copy of the spotlight object and transform its position and cone direction to view coordinates
            SpotLight currSpotLight = new SpotLight(spotLightList[i]);
            Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));

            Vector3f spotLightPos = currSpotLight.getPointLight().getPosition();
            Vector4f auxSpot = new Vector4f(spotLightPos, 1);
            auxSpot.mul(viewMatrix);
            spotLightPos.x = auxSpot.x;
            spotLightPos.y = auxSpot.y;
            spotLightPos.z = auxSpot.z;
            sceneShaderProgram.setUniform("spotLights", currSpotLight, i);
        }

        // get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);
    }

    private void renderParticles(Window window, Camera camera, Scene scene) {
        particlesShaderProgram.bind();

        particlesShaderProgram.setUniform("textureSampler", 0);
        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        particlesShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix();
        IParticleEmitter[] emitters = scene.getParticleEmitters();
        int numEmitters = emitters != null ? emitters.length : 0;

        glDepthMask(false);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);

        for (int i = 0; i < numEmitters; i++) {
            IParticleEmitter emitter = emitters[i];
            Mesh mesh = emitter.getBaseParticle().getMesh();

            mesh.renderList(emitter.getParticles(), (GameItem gameItem) -> {
                Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);

                viewMatrix.transpose3x3(modelMatrix);

                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
                modelViewMatrix.scale(gameItem.getScale());
                particlesShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            });
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(true);

        particlesShaderProgram.unbind();
    }

    private void renderHud(Window window, IHud hud) {
        hudShaderProgram.bind();

        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
        for (GameItem gameItem : hud.getGameItems()) {
            Mesh mesh = gameItem.getMesh();
            // set orthographic and model matrix for this HUD item
            Matrix4f projModelMatrix = transformation.buildOrtoProjModelMatrix(gameItem, ortho);
            hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
            hudShaderProgram.setUniform("color", gameItem.getMesh().getMaterial().getAmbientColor());

            // render the mesh for this HUD item
            mesh.render();
        }

        hudShaderProgram.unbind();
    }

    public void cleanup() {
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
        if (hudShaderProgram != null) {
            hudShaderProgram.cleanup();
        }
    }
}
