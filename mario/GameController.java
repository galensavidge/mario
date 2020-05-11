package mario;

import engine.Camera;
import engine.Game;
import engine.GameGraphics;
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
    private static Camera camera;

    public static int coins = 0;

    private static ActionQueue animationFinishAction;

    private interface ActionQueue {
        void nextAction();
    }

    public GameController() {
        super(0, 20);
        this.persistent = true;
    }

    public static void transitionToLevel(String file_name) {
        animationFinishAction = ()->switchToLevel(file_name);
        new Transition(1, Transition.Type.PIXEL_OUT);
    }

    public static void respawnPlayer() {
        transitionToLevel(player_spawn_level);
    }

    public static void setPlayerSpawn(Vector2 position) {
        player_spawn_position = position.copy();
        player_spawn_level = current_level;
    }

    public static void switchToLevel(String file_name) {
        current_level = file_name;
        WorldLoader.loadFromFile(file_name);
        _spawnPlayer();
        animationFinishAction = ()->Game.setSuspendTier(0);
        new Transition(1, Transition.Type.PIXEL_IN);
    }

    private static void _spawnPlayer() {
        player = new NewPlayer(player_spawn_position.x, player_spawn_position.y);
        camera = new Camera(player);
    }

    public static void releaseCamera() {
        camera.anchor = null;
    }

    public static void animationFinishedEvent() {
        animationFinishAction.nextAction();
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        GameGraphics.drawText(64, 64, true, Integer.toString(coins), Color.BLACK);
    }
}
