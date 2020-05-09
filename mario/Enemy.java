package mario;

import mario.objects.Types;

public class Enemy extends PlatformingObject {

    public Enemy(double x, double y) {
        super(Mario.enemy_priority, Mario.enemy_layer, x, y);
        this.type_group = Types.enemy_type_group;
    }

    public void bounceOn(NewPlayer player) {
        ((EnemyState)this.state).bounce(player);
    }

    protected abstract class EnemyState extends State {

        void bounce(NewPlayer player) {

        }

    }
}
