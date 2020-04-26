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
        new Block( 50, 50);
        new Block(200, 50);
        new Block(700,500);
        World.init(8000,600);
        Game.run();
        GameGraphics.closeWindow();
    }
}
