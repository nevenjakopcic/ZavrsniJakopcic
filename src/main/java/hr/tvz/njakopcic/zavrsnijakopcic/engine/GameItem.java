package hr.tvz.njakopcic.zavrsnijakopcic.engine;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Material;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Mesh;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.OBJLoader;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Texture;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GameItem {

    @Getter @Setter private Mesh mesh;
    @Getter         private final Vector3f position;
    @Getter @Setter private float scale;
    @Getter         private final Vector3f rotation;
    @Getter @Setter private int texturePos;

    public GameItem() {
        position = new Vector3f();
        scale = 1;
        rotation = new Vector3f();
        texturePos = 0;
    }

    public GameItem(Mesh mesh) {
        this();
        this.mesh = mesh;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public static GameItem createItem(String model, String texture, float reflectance) throws Exception {
        Mesh mesh = OBJLoader.loadMesh(model);
        Material material = new Material(new Texture(texture), reflectance);
        mesh.setMaterial(material);

        return new GameItem(mesh);
    }

    public static GameItem createItem(String model, Vector4f color, float reflectance) throws Exception {
        Mesh mesh = OBJLoader.loadMesh(model);
        Material material = new Material(color, reflectance);
        mesh.setMaterial(material);

        return new GameItem(mesh);
    }

    public void movePosition(float dx, float dy, float dz) {
        this.position.x += dx;
        this.position.y += dy;
        this.position.z += dz;
    }
}
