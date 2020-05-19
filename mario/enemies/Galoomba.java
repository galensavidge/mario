package mario.enemies;

import engine.graphics.GameGraphics;
import engine.graphics.AnimatedSprite;
import engine.objects.Collider;
import engine.objects.Collider.Collision;
import mario.GameController;
import mario.Mario;
import mario.Player;

import java.util.HashMap;

/**
 * A Galoomba, the Goomba equivalent from SMW.
 *
 * @author Galen Savidge
 * @version 5/16/2020
 */
public class Galoomba extends Enemy {

    public static final String type_name = "Galoomba";

    private static final double gravity = 1200;
    private static final double fall_speed = 900;
    private static final double walk_speed = 160;

    private static final int stun_time = 10*Mario.fps;

    private static final String[] walk_sprite_files = {Mario.sprite_path + "galoomba-walk-1.png",
            Mario.sprite_path + "galoomba-walk-2.png"};
    private final AnimatedSprite walk_sprite = new AnimatedSprite(walk_sprite_files);

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

        this.collider = Collider.newPolygon(this, 8, 0, 0, Mario.getGridScale()/2.0, 0);
        this.height = Mario.getGridScale();

        Player player = GameController.getPlayer();
        if(player != null) {
            if(player.position.x > position.x) {
                direction_facing = Direction.RIGHT;
            }
            else {
                direction_facing = Direction.LEFT;
            }
        }

        this.state = new WalkState();
        this.state.enter();
    }

    public void stun() {
        state.setNextState(new StunState());
    }


    private class WalkState extends EnemyState {
        public String name = "Walk";
        double speed;
        boolean reverse_direction = false;

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void enter() {
            walk_sprite.setFrameTime(20);
            if(direction_facing == Direction.LEFT) {
                speed = -walk_speed;
            }
            else {
                speed = walk_speed;
            }
        }

        @Override
        public void update() {
            Collision ground = snapToGround();
            if(ground.collision_found) {
                if(reverse_direction) {
                    reverse_direction = false;
                    speed = -speed;
                }

                velocity = ground.normal_reject.RHNormal().normalize().multiply(speed);
                velocity = velocity.sum(ground.collided_with.velocity);

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
        protected void handleCollisionEvent(Collision collision, GroundType c_ground_type) {
            super.handleCollisionEvent(collision, c_ground_type);
            if(c_ground_type == GroundType.NONE) {
                reverse_direction = true;
            }
        }

        @Override
        void handleBounceEvent(Player player) {
            if(player.getState().equals("Slide")) {
                state.setNextState(new DieState(player.position.x > position.x ? Direction.LEFT : Direction.RIGHT));
            }
            else if(player.position.y + player.getHeight() < Galoomba.this.position.y + Galoomba.this.height/2.0) {
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
        protected void handleCollisionEvent(Collision collision, GroundType c_ground_type) {
            super.handleCollisionEvent(collision, c_ground_type);
            if(c_ground_type != GroundType.NONE) {
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
            Collision ground = snapToGround();
            if(ground.collision_found) {
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
            GameGraphics.drawImage((int)position.x, (int)position.y, false, false,
                    direction_facing == Direction.RIGHT, Math.PI, 0, walk_sprite.getCurrentFrame());
        }
    }

    private class DieState extends Enemy.DieState {

        public DieState(Direction direction) {
            super(direction);
        }

        @Override
        public void draw() {
            GameGraphics.drawImage((int)position.x, (int)position.y, false, false,
                    direction_facing == Direction.RIGHT, rotation, 0, walk_sprite.getCurrentFrame());
        }
    }
}
