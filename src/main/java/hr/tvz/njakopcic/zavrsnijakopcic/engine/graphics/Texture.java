package hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    @Getter private final int id;
    @Getter private final int width;
    @Getter private final int height;

    public Texture(String fileName) throws Exception {
        ByteBuffer buf;
        // load texture file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load(fileName, w, h, channels, 4);
            if (buf == null) {
                throw new Exception("Image file [" + fileName + "] not loaded: " + stbi_failure_reason());
            }

            width = w.get();
            height = h.get();
        }

        int textureId = glGenTextures(); // create new OpenGL texture
        glBindTexture(GL_TEXTURE_2D, textureId); // bind the texture

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size.
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // texture interpolation
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // upload texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(buf);

        this.id = textureId;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void cleanup() {
        glDeleteTextures(id);
    }
}
