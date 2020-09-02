package hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.lights;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

public class DirectionalLight {

    @Getter @Setter private Vector3f color;
    @Getter @Setter private Vector3f direction;
    @Getter @Setter private float intensity;

    public DirectionalLight(Vector3f color, Vector3f direction, float intensity) {
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;
    }

    public DirectionalLight(DirectionalLight light) {
        this(new Vector3f(light.getColor()), new Vector3f(light.getDirection()), light.getIntensity());
    }
}
