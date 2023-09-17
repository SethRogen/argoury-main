package com.ziotic.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.ziotic.Static;
import com.ziotic.adapter.protocol.update.NPCUpdateAdapter;
import com.ziotic.adapter.protocol.update.PlayerUpdaterAdapter;
import com.ziotic.content.combat.Magic;
import com.ziotic.content.combat.Ranged;
import com.ziotic.content.magictemp.MagicTemp;
import com.ziotic.content.skill.summoning.SummoningPouch;
import com.ziotic.engine.login.LoginResponse;
import com.ziotic.engine.misc.LocalPlayerListSynchronizer;
import com.ziotic.engine.tick.Tick;
import com.ziotic.link.WCSInitiator;
import com.ziotic.link.WorldClientSession;
import com.ziotic.logic.item.EquipmentDefinition;
import com.ziotic.logic.item.GroundItem;
import com.ziotic.logic.item.GroundItemManager;
import com.ziotic.logic.item.ItemDefinition;
import com.ziotic.logic.item.ItemXMLDefinition;
import com.ziotic.logic.map.PathFinder;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.map.PathRequest;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.mask.Graphic;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.npc.NPCSpawn;
import com.ziotic.logic.npc.NPCXMLDefinition;
import com.ziotic.logic.npc.misc.FishingSpotNPC;
import com.ziotic.logic.object.DoorManager;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.object.ObjectManager;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.player.PlayerType;
import com.ziotic.logic.player.RemotePlayer;
import com.ziotic.logic.utility.EntityUpdater;
import com.ziotic.logic.utility.NodeCollection;
import com.ziotic.logic.utility.PlayerUpdater;
import com.ziotic.utility.Logging;
import com.ziotic.utility.Pool;

/**
 * @author Lazaro
 */
public final class World implements Runnable {
    private static Logger logger = Logging.log();
    private static final Random RANDOM = new Random();

    private int id;

    private Pool<WorldClientSession> wcsPool = null;

    private byte[] worldListData = null;

    private NodeCollection<Player> players = null;
    private NodeCollection<Player> lobbyPlayers = null;

    private NodeCollection<NPC> npcs = null;

    private Map<String, PlayerType> playerMap = new HashMap<String, PlayerType>();

    private CountDownLatch maskUpdateLatch = null;
    private CountDownLatch clientUpdateLatch = null;
    private CountDownLatch resetUpdateLatch = null;
    private PlayerUpdater playerUpdater = null;
    private EntityUpdater<NPC> npcUpdater = null;

    private GroundItemManager groundItemManager = null;

    private ObjectManager objectManager = null;

    private DoorManager doorManager = null;
    private HashMap<String, Tick> globalProcesses = new HashMap<String, Tick>();

    private int time = 0;

