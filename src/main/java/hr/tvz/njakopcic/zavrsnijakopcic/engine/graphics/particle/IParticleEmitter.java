package hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.GameItem;

import java.util.List;

public interface IParticleEmitter {

//    void cleanup();
    Particle getBaseParticle();
    List<GameItem> getParticles();
}
