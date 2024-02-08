package com.runescape.content.handler;

import com.runescape.Static;
import com.runescape.logic.item.GroundItem;
import com.runescape.logic.item.Item;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.map.Tile;
import com.runescape.logic.map.event.TileEventListener;
import com.runescape.logic.npc.NPC;
import com.runescape.logic.object.GameObject;
import com.runescape.logic.player.Player;
import com.runescape.utility.Logging;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Lazaro
 */
public class ActionHandlerSystem {
    private static final Logger logger = Logging.log();

    private Map<Integer, ActionHandler[]> npcOptionHandlers = new HashMap<Integer, ActionHandler[]>();
    private Map<Integer, ActionHandler[]> objectOptionHandlers = new HashMap<Integer, ActionHandler[]>();
    private Map<Integer, ActionHandler[]> itemOptionHandlers = new HashMap<Integer, ActionHandler[]>();
    private Map<Integer, ActionHandler[]> itemOnItemHandlers = new HashMap<Integer, ActionHandler[]>();
    private Map<Integer, ActionHandler[]> itemOnObjectHandlers = new HashMap<Integer, ActionHandler[]>();
    private Map<Integer, ActionHandler[]> itemSwitchHandlers = new HashMap<Integer, ActionHandler[]>();
    private Map<Integer, ActionHandler[]> buttonHandlers = new HashMap<Integer, ActionHandler[]>();
    private Map<Integer, ActionHandler[]> groundItemOptionHandlers = new HashMap<Integer, ActionHandler[]>();
    private List<ActionHandler> walkHandlers = new LinkedList<ActionHandler>();
    private List<ActionHandler> logoutHandlers = new LinkedList<ActionHandler>();
    private List<ResetListener> resetListeners = new LinkedList<ResetListener>();


    public void load() {
        try {
            int handlerCount = 0;

            BufferedReader cfg = new BufferedReader(new FileReader(Static.parseString("%WORK_DIR%/actionhandlers.ini")));
            int lineNumber = 0;
            String line;
            while ((line = cfg.readLine()) != null) {
                if (line.startsWith("#") || line.trim().length() == 0) {
                    continue;
                }

                try {
                    Class<?> clazz = Class.forName(line.trim());

                    ActionHandler handler = (ActionHandler) clazz.newInstance();
                    handler.load(this);

                    handlerCount++;
                } catch (Exception e) {
                    logger.warn("Failed to load action handler (line #" + lineNumber + ") : " + line, e);
                }

                lineNumber++;
            }

            logger.info("Loaded " + handlerCount + " action handler(s)");
        } catch (Exception e) {
            logger.error("Error loading action handlers!", e);
        }
    }

    private <K> void appendHandler(Map<K, ActionHandler[]> map, K key, ActionHandler handler) {
        ActionHandler[] handlers = map.get(key);
        if (handlers != null) {
            ActionHandler[] handlers2 = new ActionHandler[handlers.length + 1];
            System.arraycopy(handlers, 0, handlers2, 0, handlers.length);
            handlers2[handlers2.length - 1] = handler;
            map.put(key, handlers2);
        } else {
            handlers = new ActionHandler[1];
            handlers[0] = handler;
            map.put(key, handlers);
        }
    }

    public void registerNPCOptionHandler(Integer[] npcIds, NPCOptionHandler handler) {
        for (int id : npcIds) {
            appendHandler(npcOptionHandlers, id, handler);
        }
    }

    public void registerObjectOptionHandler(int[] objectIds, ObjectOptionHandler handler) {
        for (int id : objectIds) {
            appendHandler(objectOptionHandlers, id, handler);
        }
    }

    public void registerLogoutHandler(int[] objectIds, ObjectOptionHandler handler) {
        for (int id : objectIds) {
            appendHandler(objectOptionHandlers, id, handler);
        }
    }

    public void registerItemOptionHandler(int[] itemIds, ItemOptionHandler handler) {
        for (int id : itemIds) {
            appendHandler(itemOptionHandlers, id, handler);
        }
    }

    public void registerItemOnItemHandler(int[] itemIds, ItemOnItemHandler handler) {
        for (int id : itemIds) {
            appendHandler(itemOnItemHandlers, id, handler);
        }
    }

    public void registerItemOnObjectHandler(int[][] relativeIds, ItemOnObjectHandler handler) {
        for (int[] id : relativeIds) {
            // id[0] = item id
            // id[1] = obj id

            appendHandler(itemOnObjectHandlers, id[0] << 16 | id[1], handler);
        }
    }

    public void registerItemSwitchHandler(int[] interfaceIds, ItemSwitchHandler handler) {
        for (int id : interfaceIds) {
            appendHandler(itemSwitchHandlers, id, handler);
        }
    }

    public void registerButtonHandler(int[] interfaceIds, ButtonHandler handler) {
        for (int id : interfaceIds) {
            appendHandler(buttonHandlers, id, handler);
        }
    }

    public void registerWalkingListener(ResetListener listener) { //need to talk to laz about this
        resetListeners.add(listener);
    }

    public void registerMotionListener(WalkHandler handler) {
        walkHandlers.add(handler);
    }

    public void registerLogoutHandler(LogoutHandler handler) {
        logoutHandlers.add(handler);
    }

    public void registerGroundItemOptionHandler(int[] itemIds, GroundItemOptionHandler handler) {
        for (int id : itemIds) {
            appendHandler(groundItemOptionHandlers, id, handler);
        }
    }

    public void registerTileEventListener(int[] coordinates, TileEventListener listener) {
        Tile.locate(coordinates).registerEventListener(listener);
    }

