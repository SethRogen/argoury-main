package com.ziotic.content.shop;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandler;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.ButtonHandler;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class ShopManager implements ActionHandler, ButtonHandler {
    private static final Logger logger = Logging.log();

    private static Map<Integer, Shop> shops = new HashMap<Integer, Shop>();

    public static void openShop(Player player, int id) {
        Shop shop = shops.get(id);
        if (shop == null) {
            throw new RuntimeException("Invalid shop ID specified.");
        }
        shop.open(player);
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerButtonHandler(new int[]{620, 621}, this);
        Gson gson = new Gson();
        try (Reader reader = new FileReader(Static.parseString("%WORK_DIR%/world/shopData/shops.json"))) {
            Map<Integer, ShopDefinition> shopDefs = gson.fromJson(reader, new TypeToken<Map<Integer, ShopDefinition>>() {}.getType());
            for (Map.Entry<Integer, ShopDefinition> shopDefEntry : shopDefs.entrySet()) {
                Integer shopId = shopDefEntry.getKey();
                ShopDefinition shopDef = shopDefEntry.getValue();
                Shop shop = new Shop(shopId, shopDef.sampleStock, shopDef.stock, 39, shopDef.acceptedItems, shopDef.name);
                shops.put(shopId, shop);
                Static.engine.scheduleRecurringEvent(shop);
            }
            logger.info("Loaded " + shopDefs.size() + " shops.");
        }
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        switch (interfaceId) {
            case 621:
                switch (opcode) {
                    case 2: // Sell 1
                        player.getCurrentShop().sell(player, b2, 1);
                        break;
                    case 3: // Sell 5
                        player.getCurrentShop().sell(player, b2, 5);
                        break;
                    case 4: // Sell 10
                        player.getCurrentShop().sell(player, b2, 10);
                        break;
                    case 5: // Sell 50
                        player.getCurrentShop().sell(player, b2, 50);
                        break;
                }
                break;
            case 620:
                switch (b) {
                    case 26: // sample items
                        switch (opcode) {
                            case 1: // TODO - item info
                                player.getCurrentShop().info(player, b2 / 4, true);
                                break;
                            case 2: // Buy 1
                                player.getCurrentShop().buy(player, b2 / 4, 1, true);
                                break;
                            case 3: // Buy 5
                                player.getCurrentShop().buy(player, b2 / 4, 5, true);
                                break;
                            case 4: // Buy 10
                                player.getCurrentShop().buy(player, b2 / 4, 10, true);
                                break;
                            case 5: // Buy 50
                                player.getCurrentShop().buy(player, b2 / 4, 50, true);
                                break;
                            case 6: // Buy 500
                                player.getCurrentShop().buy(player, b2 / 4, 500, true);
                                break;
                            case 10:
                                player.getCurrentShop().examine(player, b2 / 4, true);
                                break;
                        }
                        break;
                    case 25: // main items
                        switch (opcode) {
                            case 1: // TODO - item info
                                player.getCurrentShop().info(player, b2 / 6, false);
                                break;
                            case 2: // Buy 1
                                player.getCurrentShop().buy(player, b2 / 6, 1, false);
                                break;
                            case 3: // Buy 5
                                player.getCurrentShop().buy(player, b2 / 6, 5, false);
                                break;
                            case 4: // Buy 10
                                player.getCurrentShop().buy(player, b2 / 6, 10, false);
                                break;
                            case 5: // Buy 50
                                player.getCurrentShop().buy(player, b2 / 6, 50, false);
                                break;
                            case 6: // Buy 500
                                player.getCurrentShop().buy(player, b2 / 6, 500, false);
                                break;
                            case 10:
                                player.getCurrentShop().examine(player, b2 / 6, false);
                                break;
                        }
                        break;
                }
                break;
        }
    }
}
