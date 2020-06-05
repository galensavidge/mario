package mario.enemies;

import engine.collider.Intersection;
import engine.graphics.GameGraphics;
import engine.graphics.AnimatedSprite;
import engine.collider.Collider;
import engine.util.Vector2;
import mario.GameController;
import mario.Mario;
import mario.Player;

import java.util.HashMap;

/**
 * A Galoomba, the Goomba equivalent from SMW.
 *
 * @author Galen Savidge
 * @version 6/4/2020
 */
public class Galoomba extends Enemy {

    public static final String type_name = "Galoomba";


    /* State names */
    public static final String walkStateName = "Walk";
    public static final String fallStateName = "Fall";
    public static final String stunStateName = "Stun";


    /* Physics constants */
    private static final double gravity = 2000;
    private static final double fall_speed = 1200;
    private static final double walk_speed = 160;


    /* Class constants */
    private static final int stun_time = 10*Mario.fps;


    /* Sprites */
    private static final String[] walk_sprite_files = {Mario.sprite_path + "galoomba-walk-1.png",
            Mario.sprite_path + "galoomba-walk-2.png"};
    private final AnimatedSprite walk_sprite = new AnimatedSprite(walk_sprite_files);


    /* Constructors */
    public Galoomba(double x, double y) {
        super(x, y);
        init();
    }

    public Galoomba(HashMap<String, Object> args) {
        super(args);
        init();
    }

    private void init() {
        this.type = Galoomba.type_name;

        double px = Mario.getPixelSize(), es = Collider.edge_separation;
        Vector2[] vertices = {new Vector2(6*px + es, px + es), new Vector2(10*px - es, px + es),
                new Vector2(15*px - es, 5*px), new Vector2(15*px - es, 16*px - es),
                new Vector2(px + es, 16*px - es), new Vector2(px + es, 5*px)};
        this.collider = new Collider(this, vertices);

        Player player = GameController.getPlayer();
        if(player != null) {
            if(player.getPosition().x > getPosition().x) {
                direction_facing = Direction.RIGHT;
            }
            else {
                direction_facing = Direction.LEFT;
            }
        }

        this.state = new WalkState();
        this.state.enter();
    }


    /* Public methods */

    public void stun() {
        state.setNextState(new StunState());
    }


    /* Event handlers */
    @Override
    public void worldLoadedEvent() {
        // Check if we spawned on a muncher
    }


    /* State machine */

    private class WalkState extends EnemyState {
        public String name = "Walk";
        double speed;
        boolean reverse_direction = false;

        public WalkState() {
            stick_to_ground = true;
        }

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void enter() {
            walk_sprite.setFrameTime(Mario.fps/3);
            if(direction_facing == Direction.LEFT) {
                speed = -walk_speed;
            }
            else {
                speed = walk_speed;
            }
        }

        @Override
        public void update() {
            if(ground_found.type != GroundType.NONE) {
                if(reverse_direction) {
                    reverse_direction = false;
                    speed = -speed;
                }

                velocity = ground_found.intersection.getNormal().RHNormal().normalize().multiply(speed);
                velocity = velocity.sum(ground_found.velocity);

                if(speed > 0) {
                    direction_facing = Direction.RIGHT;
                }
                else {
                    direction_facing = Direction.LEFT;
                }
            }
            else {
                setNextState(new FallState());
            }

            walk_sprite.incrementFrame();
        }

        @Override
        protected void handlePhysicsCollisionEvent(Ground g) {
            super.handlePhysicsCollisionEvent(g);
            if(g.type == GroundType.NONE) {
                reverse_direction = true;
            }
        }

        @Override
        void handleBounceEvent(Player player) {
            String ps = player.getState();
            if(ps.equals(Player.slideStateName)) {
                state.setNextState(new DieState(player.getPosition().x > getPosition().x ? Direction.LEFT :
                        Direction.RIGHT));
            }
            else if((ps.equals(Player.fallStateName) || ps.equals(Player.jumpStateName))
                    && player.getPosition().y + player.getHeight() < Galoomba.this.getPosition().y + getHeight()/2.0) {
                player.bounce();
                Galoomba.this.stun();
            }
            else {
                player.damage();
            }
        }

        @Override
        public void draw() {
            drawSprite(walk_sprite.getCurrentFrame());
        }
    }

    private class FallState extends WalkState {
        public String name = "Fall";

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void enter() {

        }

        @Override
        public void update() {
            velocity = applyGravity(velocity, gravity, fall_speed);
            walk_sprite.incrementFrame();
        }

        @Override
        protected void handlePhysicsCollisionEvent(Ground g) {
            super.handlePhysicsCollisionEvent(g);
            if(g.type != GroundType.NONE) {
                setNextState(new WalkState());
            }
        }
    }

    private class StunState extends EnemyState {
        public String name = "Stun";
        private int timer;

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void enter() {
            timer = stun_time;
        }

        @Override
        public void update() {
            Intersection ground = snapToGround();
            if(ground != null) {
                velocity = ground.collided_with.velocity;
            }
            else {
                velocity = applyGravity(velocity, gravity, fall_speed);
            }
            walk_sprite.incrementFrame();
            timer--;
            if(timer == 0) {
                setNextState(new WalkState());
            }
        }

        @Override
        public void draw() {
            GameGraphics.drawImage((int)pixelPosition().x, (int)pixelPosition().y, false, false,
                    direction_facing == Direction.RIGHT, Math.PI, 0, walk_sprite.getCurrentFrame());
        }
    }

    private class DieState extends Enemy.DieState {

        public DieState(Direction direction) {
            super(direction);
        }

        @Override
        public void draw() {
            GameGraphics.drawImage((int)pixelPosition().x, (int)pixelPosition().y, false, false,
                    direction_facing == Direction.RIGHT, rotation, 0, walk_sprite.getCurrentFrame());
        }
    }
}