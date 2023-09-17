package com.ziotic.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ziotic.Static;
import com.ziotic.content.combat.Combat;
import com.ziotic.content.combat.misc.HitRegisterManager;
import com.ziotic.content.combat.misc.HitRegisterManager.HitRegisterManagerTick;
import com.ziotic.content.misc.HPNormalize;
import com.ziotic.content.misc.HPRestore;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.map.Coverage;
import com.ziotic.logic.map.Directions;
import com.ziotic.logic.map.Directions.NormalDirection;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.map.Region;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.mask.Animation;
import com.ziotic.logic.mask.Chat;
import com.ziotic.logic.mask.Graphic;
import com.ziotic.logic.mask.Mask;
import com.ziotic.logic.mask.Masks;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.utility.NodeRunnable;
import com.ziotic.network.Frame;
import com.ziotic.utility.Logging;

/**
 * @author Lazaro
 */
public abstract class Entity extends Locatable implements Runnable {
	@SuppressWarnings("unused")
	private Logger logger = Logging.log();

	public static enum UpdateStage {
		CLIENT_UPDATE, MASK_UPDATE, POST_UPDATE, PRE_UPDATE
	}

	public static Tile locationNextTo(Entity other, int size) {
		int origX = other.getX();
		int origY = other.getY();
		Tile curTile = null;
		int z = other.getZ();
		for (int dirX = -1; dirX <= 1; dirX++) {
			for (int dirY = -1; dirY <= 1; dirY++) {
				int offsetX = (dirX > 0 ? (size - 1) : size) * dirX;
				int offsetY = (dirY > 0 ? (size - 1) : size) * dirY;

				int startX = origX + offsetX;
				int startY = origY + offsetY;

				int curCost = 1000;

				if (dirX != 0) {
					for (int y = startY; y <= startY + (size - 1); y++) {
						boolean clipped = false;

						clippingCheck:
							for (int x2 = startX; x2 < startX + size; x2++) {
								for (int y2 = y; y2 < y + size; y2++) {
									if (Region.getAbsoluteClipping(x2, y2, z) > 255) {
										clipped = true;
										break clippingCheck;
									}
								}
							}

						if (!clipped && curTile != other.getLocation()) {
							int cost = Math.abs(startX - origX) + Math.abs(y - origX);
							if (curTile == null || cost < curCost) {
								curTile = Tile.locate(startX, y, z);
								curCost = cost;
							}
						}

					}
				}
				if (dirY != 0) {
					for (int x = startX; x <= startX + (size - 1); x++) {
						boolean clipped = false;

						clippingCheck:
							for (int x2 = x; x2 < x + size; x2++) {
								for (int y2 = startY; y2 < startY + size; y2++) {
									if (Region.getAbsoluteClipping(x2, y2, z) > 255) {
										clipped = true;
										break clippingCheck;
									}
								}
							}

						if (!clipped && curTile != other.getLocation()) {
							int cost = Math.abs(x - origX) + Math.abs(startY - origY);
							if (curTile == null || cost < curCost) {
								curTile = Tile.locate(x, startY, z);
								curCost = cost;
							}
						}
					}
				}
			}
		}
		return curTile;
	}

	private static long identifierCounter = 0;
	private long identifier;

	private int tickIdentifierCounter = 0;

	protected int hp = 100;
	public boolean dead = false;
	public Masks masks = null;
	private Frame cachedMaskBlock = null;
	public Directions directions = null;
	private Tile teleportDestination = null;
	private boolean teleporting = false;
	public boolean teleBlocked = false;
	protected UpdateStage updateStage = null;
	private Map<String, Tick> ticks = null;
	private PathProcessor pathProcessor = null;
	private Tile previousLocation = null;
	private boolean clipping = true;
	private boolean inMulti = false;
	protected Coverage coverage;

	protected Tile mapRegionUpdatePosition = null;
	protected boolean mapRegionUpdate = false;

	protected Combat combat = null;
	protected LinkedList<Tick> hpTicks = new LinkedList<Tick>();

	public HitRegisterManager hitRegisterManager = new HitRegisterManager();
	protected Deque<Runnable> specificProcesses = new ArrayDeque<Runnable>();
	protected HashMap<String, Runnable> globalProcesses = new HashMap<String, Runnable>();

	public Entity() {
		this(true);
	}

	public Entity(boolean inGame) {
		identifier = identifierCounter++;
		if (inGame) {
			masks = new Masks(this);
			directions = new Directions();

			ticks = new HashMap<String, Tick>();

			combat = new Combat(this);

			registerTick(new HPRestore(this));
			registerTick(new HPNormalize(this));
			registerTick(new HitRegisterManagerTick(this));
		}
	}

	/**
	 * Cancels a tick event that ways registered.
	 * <p/>
	 * Note: The tick may have already been running.
	 *
	 * @param identifier The getIdentifier of the tick to be canceled.
	 */
	public void cancelTick(String identifier) {
		synchronized (ticks) {
			Tick tick = ticks.remove(identifier);
			if (tick != null) {
				tick.stop();
			}
		}
	}

