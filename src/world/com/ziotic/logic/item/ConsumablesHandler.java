package com.ziotic.logic.item;

import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.ButtonHandler;
import com.ziotic.content.handler.ItemOnItemHandler;
import com.ziotic.logic.HPHandlerTick.HPAddition;
import com.ziotic.logic.item.ItemListener.ItemEventType;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ConsumablesHandler implements ItemOnItemHandler, ButtonHandler {

    /**
     * Logger instance.
     */
    private static final Logger logger = Logging.log();

    private static Map<String, Potion> potions = null;
    private static Map<String, EatableFood> foods = null;

    private Consumable lastConsumable = new Consumable(0, false, new Consumable(0, false, null));

    private class Consumable {
        protected long time;
        protected boolean food;
        protected Consumable prev;

        public Consumable(long time, boolean food, Consumable prev) {
            this.time = time;
            this.food = food;
            this.prev = prev;
        }

        private boolean isFoodPotionCombo() {
        	if (prev == null)
        		return false;
            return (prev.food && prev.time + 800 > System.currentTimeMillis());
        }

        protected boolean canEat() {
        	if (prev == null)
        		return true;
            if (prev.food)
                return prev.time + 1800 < System.currentTimeMillis();
            else
                return prev.time + 1200 < System.currentTimeMillis();
        }

        protected boolean canDrink() {
        	if (prev == null)
        		return true;
            if (isFoodPotionCombo())
                return true;
            else if (prev.food)
                return prev.time + 1800 < System.currentTimeMillis();
            else
                return prev.time + 1200 < System.currentTimeMillis();
        }
    }

    public ConsumablesHandler() {
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        load();
        system.registerItemOnItemHandler(getItemIds(), this);
        system.registerButtonHandler(new int[]{149}, this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleItemOnItem(Player player, Item item1, int index1, Item item2, int index2) {
        handlePotionTransferring(player, item1, index1, item2, index2);
    }

    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        switch (opcode) {
            case 1:
                PossesedItem item = player.getInventory().array()[b2];
                if (item != null) {
                    if (item.getId() == b3) {
                        boolean succesful = handleComsumables(player, b2, b3);
                        if (succesful) {
                            player.getCombat().stop(false);
                            if (ConsumablesHandler.getFoodForId(b3) != null) {
                                player.getConsumablesHandler().lastConsumable.prev = player.getConsumablesHandler().lastConsumable;
                                player.getConsumablesHandler().lastConsumable.time = System.currentTimeMillis();
                                player.getConsumablesHandler().lastConsumable.food = true;
                                player.getCombat().addNextAttackTime(3);
                                player.getCombat().addNextSpellTime(3);
                            } else {
//                            	boolean bool = false;
//                            	Consumable c = player.getConsumablesHandler().lastConsumable;
//                            	if (c != null) {
//                            		Consumable c2 = player.getConsumablesHandler().lastConsumable.prev;
//                            		if (c2 != null) {
//                            			bool = player.getConsumablesHandler().lastConsumable.prev.isFoodPotionCombo();
//                            		}
//                            	}
                                player.getConsumablesHandler().lastConsumable.prev = player.getConsumablesHandler().lastConsumable;
                                player.getConsumablesHandler().lastConsumable.time = System.currentTimeMillis();
                                player.getConsumablesHandler().lastConsumable.food = false;
//                                if (!bool) {
//	                                player.getCombat().addNextAttackTime(2);
//	                                player.getCombat().addNextSpellTime(2);
//                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public static void load() throws IOException {
        if (potions != null) {
            throw new IllegalStateException("Potion definitions already loaded.");
        }
        if (foods != null) {
            throw new IllegalStateException("Food definitions already loaded.");
        }
        try {
            /**
             * Load potion definitions.
             */
            potions = (Map<String, Potion>) Static.xml.readObject(Static.parseString("%WORK_DIR%/world/itemData/consumables/potiondefs.xml"));
            foods = (Map<String, EatableFood>) Static.xml.readObject(Static.parseString("%WORK_DIR%/world/itemData/consumables/food.xml"));
            if (potions != null && foods != null) {
                logger.info("Loaded " + (potions.size() - 1) + " potion(s)");
                logger.info("Loaded " + foods.size() + " food(s)");
            } else {
                logger.error("Potion definitions not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean handleComsumables(Player player, int index, int itemId) {
        Potion potion = getPotionForId(itemId);
        boolean succesful = false;
        if (potion != null && !potion.name.equalsIgnoreCase("vial") && player.getHP() != 0) {
            if (player.getConsumablesHandler().lastConsumable.canDrink()) {
                int i = 0;
                for (int id : potion.affectedLevelIds) {
                    int currentLevel;
                    if (id == Levels.PRAYER)
                        currentLevel = (int) player.getLevels().getCurrentPrayer();
                    else
                        currentLevel = player.getLevels().getCurrentLevel(id);
                    int level = player.getLevels().getLevel(id);
                    int baseBoost = potion.baseBoosts.get(i);
                    double multiplierBoost = (level * potion.multiplyFactors.get(i));
                    int addition = (int) Math.floor(baseBoost + multiplierBoost);
                    int newCurrentLevel = currentLevel + addition;
                    if (newCurrentLevel < 1)
                        newCurrentLevel = 1;
                    if ((multiplierBoost < 0 ? -1 : 1) * currentLevel > (multiplierBoost < 0 ? -1 : 1) * newCurrentLevel) {
                        i++;
                        continue;
                    }
                    if (id == Levels.PRAYER || potion.name.contains("Restore") || potion.name.contains("restore")) {
                        if (newCurrentLevel > level) {
                            if (currentLevel > level)
                                newCurrentLevel = currentLevel;
                            else
                                newCurrentLevel = level;
                        }
                    } else {
                        if (newCurrentLevel > level + addition)
                            newCurrentLevel = level + addition;
                        if (newCurrentLevel < 0)
                            newCurrentLevel = 0;
                    }
                    if (id == Levels.PRAYER)
                        player.getLevels().setCurrentPrayer(newCurrentLevel);
                    else
                        player.getLevels().setCurrentLevel(id, newCurrentLevel);
                    i++;
                }
                if (potion.hpMultiplier > 0) {
                    float addition = player.getMaxHP() * potion.hpMultiplier;
                    player.registerHPTick(new HPAddition(player, (int) addition, 0, (int) addition, null));
                }
                int newDose = potion.getDoseForItemId(itemId) - 1;
                int newItemId = potion.getItemIdForDose(newDose);
                player.getInventory().array()[index] = new PossesedItem(newItemId);
                Static.proto.sendMessage(player, "You drink some of your " + potion.name.toLowerCase() + " potion.");
                if (newDose < 1)
                    Static.proto.sendMessage(player, "You have finished your potion.");
                else
                    Static.proto.sendMessage(player, "You have " + newDose + (newDose == 1 ? " dose " : " doses ") + "of potion left.");
                if (player.getCombat().underAttack() || player.getCombat().inCombat()) {
                    player.getCombat().executeAnimation(829, 0, false, true);
                } else {
                    player.doAnimation(829, 0);
                }
                succesful = true;
            }
        } else if (player.getHP() != 0) {
            EatableFood food = getFoodForId(itemId);
            if (food != null) {
                if (player.getConsumablesHandler().lastConsumable.canEat()) {
                    int i = 0;
                    if (food.affectedLevelIds != null) {
                        for (int id : food.affectedLevelIds) {
                            int currentLevel = (int) player.getLevels().getCurrentLevel(id);
                            int level = player.getLevels().getLevel(id);
                            int boost = food.baseBoosts.get(i);
                            int newCurrentLevel = currentLevel + boost;
                            if (currentLevel > newCurrentLevel)
                                continue;
                            if (newCurrentLevel > level + boost)
                                newCurrentLevel = level + boost;
                            player.getLevels().setCurrentLevel(id, newCurrentLevel);
                            i++;
                        }
                    }
                    if (food.constitutionBoost > 0) {
                        int currentConst = (int) player.getLevels().getCurrentLevel(3);
                        int constLevel = player.getLevels().getLevel(3);
                        int newConst = currentConst + food.constitutionBoost > constLevel + food.constitutionBoost ? constLevel + food.constitutionBoost : currentConst + food.constitutionBoost;
                        player.getLevels().setCurrentLevel(3, newConst);
                    }
                    player.registerHPTick(new HPAddition(player, food.restore, 0, 0, null));
                    int newIndex = food.getIndexForItemId(itemId) - 1;
                    int newItemId = food.getItemIdForIndex(newIndex);
                    if (newItemId >= 0) {
                        player.getInventory().array()[index] = new PossesedItem(newItemId);
                    } else {
                        player.getInventory().remove(new PossesedItem(itemId), index);
                    }
                    if (food.foodIds.size() > 1) {
                        int counter = food.getIndexForItemId(newItemId);
                        int denominator = food.foodIds.size();
                        if (food.container)
                            denominator--;
                        if (counter >= (food.container ? 1 : 0))
                            Static.proto.sendMessage(player, "You eat 1/" + denominator + " of the " + food.name.toLowerCase() + ".");
                        if (newIndex == (food.container ? 0 : -1))
                            Static.proto.sendMessage(player, "You have finished your " + food.name.toLowerCase() + ".");
                    } else {
                        Static.proto.sendMessage(player, "You eat the " + food.name.toLowerCase() + ".");
                    }
                    if (player.getCombat().underAttack() || player.getCombat().inCombat()) {
                        player.getCombat().executeAnimation(829, 0, false, true);
                    } else {
                        player.doAnimation(829, 0);
                    }
                    succesful = true;
                }
            }
        }
        player.getLevels().refresh();
        InventoryListener.INSTANCE.event(player.getInventory(), ItemEventType.CHANGE, 0);
        return succesful;
    }

    private static int[] getItemIds() {
        ArrayList<Integer> itemIds = new ArrayList<Integer>(potions.size() * 5 + foods.size());
        Collection<Potion> pots = potions.values();
        for (Potion p : pots) {
            if (p.name.equalsIgnoreCase("vial"))
                continue;
            for (int i = 1; i < p.doseIds.length; i++)
                itemIds.add(p.doseIds[i]);
        }
        itemIds.add(229);
        Collection<EatableFood> foods_ = foods.values();
        for (EatableFood f : foods_) {
            for (int i : f.foodIds) {
                itemIds.add(i);
            }
        }
        int[] ids = new int[itemIds.size()];
        for (int i = 0; i < itemIds.size(); i++) {
            ids[i] = itemIds.get(i);
        }
        return ids;
    }

    public static Potion getPotionForId(int itemId) {
        PossesedItem item = new PossesedItem(itemId);
        String key = item.getDefinition().name;
        int index = key.indexOf("(") - 1;
        if (index < 0)
            return potions.get("Vial");
        byte[] oldString = key.getBytes();
        byte[] newString = new byte[index];
        for (int i = 0; i < index; i++) {
            newString[i] = oldString[i];
        }
        key = new String(newString);
        if (potions.containsKey(key)) {
            return potions.get(key);
        }
        return null;
    }

    public static EatableFood getFoodForId(int itemId) {
        PossesedItem item = new PossesedItem(itemId);
        String key = item.getDefinition().name;
        int offset = 0;
        if (key.contains("1/") || key.contains("2/"))
            offset = 4;
        else if (key.contains("Half an"))
            offset = 8;
        else if (key.contains("Half a"))
            offset = 7;
        else if (key.contains("Slice of"))
            offset = 9;
        if (offset > 0) {
            byte[] oldString = key.getBytes();
            byte[] newString = new byte[oldString.length - offset];
            for (int i = offset; i < oldString.length; i++) {
                newString[i - offset] = oldString[i];
            }
            key = new String(newString);
            key = key.substring(0, 1).toUpperCase() + key.substring(1);
        }
        if (foods.containsKey(key)) {
            return foods.get(key);
        }
        return null;
    }

    public static void handlePotionTransferring(Player player, Item item1, int index1, Item item2, int index2) {
        Potion potion1 = getPotionForId(item1.getId());
        Potion potion2 = getPotionForId(item2.getId());
        if (index1 != index2) {
            if (potion1.name.equalsIgnoreCase("vial") && potion2.name.equalsIgnoreCase("vial")) {
                Static.proto.sendMessage(player, "Nothing interesting happens.");
                return;
            } else if (potion1.name.equalsIgnoreCase("vial")) {
                if (potion2.getDoseForItemId(item2.getId()) % 2 == 0) {
                    int newDose = potion2.getDoseForItemId(item2.getId()) / 2;
                    int newId = potion2.getItemIdForDose(newDose);
                    player.getInventory().array()[index1] = new PossesedItem(newId);
                    player.getInventory().array()[index2] = new PossesedItem(newId);
                } else {
                    Static.proto.sendMessage(player, "Nothing interesting happens.");
                }
            } else if (potion2.name.equalsIgnoreCase("vial")) {
                if (potion1.getDoseForItemId(item1.getId()) % 2 == 0) {
                    int newDose = potion1.getDoseForItemId(item1.getId()) / 2;
                    int newId = potion1.getItemIdForDose(newDose);
                    player.getInventory().array()[index1] = new PossesedItem(newId);
                    player.getInventory().array()[index2] = new PossesedItem(newId);
                } else {
                    Static.proto.sendMessage(player, "Nothing interesting happens.");
                }
            } else if (potion1 == potion2) {
                int dose1 = potion1.getDoseForItemId(item1.getId());
                int dose2 = potion2.getDoseForItemId(item2.getId());
                if (dose1 == 4 && dose2 == 4)
                    Static.proto.sendMessage(player, "Nothing interesting happens.");
                else {
                    int newDose1 = dose1 + dose2 - 4 < 0 ? 0 : dose1 + dose2 - 4;
                    int newDose2 = dose1 + dose2 > 4 ? 4 : dose1 + dose2;
                    player.getInventory().array()[index1] = new PossesedItem(potion1.getItemIdForDose(newDose1));
                    player.getInventory().array()[index2] = new PossesedItem(potion1.getItemIdForDose(newDose2));
                }
            }
            InventoryListener.INSTANCE.event(player.getInventory(), ItemEventType.CHANGE, 0);
        } else
            Static.proto.sendMessage(player, "Nothing interesting happens.");
    }

    public class Potion {

        private int[] doseIds = new int[5];
        private String name = "";
        private ArrayList<Integer> affectedLevelIds = new ArrayList<Integer>();
        private ArrayList<Integer> baseBoosts = new ArrayList<Integer>();
        private ArrayList<Float> multiplyFactors = new ArrayList<Float>();
        private float hpMultiplier;

        private int getDoseForItemId(int itemId) {
            for (int i = 0; i < doseIds.length; i++)
                if (doseIds[i] == itemId)
                    return i;
            return -1;
        }

        private int getItemIdForDose(int dose) {
            return doseIds[dose];
        }
    }

    public class EatableFood {

        private String name = "";
        private ArrayList<Integer> foodIds = new ArrayList<Integer>();
        private int restore;
        private int constitutionBoost;
        private ArrayList<Integer> affectedLevelIds = new ArrayList<Integer>();
        private ArrayList<Integer> baseBoosts = new ArrayList<Integer>();
        private boolean container;

        private int getIndexForItemId(int itemId) {
            int index = 0;
            for (int i : foodIds) {
                if (itemId == i)
                    return index;
                index++;
            }
            return -1;
        }

        private int getItemIdForIndex(int index) {
            if (foodIds.size() > index && index >= 0)
                return foodIds.get(index);
            return -1;
        }
    }
}
