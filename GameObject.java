/**
 * Base game object class which is the parent of all other game object types.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public abstract class GameObject
{
    protected int priority = 0;
    protected int layer = 0;

    // Object position
    public int x = 0;
    public int y = 0;

    public GameObject()
    {
        Game.addObject(this);
    }

    /**
     * @param priority The execution priority for this object's {@link #update()}. Higher priority means this object
     *                 is handled earlier in the update queue.
     * @param layer The layer on which this object's {@link #draw()} method takes place. Higher layer means this object
     *              is drawn later, on top of objects with lower layer.
     */
    public GameObject(int priority, int layer)
    {
        // initialise instance variables
        this.priority = priority;
        this.layer = layer;
        x = 0;
        y = 0;
        
        Game.addObject(this);
    }

    /**
     * @param priority The execution priority for this object's {@link #update()}. Higher priority means this object
     *                 is handled earlier in the update queue.
     * @param layer The layer on which this object's {@link #draw()} method takes place. Higher layer means this object
     *              is drawn later, on top of objects with lower layer.
     * @param x This object's starting x position in the world.
     * @param y This object's starting y position in the world.
     */
    public GameObject(int priority, int layer, int x, int y)
    {
        // initialise instance variables
        this.priority = priority;
        this.layer = layer;
        this.x = x;
        this.y = y;

        Game.addObject(this);
    }
    
    public int getPriority() {
        return priority;
    }
    
    public int getLayer() {
        return layer;
    }

    /**
     * This method is called by Game every step and should be overwritten in child classes. Order depends on the
     * priority of the created objects.
     */
    public abstract void update();

    /**
     * This method is called by Game every step and should be overwritten in child classes.
     */
    public abstract void draw();
}
