package hr.tvz.njakopcic.zavrsnijakopcic.game;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.GameItem;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.IHud;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.TextItem;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.Window;
import org.joml.Vector4f;

public class Hud implements IHud {

    private static final int FONT_COLS = 16;
    private static final int FONT_ROWS = 16;
    private static final String FONT_TEXTURE = "textures/font_texture.png";

    private final GameItem[] gameItems;
    private final TextItem statusTextItem;

    public Hud(String statusText) throws Exception {
        this.statusTextItem = new TextItem(statusText, FONT_TEXTURE, FONT_COLS, FONT_ROWS);
        this.statusTextItem.getMesh().getMaterial().setAmbientColor(new Vector4f(1, 1, 1, 1));
        gameItems = new GameItem[] { statusTextItem };
    }

    public void setStatusText(String statusText) {
        this.statusTextItem.setText(statusText);
    }

    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }

    public void updateSize(Window window) {
        this.statusTextItem.setPosition(10f, window.getHeight() - 50f, 0);
    }
}
