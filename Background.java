import java.awt.*;

public class Background extends GameObject {

    private Color color = Color.DARK_GRAY;

    public Background() {
        super(0, -1);
    }

    @Override
    public void update() {}

    @Override
    public void draw() {
        GameGraphics.drawRectangle(0,0,GameGraphics.getWindowWidth(),GameGraphics.getWindowHeight(), color);
    }
}
