package com.ziotic.logic.item;

import com.ziotic.Static;
import com.ziotic.adapter.protocol.cache.format.ItemDefinitionAdapter;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class ItemDefinition {
    private static final Logger logger = Logging.log();

    private static Map<Integer, ItemDefinition> cachedDefinitions = new HashMap<Integer, ItemDefinition>();
    private static Map<Integer, Integer> equipmentIds = new HashMap<Integer, Integer>();

    public static ItemDefinition forId(int id) {
        return forId(id, true);
    }

    public static ItemDefinition forId(int id, boolean cache) {
        synchronized (cachedDefinitions) {
            ItemDefinition def = cachedDefinitions.get(id);
            if (def == null) {
                try {
                    def = ItemDefinitionAdapter.forId(id);
                } catch (Exception e) {
                    logger.error("Failed to load item definition for id : " + id/*, e*/);
                    def = new ItemDefinition();
                }
                if (def != null) {
                    ItemXMLDefinition xmlDef;
                    if (def.lendedParentId2 != -1) {
                        def.copyDefinition(forId(def.lendedParentId), forId(def.lendedParentId2));

                        xmlDef = ItemXMLDefinition.getXMLDefinitions().get(def.lendedParentId);
                    } else {
                        xmlDef = ItemXMLDefinition.getXMLDefinitions().get(id);
                    }

                    if (!def.isNoted()) {
                        if (xmlDef != null) {
                            def.examine = xmlDef.examine;
                            def.weight = xmlDef.weight;
                        }
                    } else {
                        def.weight = 0;
                        def.examine = "Swap this note at any bank for the equivalent item.";
                    }

                    if (def.examine == null) {
                        def.examine = "It's a(n) " + def.name;
                    }

                    if (def.shopPrice != -1) {
                        def.highAlch = (int) Math.round(def.shopPrice * 0.6D);
                        def.lowAlch = (int) Math.round(def.shopPrice * 0.4D);
                    }

                    for (String option : def.options) {
                        if (option != null && option.equals("Destroy")) {
                            def.remove = DiscardType.DESTROY;
                        }
                    }

                    if (cache) {
                        cachedDefinitions.put(id, def);
                    }
                }
            }
            return def;
        }
    }

    public static void loadEquipmentIds() {
        try {
            try {
                equipmentIds = Static.xml.readObject(Static.parseString("%WORK_DIR%/world/itemData/equipids.xml"));
            } catch (IOException e) {
                equipmentIds = new HashMap<Integer, Integer>();

                int maxId = Static.rs2Cache.getIndex(19).getLength() * 256;
                int equipId = 0;

                for (int i = 0; i < maxId; i++) {
                    ItemDefinition def = forId(i, false);

                    if (def != null && (def.maleWieldModel >= 0 || def.femaleWieldModel >= 0)) {
                        equipmentIds.put(i, equipId++);
                    }
                }

                Static.xml.writeObject(equipmentIds, Static.parseString("%WORK_DIR%/world/itemData/equipids.xml"));
            }

            logger.info("Loaded " + equipmentIds.size() + " equipment ids");
        } catch (Exception e) {
            logger.error("Failed to load equipment ids!", e);
        }
    }

    public static int getEquipmentId(int id) {
        return equipmentIds.get(id);
    }

    public static enum DiscardType {
        DESTROY, DROP;
    }

    /**
     * Cache acquired data.
     */
    public int id;
    public String name;
    public String[] options = new String[5];
    public int modelId;
    public int certID;
    public int certTemplateID;
    public int maleWieldModel;
    public int femaleWieldModel;
    public int shopPrice = -1;
    public int renderId = 1426;
    public int[] stackAmounts;
    public int[] stackTypes;
    public Map<Integer, Integer> levelRequirements;
    public Map<Integer, Integer> itemRequirements;
    public int weaponGroupId = 0;
    public boolean weaponSpecial = false;

    /**
     * XML acquired data.
     */
    public String examine = null;
    public double weight = 0;

    /**
     * Dynamically acquired data.
     */
    public int highAlch = -1;
    public int lowAlch = -1;
    public DiscardType remove = DiscardType.DROP;
    public EquipmentDefinition equipmentDefinition;

    /**
     * Un-refactored cache acquired data.
     */
    public boolean aBoolean1463;
    public boolean aBoolean1502;
    public byte[] aByteArray1501;
    public int anInt1423;
    public int anInt1425;
    public int anInt1426;
    public int lendedParentId;
    public int lendedParentId2;
    public int anInt1433;
    public int anInt1435;
    public int anInt1436;
    public int anInt1437;
    public int anInt1438;
    public int anInt1439 = 0;
    public int anInt1440;
    public int anInt1442;
    public int anInt1443 = 0;
    public int anInt1444;
    public int anInt1446;
    public int anInt1449;
    public int anInt1450;
    public int anInt1454;
    public int anInt1455;
    public int anInt1456;
    public int anInt1458;
    public int anInt1462;
    public int anInt1464;
    public int anInt1465;
    public int anInt1466;
    public int anInt1468;
    public int anInt1470;
    public int anInt1476;
    public int anInt1477;
    public int anInt1479;
    public int anInt1480;
    public int anInt1483;
    public int anInt1490;
    public int anInt1491;
    public int anInt1493;
    public int anInt1494;
    public int anInt1498;
    public int anInt1503;
    public int[] anIntArray1441;
    public short[] aShortArray1457;
    public short[] aShortArray1488;
    public short[] aShortArray1492;
    public short[] aShortArray1504;
    public String[] aStringArray1485 = new String[5];

    public ItemDefinition() {
        this.anInt1437 = 0;
        this.anInt1425 = 0;
        this.anInt1433 = -1;
        this.anInt1436 = 2000;
        this.anInt1458 = 0;
        this.anInt1444 = 0;
        this.anInt1455 = -1;
        this.aBoolean1463 = false;
        this.lendedParentId = -1;
        this.anInt1468 = -1;
        this.anInt1449 = -1;
        this.certID = -1;
        this.anInt1426 = -1;
        this.anInt1479 = -1;
        this.anInt1446 = 0;
        this.anInt1470 = 0;
        this.anInt1442 = -1;
        this.anInt1440 = -1;
        this.anInt1466 = -1;
        this.anInt1435 = -1;
        this.maleWieldModel = -1;
        this.anInt1438 = -1;
        this.anInt1450 = -1;
        this.anInt1454 = -1;
        this.lendedParentId2 = -1;
        this.anInt1465 = 0;
        this.anInt1462 = 0;
        this.anInt1477 = 0;
        this.anInt1423 = 128;
        this.anInt1483 = -1;
        this.femaleWieldModel = -1;
        this.name = "null";
        this.anInt1476 = -1;
        this.anInt1490 = -1;
        this.anInt1480 = 128;
        this.anInt1491 = 0;
        this.aBoolean1502 = false;
        this.anInt1498 = 0;
        this.anInt1503 = 128;
        this.certTemplateID = -1;
        this.shopPrice = 1;
        this.anInt1456 = 0;
        this.anInt1493 = 0;
        this.anInt1494 = 0;
    }

    public void copyDefinition(ItemDefinition def1, ItemDefinition def2) {
        shopPrice = 0;

        name = def1.name;
        maleWieldModel = def1.maleWieldModel;
        femaleWieldModel = def1.femaleWieldModel;

        renderId = def1.renderId;
        levelRequirements = def1.levelRequirements;

        options = new String[5];
        if (def1.options != null) {
            for (int i = 0; i < 4; i++)
                options[i] = def1.options[i];
        }

        aShortArray1457 = def1.aShortArray1457;
        aShortArray1492 = def1.aShortArray1492;
        anInt1462 = def1.anInt1462;
        anInt1490 = def1.anInt1490;
        anInt1491 = def2.anInt1491;
        anInt1435 = def1.anInt1435;
        aBoolean1502 = def1.aBoolean1502;
        anInt1454 = def1.anInt1454;
        aShortArray1504 = def1.aShortArray1504;
        aStringArray1485 = def1.aStringArray1485;
        aByteArray1501 = def1.aByteArray1501;
        anInt1470 = def1.anInt1470;
        anInt1450 = def1.anInt1450;
        anInt1498 = def1.anInt1498;
        anInt1438 = def1.anInt1438;
        anInt1479 = def1.anInt1479;
        anInt1465 = def1.anInt1465;
        aShortArray1488 = def1.aShortArray1488;
        anInt1493 = def1.anInt1493;
        anInt1466 = def1.anInt1466;
        anInt1446 = def1.anInt1446;
        anInt1449 = def1.anInt1449;
        anInt1437 = def1.anInt1437;

        modelId = def2.modelId;
        anInt1494 = def2.anInt1494;
        anInt1477 = def2.anInt1477;
        anInt1436 = def2.anInt1436;
        anInt1444 = def2.anInt1444;
        anInt1425 = def2.anInt1425;
    }

    public boolean isNoted() {
        return certTemplateID == 799;
    }

    public boolean isStackable() {
        if (isNoted())
            return true;
        if (anInt1456 == 1)
        	return true;
        if (id == 9075 || id == 15243 || id >= 554 && id <= 566 || id >= 863 && id <= 869)
            return true;
        return false;
    }

    /**
     * @return the equipmentDefinition.
     */
    public EquipmentDefinition getEquipmentDefinition() {
        if (this.equipmentDefinition != null) {
            return this.equipmentDefinition;
        }
        this.equipmentDefinition = EquipmentDefinition.forId(id);
        return this.equipmentDefinition;
    }
}
