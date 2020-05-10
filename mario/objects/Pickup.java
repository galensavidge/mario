package mario.objects;

import mario.Mario;

/**
 * The parent class for objects that the player object can collect.
 *
 * @author Galen Savidge
 * @version 5/9/2020
 */
public abstract class Pickup extends WorldObject {
    public Pickup(double x, double y) {
        super(Mario.gizmo_priority, Mario.gizmo_layer, x, y);
        this.type_group = Types.pickup_type_group;
    }

    public abstract void collect();
}
