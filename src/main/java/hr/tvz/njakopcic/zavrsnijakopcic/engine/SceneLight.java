package hr.tvz.njakopcic.zavrsnijakopcic.engine;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.lights.DirectionalLight;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.lights.PointLight;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.lights.SpotLight;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.List;

public class SceneLight {

    @Getter @Setter private Vector3f ambientLight;
    @Getter @Setter private List<PointLight> pointLightList;
    @Getter @Setter private SpotLight[] spotLightList;
    @Getter @Setter private DirectionalLight directionalLight;
}
