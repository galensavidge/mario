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
        GameGraphics.initWindow("Mario",800,600, 2);
        Background b = new Background();
        World.init(8000,600);
        GameController gc = new GameController();
        Game.run();
        GameGraphics.closeWindow();
    }
}
