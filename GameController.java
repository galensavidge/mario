public class GameController extends GameObject {
    Block b1;
    Block b2;

    public GameController() {
        super(10, 0);
        b1 = new Block(100, 300);
        b2 = new Block(300, 300);

        b1.vx = 100;

        this.persistent = true;
    }

    @Override
    public void update() {
        if(InputManager.getPressed(InputManager.K_LEFT)) {
            b2.x -= 10;
        }
        if(InputManager.getReleased(InputManager.K_RIGHT)) {
            b2.x += 10;
        }
        if(b1.collider.checkCollision(0, 0,false) != null) {
            b1.vx *= -1;
        }
    }

    @Override
    public void draw() {

    }
}
