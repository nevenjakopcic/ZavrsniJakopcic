package hr.tvz.njakopcic.zavrsnijakopcic.engine;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Mesh;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle.IParticleEmitter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {

    @Getter         private final Map<Mesh, List<GameItem>> meshMap;
    @Getter @Setter private SceneLight sceneLight;
    @Getter @Setter private IParticleEmitter[] particleEmitters;

    public Scene() {
        meshMap = new HashMap<>();
    }

    public void setGameItems(GameItem[] gameItems) {
        int numGameItems = gameItems != null ? gameItems.length : 0;
        for (int i = 0; i < numGameItems; i++) {
            GameItem gameItem = gameItems[i];
            Mesh mesh = gameItem.getMesh();

            List<GameItem> list = meshMap.computeIfAbsent(mesh, k -> new ArrayList<>());
            list.add(gameItem);
        }
    }
}
