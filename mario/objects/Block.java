package mario.objects;

import engine.objects.Collider;
import engine.objects.PhysicsObject;
import mario.Mario;

/**
 * The base class for ground and other types of blocks.
 *
 * @author Galen Savidge
 * @version 5/9/2020
 */
public abstract class Block extends PhysicsObject {

    public Block(double x, double y) {
        super(Mario.block_priority, Mario.block_layer, x, y);
        this.type_group = Types.block_type_group;
        this.collider = Collider.newBox(this,0,0, Mario.getGridScale(),Mario.getGridScale());
        this.solid = true;
    }
}