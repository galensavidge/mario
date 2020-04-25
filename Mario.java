
/**
 * Main game class that runs the program.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class Mario
{
    public static void main(String args[])
    {
        System.out.println("Main started...");
        
        Game.setTargetFPS(60);
        Background b = new Background();
        GameObject o1 = new GameObject(1, 1, 50, 50);
        GameObject o2 = new GameObject(2, 2, 200, 50);
        GameObject o3 = new GameObject(0, 0, 700,500);
        GameGraphics.initWindow("Mario",800,600, 2);
        Game.run();
        GameGraphics.closeWindow();
        return;
    }
}
