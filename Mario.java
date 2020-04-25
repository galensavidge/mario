
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
        GameObject o1 = new GameObject(1, 1);
        GameObject o2 = new GameObject(2, 2);
        GameObject o3 = new GameObject(0, 0);
        GameGraphics.initWindow("Mario",800,600, 2);
        Game.run();
        GameGraphics.closeWindow();
        return;
    }
}
