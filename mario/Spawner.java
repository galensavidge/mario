package mario;

import engine.GameGraphics;
import engine.LevelParser;
import engine.objects.PhysicsObject;
import mario.objects.MovingPlatform;

import java.util.HashMap;

/**
 * @author Galen Savidge
 * @version 5/15/2020
 */
public class Spawner extends PhysicsObject {

    private static HashMap<String, LevelParser.TypeMap> type_table;

    static private void buildTypeTable() {
        type_table = new HashMap<>();
        type_table.put(MovingPlatform.type_name.toLowerCase(), MovingPlatform::new);
        type_table.put(Galoomba.type_name.toLowerCase(), Galoomba::new);
    }


    HashMap<String, Object> args;
    PhysicsObject instance;
    boolean globally_loaded; // Default: false
    boolean despawns; // Default: true
    double spawn_distance; // In pixels. Default: 1 grid square
    double despawn_distance; // In pixels. Default: 10 grid squares
    double object_width; // In pixels. Default 1 grid square
    boolean in_spawn_box_last_frame = false;

    public Spawner(HashMap<String, Object> args) {
        super(0, 0, args);
        this.args = args;
        if(type_table == null) {
            buildTypeTable();
        }
    }

    @Override
    protected void parseArgs(HashMap<String, Object> args) {
        super.parseArgs(args);
        Object globally_loaded = args.get("globally loaded");
        if(globally_loaded != null) this.globally_loaded = (boolean)globally_loaded;
        else this.globally_loaded = false;
        Object despawns = args.get("despawns");
        if(despawns != null) this.despawns = (boolean)despawns;
        else this.despawns = true;
        Object spawn_distance = args.get("spawn distance");
        if(spawn_distance != null) this.spawn_distance = (long)spawn_distance*Mario.getGridScale();
        else this.spawn_distance = Mario.getGridScale();
        Object despawn_distance = args.get("despawn distance");
        if(despawn_distance != null) this.despawn_distance = (long)despawn_distance*Mario.getGridScale();
        else this.despawn_distance = 10*Mario.getGridScale();
        Object size = args.get("size");
        if(size != null) this.object_width = (long)size*Mario.getGridScale();
        else this.object_width = Mario.getGridScale();
    }

    @Override
    public void worldLoadedEvent() {
        System.out.println("Spawner world loaded event");
        System.out.println("Camera is at: ("+ GameGraphics.camera_x+","+GameGraphics.camera_y+")");
        if(this.isOnScreen(Mario.getGridScale(), Mario.getGridScale(), 0)) {
            spawnInstance();
        }
    }

    private void spawnInstance() {
        Object type_object = args.get("type");
        if (type_object != null) {
            instance = type_table.get((String) type_object).spawn(args);
            System.out.println("Spawned "+type_object+" with args:");
            System.out.println(args);
        }
    }

    private void despawnIfOffscreen() {
        if(!globally_loaded && despawns && !instance.isOnScreen(object_width, Mario.getGridScale(), despawn_distance)) {
            System.out.println("Despawned instance of "+instance.getType());
            instance.delete();
            instance = null;
        }
    }

    @Override
    public void update() {
        boolean spawn_condition = globally_loaded || (!this.isOnScreen(object_width, Mario.getGridScale(), 0)
                        && this.isOnScreen(object_width, Mario.getGridScale(), spawn_distance));
        if(spawn_condition && !in_spawn_box_last_frame) {
            System.out.println("Spawn condition triggered!");
        }
        if(spawn_condition && !in_spawn_box_last_frame && instance == null) {
            spawnInstance();
        }
        in_spawn_box_last_frame = this.isOnScreen(object_width, Mario.getGridScale(), spawn_distance);

        if(instance != null) {
            if(instance.isDeleted()) {
                instance = null;
            }
            else {
                despawnIfOffscreen();
            }
        }
    }
}
