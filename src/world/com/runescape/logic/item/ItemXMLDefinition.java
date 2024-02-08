package com.runescape.logic.item;

import org.apache.log4j.Logger;

import com.runescape.Static;
import com.runescape.utility.Logging;

import java.io.IOException;
import java.util.Map;

/**
 * @author Lazaro
 */
public class ItemXMLDefinition implements Comparable<ItemXMLDefinition> {
    private static final Logger logger = Logging.log();

    private static Map<Integer, ItemXMLDefinition> xmlDefinitions = null;

    public static void load() throws IOException {
        xmlDefinitions = Static.xml.readObject(Static.parseString("%WORK_DIR%/world/items/itemdefs.xml"));
        logger.info("Loaded " + xmlDefinitions.size() + " XML item definitions");
    }

    public static Map<Integer, ItemXMLDefinition> getXMLDefinitions() {
        return xmlDefinitions;
    }

    public int id = -1;
    public String examine = "Nothing interesting.";
    public double weight = 0;

    @Override
    public int compareTo(ItemXMLDefinition def) {
        return def.id - id;
    }
}
