package mario;

import engine.LevelParser;
import mario.enemies.Galoomba;
import mario.objects.*;

import java.util.HashMap;

/**
 * This class loads JSON files using {@link engine.LevelParser}.
 *
 * @author Galen Savidge
 * @version 6/8/2020
 */
public class WorldLoader {

    private static HashMap<String, LevelParser.TypeMap> type_table;


    public static void loadFromFile(String directory, String file_name) {
        if(type_table == null) {
            buildTypeTable();
        }
        LevelParser.loadFromJson(directory, file_name, type_table);
    }

    static private void buildTypeTable() {
        type_table = new HashMap<>();
        type_table.put(GameController.spawn_point_type_name.toLowerCase(), GameController::createSpawn);
        type_table.put(Ground.type_name.toLowerCase(), Ground::new);
        type_table.put(HardBlock.type_name.toLowerCase(), HardBlock::new);
        type_table.put(Spike.type_name.toLowerCase(), Spike::new);
        type_table.put(MovingPlatform.type_name.toLowerCase(), Spawner::new);
        type_table.put(Coin.type_name.toLowerCase(), Coin::new);
        type_table.put(Galoomba.type_name.toLowerCase(), Spawner::new);
    }
}
