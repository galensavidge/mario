package engine.objects;

import engine.Game;

/**
 * Base game object class which is the parent of all other game object types.
 *
 * @author Galen Savidge
 * @version 5/6/2020
 */
public abstract class GameObject {

    protected int priority;
    protected int layer;
    public boolean visible = true;
    private boolean deleted = false;

    /**
     * Any objects with lower suspend_tier than {@code Game.suspend_tier} will not run {@code update}.
     */
    protected int suspend_tier;

    /**
     * Whether this object persists between scenes or is deleted when the scene changes
     */
    protected boolean persistent;

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
        this.suspend_tier = 0;
        this.persistent = false;
        Game.addObject(this);
    }

    /**
     * Flags the object for deletion. It will no longer receive update or draw events and will be removed from the
     * update and draw queues at the end of the current step.
     */
    public void delete() {
        deleted = true;
    }

    /* Accessors */

    public int getPriority() {
        return priority;
    }
    
    public int getLayer() {
        return layer;
    }

    public int getSuspendTier() {
        return suspend_tier;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isDeleted() {
        return deleted;
    }

    /* Events */

    /**
     * This method is called by {@link Game} every step and should be overwritten in child classes. Order depends on the
     * priority of the created objects.
     */
    public abstract void update();

    /**
     * This method is called by {@link Game} every step while {@code this.visible == true}. Order depends on
     * {@code layer}.
     */
    public abstract void draw();

    /**
     * This method is called by {@link Game} at the end of a step when this object is deleted.
     */
    public void deleteEvent() {}
}
