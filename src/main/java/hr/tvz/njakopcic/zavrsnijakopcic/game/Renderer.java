package hr.tvz.njakopcic.zavrsnijakopcic.game;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.Utils;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.Window;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Mesh;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.ShaderProgram;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Renderer {

    private int vboId;
    private int vaoId;

    private ShaderProgram shaderProgram;

    public Renderer() {}

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/vertex.glsl"));
        shaderProgram.createFragmentShader(Utils.loadResource("/fragment.glsl"));
        shaderProgram.link();
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Mesh mesh) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        // draw the mesh
        glBindVertexArray(mesh.getVaoId()); // bind to the VAO
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        // restore state
        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
