
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
        System.out.println("Main started...");
        Game.setTargetFPS(60);
        Background b = new Background();
        Block o1 = new Block( 50, 50);
        Block o2 = new Block(200, 50);
        Block o3 = new Block(700,500);
        GameGraphics.initWindow("Mario",800,600, 2);
        Game.run();
        GameGraphics.closeWindow();
    }
}
