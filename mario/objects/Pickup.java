package mario.objects;

import mario.Mario;

public abstract class Pickup extends WorldObject {
    public Pickup(double x, double y) {
        super(Mario.gizmo_priority, Mario.gizmo_layer, x, y);
        this.type_group = Types.pickup_type_group;
    }

    public abstract void collect();
}
