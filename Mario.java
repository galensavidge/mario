/**
 * Main game class that runs the program.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class Mario
{
    public static void main(String[] args)
    {
        Game.setTargetFPS(60);
        GameGraphics.initWindow("Mario",360,240, 4);
        Background b = new Background();
        World.init(4000,300);
        GameController gc = new GameController();
        InputManager.init();

        World.loadFromFile("./levels/level0");
        new Player(128,48);
        Game.run();
        GameGraphics.closeWindow();
    }
}
