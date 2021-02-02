package mario.enemies;

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
 * @version 6/6/2020
 */
public class Galoomba extends Enemy {

    public static final String type_name = "Galoomba";


    /* State names */
    public static final String walkStateName = "Walk";
    public static final String fallStateName = "Fall";
    public static final String stunStateName = "Stun";
    public static final String kickStateName = "Kick";


    /* Physics constants */
    private static final double walk_speed = 160;
    private static final double friction = 1000;


    /* Class constants */
    private static final int stun_time = 10*Mario.fps;


    /* Sprites */
    private static final String[] walk_sprite_files = {Mario.sprite_path + "galoomba-walk-1.png",
            Mario.sprite_path + "galoomba-walk-2.png"};
    private final AnimatedSprite walk_sprite = new AnimatedSprite(walk_sprite_files, suspend_tier);


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
        double speed;
        boolean reverse_direction = false;

        public WalkState() {
            stick_to_ground = true;
        }

        @Override
        public String getState() {
            return walkStateName;
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
        }

        @Override
        protected void handlePhysicsCollisionEvent(GroundCollision g) {
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

        @Override
        public String getState() {
            return fallStateName;
        }

        @Override
        public void enter() {

        }

        @Override
        public void update() {
            velocity = applyGravity(velocity, gravity, fall_speed);
        }

        @Override
        protected void handlePhysicsCollisionEvent(GroundCollision g) {
            super.handlePhysicsCollisionEvent(g);
            if(g.type != GroundType.NONE) {
                setNextState(new WalkState());
            }
        }
    }

    private class StunState extends EnemyState {
        private int timer;

        @Override
        public String getState() {
            return stunStateName;
        }

        @Override
        public void enter() {
            timer = stun_time;
            stick_to_ground = true;
        }

        @Override
        public void update() {
            if(ground_found.type != GroundType.NONE) {
                velocity = ground_found.velocity;
            }
            else {
                velocity = applyGravity(velocity, gravity, fall_speed);
            }
            timer--;
            if(timer == 0) {
                setNextState(new WalkState());
            }
        }

        @Override
        void handleBounceEvent(Player player) {
            Direction d = player.getPosition().x > getPosition().x ? Direction.LEFT : Direction.RIGHT;
            if(player.getState().equals(Player.slideStateName)) {
                state.setNextState(new DieState(d));
            }
            else {
                state.setNextState(new KickState(d));
            }
        }

        @Override
        public void draw() {
            GameGraphics.drawImage((int)pixelPosition().x, (int)pixelPosition().y, false, false,
                    direction_facing == Direction.RIGHT, Math.PI, 0, walk_sprite.getCurrentFrame());
        }
    }

    private class KickState extends EnemyState {

        Direction direction;
        double kick_speed_x = 300;
        double kick_speed_y = -300;
        int bounces = 0;
        double slide_distance = kick_speed_x*kick_speed_x/(2*friction);
        boolean apply_friction = false;

        public KickState(Direction direction) {
            this.direction = direction;
        }

        @Override
        public String getState() {
            return kickStateName;
        }

        @Override
        public void enter() {
            velocity.y = kick_speed_y;
            velocity.x = direction == Direction.RIGHT ? kick_speed_x : -kick_speed_x;
        }

        @Override
        public void update() {
            if(ground_found.type != GroundType.NONE) {
                Vector2 local_velocity;
                if(last_ground.sameObject(ground_found)) {
                    local_velocity = velocity.difference(last_ground.velocity);
                }
                else {
                    local_velocity = velocity.difference(ground_found.velocity);
                }

                double x_pos = getPosition().x - ground_found.intersection.collided_with.getPosition().x;
                double nearest_grid;
                if(velocity.x > 0) {
                    nearest_grid = Mario.getGridScale() - x_pos % Mario.getGridScale();
                }
                else {
                    nearest_grid = x_pos % Mario.getGridScale();
                }

                // Snap to grid
                if(nearest_grid < Mario.getPixelSize()) {
                    apply_friction = true;
                    //setPosition(nearest_grid, getPosition().y);
                }
                if(apply_friction && nearest_grid < slide_distance) {
                    double f = new Vector2(friction, 0).normalComponent(ground_found.intersection.getNormal()).abs();
                    local_velocity = applyFriction(local_velocity, f);
                }

                // Set global velocity
                velocity = local_velocity.sum(ground_found.velocity);

                if(local_velocity.equals(Vector2.zero())) {
                    setNextState(new StunState());
                }
            }
            else {
                velocity = applyGravity(velocity, gravity, fall_speed);
            }
        }

        @Override
        protected void handlePhysicsCollisionEvent(GroundCollision g) {
            if(g.type != GroundType.NONE && bounces < 2)
            {
                bounces++;
                kick_speed_y /= 2;
                velocity.y = kick_speed_y;
            }
            else {
                super.handlePhysicsCollisionEvent(g);
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
