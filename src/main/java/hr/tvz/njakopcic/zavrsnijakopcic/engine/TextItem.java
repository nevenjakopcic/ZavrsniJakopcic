package hr.tvz.njakopcic.zavrsnijakopcic.engine;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Material;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Mesh;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.graphics.Texture;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TextItem extends GameItem {

    private static final float ZPOS = 0.0f;
    private static final int VERTICES_PER_QUAD = 4;
    @Getter private String text;
    private final int numCols;
    private final int numRows;

    public TextItem(String text, String fontFileName, int numCols, int numRows) throws Exception {
        super();
        this.text = text;
        this.numCols = numCols;
        this.numRows = numRows;
        Texture texture = new Texture(fontFileName);
        this.setMesh(buildMesh(texture, numCols, numRows));
    }

    private Mesh buildMesh(Texture texture, int numCols, int numRows) {
        byte[] chars = text.getBytes(StandardCharsets.ISO_8859_1);
        int numChars = chars.length;

        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        float[] normals = new float[0];
        List<Integer> indices = new ArrayList<>();

        float tileWidth = (float)texture.getWidth() / (float)numCols;
        float tileHeight = (float)texture.getHeight() / (float)numRows;

        for (int i = 0; i < numChars; i++) {
            byte currChar = chars[i];
            int col = currChar % numCols;
            int row = currChar / numCols;

            // build a character tile composed by two triangles

            // left top vertex
            positions.add((float) i*tileWidth); // x
            positions.add(0.0f);                // y
            positions.add(ZPOS);                // z
            textCoords.add((float)col / (float)numCols);
            textCoords.add((float)row / (float)numRows);
            indices.add(i*VERTICES_PER_QUAD);

            // left bottom vertex
            positions.add((float)i*tileWidth);  // x
            positions.add(tileHeight);          // y
            positions.add(ZPOS);                // z
            textCoords.add((float)col / (float)numCols);
            textCoords.add((float)(row + 1) / (float)numRows);
            indices.add(i*VERTICES_PER_QUAD + 1);

            // right bottom vertex
            positions.add((float)i*tileWidth + tileWidth);  // x
            positions.add(tileHeight);                      // y
            positions.add(ZPOS);                            // z
            textCoords.add((float)(col + 1) / (float)numCols);
            textCoords.add((float)(row + 1) / (float)numRows);
            indices.add(i*VERTICES_PER_QUAD + 2);

            // right top vertex
            positions.add((float)i*tileWidth + tileWidth);  // x
            positions.add(0.0f);                            // y
            positions.add(ZPOS);                            // z
            textCoords.add((float)(col + 1) / (float)numCols);
            textCoords.add((float)row / (float)numRows);
            indices.add(i*VERTICES_PER_QUAD + 3);

            // indices for left top and bottom right vertices
            indices.add(i * VERTICES_PER_QUAD);
            indices.add(i * VERTICES_PER_QUAD + 2);
        }

        float[] posArr = Utils.listToArray(positions);
        float[] textCoordsArr = Utils.listToArray(textCoords);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();
        Mesh mesh = new Mesh(posArr, textCoordsArr, normals, indicesArr);
        mesh.setMaterial(new Material(texture));

        return mesh;
    }


    public void setText(String text) {
        this.text = text;
        Texture texture = this.getMesh().getMaterial().getTexture();
        this.getMesh().deleteBuffers();
        this.setMesh(buildMesh(texture, numCols, numRows));
    }
}