	/**
	 * Cancels a tick event that ways registered.
	 * <p/>
	 * Note: The tick may have already been running.
	 *
	 * @param tick The tick to be canceled.
	 */
	public void cancelTick(Tick tick) {
		cancelTick(tick.getIdentifier());
	}

	/**
	 * Registers a tick event onto the entity's tick processor.
	 *
	 * @param tick The tick to be registered.
	 */
	public void registerTick(Tick tick) {
		if (tick.getIdentifier() == null) {
			tick.setIdentifier(new StringBuilder().append(tickIdentifierCounter++).toString());
		}
		if (tick.getPolicy() == Tick.TickPolicy.STRICT) {
			cancelStrictTicks();
		}
		tick.onStart();
		synchronized (ticks) {
			Tick oldTick = ticks.put(tick.getIdentifier(), tick);
			if (oldTick != null) {
				oldTick.stop();
			}
		}
		Static.engine.submit(tick);
	}

	public void cancelStrictTicks() {
		synchronized (ticks) {
			for (Tick tick : new ArrayList<Tick>(ticks.values())) {
				if (tick.getPolicy() == Tick.TickPolicy.STRICT) {
					cancelTick(tick);
				}
			}
		}
	}

	public void resetEvents() {
		resetEvents(true);
	}

	public void resetEvents(boolean nullCoordinateFuture) {
		cancelTick("event");
		cancelStrictTicks();

		pathProcessor.reset(nullCoordinateFuture);

		//resetAnimation();
		resetFaceDirection();
		//resetGraphics();

		subResetEvents();
	}

	public abstract void subResetEvents();

	/**
	 * Gets a tick from it's getIdentifier string.
	 *
	 * @param identifier The tick's getIdentifier.
	 * @return The tick found registered using the specified getIdentifier.
	 */
	public Tick retrieveTick(String identifier) {
		synchronized (ticks) {
			return ticks.get(identifier);
		}
	}

	public final boolean hasCachedUpdateBlock() {
		return cachedMaskBlock != null;
	}

	public final Frame getCachedMaskBlock() {
		return cachedMaskBlock;
	}

	public final void setCachedMaskBlock(Frame cachedMaskBlock) {
		this.cachedMaskBlock = cachedMaskBlock;
	}

	public final Directions getDirections() {
		return directions;
	}

	public final void preProcess() {
		processTicks();

		subPreProcess();

		processHP();

		masks.processQueuedMasks();

		pathProcessor.processPathRequest();

		pathProcessor.process();
	}

	public final void postProcess() {
		masks.reset();
		directions.reset();
		teleportDestination = null;
		teleporting = false;
		mapRegionUpdate = false;
		cachedMaskBlock = null;


		subPostProcess();
	}

	private final void processTicks() {
		synchronized (ticks) {
			for (Iterator<Map.Entry<String, Tick>> it = new HashSet<Map.Entry<String, Tick>>(ticks.entrySet()).iterator(); it.hasNext(); ) {
				Tick tick = it.next().getValue();
				if (!tick.running()) {
					ticks.remove(tick.getIdentifier());
				} else {
					/*try {
                        tick.run();
                    } catch (Throwable e) {
                        logger.error("Error handling tick [" + tick + "]");
                        e.printStackTrace();
                    }*/
				}
			}
		}
	}
	
	public abstract void onDeath();

	private final void processHP() {
		if (!dead && hp <= 0) {
			onDeath();
		}
	}

	public final Masks getMasks() {
		return masks;
	}

	public final Tile getTeleportDestination() {
		return teleportDestination;
	}

	public final void setTeleportDestination(Tile teleportDestination) {
		this.teleportDestination = teleportDestination;
	}

	public final boolean isTeleporting() {
		return teleporting;
	}

	public final void setTeleporting(boolean teleporting) {
		this.teleporting = teleporting;
	}

	public final UpdateStage getUpdateStage() {
		return updateStage;
	}

	public final void setUpdateStage(UpdateStage updateStage) {
		this.updateStage = updateStage;
	}

	public final PathProcessor getPathProcessor() {
		return pathProcessor;
	}

	public final void setPathProcessor(PathProcessor pathProcessor) {
		this.pathProcessor = pathProcessor;
	}

	public final int getHP() {
		return hp;
	}

	public final void setHP(int hp) {
		if (hp < 0)
			hp = 0;
		this.hp = hp;
		onChangedHP();
	}

	protected final void addHP(int hp, int addition) {
		if (!dead) {
			this.hp += hp;
			if (this.hp > getMaxHP() + addition)
				this.hp = getMaxHP() + addition;
			onChangedHP();
		}
	}

	protected final void removeHP(int hp) {
		if (!dead) {
			this.hp -= hp;
			if (this.hp < 0)
				this.hp = 0;
			onChangedHP();
		}
	}

	public final void registerHPTick(Tick tick) {
		hpTicks.add(tick);
		registerTick(tick);
	}

