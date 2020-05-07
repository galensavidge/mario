package mario.objects;

import engine.GameGraphics;
import engine.objects.Collider;
import engine.objects.PhysicsObject;
import mario.Mario;

/**
 * The base class for ground and other types of blocks.
 *
 * @author Galen Savidge
 * @version 5/6/2020
 */
public abstract class Block extends WorldObject {

    public Block(double x, double y) {
        super(1, 1, x, y);
        this.type_group = Types.block_type_group;
        this.collider = Collider.newBox(this,0,0, Mario.getGridScale(),Mario.getGridScale());
        this.solid = true;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {

    }
}