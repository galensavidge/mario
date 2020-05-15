package mario;

import engine.Camera;
import engine.Game;
import engine.GameGraphics;
import engine.objects.GameObject;
import engine.util.Vector2;

import java.awt.*;
import java.util.HashMap;

/**
 * A persistent object that keeps track of game variables and controls the high level flow of the game.
 *
 * @author Galen Savidge
 * @version 5/14/2020
 */
public class GameController extends GameObject {

    public static final String spawn_point_type_name = "Spawnpoint";

    private static String current_level;
    private static int player_spawn;
    private static String player_spawn_level;

    private static HashMap<Integer, Vector2> spawn_points;

    private static NewPlayer player;
    private static Camera camera;

    public static int coins = 0;

    public GameController() {
        super(0, 20);
        this.persistent = true;
    }

    public static void switchLevel(String level_file_name) {
        setPlayerSpawn(level_file_name, 0);
        new Transition(1, Transition.Type.PIXEL_OUT, ()-> _loadArea(level_file_name, 0));
    }

    public static void switchArea(String level_file_name, int spawn_id) {
        new Transition(1, Transition.Type.PIXEL_OUT, ()-> _loadArea(level_file_name, spawn_id));
    }

    public static void createSpawn(Vector2 position, int spawn_id) {
        spawn_points.put(spawn_id, position);
    }

    public static void createSpawn(HashMap<String, Object> args) {
        int spawn_id = 0;
        Object spawn_id_object = args.get("spawn id");
        if(spawn_id_object != null) spawn_id = (int)(long)spawn_id_object;
        Vector2 position;
        Object position_object = args.get("position");
        if(position_object != null)
        {
            position = (Vector2)position_object;
            createSpawn(position, spawn_id);
        }
    }

    public static void setPlayerSpawn(String level_file_name, int spawn_id) {
        player_spawn_level = level_file_name;
        player_spawn = spawn_id;
    }

    public static void respawnPlayer() {
        switchArea(player_spawn_level, player_spawn);
    }

    private static void _loadArea(String level_file_name, int spawn_id) {
        current_level = level_file_name;
        spawn_points = new HashMap<>();
        Game.clearNonPersistentObjects();
        WorldLoader.loadFromFile(Mario.level_path, level_file_name);
        _spawnPlayer(spawn_id);
        Transition.EventPointer animation_finish_action = ()->Game.setSuspendTier(0);
        new Transition(1, Transition.Type.PIXEL_IN, animation_finish_action);
    }

    private static void _spawnPlayer(int spawn_id) {
        Vector2 position = spawn_points.get(spawn_id);
        player = new NewPlayer(position.x, position.y);
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
