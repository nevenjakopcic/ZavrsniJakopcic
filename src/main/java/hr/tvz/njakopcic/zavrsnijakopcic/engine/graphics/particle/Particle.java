package hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.particle;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.GameItem;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Mesh;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Texture;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

public class Particle extends GameItem {

    private long currentAnimTimeMillis;
    @Getter @Setter private long updateTextureMillis;
    @Getter @Setter private Vector3f speed;
    @Getter @Setter private long lifespan;
    @Getter         private int animFrames;

    public Particle(Mesh mesh, Vector3f speed, long lifespan, long updateTextureMillis) {
        super(mesh);
        this.speed = new Vector3f(speed);
        this.lifespan = lifespan;
        this.updateTextureMillis = updateTextureMillis;
        this.currentAnimTimeMillis = 0;
        Texture texture = this.getMesh().getMaterial().getTexture();
        this.animFrames = texture.getNumCols() * texture.getNumRows();
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
        this.updateTextureMillis = baseParticle.getUpdateTextureMillis();
        this.currentAnimTimeMillis = 0;
        this.animFrames = baseParticle.getAnimFrames();
    }

    public long updateLifespan(long elapsedTime) {
        this.lifespan -= elapsedTime;
        this.currentAnimTimeMillis += elapsedTime;
        if (this.currentAnimTimeMillis >= this.getUpdateTextureMillis() && this.animFrames > 0) {
            this.currentAnimTimeMillis = 0;
            int pos = this.getTexturePos();
            pos++;
            if (pos < this.animFrames) {
                this.setTexturePos(pos);
            } else {
                this.setTexturePos(0);
            }
        }

        return this.lifespan;
    }
}
