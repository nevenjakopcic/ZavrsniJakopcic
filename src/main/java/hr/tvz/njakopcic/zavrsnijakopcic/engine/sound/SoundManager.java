package hr.tvz.njakopcic.zavrsnijakopcic.engine.sound;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Camera;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Transformation;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundManager {

    private long device;
    private long context;
    @Getter @Setter private SoundListener listener;
    private final List<SoundBuffer> soundBufferList;
    private final Map<Integer, SoundSource> soundSourceMap;
    private final Matrix4f cameraMatrix;

    public SoundManager() {
        soundBufferList = new ArrayList<>();
        soundSourceMap = new HashMap<>();
        cameraMatrix = new Matrix4f();
    }

    public void init() {
        this.device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        this.context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        setListener(new SoundListener(new Vector3f(0, 0, 0)));
    }

    public void addSound(Integer id, String file, boolean loop, boolean relative) throws Exception {
        SoundBuffer buffer = new SoundBuffer(file);
        addSoundBuffer(buffer);
        SoundSource source = new SoundSource(loop, relative);
        source.setBuffer(buffer.getBufferId());
        addSoundSource(id, source);
    }

    public void addSoundSource(Integer id, SoundSource soundSource) {
        this.soundSourceMap.put(id, soundSource);
    }

    public SoundSource getSoundSource(Integer id) {
        return this.soundSourceMap.get(id);
    }

    public void playSoundSource(Integer id) {
        SoundSource soundSource = this.soundSourceMap.get(id);
        if (soundSource != null && !soundSource.isPlaying()) {
            soundSource.play();
        }
    }

    public void removeSoundSource(Integer id) {
        this.soundSourceMap.remove(id);
    }

    public void addSoundBuffer(SoundBuffer soundBuffer) {
        this.soundBufferList.add(soundBuffer);
    }

    public void updateListenerPosition(Camera camera) {
        // update camera matrix with camera data
        Transformation.updateGenericViewMatrix(camera.getPosition(), camera.getRotation(), cameraMatrix);

        listener.setPosition(camera.getPosition());
        Vector3f at = new Vector3f();
        cameraMatrix.positiveZ(at).negate();
        Vector3f up = new Vector3f();
        cameraMatrix.positiveY(up);
        listener.setOrientation(at, up);
    }

    public void setAttenuationModel(int model) {
        alDistanceModel(model);
    }

    public void cleanup() {
        for (SoundSource soundSource : soundSourceMap.values()) {
            soundSource.cleanup();
        }
        soundSourceMap.clear();
        for (SoundBuffer soundBuffer : soundBufferList) {
            soundBuffer.cleanup();
        }
        soundBufferList.clear();
        if (context != NULL) {
            alcDestroyContext(context);
        }
        if (device != NULL) {
            alcCloseDevice(device);
        }
    }
}
