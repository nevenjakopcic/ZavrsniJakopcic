package hr.tvz.njakopcic.zavrsnijakopcic.game;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.GameItem;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.IGameLogic;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.MouseInput;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.Window;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Camera;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Mesh;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.OBJLoader;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private GameItem[] gameItems;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        Mesh grassMesh = OBJLoader.loadMesh("/models/cube.obj");
        grassMesh.setTexture(new Texture("textures/grassblock.png"));

        Mesh bunnyMesh = OBJLoader.loadMesh("/models/bunny.obj");

        GameItem gameItem1 = new GameItem(grassMesh);
        gameItem1.setScale(0.25f);
        gameItem1.setPosition(0, 0, -2);
        GameItem gameItem2 = new GameItem(grassMesh);
        gameItem2.setScale(0.25f);
        gameItem2.setPosition(0.5f, 0.5f, -2);
        GameItem gameItem3 = new GameItem(grassMesh);
        gameItem3.setScale(0.25f);
        gameItem3.setPosition(0, 0, -2.5f);
        GameItem gameItem4 = new GameItem(grassMesh);
        gameItem4.setScale(0.25f);
        gameItem4.setPosition(0.5f, 0, -2.5f);

        GameItem bunnyItem = new GameItem(bunnyMesh);
        bunnyItem.setPosition(0, 0, -5);

        gameItems = new GameItem[]{ gameItem1, gameItem2, gameItem3, gameItem4, bunnyItem };
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
    }

    @Override
    public void render(Window window) {
        renderer.render(window, camera, gameItems);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanup();
        }
    }
}
