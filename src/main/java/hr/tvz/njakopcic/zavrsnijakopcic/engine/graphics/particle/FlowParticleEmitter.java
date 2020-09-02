package hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.GameItem;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlowParticleEmitter implements IParticleEmitter {

    @Getter @Setter private int maxParticles;
    @Getter @Setter private boolean active;
    @Getter         private final List<GameItem> particles;
    @Getter         private final Particle baseParticle;
    @Getter @Setter private long creationPeriodMillis;
                    private long lastCreationTime;
    @Getter @Setter private float speedRndRange;
    @Getter @Setter private float positionRndRange;
    @Getter @Setter private float scaleRndRange;

    public FlowParticleEmitter(Particle baseParticle, int maxParticles, long creationPeriodMillis) {
        particles = new ArrayList<>();
        this.baseParticle = baseParticle;
        this.maxParticles = maxParticles;
        this.active = true;
        this.lastCreationTime = 0;
        this.creationPeriodMillis = creationPeriodMillis;
    }

    public void update(long elapsedTime) {
        Iterator<? extends GameItem> it = particles.iterator();
        while (it.hasNext()) {
            Particle particle = (Particle) it.next();
            if (particle.updateLifespan(elapsedTime) < 0) {
                it.remove();
            } else {
                updatePosition(particle, elapsedTime);
            }
        }

        long now = System.currentTimeMillis();
        if (lastCreationTime == 0) {
            lastCreationTime = now;
        }

        int length = this.getParticles().size();
        if (now - lastCreationTime >= this.creationPeriodMillis && length < maxParticles) {
            createParticle();
            this.lastCreationTime = now;
        }
    }

    private void updatePosition(Particle particle, long elapsedTime) {
        Vector3f speed = particle.getSpeed();
        float delta = elapsedTime / 1000.0f;
        float dx = speed.x * delta;
        float dy = speed.y * delta;
        float dz = speed.z * delta;
        Vector3f pos = particle.getPosition();
        particle.movePosition(dx, dy, dz);
    }

    private void createParticle() {
        Particle particle = new Particle(this.getBaseParticle());

        float sign = Math.random() > 0.5d ? -1.0f : 1.0f; // plus or minus
        float posInc   = sign * (float)Math.random() * this.positionRndRange;
        float speedInc = sign * (float)Math.random() * this.speedRndRange;
        float scaleInc = sign * (float)Math.random() * this.scaleRndRange;

        particle.getPosition().add(posInc, posInc, posInc);
        particle.getSpeed().add(speedInc, speedInc, speedInc);
        particle.setScale(particle.getScale() + scaleInc);

        particles.add(particle);
    }
}
