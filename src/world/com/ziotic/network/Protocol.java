package com.ziotic.network;

import com.ziotic.engine.login.LoginResponse;
import com.ziotic.logic.Entity;
import com.ziotic.logic.item.GroundItem;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.mask.Chat;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.utility.GameInterface;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public interface Protocol {

    public Protocol sendAnimateObject(Player player, GameObject obj, int animation);

    public Protocol sendLobbyResponse(IoSession session, Player player, LoginResponse resp);

    public Protocol sendLoginResponse(IoSession session, Player player, LoginResponse resp);

    public Protocol sendWorldList(IoSession session);

    public Protocol sendMapRegion(Player player);

    public Protocol sendOnLogin(Player player);

    public Protocol sendWindow(Player player, int window);

    public Protocol sendFixedScreen(Player player);

    public Protocol sendResizableScreen(Player player);

    public Protocol switchToFixedScreen(Player player);

    public Protocol switchToResizableScreen(Player player);

    public Protocol sendInterface(Player player, int id, int window, int location, boolean walkable);

    public Protocol sendInterface(Player player, GameInterface gameInterface);

    public Protocol sendMessage(Player player, String message);

    public Protocol sendMessage(Player player, String message, String messageExtension, int req);

    public Protocol sendMessage(Player player, String message, String messageExtension, String messageExtension2, int req);

    public Protocol sendCloseInterface(Player player);

    public Protocol sendCloseInterface(Player player, GameInterface gameInterface);

    public Protocol sendCloseInterface(Player player, int window, int location);

    public Protocol sendAccessMask(Player player, int range1, int range2, int interfaceId1, int childId1, int interfaceId2, int childId2);

    public Protocol sendInterfaceScript(Player player, Object... args);

    public Protocol sendPublicChat(Player player, Player p2, Chat chat);

    public Protocol sendItems(Player player, int position, boolean posBoolean, PossesedItem[] items);

    public Protocol sendLocation(Player player, Tile location);

    public Protocol sendCreateGameObject(Player player, GameObject obj);

    public Protocol sendDestroyGameObject(Player player, GameObject obj);

    public Protocol sendCreateGroundItem(Player player, GroundItem item);

    public Protocol sendDestroyGroundItem(Player player, GroundItem item);

    public Protocol sendLevel(Player player, int skillId);

    public Protocol sendConfig(Player player, int id, int val);

    public Protocol sendRunEnergy(Player player);

    public Protocol sendExitToLogin(Player player);

    public Protocol sendExitToLobby(Player player);

    public Protocol sendPlayerOption(Player player, String option, int index, boolean onTop);

    public Protocol sendFriends(Player player, String... friends);

    public Protocol sendIncommingPM(Player player, String sender, int rights, String message, long id);

    public Protocol sendOutgoingPM(Player player, String recipient, String message);

    public Protocol sendPrivateChatSetting(Player player, int setting);

    public Protocol sendIgnores(Player player);

    public Protocol sendClan(Player player);

    public Protocol requestAmountInput(Player player);

    public Protocol requestTextInput(Player player, String request);

    public Protocol sendString(Player player, int interfaceId, int childId, String string);

    public Protocol sendSpecialString(Player player, int id, String string);

    public Protocol sendClanChatMessage(Player player, String clan, String sender, int rights, String message, long id);

    public Protocol sendInterfaceVariable(Player player, int id, int val);

    public Protocol sendNPCHead(Player player, int interfaceId, int childId, int npcId);

    public Protocol sendPlayerHead(Player player, int interfaceId, int childId);

    public Protocol sendInterfaceAnimation(Player player, int interfaceId, int childId, int animId);

    public Protocol sendChatboxInterface(Player player, int interfaceId);

    public Protocol sendCloseChatboxInterface(Player player);

    public Protocol sendInterfaceConfig(Player player, int interfaceId, int value);

    public Protocol sendPing(Player player);

    /**
     * This creates a projectile packet for a specified player.
     *
     * @param player              The player to create this packet for.
     * @param receiver            The entity target the projectile should follow if any.
     * @param projectileId        The id of the projectile to send.
     * @param start               The starting tile.
     * @param end                 The ending tile.
     * @param startHeight         The starting height of the projectile.
     * @param endHeight           The ending height of the projectile.
     * @param slowness            The slowness the projectile travels with.
     * @param delay               The delay before the projectile is being sent.
     * @param curve               The vertical curve the projectile will follow.
     * @param startDistanceOffset The distance outside the casting entity that the projectile should start at. 0 is just in front of the casting entity.
     * @param creatorSize         The size of the casting entity.
     * @return The projectile packet with the specified parameters.
     * @author Maxime Meire (Ziotic development)
     */
    public Protocol sendProjectile(Player player, Entity receiver, int projectileId, Tile start, Tile end,
                                   int startHeight, int endHeight, int slowness, int delay, int curve, int startDistanceOffset, int creatorSize);

    /**
     * This is to be used by the link server ONLY!
     *
     * @return The raw world list data.
     */
    public Frame generateWorldList();

    public Protocol sendInterfaceShowConfig(Player player, int interfaceId, int childId,
                                            boolean hidden);

}
