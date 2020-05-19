package mario.enemies;

import engine.Game;
import engine.World;
import mario.Mario;
import mario.PlatformingObject;
import mario.Player;
import mario.objects.Types;

import java.util.HashMap;

/**
 * The parent class for enemies and moving obstacles.
 *
 * @author Galen Savidge
 * @version 5/14/2020
 */
public class Enemy extends PlatformingObject {

    public Enemy(double x, double y) {
        super(Mario.enemy_priority, Mario.enemy_layer, x, y);
        this.type_group = Types.enemy_type_group;
    }

    public Enemy(HashMap<String, Object> args) {
        super(Mario.enemy_priority, Mario.enemy_layer, args);
        this.type_group = Types.enemy_type_group;
    }

    /**
     * Called when the player collides with this object.
     *
     * @param player A reference to the player object.
     */
    public void bounceEvent(Player player) {
        ((EnemyState)this.state).handleBounceEvent(player);
    }

    protected abstract class EnemyState extends State {

        void handleBounceEvent(Player player) {

        }

    }

    /**
     * The common enemy death state in which the object spins and falls off the screen. Draw events will need to be
     * overridden.
     */
    protected abstract class DieState extends EnemyState {
        protected String name = "Die";

        protected static final double hop_yspeed = -400;
        protected static final double hop_xspeed = 200;
        protected static final double gravity = 1500;
        protected static final double max_fall_speed = 800;
        protected static final double spin_speed = 9; // Rad/s

        protected Direction direction;
        protected double rotation;

        public DieState(Direction direction) {
            this.direction = direction;
        }

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void enter() {
            collider.disable();
            rotation = 0;
            velocity.y = hop_yspeed;
            velocity.x = hop_xspeed;
            if(direction == Direction.LEFT) {
                velocity.x *= -1;
            }
        }

        @Override
        public void update() {
            velocity = applyGravity(velocity, gravity, max_fall_speed);
            rotation += spin_speed*Game.stepTimeSeconds();

            if(position.y > World.getHeight()) {
                Enemy.this.delete();
            }
        }
    }
}
