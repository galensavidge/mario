/**
 * Base game object class which is the parent of all other game object types.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public abstract class GameObject
{
    protected int priority;
    protected int layer;

    // Object position
    public double x;
    public double y;

    // Whether this object persists between scenes or is deleted when the scene changes
    protected boolean persistent = false;

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
    public GameObject(int priority, int layer, double x, double y)
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

    public boolean isPersistent() {
        return persistent;
    }

    public void delete() {
        Game.removeObject(this);
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

    /* @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable
    {
        // will print name of object
        System.out.println(this + " successfully garbage collected");
    }*/
}