    public World(int id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public Pool<WorldClientSession> getWCSPool() {
        return wcsPool;
    }

    public LoginResponse register(Player player) {
        boolean success = false;
        if (player.inGame()) {
            if (!players.contains(player)) {
                success = players.add(player);
            }
        } else {
            if (!lobbyPlayers.contains(player)) {
                success = lobbyPlayers.add(player);
            }
        }
        if (success) {

            synchronized (playerMap) {
                playerMap.put(player.getProtocolName(), player);
            }

            logger.debug("Registered player [" + player + "]");

            notifyLoginState(player);

            return LoginResponse.LOGIN;
        }
        return LoginResponse.WORLD_FULL;
    }

    public void register(NPC npc) {
        npcs.add(npc);
    }

    public void unregister(final Player player) {
        if (player.getCombat() != null) {
            if (!player.getCombat().canLogout() && player.inGame()) {
                player.registerTick(new Tick("CombatLogoutTick1", 1) {
                    @Override
                    public boolean execute() {
                        if (player.getCombat().inCombat()) {
                            unregister(player);
                            return false;
                        }
                        return true;
                    }
                });
                return;
            }
        }

        if (!player.isDestroyed()) {
            player.destroy();

            logger.debug("Un-registered player [" + player + "]");
        }

        if (player.inGame()) {
            players.remove(player);
        } else {
            lobbyPlayers.remove(player);
        }

        synchronized (playerMap) {
            playerMap.remove(player.getProtocolName());
        }

        notifyLoginState(player);
    }

    public void unregister(NPC npc) {
        npc.destroy();
        npcs.remove(npc);
    }

    public void kick(String name) {
        Player p = (Player) findPlayer(name);
        if (p != null)
            Static.proto.sendExitToLogin(p);
    }

    public PlayerType findPlayer(String name) {
        synchronized (playerMap) {
            return playerMap.get(name);
        }
    }

    public void mutePlayer(final String moderator, final String name, final boolean mute) {
        Static.engine.dispatchToLinkWorker(new Runnable() {
            @Override
            public void run() {
                WorldClientSession session = null;
                try {
                    session = Static.world.getWCSPool().acquire();

                    session.sendPlayerMuteSettings(moderator, name, mute);
                } finally {
                    if (session != null) {
                        Static.world.getWCSPool().release(session);
                    }
                }
            }
        });
        synchronized (playerMap) {
            Player p = (Player) playerMap.get(name);
            if (p != null) {
                p.setMuted(mute);
            } else {
                System.out.println("player is nulled");
            }
        }
    }
    
    public void teleportPlayerToMe(Player player, String subject) {
    	Player p = (Player) playerMap.get(subject);
    	if (p != null) {
    		p.sendMessage("" + player.getName() + " teleports you to him.");
    		MagicTemp.teleport(p, player.getLocation().translate(0, -1, 0), 2, 1816, 8941, new Graphic(1577), null, 0, true);
    	} else {
    		player.sendMessage("That player is not online in this world.");
    	}
    }

    public GroundItemManager getGroundItemManager() {
        return groundItemManager;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public DoorManager getDoorManager() {
        return doorManager;
    }

	public void load() throws Throwable {
        wcsPool = new Pool<WorldClientSession>(WorldClientSession.class, new WCSInitiator(), Static.conf.getInt("link_channels"));

        logger.info("Pooled " + Static.conf.getInt("link_channels") + " link client(s)");
        if (Static.isGame()) {
            players = new NodeCollection<Player>(1, 2048);
            npcs = new NodeCollection<NPC>(1, 32768);

            groundItemManager = new GroundItemManager();
            objectManager = new ObjectManager();
            doorManager = new DoorManager();
            doorManager.load();
            playerUpdater = new PlayerUpdaterAdapter();
            npcUpdater = new NPCUpdateAdapter();

            ItemXMLDefinition.load();
            ItemDefinition.loadEquipmentIds();

            EquipmentDefinition.load();

            NPCXMLDefinition.load();
            loadNPCsAndCorrespondingRegions();

            Magic.load();
            Ranged.load();
            SummoningPouch.load();
        }
        if (Static.isLobby()) {
            lobbyPlayers = new NodeCollection<Player>(1, 2048);
        }
        Static.engine.submit(new LocalPlayerListSynchronizer());
    }

    private void loadNPCsAndCorrespondingRegions() {
        if (!npcs.isEmpty()) {
            for (NPC npc : npcs) {
                npc.destroy();
            }
            npcs.clear();

            logger.info("Destroyed all currently loaded NPCs!");
        }
        try {
            List<NPCSpawn> spawns = Static.xml.readObject(Static.parseString("%WORK_DIR%/world/npcData/npcspawns.xml"));
            for (NPCSpawn spawn : spawns) {
                register(new NPC(spawn));
            }
            FishingSpotNPC.load(); // loads fishing spots

            logger.info("Loaded " + spawns.size() + " NPC spawns");
        } catch (Exception e) {
            logger.error("Error loading NPC spawns!", e);
        }
    }

    public int getId() {
        return id;
    }

    public byte[] getWorldListData() {
        return worldListData;
    }

    public void setWorldListData(byte[] worldListData) {
        this.worldListData = worldListData;
    }

    public NodeCollection<Player> getPlayers() {
        return players;
    }

    public NodeCollection<NPC> getNPCs() {
        return npcs;
    }

    /* (non-Javadoc)
      * @see java.lang.Runnable#run()
      */

    @Override
    public void run() {
        try {
            synchronized (players) {
                Player[] playerArray = players.toArray(playerUpdater.getPlayers());
                if (playerArray != playerUpdater.getPlayers()) {
                    throw new RuntimeException("Updated player array doesn't equal the adapter's array!");
                }
                List<NPC> npcList = npcs.toList();

                maskUpdateLatch = new CountDownLatch(playerArray.length + npcList.size());
                clientUpdateLatch = new CountDownLatch(playerArray.length);
                resetUpdateLatch = new CountDownLatch(playerArray.length + npcList.size());
                /**
                 * Pre-update: Processes game logic.
                 */
                processGlobalProcesses();
                for (Player player : playerArray) {
                    if (player == null) continue;

                    if (player.isConnected() && !player.isOnLogin()) {
                        player.setUpdateStage(Entity.UpdateStage.PRE_UPDATE);
                        player.run();
                    }
                }
                for (NPC npc : npcList) {
                    npc.setUpdateStage(Entity.UpdateStage.PRE_UPDATE);
                    npc.run();
                }

                /**
                 * Mask update: Compiles every entity's mask before-hand so that
                 * no synchronization is needed later.
                 */
                for (Player player : playerArray) {
                    if (player == null) {
                        maskUpdateLatch.countDown();
                        continue;
                    }
                    if (player != null && player.isConnected() && !player.isOnLogin()) {
                        player.setUpdateStage(Entity.UpdateStage.MASK_UPDATE);
                        Static.engine.dispatchToWorldWorker(player);
                    } else {
                        maskUpdateLatch.countDown();
                    }
                }
                for (NPC npc : npcList) {
                    npc.setUpdateStage(Entity.UpdateStage.MASK_UPDATE);
                    Static.engine.dispatchToWorldWorker(npc);
                }
                try {
                    maskUpdateLatch.await();
                } catch (InterruptedException e) {
                    return;
                }
                /**
                 * Client update: Prepares and sends the update packets to the
                 * client.
                 */
                for (Player player : playerArray) {
                    if (player == null) {
                        clientUpdateLatch.countDown();
                        continue;
                    }

                    if (player.isConnected() && !player.isOnLogin()) {
                        player.setUpdateStage(Entity.UpdateStage.CLIENT_UPDATE);
                        Static.engine.dispatchToWorldWorker(player);
                    } else {
                        clientUpdateLatch.countDown();
                    }
                }
                try {
                    clientUpdateLatch.await();
                } catch (InterruptedException e) {
                    return;
                }
                /**
                 * Post-update: Resets all update cycle flags.
                 */
                for (Player player : playerArray) {
                    if (player == null) {
                        resetUpdateLatch.countDown();
                        continue;
                    }

                    if (player.isConnected() && !player.isOnLogin()) {
                        player.setUpdateStage(Entity.UpdateStage.POST_UPDATE);
                        Static.engine.dispatchToWorldWorker(player);
                    } else if (!player.isConnected() || (System.currentTimeMillis() - player.getLastPacketTime()) >= 15000) {
                        unregister(player);
                        resetUpdateLatch.countDown();
                    } else {
                        resetUpdateLatch.countDown();
                    }
                }
                for(PlayerType playerType : new ArrayList<PlayerType>(playerMap.values())) {
                    if(playerType instanceof Player) {
                        Player player = (Player) playerType;
                        if (!player.isConnected() || (System.currentTimeMillis() - player.getLastPacketTime()) >= 15000) {
                            unregister(player);
                        }
                    }
                }
                for (NPC npc : npcList) {
                    if (npc.isDestroyed()) {
                        unregister(npc);
                        resetUpdateLatch.countDown();
                    } else {
                        npc.setUpdateStage(Entity.UpdateStage.POST_UPDATE);
                        Static.engine.dispatchToWorldWorker(npc);
                    }
                }
                try {
                    resetUpdateLatch.await();
                } catch (InterruptedException e) {
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Error updating world!", e);
        }
        //logger.info("Registered NPCS: " + npcs.size());
        time++;
    }

    public CountDownLatch getMaskUpdateLatch() {
        return maskUpdateLatch;
    }

    public CountDownLatch getClientUpdateLatch() {
        return clientUpdateLatch;
    }

    public CountDownLatch getResetUpdateLatch() {
        return resetUpdateLatch;
    }

    public PlayerUpdater getPlayerUpdater() {
        return playerUpdater;
    }

    public EntityUpdater<NPC> getNPCUpdater() {
        return npcUpdater;
    }

    public final Player[] getLocalPlayers(Tile tile) {
        return getLocalPlayers(tile, 16);
    }

    public final Player[] getLocalPlayers(Tile tile, int depth) {
        Tile[] regionTiles = getRegionTiles(tile, depth);
        int i = 0;
        for (Tile t : regionTiles) {
            i += t.getPlayerCount();
        }
        Player[] players = new Player[i];
        i = 0;
        for (Tile t : regionTiles) {
            if (t.containsPlayers()) {
                for (Player player : t.getPlayers()) {
                    players[i++] = player;
                }
            }
        }
        return players;
    }

    public final NPC[] getLocalNPCs(Tile tile) {
        return getLocalNPCs(tile, 16);
    }

    public final NPC[] getLocalNPCs(Tile tile, int depth) {
        Tile[] regionTiles = getRegionTiles(tile, depth);
        int i = 0;
        for (Tile t : regionTiles) {
            i += t.getNPCCount();
        }
        NPC[] npcs = new NPC[i];
        i = 0;
        for (Tile t : regionTiles) {
            if (t.containsNPCs()) {
                for (NPC npc : t.getNPCs()) {
                    npcs[i++] = npc;
                }
            }
        }
        return npcs;
    }

    public final GroundItem[] getLocalItems(Tile tile) {
        return getLocalItems(tile, 48);//24
    }

    public final GroundItem[] getLocalItems(Tile tile, int depth) {
        Tile[] regionTiles = getRegionTiles(tile, depth);
        int i = 0;
        for (Tile t : regionTiles) {
            i += t.getItemCount();
        }
        GroundItem[] items = new GroundItem[i];
        i = 0;
        for (Tile t : regionTiles) {
            if (t.containsItems()) {
                for (GroundItem item : t.getItems()) {
                    items[i++] = item;
                }
            }
        }
        return items;
    }

    public final GameObject[] getLocalObjects(Tile tile) {
        return getLocalObjects(tile, 48);
    }

    public final GameObject[] getLocalObjects(Tile tile, int depth) {
        Tile[] regionTiles = getRegionTiles(tile, depth);
        GameObject[] objects = new GameObject[regionTiles.length];
        int i = 0;
        for (Tile t : regionTiles) {
            if (t.getSpawnedObject() != null) {
                objects[i++] = t.getSpawnedObject();
            }
        }
        return objects;
    }

    public final Tile[] getRegionTiles(Tile tile, int depth) {
        int baseX = tile.getX();
        int baseY = tile.getY();
        int z = tile.getZ();
        Tile[] regionTiles = new Tile[(int) Math.pow(((depth * 2) - 1), 2)];
        int i = 0;
        for (int x = -depth + 1; x < depth; x++) {
            for (int y = -depth + 1; y < depth; y++) {
                regionTiles[i++] = Tile.locate(baseX + x, baseY + y, z);
            }
        }
        return regionTiles;
    }

    public void submitPath(PathFinder pathFinder, Entity entity, int x, int y, Runnable r) {
        submitPath(pathFinder, entity, x, y, null, r);
    }

    public void submitPath(PathFinder pathFinder, Entity entity, int x, int y, Locatable target, Runnable r) {
        submitPath(pathFinder, entity, x, y, target, PathProcessor.MOVE_SPEED_ANY, false, r);
    }

    public void submitPath(final PathFinder pathFinder, final Entity entity, final int x, final int y, Locatable target, final int moveSpeed, boolean automated, Runnable r) {
        entity.getPathProcessor().pathRequest = new PathRequest(pathFinder, x, y, target, moveSpeed, automated, r);
    }

    public NodeCollection<Player> getLobbyPlayers() {
        return lobbyPlayers;
    }

    public void register(RemotePlayer player) {
        synchronized (playerMap) {
            playerMap.put(player.getProtocolName(), player);
        }

        logger.info("Registered remote player [" + player + "]");

        notifyLoginState(player);
    }

    public void unregister(String name) {
        PlayerType player;
        synchronized (playerMap) {
            player = playerMap.remove(name);
        }

        if (player != null) {
            logger.info("Un-registered remote player [" + player + "]");

            notifyLoginState(player);
        }
    }

    private void notifyLoginState(PlayerType player) {
        if (Static.isGame()) {
            if (players != null) {
                for (Player p2 : players) {
                    if (p2 != player) {
                        p2.getFriends().notify(player);
                    }
                }
            }
        }
        if (Static.isLobby()) {
            if (lobbyPlayers != null) {
                for (Player p2 : lobbyPlayers) {
                    if (p2 != player) {
                        p2.getFriends().notify(player);
                    }
                }
            }
        }
    }

    public NPC[] searchForNPCs(int id) {
        List<NPC> results = new ArrayList<NPC>();
        for (NPC npc : npcs) {
            if (npc.getId() == id) {
                results.add(npc);
            }
        }
        return results.toArray(new NPC[0]);
    }

    public void sendProjectile(Entity shooter, Entity receiver, int projectileId, Tile start, Tile end,
                               int startHeight, int endHeight, int slowness, int delay, int middleHeight, int startDistanceOffset, int creatorSize) {
        for (Player player : getLocalPlayers(shooter.getLocation(), 32)) {
            if (player.getLocation().withinRange(shooter.getLocation()) || player.getLocation().withinRange(receiver.getLocation())) {
                Static.proto.sendProjectile(player, receiver, projectileId, start, end, startHeight, endHeight, slowness, delay, middleHeight, startDistanceOffset, creatorSize);
            }
        }
    }

    public static int getRandom(int size) {
        return RANDOM.nextInt(size);
    }

    public Map<String, PlayerType> getPlayerMap() {
        return playerMap;
    }
    
    private void processGlobalProcesses() {
    	Iterator<Tick> ticks = globalProcesses.values().iterator();
    	Tick t;
    	while (ticks.hasNext()) {
    		t = ticks.next();
    		t.run();
    		if (!t.running())
    			ticks.remove();
    	}
    }
    
    /**
     * Using this method you can add global processes to the game engine update cycle.
     * Note that the identifier is unique and will overwrite any existing global
     * processes registered with the same name.
     * @param identifier The unique identifier of this global process.
     * @param r The Runnable to execute upon execution.
     */
    public void addGlobalProcess(String identifier, final Runnable r) {
    	globalProcesses.put(identifier, new Tick(null, 0) {
    		@Override
    		public boolean execute() {
    			r.run();
    			return false;
    		}
    	});
    }
    
    /**
     * Using this method you can add global processes to the game engine update cycle.
     * Note that the identifier is unique and will overwrite any existing global
     * processes registered with the same name.
     * @param identifier The unique identifier of this global process.
     * @param r The Runnable to execute upon execution.
     * @param delay The amount of game ticks before executing the Runnable.
     */
    public void addGlobalProcess(String identifier, final Runnable r, int delay) {
    	globalProcesses.put(identifier, new Tick(null, delay) {
    		@Override
    		public boolean execute() {
    			r.run();
    			return false;
    		}
    	});
    }
    
}
