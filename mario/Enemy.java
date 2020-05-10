package mario;

import mario.objects.Types;

/**
 * The parent class for enemies and moving obstacles.
 *
 * @author Galen Savidge
 * @version 5/9/2020
 */
public class Enemy extends PlatformingObject {

    public Enemy(double x, double y) {
        super(Mario.enemy_priority, Mario.enemy_layer, x, y);
        this.type_group = Types.enemy_type_group;
    }

    /**
     * Called when the player collides with this object.
     * @param player A reference to the player object.
     */
    public void bounceEvent(NewPlayer player) {
        ((EnemyState)this.state).handleBounceEvent(player);
    }

    protected abstract class EnemyState extends State {

        void handleBounceEvent(NewPlayer player) {

        }

    }
}
