package hr.tvz.njakopcic.zavrsnijakopcic.engine;

import lombok.Getter;
import lombok.Setter;

public class Scene {

    @Getter @Setter private GameItem[] gameItems;
    @Getter @Setter private Skybox skybox;
    @Getter @Setter private SceneLight sceneLight;
}
