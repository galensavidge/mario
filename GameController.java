public class GameController extends GameObject {
    Block b1;
    Block b2;

    public GameController() {
        super(0, 0);
        b1 = new Block(16*3, 128);
        b2 = new Block(16*9, 128);

        b1.velocity.x = 50;

        this.persistent = true;
    }

    @Override
    public void update() {
        if(InputManager.getPressed(InputManager.K_LEFT)) {
            b2.position.x -= 16;
        }
        if(InputManager.getReleased(InputManager.K_RIGHT)) {
            b2.position.x += 16;
        }
        if(b1.collider.checkCollision(Vector2.zero,false) != null) {
            b1.velocity.x *= -1;
        }
    }

    @Override
    public void draw() {

    }
}
