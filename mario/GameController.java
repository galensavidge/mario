package mario;

import engine.Camera;
import engine.GameGraphics;
import engine.objects.Collider;
import engine.objects.GameObject;
import engine.util.Vector2;

import java.awt.*;

/**
 * A persistent object that keeps track of game variables and controls the high level flow of the game.
 *
 * @author Galen Savidge
 * @version 5/8/2020
 */
public class GameController extends GameObject {

    private static String current_level;
    private static Vector2 player_spawn_position;
    private static String player_spawn_level;

    private static NewPlayer player;

    public static int coins = 0;

    public GameController() {
        super(0, 20);
        this.persistent = true;
    }

    public static void switchLevel(String file_name) {
        current_level = file_name;
        WorldLoader.loadFromFile(file_name);
        spawnPlayer();
    }

    public static void spawnPlayer() {
        if(!current_level.equals(player_spawn_level)) {
            WorldLoader.loadFromFile(player_spawn_level);
        }
        player = new NewPlayer(player_spawn_position.x, player_spawn_position.y);
        new Camera(player);
    }

    public static void setPlayerSpawn(Vector2 position) {
        player_spawn_position = position.copy();
        player_spawn_level = current_level;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        GameGraphics.drawText(64, 64, true, Integer.toString(coins), Color.BLACK);
    }
}
