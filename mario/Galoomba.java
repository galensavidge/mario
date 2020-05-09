package mario;

import engine.Sprite;
import engine.objects.Collider;
import engine.objects.Collider.Collision;
import mario.objects.Types;

public class Galoomba extends PlatformingObject {

    public static final String type_name = "Galoomba";

    private static final double gravity = 1200;
    private static final double fall_speed = 900;
    private static final double walk_speed = 160;

    private static final int stun_time = 10*Mario.fps;

    private static final String[] walk_sprite_files = {"./sprites/galoomba-walk-1.png", "./sprites/galoomba-walk-2.png"};
    private final Sprite walk_sprite = new Sprite(walk_sprite_files);

    public Galoomba(double x, double y) {
        super(12, 5, x, y);
        this.type = Galoomba.type_name;
        this.type_group = Types.enemy_type_group;

        this.collider = Collider.newPolygon(this, 8, 0, 0, Mario.getGridScale()/2.0, 0);
        this.height = Mario.getGridScale();

        this.state = new WalkState();
        this.state.enter();
    }

    public void stun() {
        state.setNextState(new StunState());
    }

    private class WalkState extends State {
        public String name = "Walk";
        double speed = -walk_speed;
        boolean reverse_direction = false;

        @Override
        String getState() {
            return name;
        }

        @Override
        void enter() {
            walk_sprite.setFrameTime(20);
        }

        @Override
        void update() {
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
        void handleCollision(Collision c, GroundType c_ground_type) {
            super.handleCollision(c, c_ground_type);
            if(c_ground_type == GroundType.NONE) {
                reverse_direction = true;
            }
        }

        @Override
        void draw() {
            drawSprite(walk_sprite.getCurrentFrame());
        }
    }

    private class FallState extends State {
        public String name = "Fall";

        @Override
        String getState() {
            return name;
        }

        @Override
        void update() {
            velocity = applyGravity(velocity, gravity, fall_speed);
            walk_sprite.incrementFrame();
        }

        @Override
        void handleCollision(Collision c, GroundType c_ground_type) {
            super.handleCollision(c, c_ground_type);
            if(c_ground_type != GroundType.NONE) {
                setNextState(new WalkState());
            }
        }

        @Override
        void draw() {
            drawSprite(walk_sprite.getCurrentFrame());
        }
    }

    private class StunState extends State {
        public String name = "Stun";
        private int timer;

        @Override
        String getState() {
            return name;
        }

        @Override
        void enter() {
            timer = stun_time;
        }

        @Override
        void update() {
            Collision ground = snapToGround();
            velocity = ground.collided_with.velocity;
            walk_sprite.incrementFrame();
            timer--;
            if(timer == 0) {
                setNextState(new WalkState());
            }
        }

        @Override
        void draw() {
            drawSprite(walk_sprite.getCurrentFrame(), true);
        }
    }
}
