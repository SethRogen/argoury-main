package com.runescape.logic.player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.runescape.Static;
import com.runescape.content.cc.Clan;
import com.runescape.content.cc.ClanManager;
import com.runescape.content.chardesign.CharacterDesign;
import com.runescape.content.combat.misc.SkullRegisterManager;
import com.runescape.content.grotto.War;
import com.runescape.content.misc.LevelNormalize;
import com.runescape.content.misc.LevelRestore;
import com.runescape.content.misc.RunEnergyRestore;
import com.runescape.content.prayer.PlayerPrayerManager;
import com.runescape.content.prayer.PrayerManager;
import com.runescape.content.prayer.PrayerManager.Book;
import com.runescape.content.shop.Shop;
import com.runescape.content.trading.TradingManager;
import com.runescape.engine.tick.Tick;
import com.runescape.link.WorldClientSession;
import com.runescape.logic.Entity;
import com.runescape.logic.dialogue.Conversation;
import com.runescape.logic.item.AppearanceListener;
import com.runescape.logic.item.BankContainer;
import com.runescape.logic.item.ConsumablesHandler;
import com.runescape.logic.item.EquipmentDefinition;
import com.runescape.logic.item.EquipmentListener;
import com.runescape.logic.item.GroundItem;
import com.runescape.logic.item.InventoryListener;
import com.runescape.logic.item.ItemContainer;
import com.runescape.logic.item.ItemDefinition;
import com.runescape.logic.item.ItemsOnDeathManager;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.map.PathProcessor;
import com.runescape.logic.map.Tile;
import com.runescape.logic.map.Directions.NormalDirection;
import com.runescape.logic.npc.NPCDefinition;
import com.runescape.logic.npc.summoning.Familiar;
import com.runescape.logic.object.GameObject;
import com.runescape.logic.utility.GameInterface;
import com.runescape.logic.utility.NodeRunnable;
import com.runescape.network.Frame;
import com.runescape.utility.Destroyable;
import com.runescape.utility.Logging;
import com.runescape.utility.Text;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class Player extends Entity implements PlayerType, Destroyable {
    private static final Logger logger = Logging.log();

    public static final Tile NOOB_SPAWN_POINT = Tile.locate(2677, 3299, 0);
    public static final Tile DEFAULT_SPAWN_POINT = Tile.locate(2656, 3296, 0);

    public static final NodeRunnable<Entity> DEATH_EVENT_1 = new NodeRunnable<Entity>() {
        @Override
        public void run(Entity entity) {
            Player player = (Player) entity;

            player.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_WALK);

            Static.proto.sendMessage(player, "Oh dear you are dead!");

            player.doAnimation(7197);

            player.getCombat().uponDeath1();
        }
    };

    public static final NodeRunnable<Entity> DEATH_EVENT_2 = new NodeRunnable<Entity>() {
        @Override
        public void run(Entity entity) {
            final Player player = (Player) entity;
            player.resetEvents();
            PrayerManager.resetPrayers(player);
            PrayerManager.turnQuickSelectionOff(player, false);
            player.getLevels().setCurrentPrayer(player.getLevels().getLevel(Levels.PRAYER));
            Static.proto.sendLevel(player, Levels.PRAYER);
            player.setTeleportDestination(player.spawnPoint());

            if (player.getLocation().wildernessLevel() > 0) {
                player.registerTick(new Tick() {
                    @Override
                    public boolean execute() {
                        try {
                            new Conversation(Conversation.loadDialogue("misc/dialogue/wildernessdeath"), player, null).init();
                        } catch (Exception e) {
                            logger.error("Failed wilderness death dialogue!", e);
                        }
                        return false;
                    }
                });
            }

            player.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_ANY);
        }
    };
    
    @Override
    public void onDeath() {
		for (Tick t : hpTicks) {
			t.stop();
		}
		final Entity thisEntity = this;
		Tick tick = retrieveTick("SetHPDelay");
		int extraDelay = 0;
		if (tick != null)
			if (tick.running())
				extraDelay = tick.getCounter();
		registerTick(new Tick("death_event1", 1 + extraDelay) {
			@Override
			public boolean execute() {
				NodeRunnable<Entity> death1 = getDeathEvent(1);
				if (death1 != null) {
					death1.run(thisEntity);
				}
				return false;
			}
		});
		registerTick(new Tick("death_event2", 6 + extraDelay) {
			@Override
			public boolean execute() {
				NodeRunnable<Entity> death2 = getDeathEvent(2);
				if (death2 != null) {
					death2.run(thisEntity);
				}
				setHP(getMaxHP());
				masks.setUpdateHPBar(true);
				dead = false;
				return false;
			}
		});
		dead = true;
    }

    public static enum Rights {
        ADMINISTRATOR(2), MODERATOR(1), PLAYER(0);

        public static Rights forValue(int value) {
            switch (value) {
                case 0:
                    return PLAYER;
                case 1:
                    return MODERATOR;
                case 2:
                    return ADMINISTRATOR;
            }
            return null;
        }

        private int value;

        private Rights(int value) {
            this.value = value;
        }

        public int intValue() {
            return value;
        }
    }

    private long lastPacketTime = System.currentTimeMillis();

    private boolean destroyed = false;

    private IoSession session;
    private Queue<Frame> frameQueue = new ArrayDeque<Frame>();
    private boolean[] frameHistory = new boolean[256];

    private int timeLoggedIn = -1;

    private String name;
  
    private String protocolName;

    private String password;

    private int userId;

    public long lastLoggedIn = 0;

    public String email = null;
    public int unreadMessages = 0;
    public long subscriptionEnd = 0;

    private Rights rights = Rights.PLAYER;

    private int loginOpcode = -1;
    private boolean onLogin = true;

    private Appearance appearance = null;
    private Levels levels = null;
    private Friends friends = null;
    public GEI gei = null;

    private DisplayMode displayMode = null;
    private Map<Integer, GameInterface> currentInterfaces = null;

    private Frame cachedAppearanceBlock = null;

    private NormalDirection mapRegionDirection = null;
    private boolean running = false;
    private int runningEnergy = 100;
    private int movementMode = 0; // 1 is walk, 2 is run
    private boolean forcedTeleporting = false;
    private boolean heightUpdate = false;
    private boolean firstCycle = true;

    private ItemContainer inventory = null;
    private ItemContainer equipment = null;
    private BankContainer bank = null;

    private TradingManager tradingManager = null;
    private ItemsOnDeathManager iodManager = null;
    private ConsumablesHandler foodPotionHandler = null;
    private PrayerManager prayerManager = null;
    private CharacterDesign characterDesign;

    private List<GroundItem> localGroundItems;
    private List<GameObject> localGameObjects;

    private boolean isMuted = false;

    private String[] lastIPs = null;

    private boolean inPVP = false;

    private Clan clan = null;

    private Clan ownClan = null;

    private Conversation currentConversation = null;

    private double[] bonuses = new double[18];

    private long lastPing = 0;

    private Shop currentShop = null;

    private boolean inGrotto;

    private Familiar familiar;

    public SkullRegisterManager skullRegisterManager = new SkullRegisterManager(this);

    private boolean noob = false;

    public Player(IoSession session, int opcode) {
        super(opcode == 16 || opcode == 18);

        this.session = session;
        this.loginOpcode = opcode;

        friends = new Friends(this);

        if (inGame()) {
            gei = new GEI(this);

            currentInterfaces = new HashMap<Integer, GameInterface>();

            appearance = new Appearance(this);

            levels = new Levels(this);

            localGroundItems = new ArrayList<GroundItem>();
            localGameObjects = new ArrayList<GameObject>();

            inventory = new ItemContainer(this, 28);
            inventory.addListener(InventoryListener.INSTANCE);

            tradingManager = new TradingManager();
            iodManager = new ItemsOnDeathManager();
            foodPotionHandler = new ConsumablesHandler();
            prayerManager = new PlayerPrayerManager(Book.ANCIENT_CURSES);

            equipment = new ItemContainer(this, 14);
            equipment.addListener(EquipmentListener.INSTANCE);
            equipment.addListener(AppearanceListener.INSTANCE);

            bank = new BankContainer(this);

            setPathProcessor(new PlayerPathProcessor(this));

            registerTick(new RunEnergyRestore(this));
            registerTick(new LevelNormalize(this));
            registerTick(new LevelRestore(this));
        }
    }

    public Tile spawnPoint() {
        if (noob && onLogin) {
            return NOOB_SPAWN_POINT.randomize(11, 10);
        }
        return DEFAULT_SPAWN_POINT.randomize(10, 20);
    }

    public void onLogin() {
        friends.initiate();
    }

    @Override
    public int getWorld() {
        return Static.world.getId();
    }

    @Override
    public String getClanOwner() {
        if (clan != null) {
            return clan.getOwner();
        }
        return null;
    }

    @Override
    public boolean inGame() {
        return loginOpcode == 16 || loginOpcode == 18;
    }

    @Override
    public int getMaxHP() {
        return (int) (levels.getCurrentLevel(Levels.CONSTITUTION) * 10);
    }

    @Override
    public void onChangedHP() {
        if (!isOnLogin()) {
            Static.proto.sendConfig(this, 1240, getHP() * 2);
        }
    }

    @Override
    public NodeRunnable<Entity> getDeathEvent(int stage) {
        if (stage == 1) {
            return DEATH_EVENT_1;
        } else if (stage == 2) {
            return DEATH_EVENT_2;
        }
        return null;
    }

    @Override
    public double[] getBonuses() {
        return bonuses;
    }

    @Override
    public void subResetEvents() {
        if (currentConversation != null) {
            currentConversation.end();
        }
        Static.ahs.onReset(this);
    }

    @Override
    public void subPreProcess() {
        if (timeLoggedIn == -1) {
            timeLoggedIn = Static.world.getTime();
        }
        Runnable r;
        while ((r = specificProcesses.poll()) != null) {
            r.run();
        }
    }

    @Override
    public void subPostProcess() {
        mapRegionDirection = null;
        heightUpdate = false;
        forcedTeleporting = false;

        for (int i = 0; i < 256; i++) {
            frameHistory[i] = false;
        }

        gei.reset();
        Static.world.getPlayerUpdater().getPlayerLocations()[getIndex()] = gei.updateLocation(this);

    }

    @Override
    public void destroy() {
        if (Static.isGame())
            Static.ahs.handleDisconnect(this, false);

        destroyed = true;
        if (this.inGame())
            TradingManager.handleDisconnection(this, this.tradingManager.getOtherPlayer());
        if (Static.isGame() && getCombat() != null) {
            getCombat().stop(true);
        }
        final Player thisPlayer = this;
        Static.engine.dispatchToLinkWorker(new Runnable() {
            @Override
            public void run() {
                WorldClientSession session = null;
                try {
                    session = Static.world.getWCSPool().acquire();

                    session.unregisterPlayer(thisPlayer);
                } finally {
                    if (session != null) {
                        Static.world.getWCSPool().release(session);
                    }
                }
            }
        });
        if (clan != null) {
            ClanManager.leaveChannel(this);
        }
    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rights getRights() {
        return rights;
    }

    public void setRights(Rights rights) {
        this.rights = rights;
    }

    public boolean isConnected() {
        return session.isConnected();
    }

    /**
     * @param frame The frame to be queued.
     */
    public void queueFrame(Frame frame) {
        frameQueue.add(frame);
    }

    public Queue<Frame> getFrameQueue() {
        return frameQueue;
    }

    public boolean[] getFrameHistory() {
        return frameHistory;
    }

    public void preventFrameSpam(int opcode) {
        frameHistory[opcode] = true;
    }

    public boolean isOnLogin() {
        return onLogin;
    }

    public void setOnLogin(boolean onLogin) {
        this.onLogin = onLogin;
    }

    /**
     * @return the noob
     */
    public boolean isNoob() {
        return noob;
    }

    /**
     * @param noob the noob to set
     */
    public void setNoob(boolean noob) {
        this.noob = noob;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public GameInterface getCurrentInterface() {
        return currentInterfaces.get(GameInterface.DEFAULT_POS[displayMode == DisplayMode.FIXED ? 0 : 1]);
    }

    public Map<Integer, GameInterface> getCurrentInterfaces() {
        return currentInterfaces;
    }

    public void updateCurrentInterface() {
        GameInterface gi = getCurrentInterface();
        if (gi != null) {
            gi.update(this);
        }
    }

    public void clearCurrentInterfaces() {
        currentInterfaces.clear();
    }

    public void sendMessage(String message) {
        Static.proto.sendMessage(this, message);
    }

    public Appearance getAppearance() {
        return appearance;
    }

    public Frame getCachedAppearanceBlock() {
        return cachedAppearanceBlock;
    }

    public void setCachedAppearanceBlock(Frame cachedAppearanceBlock) {
        this.cachedAppearanceBlock = cachedAppearanceBlock;
    }

    public boolean isInPVP() {
        return inPVP;
    }

    public void setInPVP(boolean inPVP) {
        this.inPVP = inPVP;
    }

    public String getLastIP() {
        return lastIPs[0];
    }

    public String[] getLastIPs() {
        return lastIPs;
    }

    public void setLastIP(String ip) {
        String lastIP = lastIPs[0];
        if (!lastIP.equals(ip)) {
            String[] newLastIPs = new String[3];
            for (int i = 0; i < 2; i++) {
                newLastIPs[i + 1] = lastIPs[i];
            }
            lastIPs = newLastIPs;
        }
        lastIPs[0] = ip;
    }

    public void setLastIPs(String[] lastIPs) {
        this.lastIPs = lastIPs;
    }

    public boolean inLobby() {
        return loginOpcode == 19;
    }

    public int getLoginOpcode() {
        return loginOpcode;
    }

    public void setLoginOpcode(int loginOpcode) {
        this.loginOpcode = loginOpcode;
    }

    public int getMovementMode() {
        return movementMode;
    }

    public void setMovementMode(int movementMode) {
        this.movementMode = movementMode;
    }

    public List<GroundItem> getLocalGroundItems() {
        return localGroundItems;
    }

    public Levels getLevels() {
        return levels;
    }

    @Override
    public String toString() {
        return "name=" + protocolName + ", index=" + getIndex();
    }
    /*
      * (non-Javadoc)
      *
      * @see java.lang.Runnable#run()
      */

    @Override
    public void run() {
        switch (updateStage) {
            case PRE_UPDATE:
                try {
                    preProcess();
                } catch (Throwable e) {
                    logger.error("Error executing pre-update!", e);
                }
                return;
            case MASK_UPDATE:
                try {
                    setCachedMaskBlock(Static.world.getPlayerUpdater().doMaskBlock(null, this));
                } catch (Throwable e) {
                    logger.error("Error executing mask update!", e);
                }
                Static.world.getMaskUpdateLatch().countDown();
                return;
            case CLIENT_UPDATE:
                try {
                    if (!isConnected()) {
                        session.close(true);
                    } else {
                        Static.world.getPlayerUpdater().update(this);
                        Static.world.getNPCUpdater().update(this);
                    }
                } catch (Throwable e) {
                    logger.error("Error executing client update!", e);
                }
                Static.world.getClientUpdateLatch().countDown();
                return;
            case POST_UPDATE:
                try {
                    boolean sendMapRegionUpdate = mapRegionUpdate;
                    postProcess();
                    if (sendMapRegionUpdate) {
                        Static.proto.sendMapRegion(this);
                    }
                } catch (Throwable e) {
                    logger.error("Error executing post update!", e);
                }
                Static.world.getResetUpdateLatch().countDown();
                return;
        }
    }

    public NormalDirection getMapRegionDirection() {
        return mapRegionDirection;
    }

    public void setMapRegionDirection(NormalDirection mapRegionDirection) {
        this.mapRegionDirection = mapRegionDirection;
    }

    public boolean isHeightUpdate() {
        return heightUpdate;
    }

    public void setHeightUpdate(boolean heightUpdate) {
        this.heightUpdate = heightUpdate;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isForcedTeleporting() {
        return forcedTeleporting;
    }

    public void setForcedTeleporting(boolean forcedTeleporting) {
        this.forcedTeleporting = forcedTeleporting;
    }

    public int getRunningEnergy() {
        return runningEnergy;
    }

    public void setRunningEnergy(int runningEnergy) {
        this.runningEnergy = runningEnergy;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public ItemContainer getInventory() {
        return inventory;
    }

    public ItemContainer getEquipment() {
        return equipment;
    }

    public BankContainer getBank() {
        return bank;
    }

    public List<GameObject> getLocalGameObjects() {
        return localGameObjects;
    }

    public Friends getFriends() {
        return friends;
    }

    @Override
    public int getSize() {
        if (appearance.isNPC()) {
            NPCDefinition def = NPCDefinition.forId(appearance.getNPCType());
            if (def != null) {
                return def.size;
            }
        }
        return 1;
    }

    public Clan getClan() {
        return clan;
    }

    public void setClan(Clan clan) {
        this.clan = clan;
    }

    /**
     * @return the ownClan
     */
    public Clan getOwnClan() {
        return ownClan;
    }

    /**
     * @param ownClan the ownClan to set
     */
    public void setOwnClan(Clan ownClan) {
        this.ownClan = ownClan;
    }

    public Conversation getCurrentConversation() {
        return currentConversation;
    }

    public void setCurrentConversation(Conversation currentConversation) {
        this.currentConversation = currentConversation;
    }

    public void calculateBonuses() {
        for (int i = 0; i < 18; i++) {
            bonuses[i] = 0;
        }
        for (PossesedItem item : equipment.array()) {
            if (item != null) {
                ItemDefinition idef = item.getDefinition();
                if (idef != null) {
                    EquipmentDefinition def = idef.getEquipmentDefinition();
                    if (def == null) {
                        continue;
                    }

                    if (def.getBonuses() != null) {
                        for (int i = 0; i < 18; i++) {
                            if (def.getBonuses().length < 18)
                                break;
                            bonuses[i] += def.getBonuses()[i];
                        }
                    }
                }
            }
        }
    }

    public void initiateCharacterDesign() {
        characterDesign = new CharacterDesign();
        CharacterDesign.initiate(this, appearance);
    }

    public CharacterDesign getCharacterDesign() {
        return characterDesign;
    }

    public TradingManager getTradingManager() {
        return tradingManager;
    }

    public ItemsOnDeathManager getItemsOnDeathManager() {
        return iodManager;
    }

    public ConsumablesHandler getConsumablesHandler() {
        return foodPotionHandler;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public long getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(long lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public long getLastPing() {
        return lastPing;
    }

    public void sendPing() {
        lastPing = System.currentTimeMillis();
        Static.proto.sendPing(this);
    }

    public long getSubscriptionEnd() {
        return subscriptionEnd;
    }

    public void setSubscriptionEnd(long subscriptionEnd) {
        this.subscriptionEnd = subscriptionEnd;
    }

    public PrayerManager getPrayerManager() {
        return prayerManager;
    }

    public boolean isMember() {
        return subscriptionEnd > System.currentTimeMillis();
    }

    public void onMembersOnlyFeature() {
        Static.proto.sendMessage(this, "You need to be a suscriber to use this feature.");
    }

    public boolean isModerator() {
        return rights.intValue() >= Rights.MODERATOR.intValue();
    }

    public boolean isAdministrator() {
        return rights.intValue() >= Rights.ADMINISTRATOR.intValue();
    }

    public Shop getCurrentShop() {
        return currentShop;
    }

    public void setCurrentShop(Shop currentShop) {
        this.currentShop = currentShop;
    }

    public War getOwnedWar() {
        return (getClanOwner() != null && getClanOwner().equalsIgnoreCase(getName()) && getClan().getWar() != null) ? getClan().getWar() : null;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTimeLoggedIn() {
        return timeLoggedIn;
    }

    public boolean isFirstCycle() {
        return firstCycle;
    }

    public void setFirstCycle(boolean firstCycle) {
        this.firstCycle = firstCycle;
    }

    public void updateXPCounter() {
        Static.proto.sendConfig(this, 1801, (int) (levels.getXPGained() * 10));
    }

    public void setInGrotto(boolean inGrotto) {
        this.inGrotto = inGrotto;
    }

    public boolean isInGrotto() {
        return inGrotto;
    }

    public long getLastPacketTime() {
        return lastPacketTime;
    }

    public void setLastPacketTime(long lastPacket) {
        this.lastPacketTime = lastPacket;
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setFamiliar(Familiar familiar) {
        this.familiar = familiar;
    }

    public Familiar getFamiliar() {
        return familiar;
    }

}