    public boolean handleNPCOption(Player player, NPC npc, int type) {
        ActionHandler[] handlers = npcOptionHandlers.get(npc.getId());
        if (handlers != null) {
            for (ActionHandler handler : handlers) {
                if (handler.explicitlyForMembers() && !player.isMember()) {
                    player.onMembersOnlyFeature();
                    continue;
                }
                switch (type) {
                    case 1:
                        ((NPCOptionHandler) handler).handleNPCOption1(player, npc);
                        break;
                    case 2:
                        ((NPCOptionHandler) handler).handleNPCOption2(player, npc);
                        break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean handleObjectOption(Player player, GameObject obj, int type) {
        ActionHandler[] handlers = objectOptionHandlers.get(obj.getId());
        if (handlers != null) {
            for (ActionHandler handler : handlers) {
                if (handler.explicitlyForMembers() && !player.isMember() && handlers.length == 1) {
                    player.onMembersOnlyFeature();
                    continue;
                }

                if (type == 1) {
                    ((ObjectOptionHandler) handler).handleObjectOption1(player, obj);
                } else if (type == 2) {
                    ((ObjectOptionHandler) handler).handleObjectOption2(player, obj);
                } else if (type == 3) {
                    ((ObjectOptionHandler) handler).handleObjectOption3(player, obj);
                }
            }
            return true;
        }
        return false;
    }

    public boolean handleItemOption(Player player, PossesedItem item, int index, int type) {
        ActionHandler[] handlers = itemOptionHandlers.get(item.getId());
        System.out.println(item.getId());
        if (handlers != null) {
            for (ActionHandler handler : handlers) {
                if (handler.explicitlyForMembers() && !player.isMember() && handlers.length == 1) {
                    player.onMembersOnlyFeature();
                    continue;
                }
                if (type == 1) {
                    ((ItemOptionHandler) handler).handleItemOption1(player, item, index);
                } else if (type == 2) {
                    ((ItemOptionHandler) handler).handleItemOption2(player, item, index);
                } else if (type == 3) {
                    ((ItemOptionHandler) handler).handleItemOption3(player, item, index);
                }
            }
            return true;
        }
        return false;
    }

    public boolean handleItemOnItem(Player player, Item item1, int index1, Item item2, int index2) {
        ActionHandler[] handlers = itemOnItemHandlers.get(item1.getId());
        if (handlers != null) {
            for (ActionHandler handler : handlers) {
                if (handler.explicitlyForMembers() && !player.isMember() && handlers.length == 1) {
                    player.onMembersOnlyFeature();
                    continue;
                }

                ((ItemOnItemHandler) handler).handleItemOnItem(player, item1, index1, item2, index2);
            }
            return true;
        }
        return false;
    }

    public boolean handleItemOnObject(Player player, PossesedItem item, int itemIndex, GameObject obj) {
        ActionHandler[] handlers = itemOnObjectHandlers.get(item.getId() << 16 | obj.getId());
        if (handlers != null) {
            for (ActionHandler handler : handlers) {
                if (handler.explicitlyForMembers() && !player.isMember() && handlers.length == 1) {
                    player.onMembersOnlyFeature();
                    continue;
                }

                ((ItemOnObjectHandler) handler).handleItemOnObject(player, item, itemIndex, obj);
            }
            return true;
        }
        return false;
    }

    public boolean handleItemSwitch(Player player, int interfaceId1, int childId1, int interfaceId2, int childId2, int id1, int id2, int indexFrom, int indexTo) {
        ActionHandler[] handlers = itemSwitchHandlers.get(interfaceId1);
        if (handlers != null) {
            for (ActionHandler handler : handlers) {
                if (handler.explicitlyForMembers() && !player.isMember() && handlers.length == 1) {
                    player.onMembersOnlyFeature();
                    continue;
                }

                ((ItemSwitchHandler) handler).handleItemSwitch(player, interfaceId1, childId1, interfaceId2, childId2, id1, id2, indexFrom, indexTo);
            }
            return true;
        }
        return false;
    }

    public boolean handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        ActionHandler[] handlers = buttonHandlers.get(interfaceId);
        if (handlers != null) {
            for (ActionHandler handler : handlers) {
                if (handler.explicitlyForMembers() && !player.isMember() && handlers.length == 1) {
                    player.onMembersOnlyFeature();
                    continue;
                }

                ((ButtonHandler) handler).handleButton(player, opcode, interfaceId, b, b2, b3);
            }
            return true;
        }
        return false;
    }

    public void onReset(Player player) {
        for (ResetListener listener : resetListeners) {
            listener.onReset(player);
        }
    }

    public boolean handleGroundItemOption(Player player, GroundItem item) {
        ActionHandler[] handlers = groundItemOptionHandlers.get(item.getId());
        if (handlers != null) {
            for (ActionHandler handler : handlers) {
                if (handler.explicitlyForMembers() && !player.isMember() && handlers.length == 1) {
                    player.onMembersOnlyFeature();
                    continue;
                }

                ((GroundItemOptionHandler) handler).handleGroundItemOption(player, item);
            }
            return true;
        }
        return false;
    }

    public void handleWalk(Player player) {
        for (ActionHandler handlers : walkHandlers)
            ((WalkHandler) handlers).onWalk(player);
    }

    public void handleDisconnect(Player player, boolean lobby) {
        for (ActionHandler handlers : logoutHandlers)
            ((LogoutHandler) handlers).onLogout(player, lobby);
    }
}
