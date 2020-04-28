public class GameController extends GameObject {
    RoundBlock b1;
    Block b2;

    public GameController() {
        super(0, 0);
        b1 = new RoundBlock(48, 50);
        b2 = new Block(48, 100);

        this.persistent = true;
    }

    @Override
    public void update() {
        /*if(InputManager.getPressed(InputManager.K_LEFT)) {
            b2.position.x -= 4;
        }
        if(InputManager.getPressed(InputManager.K_RIGHT)) {
            b2.position.x += 4;
        }
        if(InputManager.getPressed(InputManager.K_DOWN)) {
            Vector2 v = b2.collider.vectorToContact(b1.collider, new Vector2(0.1, -0.8));
            if(v != null) {
                b2.position = b2.position.add(v);
            }
        }
        if(b1.collider.getCollisions(Vector2.zero(),false, false).size() != 0) {
            b1.velocity = b1.velocity.multiply(-1);
        }*/
    }

    @Override
    public void draw() {

    }
}