	public final boolean isDead() {
		return dead;
	}

	public final Tile getPreviousLocation() {
		return previousLocation;
	}

	public final void setPreviousLocation(Tile previousLocation) {
		this.previousLocation = previousLocation;
	}

	public final boolean isClipping() {
		return clipping;
	}

	public final void setClipping(boolean clipping) {
		this.clipping = clipping;
	}

	public final boolean isMapRegionUpdate() {
		return mapRegionUpdate;
	}

	public final void setMapRegionUpdate(boolean mapRegionUpdate) {
		this.mapRegionUpdate = mapRegionUpdate;
	}

	public final Tile getMapRegionUpdatePosition() {
		return mapRegionUpdatePosition;
	}

	public final void setMapRegionUpdatePosition(Tile mapRegionUpdatePosition) {
		this.mapRegionUpdatePosition = mapRegionUpdatePosition;
	}

	public void subPreProcess() {

	}

	public abstract void subPostProcess();

	public abstract void onChangedHP();

	public abstract int getSize();

	public abstract int getMaxHP();

	public abstract boolean inGame();

	public abstract NodeRunnable<Entity> getDeathEvent(int stage);

	public abstract double[] getBonuses();

	public final void doAnimation(int id) {
		doAnimation(id, 0);
	}

	public final void doAnimation(int id, int delay) {
		masks.setAnimation(id, delay);
	}

	public final void doAnimation(Animation animation) {
		doAnimation(animation.getId(), animation.getDelay());
	}

	public final void doGraphics(int id) {
		doGraphics(id, 0, 0);
	}

	public final void doGraphics(int id, int delay) {
		doGraphics(id, delay, 0, 0, 0);
	}

	public final void doGraphics(int id, int delay, int height) {
		doGraphics(id, delay, height, 0, 0);
	}

	public final void doGraphics(int id, int delay, int height, int direction) {
		doGraphics(id, delay, height, direction, 0);
	}

	public final void doGraphics(int id, int delay, int height, int direction, int direction2) {
		masks.submitGraphics(new Graphic(id, delay, height, direction, direction2));
	}

	public final void doForceChat(String message) {
		masks.setForcedChat(new Chat(message, 0, 255));
	}

	public final void doGraphics(Graphic graphic) {
		doGraphics(graphic.getId(), graphic.getSettings(), graphic.getDirection());
	}

	public final void resetAnimation() {
		doAnimation(Mask.MASK_RESET);
	}

	public final void resetFaceDirection() {
		masks.resetDirection();
	}

	public final void resetCurrentGraphics() {
		doGraphics(Mask.MASK_RESET);
	}

	public final void resetGraphics() {
		masks.clearGraphics();
	}

	public final void faceEntity(Entity entity) {
		masks.setFaceEntity(entity instanceof Player ? entity.getIndex() + 32768 : entity.getIndex());
	}

	public final void faceDirection(int dir) {
		masks.setFaceDirection(dir);
	}

	public final void faceDirection(Tile loc) {
		masks.setFaceDirection(loc);
	}

	public final Combat getCombat() {
		return combat;
	}

	public long getIdentifier() {
		return identifier;
	}

	public boolean isInMulti() {
		return inMulti;
	}

	public void setInMulti(boolean inMulti) {
		this.inMulti = inMulti;
	}

	/**
	 * Using this method you can add specific entity behaviour
	 * that will be executed in the first upcoming engine update cycle.
	 * @param r The runnable to execute in the first upcoming update cycle.
	 */
	public void addSpecificProcess(Runnable r) {
		specificProcesses.add(r);
	}

	/**
	 * Using this method you can add specific entity behaviour
	 * that will be executed in the first upcoming engine update cycle.
	 * @param r The runnable to execute in the first upcoming update cycle.
	 * @param delay The amount of ticks to wait to add this Runnable.
	 */
	public void addSpecificProcess(final Runnable r, int delay) {
		registerTick(new Tick(null, delay) {
			@Override
			public boolean execute() {
				specificProcesses.add(r);
				return false;
			}
		});
	}

	/**
	 * Using this method you can add specific entity behaviour
	 * that will be executed in the first upcoming engine update cycle.
	 * @param r The runnable to execute in the first upcoming update cycle.
	 * @param delay The amount of ticks to wait to add this Runnable.
	 * @param identifier The identifier of the tick used to add this Runnable.
	 */
	public void addSpecificProcess(final Runnable r, int delay, String identifier) {
		registerTick(new Tick(identifier, delay) {
			@Override
			public boolean execute() {
				specificProcesses.add(r);
				return false;
			}
		});
	}

	public Coverage getCoverage() {
		return coverage;
	}
	
	public void setCoverage() {
		coverage = new Coverage(getLocation(), getSize());
	}
	
	public void updateCoverage(NormalDirection direction) {
		coverage.update(direction, getSize());
	}
	
	public void updateCoverage(Tile loc) {
		coverage.update(loc, getSize());
	}
}