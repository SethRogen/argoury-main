/**
 *
 */
package com.ziotic.logic.object;

import com.ziotic.adapter.protocol.cache.format.ObjectDefinitionAdapter;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class ObjectDefinition {
    private static final Logger logger = Logging.log();

    private static Map<Integer, ObjectDefinition> cachedDefinitions = new HashMap<Integer, ObjectDefinition>();

    public static ObjectDefinition forId(int id) {
        ObjectDefinition def = cachedDefinitions.get(id);
        if (def == null) {
            try {
                def = ObjectDefinitionAdapter.forId(id);
            } catch (Exception e) {
                logger.error("Failed to load object definition for id : " + id/*, e*/);
                def = new ObjectDefinition();
            }
            cachedDefinitions.put(id, def);
        }
        return def;
    }

    public int id;
    public String name = "null";
    public boolean walkable = true;
    public boolean clippingFlag = false;
    public String[] actions = new String[5];
    public int actionCount = 2;
    public int sizeX = 1;
    public int sizeY = 1;
    public int walkToData = 0;
    public int miniMapSpriteId = -1;

    public boolean hasActions() {
        for (String s : actions) {
            if (s != null && !s.equals("null") && !s.equals("")) {
                return true;
            }
        }
        return false;
    }
}
