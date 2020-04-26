public class GameController extends GameObject {
    Block b1;
    Block b2;

    int b1_xspeed = -1;

    public GameController() {
        super(10, 0);
        b1 = new Block(300, 300);
        b2 = new Block(100, 300);

        this.persistent = true;
    }

    @Override
    public void update() {
        if(b1.collider.checkCollision(b1_xspeed, 0,false)) {
            b1_xspeed *= -1;
        }
        b1.x += b1_xspeed;
    }

    @Override
    public void draw() {

    }
}
