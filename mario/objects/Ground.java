package mario.objects;

import engine.World;
import engine.collider.Collider;
import engine.objects.PhysicsObject;
import engine.util.Vector2;
import mario.Mario;

import java.util.HashMap;

public class Ground extends PhysicsObject {
    public static final String type_name = "Ground";

    public Ground(Vector2[] vertices, boolean semisolid) {
        super(Mario.block_priority, Mario.block_layer, 0, 0);
        this.collider = new Collider(this, vertices);
        this.solid = !semisolid;
        init();
    }

    public Ground(HashMap<String, Object> args) {
        super(Mario.block_priority, Mario.block_layer, args);
        init();
    }

    private void init() {
        this.type = Ground.type_name;
        this.type_group = Types.block_type_group;
        this.tags.add(Types.ground_tag);
        this.visible = true;
    }

    public Vector2 getSurfaceVelocity() {
        return velocity.copy();
    }

    @Override
    public void draw() {
        collider.draw_self = true;
        collider.draw();
    }

    @Override
    protected void parseArgs(HashMap<String, Object> args) {
        super.parseArgs(args);

        try {
            Object solid = args.get("semisolid");
            if(solid != null) {
                this.solid = !(boolean)solid;
                if((boolean)solid) {
                    this.tags.add(Types.semisolid_tag);
                }
            }
            else {
                this.solid = true;
            }
            Object vertices = args.get("vertices");
            if(vertices != null) {
                this.collider = new Collider(this, (Vector2[])vertices);
            }
            else {
                this.collider = Collider.newBox(this, 0, 0, World.getGridSize(), World.getGridSize());
            }
        }
        catch(ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
