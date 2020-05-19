package mario;

import engine.Camera;
import engine.Game;
import engine.GameGraphics;
import engine.objects.GameObject;
import engine.objects.PhysicsObject;
import engine.util.Vector2;

import java.awt.*;
import java.util.HashMap;

/**
 * A persistent object that keeps track of game variables and controls the high level flow of the game.
 *
 * @author Galen Savidge
 * @version 5/16/2020
 */
public class GameController extends GameObject {

    public static final String spawn_point_type_name = "Spawnpoint";

    private static String current_level;
    private static int player_spawn;
    private static String player_spawn_level;

    private static int spawning_player_at;

    private static HashMap<Integer, Vector2> spawn_points;

    private static Player player;
    private static Camera camera;

    public static int coins = 0;

    public GameController() {
        super(0, 20);
        this.persistent = true;
    }

    public static Player getPlayer() {
        return player;
    }

    public static void switchLevel(String level_file_name) {
        setPlayerSpawn(level_file_name, 0);
        new Transition(1, Transition.Type.PIXEL_OUT, () -> _loadArea(level_file_name, 0, Transition.Type.PIXEL_IN));
    }

    public static void switchArea(String level_file_name, int spawn_id) {
        new Transition(1, Transition.Type.FADE_OUT, () -> _loadArea(level_file_name, spawn_id,
                Transition.Type.FADE_IN));
    }

    public static void createSpawn(Vector2 position, int spawn_id) {
        spawn_points.put(spawn_id, position);
    }

    public static PhysicsObject createSpawn(HashMap<String, Object> args) {
        int spawn_id = 0;
        Object spawn_id_object = args.get("spawn id");
        if(spawn_id_object != null) spawn_id = (int)(long)spawn_id_object;
        Vector2 position;
        Object position_object = args.get("position");
        if(position_object != null) {
            position = (Vector2)position_object;
            System.out.println("Spawnpoint created at " + position);
            createSpawn(position, spawn_id);
            if(spawning_player_at == spawn_id) {
                _spawnPlayer(spawn_id);
            }
        }
        return player;
    }

    public static void setPlayerSpawn(String level_file_name, int spawn_id) {
        player_spawn_level = level_file_name;
        player_spawn = spawn_id;
    }

    public static void respawnPlayer() {
        switchArea(player_spawn_level, player_spawn);
    }

    private static void _loadArea(String level_file_name, int spawn_id, Transition.Type transition_type) {
        current_level = level_file_name;
        spawn_points = new HashMap<>();
        Game.clearNonPersistentObjects();
        spawning_player_at = spawn_id;
        WorldLoader.loadFromFile(Mario.level_path, level_file_name);
        spawning_player_at = -1;
        Transition.EventPointer animation_finish_action = () -> Game.setSuspendTier(0);
        new Transition(1, transition_type, animation_finish_action);
    }

    private static void _spawnPlayer(int spawn_id) {
        Vector2 position = spawn_points.get(spawn_id);
        player = new Player(position.x, position.y);
        camera = new Camera(player);
    }

    public static void releaseCamera() {
        camera.anchor = null;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        GameGraphics.drawText(64, 64, true, Integer.toString(coins), Color.BLACK);
    }
}
