package mario;

import engine.LevelParser;
import mario.objects.*;

import java.util.HashMap;

/**
 * This class loads JSON files using {@link engine.LevelParser}.
 *
 * @author Galen Savidge
 * @version 5/12/2020
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
        type_table.put(Ground.type_name.toLowerCase(), Ground::new);
    }
}
