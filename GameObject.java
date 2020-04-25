import java.awt.*;

/**
 * Base game object class which is the parent of all other game object types.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class GameObject
{
    private int p;
    private int l;

    // Object position
    public int x;
    public int y;

    public GameObject(int priority, int layer)
    {
        // initialise instance variables
        p = priority;
        l = layer;
        x = 0;
        y = 0;
        
        Game.addObject(this);
    }

    public GameObject(int priority, int layer, int x_pos, int y_pos)
    {
        // initialise instance variables
        p = priority;
        l = layer;
        x = x_pos;
        y = y_pos;

        Game.addObject(this);
    }
    
    public int getPriority() {
        return p;
    }
    
    public int getLayer() {
        return l;
    }
    
    public void update() {
        x++;
    }
    
    public void draw() {
        GameGraphics.drawRectangle(x, y, 50, 50, Color.BLUE);
    }
}
