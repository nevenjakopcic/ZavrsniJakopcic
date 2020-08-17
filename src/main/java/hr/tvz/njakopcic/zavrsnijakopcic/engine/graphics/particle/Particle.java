package hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.GameItem;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Mesh;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

public class Particle extends GameItem {

    @Getter @Setter private Vector3f speed;
    @Getter @Setter private long lifespan;

    public Particle(Mesh mesh, Vector3f speed, long lifespan) {
        super(mesh);
        this.speed = new Vector3f(speed);
        this.lifespan = lifespan;
    }

    public Particle(Particle baseParticle) {
        super(baseParticle.getMesh());
        Vector3f aux = baseParticle.getPosition();
        setPosition(aux.x, aux.y, aux.z);
        aux = baseParticle.getRotation();
        setRotation(aux.x, aux.y, aux.z);
        setScale(baseParticle.getScale());
        this.speed = new Vector3f(baseParticle.speed);
        this.lifespan = baseParticle.getLifespan();
    }

    public long updateLifespan(long elapsedTime) {
        this.lifespan -= elapsedTime;
        return this.lifespan;
    }
}
