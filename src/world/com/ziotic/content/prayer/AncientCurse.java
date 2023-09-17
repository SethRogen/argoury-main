package com.ziotic.content.prayer;

import com.ziotic.Static;
import com.ziotic.content.prayer.definitions.AncientCurseDefinition;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.Entity;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.player.Player;

import java.util.LinkedList;
import java.util.Random;

public class AncientCurse extends AbstractPrayer {

    private static final Random r = new Random();

    private static final String CURSE_ATTACK_RESTORE = "curse_attack_restore";
    private static final String CURSE_STRENGTH_RESTORE = "curse_strength_restore";
    private static final String CURSE_DEFENCE_RESTORE = "curse_defence_restore";
    private static final String CURSE_RANGED_RESTORE = "curse_ranged_restore";
    private static final String CURSE_MAGIC_RESTORE = "curse_magic_restore";

    private static final String CURSE_ATTACK_NORMALIZE = "curse_attack_normalize";
    private static final String CURSE_STRENGTH_NORMALIZE = "curse_strength_normalize";
    private static final String CURSE_DEFENCE_NORMALIZE = "curse_defence_normalize";
    private static final String CURSE_RANGED_NORMALIZE = "curse_ranged_normalize";
    private static final String CURSE_MAGIC_NORMALIZE = "curse_magic_normalize";

    protected AncientCurseDefinition definition;

    protected CurseCounter curseCounter;
    protected Class<? extends SapCurseCounter> scClass = null;
    protected Class<? extends LeechCurseCounter> lcClass = null;

    public AncientCurse(AncientCurseDefinition definition) {
        super(definition);
        this.definition = definition;
    }

    protected AncientCurseDefinition getDefinition() {
        return definition;
    }

    /**
     * Used when the prayer is initiated
     *
     * @param player
     */
    protected void initializeCurseCounter(Player player) {
        if (curseCounter == null) {
            switch (definition.getType()) {
                case SAP:
                    switch (definition.getId()) {
                        case 1:
                            curseCounter = new SapMeleeCounter(definition.getId());
                            break;
                        case 2:
                            curseCounter = new SapRangedCounter(definition.getId());
                            break;
                        case 3:
                            curseCounter = new SapMagicCounter(definition.getId());
                            break;
                    }
                    break;
                case LEECH:
                    switch (definition.getId()) {
                        case 10:
                            curseCounter = new LeechAttackCounter(definition.getId());
                            break;
                        case 11:
                            curseCounter = new LeechRangedCounter(definition.getId());
                            break;
                        case 12:
                            curseCounter = new LeechMagicCounter(definition.getId());
                            break;
                        case 13:
                            curseCounter = new LeechDefenceCounter(definition.getId());
                            break;
                        case 14:
                            curseCounter = new LeechStrengthCounter(definition.getId());
                            break;
                        case 16:
                            curseCounter = new LeechSpecialCounter(definition.getId());
                    }
                    break;
            }
        }
        curseCounter.initialize(player);
    }

    /**
     * Used when the prayer is switched off
     */
    protected void terminateCurseCounter() {
        if (curseCounter != null)
            curseCounter.terminate();
    }

    public void hit(Player player, Entity victim) {
        if (curseCounter != null)
            curseCounter.onHit(player, victim);
    }

    public void hit(Player player, Player victim) {
        if (curseCounter != null)
            curseCounter.onHit(player, victim);
    }

    private class EntityCurseTick extends Tick {

        protected Entity entity;
        protected LinkedList<EntityCurseTick> list;

        public EntityCurseTick(LinkedList<EntityCurseTick> list, Entity entity, int id, final CurseCounter cs) {
            super(entity.getIdentifier() + "_" + id, 17);
            this.list = list;
            this.entity = entity;
        }

        @Override
        public boolean execute() {
            list.remove(this);
            if (entity instanceof Player) {
                Player p = (Player) entity;
                p.getPrayerManager().initialDrainedAttackPercentage = 0;
                p.getPrayerManager().initialDrainedStrengthPercentage = 0;
                p.getPrayerManager().initialDrainedDefencePercentage = 0;
                p.getPrayerManager().initialDrainedRangedPercentage = 0;
                p.getPrayerManager().initialDrainedMagicPercentage = 0;
                PrayerManager.updateAdjustments(p);
            }
            return false;
        }

    }

    private class PlayerNormalizeTick extends Tick {

        public PlayerNormalizeTick(String identifier) {
            super(identifier, 50);
        }

        @Override
        public boolean execute() {
            return false;
        }

    }

    private class PlayerRestoreTick extends Tick {

        public PlayerRestoreTick(String identifier) {
            super(identifier, 50);
        }

        @Override
        public boolean execute() {
            return false;
        }

    }

    private abstract class CurseCounter {

        protected float drainPercentage = 0;
        protected int id;

        protected Tick executor;

        protected final LinkedList<EntityCurseTick> activeEntities = new LinkedList<EntityCurseTick>();

        public CurseCounter(int id) {
            this.id = id;
        }

        public void initialize(final Player player) {
            executor = new Tick("curse_counter_executor_" + id, 4 + r.nextInt(3)) {
                @Override
                public boolean execute() {
                    for (EntityCurseTick t : activeEntities) {
                        if (r.nextInt(5) == 0) {
                            executeCurse(player, t.entity);
                            this.interval = 4 + r.nextInt(3);
                        }
                    }
                    return true;
                }
            };
            player.registerTick(executor);
            PrayerManager.updateAdjustments(player);
        }

        public void onHit(Player player, Entity victim) {
            Tick viTick = player.retrieveTick(victim.getIdentifier() + "_" + id);
            if (player.getPrayerManager().curses.get(id).isOn) {
                if (viTick == null) {
                    viTick = new EntityCurseTick(activeEntities, victim, id, this);
                    activeEntities.add((EntityCurseTick) viTick);
                    player.registerTick(viTick);
                } else {
                    viTick.setCounter(17);
                }
            }
        }

        public void onHit(Player player, Player victim) {
            Tick viTick = player.retrieveTick(victim.getIdentifier() + "_" + id);
            if (player.getPrayerManager().curses.get(id).isOn) {
                if (viTick == null) {
                    viTick = new EntityCurseTick(activeEntities, victim, id, this);
                    activeEntities.add((EntityCurseTick) viTick);
                    player.registerTick(viTick);
                } else {
                    viTick.setCounter(17);
                }
            }
        }

        protected void executeCurse(Player player, Entity victim) {
            if (victim instanceof Player)
                executePlayerVictimCurse(player, (Player) victim);
            else
                executeNPCVictimCurse(player, (NPC) victim);
        }

        protected abstract void executePlayerVictimCurse(Player player, Player victim);

        protected abstract void executeNPCVictimCurse(Player player, NPC victim);

        public void terminate() {
            executor.stop();
            activeEntities.clear();
        }

    }

    private abstract class SapCurseCounter extends CurseCounter {

        protected static final float OVERTIME_MAX_DRAIN = -0.10f;
        protected static final float INITIAL_DRAIN = -0.10f;

        public SapCurseCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(Player player, Player victim) {
            drainPercentage = -(0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f));
        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            drainPercentage = -(0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f));
        }

        @Override
        public void terminate() {
            super.terminate();
        }

    }

    private abstract class LeechCurseCounter extends CurseCounter {

        protected static final float OVERTIME_MAX_DRAIN = -0.15f;
        protected static final float OVERTIME_MAX_BOOST = 0.05f;
        protected static final float INITIAL_DRAIN = -0.10f;

        protected float boostPercentage = 0;

        public LeechCurseCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(Player player, Player victim) {
            float percentage = 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
            boostPercentage += percentage;
            drainPercentage = -percentage;
            if (boostPercentage > OVERTIME_MAX_BOOST)
                boostPercentage = OVERTIME_MAX_BOOST;

        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            float percentage = 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
            boostPercentage += percentage;
            drainPercentage = -percentage;
            if (boostPercentage > OVERTIME_MAX_BOOST)
                boostPercentage = OVERTIME_MAX_BOOST;
        }

        @Override
        public void terminate() {
            boostPercentage = 0;
            super.terminate();
        }

    }

    private class SapMeleeCounter extends SapCurseCounter {

        public SapMeleeCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            victim.getPrayerManager().initialDrainedAttackPercentage = INITIAL_DRAIN;
            victim.getPrayerManager().initialDrainedStrengthPercentage = INITIAL_DRAIN;
            victim.getPrayerManager().initialDrainedDefencePercentage = INITIAL_DRAIN;
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(final Player player, final Player victim) {
            super.executePlayerVictimCurse(player, victim);
            final PrayerManager vManager = victim.getPrayerManager();
            player.getCombat().executeGraphics(2214, 0, 0);
            player.getCombat().executeAnimation(12569, 0, false, false);
            Static.world.sendProjectile(player, victim, 2215, player.getLocation(), victim.getLocation(), 30, 35, 30, 45, 25, 0, player.getSize());
            int distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = 40 + 20 + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            Tick tick = new Tick(null, (int) hpDrainDelay) {
                @Override
                public boolean execute() {
                    vManager.additionalDrainedAttackPercentage += drainPercentage;
                    vManager.additionalDrainedStrengthPercentage += drainPercentage;
                    vManager.additionalDrainedDefencePercentage += drainPercentage;
                    if (vManager.additionalDrainedAttackPercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedAttackPercentage = OVERTIME_MAX_DRAIN;
                    if (vManager.additionalDrainedStrengthPercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedStrengthPercentage = OVERTIME_MAX_DRAIN;
                    if (vManager.additionalDrainedDefencePercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedDefencePercentage = OVERTIME_MAX_DRAIN;
                    victim.getCombat().executeGraphics(2216, 0, 0);
                    String pString;
                    String vString;
                    if (vManager.additionalDrainedAttackPercentage <= OVERTIME_MAX_DRAIN) {
                        pString = "You can't drain your opponents attack any further, your curse is at maximum power.";
                        vString = "Your attack can't be drained any further by the opponent's curse, his curse is at maximum power";
                    } else {
                        pString = "Your curse drains some of your victim's attack.";
                        vString = "Some of your attack has been drained by a curse.";
                    }
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    if (vManager.additionalDrainedStrengthPercentage <= OVERTIME_MAX_DRAIN) {
                        pString = "You can't drain your opponents strength any further, your curse is at maximum power.";
                        vString = "Your strength can't be drained any further by the opponent's curse, his curse is at maximum power";
                    } else {
                        pString = "Your curse drains some of your victim's strength.";
                        vString = "Some of your strength has been drained by a curse.";
                    }
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    if (vManager.additionalDrainedDefencePercentage <= OVERTIME_MAX_DRAIN) {
                        pString = "You can't drain your opponents defence any further, your curse is at maximum power.";
                        vString = "Your defence can't be drained any further by the opponent's curse, his curse is at maximum power";
                    } else {
                        pString = "Your curse drains some of your victim's defence.";
                        vString = "Some of your defence has been drained by a curse.";
                    }
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    PrayerManager.updateAdjustments(victim);
                    return false;
                }
            };
            player.registerTick(tick);
            if (victim.retrieveTick(CURSE_ATTACK_RESTORE) == null) {
                PlayerRestoreTick attack = new PlayerRestoreTick(CURSE_ATTACK_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedAttackPercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedAttackPercentage > 0) {
                            vManager.additionalDrainedStrengthPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(attack);
            }
            if (victim.retrieveTick(CURSE_STRENGTH_RESTORE) == null) {
                PlayerRestoreTick strength = new PlayerRestoreTick(CURSE_STRENGTH_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedStrengthPercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedStrengthPercentage > 0) {
                            vManager.additionalDrainedStrengthPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(strength);
            }
            if (victim.retrieveTick(CURSE_DEFENCE_RESTORE) == null) {
                PlayerRestoreTick defence = new PlayerRestoreTick(CURSE_DEFENCE_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedDefencePercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedDefencePercentage > 0) {
                            vManager.additionalDrainedDefencePercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(defence);
            }
        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            super.executeNPCVictimCurse(player, victim);
            // TODO npc prayer adjustment
        }

        @Override
        public void terminate() {
            for (EntityCurseTick t : activeEntities) {
                if (t.entity instanceof Player) {
                    Player p = (Player) t.entity;
                    p.getPrayerManager().initialDrainedAttackPercentage = 0;
                    p.getPrayerManager().initialDrainedStrengthPercentage = 0;
                    p.getPrayerManager().initialDrainedDefencePercentage = 0;
                    PrayerManager.updateAdjustments(p);
                }
            }
            super.terminate();
        }

    }

    private class SapRangedCounter extends SapCurseCounter {

        public SapRangedCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            victim.getPrayerManager().initialDrainedRangedPercentage = INITIAL_DRAIN;
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(final Player player, final Player victim) {
            super.executePlayerVictimCurse(player, victim);
            final PrayerManager vManager = victim.getPrayerManager();
            player.getCombat().executeGraphics(2217, 0, 0);
            player.getCombat().executeAnimation(12569, 0, false, false);
            Static.world.sendProjectile(player, victim, 2218, player.getLocation(), victim.getLocation(), 30, 35, 30, 45, 25, 0, player.getSize());
            int distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = 40 + 20 + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            Tick tick = new Tick(null, (int) hpDrainDelay) {
                @Override
                public boolean execute() {
                    vManager.additionalDrainedRangedPercentage += drainPercentage;
                    if (vManager.additionalDrainedRangedPercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedRangedPercentage = OVERTIME_MAX_DRAIN;
                    vManager.additionalDrainedDefencePercentage += drainPercentage;
                    if (vManager.additionalDrainedDefencePercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedDefencePercentage = OVERTIME_MAX_DRAIN;
                    victim.getCombat().executeGraphics(2219, 0, 0);
                    String pString;
                    String vString;
                    if (vManager.additionalDrainedRangedPercentage <= OVERTIME_MAX_DRAIN) {
                        pString = "You can't drain your opponents ranged any further, your curse is at maximum power.";
                        vString = "Your ranged can't be drained any further by the opponent's curse, his curse is at maximum power";
                    } else {
                        pString = "Your curse drains some of your victim's ranged.";
                        vString = "Some of your ranged has been drained by a curse.";
                    }
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    if (vManager.additionalDrainedDefencePercentage <= OVERTIME_MAX_DRAIN) {
                        pString = "You can't drain your opponents defence any further, your curse is at maximum power.";
                        vString = "Your defence can't be drained any further by the opponent's curse, his curse is at maximum power";
                    } else {
                        pString = "Your curse drains some of your victim's defence.";
                        vString = "Some of your defence has been drained by a curse.";
                    }
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    PrayerManager.updateAdjustments(victim);
                    return false;
                }
            };
            player.registerTick(tick);
            if (victim.retrieveTick(CURSE_RANGED_RESTORE) == null) {
                PlayerRestoreTick ranged = new PlayerRestoreTick(CURSE_RANGED_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedRangedPercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedRangedPercentage > 0) {
                            vManager.additionalDrainedRangedPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(ranged);
            }
            if (victim.retrieveTick(CURSE_DEFENCE_RESTORE) == null) {
                PlayerRestoreTick defence = new PlayerRestoreTick(CURSE_DEFENCE_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedDefencePercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedDefencePercentage > 0) {
                            vManager.additionalDrainedDefencePercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(defence);
            }
        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            super.executeNPCVictimCurse(player, victim);
            // TODO npc prayer adjustment
        }

        @Override
        public void terminate() {
            for (EntityCurseTick t : activeEntities) {
                if (t.entity instanceof Player) {
                    Player p = (Player) t.entity;
                    p.getPrayerManager().initialDrainedRangedPercentage = 0;
                    PrayerManager.updateAdjustments(p);
                }
            }
            super.terminate();
        }

    }

    private class SapMagicCounter extends SapCurseCounter {

        public SapMagicCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            victim.getPrayerManager().initialDrainedMagicPercentage = INITIAL_DRAIN;
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(final Player player, final Player victim) {
            super.executePlayerVictimCurse(player, victim);
            final PrayerManager vManager = victim.getPrayerManager();
            player.getCombat().executeGraphics(2220, 0, 0);
            player.getCombat().executeAnimation(12569, 0, false, false);
            Static.world.sendProjectile(player, victim, 2221, player.getLocation(), victim.getLocation(), 30, 35, 30, 45, 25, 0, player.getSize());
            int distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = 40 + 20 + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            Tick tick = new Tick(null, (int) hpDrainDelay) {
                @Override
                public boolean execute() {
                    vManager.additionalDrainedMagicPercentage += drainPercentage;
                    if (vManager.additionalDrainedMagicPercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedMagicPercentage = OVERTIME_MAX_DRAIN;
                    vManager.additionalDrainedDefencePercentage += drainPercentage;
                    if (vManager.additionalDrainedDefencePercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedDefencePercentage = OVERTIME_MAX_DRAIN;

                    victim.getCombat().executeGraphics(2222, 0, 0);
                    String pString;
                    String vString;
                    if (vManager.additionalDrainedMagicPercentage <= OVERTIME_MAX_DRAIN) {
                        pString = "You can't drain your opponents magic any further, your curse is at maximum power.";
                        vString = "Your magic can't be drained any further by the opponent's curse, his curse is at maximum power";
                    } else {
                        pString = "Your curse drains some of your victim's magic.";
                        vString = "Some of your magic has been drained by a curse.";
                    }
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    if (vManager.additionalDrainedDefencePercentage <= OVERTIME_MAX_DRAIN) {
                        pString = "You can't drain your opponents defence any further, your curse is at maximum power.";
                        vString = "Your defence can't be drained any further by the opponent's curse, his curse is at maximum power";
                    } else {
                        pString = "Your curse drains some of your victim's defence.";
                        vString = "Some of your defence has been drained by a curse.";
                    }
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    PrayerManager.updateAdjustments(victim);
                    return false;
                }
            };
            player.registerTick(tick);
            if (victim.retrieveTick(CURSE_MAGIC_RESTORE) == null) {
                PlayerRestoreTick magic = new PlayerRestoreTick(CURSE_MAGIC_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedMagicPercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedMagicPercentage > 0) {
                            vManager.additionalDrainedMagicPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(magic);
            }
            if (victim.retrieveTick(CURSE_DEFENCE_RESTORE) == null) {
                PlayerRestoreTick defence = new PlayerRestoreTick(CURSE_DEFENCE_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedDefencePercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedDefencePercentage > 0) {
                            vManager.additionalDrainedDefencePercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(defence);
            }
        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            super.executeNPCVictimCurse(player, victim);
            // TODO npc prayer adjustment
        }

        @Override
        public void terminate() {
            for (EntityCurseTick t : activeEntities) {
                if (t.entity instanceof Player) {
                    Player p = (Player) t.entity;
                    p.getPrayerManager().initialDrainedMagicPercentage = 0;
                    PrayerManager.updateAdjustments(p);
                }
            }
            super.terminate();
        }

    }

    private class LeechAttackCounter extends LeechCurseCounter {

        public LeechAttackCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            victim.getPrayerManager().initialDrainedAttackPercentage = INITIAL_DRAIN;
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(final Player player, final Player victim) {
            super.executePlayerVictimCurse(player, victim);
            final PrayerManager pManager = player.getPrayerManager();
            final PrayerManager vManager = victim.getPrayerManager();
            player.getCombat().executeAnimation(12575, 0, false, false);
            Static.world.sendProjectile(player, victim, 2231, player.getLocation(), victim.getLocation(), 30, 35, 30, 45, 25, 0, player.getSize());
            int distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = 40 + 20 + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            Tick tick = new Tick(null, (int) hpDrainDelay) {
                @Override
                public boolean execute() {
                    pManager.additionalAttackBoostPercentage = boostPercentage;
                    vManager.additionalDrainedAttackPercentage += drainPercentage;
                    if (vManager.additionalDrainedAttackPercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedAttackPercentage = OVERTIME_MAX_DRAIN;

                    victim.getCombat().executeGraphics(2232, 0, 0);
                    String pString;
                    String vString;
                    if (pManager.additionalAttackBoostPercentage >= OVERTIME_MAX_BOOST)
                        pString = "You can't leech any more attack from your victim.";
                    else
                        pString = "You leech some of your victim's attack.";
                    if (vManager.additionalDrainedAttackPercentage <= OVERTIME_MAX_DRAIN)
                        vString = "Your attack can't be leeched any further by the opponent's curse, his curse is at maximum power";
                    else
                        vString = "Some of your attack has been leeched by a curse.";
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    PrayerManager.updateAdjustments(player);
                    PrayerManager.updateAdjustments(victim);
                    return false;
                }
            };
            player.registerTick(tick);
            if (player.retrieveTick(CURSE_ATTACK_NORMALIZE) == null) {
                PlayerNormalizeTick nAttack = new PlayerNormalizeTick(CURSE_ATTACK_NORMALIZE) {
                    @Override
                    public boolean execute() {
                        pManager.additionalAttackBoostPercentage -= 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (pManager.additionalAttackBoostPercentage < 0) {
                            pManager.additionalAttackBoostPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(player);
                        return true;
                    }
                };
                player.registerTick(nAttack);
            }
            if (victim.retrieveTick(CURSE_ATTACK_RESTORE) == null) {
                PlayerRestoreTick rAttack = new PlayerRestoreTick(CURSE_ATTACK_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedAttackPercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedAttackPercentage > 0) {
                            vManager.additionalDrainedAttackPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(rAttack);
            }
        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            super.executeNPCVictimCurse(player, victim);
            // TODO npc prayer adjustment
        }

        @Override
        public void initialize(Player player) {
            boostPercentage = (float) player.getPrayerManager().additionalAttackBoostPercentage;
            super.initialize(player);
        }

        @Override
        public void terminate() {
            for (EntityCurseTick t : activeEntities) {
                if (t.entity instanceof Player) {
                    Player p = (Player) t.entity;
                    p.getPrayerManager().initialDrainedAttackPercentage = 0;
                    PrayerManager.updateAdjustments(p);
                }
            }
            super.terminate();
        }

    }

    private class LeechStrengthCounter extends LeechCurseCounter {

        public LeechStrengthCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            victim.getPrayerManager().initialDrainedStrengthPercentage = INITIAL_DRAIN;
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(final Player player, final Player victim) {
            super.executePlayerVictimCurse(player, victim);
            final PrayerManager pManager = player.getPrayerManager();
            final PrayerManager vManager = victim.getPrayerManager();
            player.getCombat().executeAnimation(12575, 0, false, false);
            Static.world.sendProjectile(player, victim, 2248, player.getLocation(), victim.getLocation(), 30, 35, 30, 45, 25, 0, player.getSize());
            int distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = 40 + 20 + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            Tick tick = new Tick(null, (int) hpDrainDelay) {
                @Override
                public boolean execute() {
                    pManager.additionalStrengthBoostPercentage = boostPercentage;
                    vManager.additionalDrainedStrengthPercentage += drainPercentage;
                    if (vManager.additionalDrainedStrengthPercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedStrengthPercentage = OVERTIME_MAX_DRAIN;
                    victim.getCombat().executeGraphics(2250, 0, 0);
                    String pString;
                    String vString;
                    if (pManager.additionalStrengthBoostPercentage >= OVERTIME_MAX_BOOST)
                        pString = "You can't leech any more strength from your victim.";
                    else
                        pString = "You leech some of your victim's strength.";
                    if (vManager.additionalDrainedStrengthPercentage <= OVERTIME_MAX_DRAIN)
                        vString = "Your strength can't be leeched any further by the opponent's curse, his curse is at maximum power";
                    else
                        vString = "Some of your strength has been leeched by a curse.";
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    PrayerManager.updateAdjustments(player);
                    PrayerManager.updateAdjustments(victim);
                    return false;
                }
            };
            player.registerTick(tick);
            if (player.retrieveTick(CURSE_STRENGTH_NORMALIZE) == null) {
                PlayerNormalizeTick nStrength = new PlayerNormalizeTick(CURSE_STRENGTH_NORMALIZE) {
                    @Override
                    public boolean execute() {
                        pManager.additionalStrengthBoostPercentage -= 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (pManager.additionalStrengthBoostPercentage < 0) {
                            pManager.additionalStrengthBoostPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(player);
                        return true;
                    }
                };
                player.registerTick(nStrength);
            }
            if (victim.retrieveTick(CURSE_STRENGTH_RESTORE) == null) {
                PlayerRestoreTick rStrength = new PlayerRestoreTick(CURSE_STRENGTH_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedStrengthPercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedStrengthPercentage > 0) {
                            vManager.additionalDrainedStrengthPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(rStrength);
            }
        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            super.executeNPCVictimCurse(player, victim);
            // TODO npc prayer adjustment
        }

        @Override
        public void initialize(Player player) {
            boostPercentage = (float) player.getPrayerManager().additionalStrengthBoostPercentage;
            super.initialize(player);
        }

        @Override
        public void terminate() {
            for (EntityCurseTick t : activeEntities) {
                if (t.entity instanceof Player) {
                    Player p = (Player) t.entity;
                    p.getPrayerManager().initialDrainedStrengthPercentage = 0;
                    PrayerManager.updateAdjustments(p);
                }
            }
            super.terminate();
        }

    }

    private class LeechDefenceCounter extends LeechCurseCounter {

        public LeechDefenceCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            victim.getPrayerManager().initialDrainedDefencePercentage = INITIAL_DRAIN;
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(final Player player, final Player victim) {
            super.executePlayerVictimCurse(player, victim);
            final PrayerManager pManager = player.getPrayerManager();
            final PrayerManager vManager = victim.getPrayerManager();
            player.getCombat().executeAnimation(12575, 0, false, false);
            Static.world.sendProjectile(player, victim, 2244, player.getLocation(), victim.getLocation(), 30, 35, 30, 45, 25, 0, player.getSize());
            int distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = 40 + 20 + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            Tick tick = new Tick(null, (int) hpDrainDelay) {
                @Override
                public boolean execute() {
                    pManager.additionalDefenceBoostPercentage = boostPercentage;
                    vManager.additionalDrainedDefencePercentage += drainPercentage;
                    if (vManager.additionalDrainedDefencePercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedDefencePercentage = OVERTIME_MAX_DRAIN;
                    victim.getCombat().executeGraphics(2246, 0, 0);
                    String pString;
                    String vString;
                    if (pManager.additionalDefenceBoostPercentage >= OVERTIME_MAX_BOOST)
                        pString = "You can't leech any more defence from your victim.";
                    else
                        pString = "You leech some of your victim's defence.";
                    if (vManager.additionalDrainedStrengthPercentage <= OVERTIME_MAX_DRAIN)
                        vString = "Your defence can't be leeched any further by the opponent's curse, his curse is at maximum power";
                    else
                        vString = "Some of your defence has been leeched by a curse.";
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    PrayerManager.updateAdjustments(player);
                    PrayerManager.updateAdjustments(victim);
                    return false;
                }
            };
            player.registerTick(tick);
            if (player.retrieveTick(CURSE_DEFENCE_NORMALIZE) == null) {
                PlayerNormalizeTick nDefence = new PlayerNormalizeTick(CURSE_DEFENCE_NORMALIZE) {
                    @Override
                    public boolean execute() {
                        pManager.additionalDefenceBoostPercentage -= 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (pManager.additionalDefenceBoostPercentage < 0) {
                            pManager.additionalDefenceBoostPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(player);
                        return true;
                    }
                };
                player.registerTick(nDefence);
            }
            if (victim.retrieveTick(CURSE_DEFENCE_RESTORE) == null) {
                PlayerRestoreTick rDefence = new PlayerRestoreTick(CURSE_DEFENCE_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedDefencePercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedDefencePercentage > 0) {
                            vManager.additionalDrainedDefencePercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(rDefence);
            }
        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            super.executeNPCVictimCurse(player, victim);
            // TODO npc prayer adjustment
        }

        @Override
        public void initialize(Player player) {
            boostPercentage = (float) player.getPrayerManager().additionalDefenceBoostPercentage;
            super.initialize(player);
        }

        @Override
        public void terminate() {
            for (EntityCurseTick t : activeEntities) {
                if (t.entity instanceof Player) {
                    Player p = (Player) t.entity;
                    p.getPrayerManager().initialDrainedDefencePercentage = 0;
                    PrayerManager.updateAdjustments(p);
                }
            }
            super.terminate();
        }

    }

    private class LeechRangedCounter extends LeechCurseCounter {

        public LeechRangedCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            victim.getPrayerManager().initialDrainedRangedPercentage = INITIAL_DRAIN;
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(final Player player, final Player victim) {
            super.executePlayerVictimCurse(player, victim);
            final PrayerManager pManager = player.getPrayerManager();
            final PrayerManager vManager = victim.getPrayerManager();
            player.getCombat().executeAnimation(12575, 0, false, false);
            Static.world.sendProjectile(player, victim, 2236, player.getLocation(), victim.getLocation(), 30, 35, 30, 45, 25, 0, player.getSize());
            int distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = 40 + 20 + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            Tick tick = new Tick(null, (int) hpDrainDelay) {
                @Override
                public boolean execute() {
                    pManager.additionalRangedBoostPercentage = boostPercentage;
                    vManager.additionalDrainedRangedPercentage += drainPercentage;
                    if (vManager.additionalDrainedRangedPercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedRangedPercentage = OVERTIME_MAX_DRAIN;
                    victim.getCombat().executeGraphics(2238, 0, 0);
                    String pString;
                    String vString;
                    if (pManager.additionalRangedBoostPercentage >= OVERTIME_MAX_BOOST)
                        pString = "You can't leech any more ranged from your victim.";
                    else
                        pString = "You leech some of your victim's ranged.";
                    if (vManager.additionalDrainedRangedPercentage <= OVERTIME_MAX_DRAIN)
                        vString = "Your ranged can't be leeched any further by the opponent's curse, his curse is at maximum power";
                    else
                        vString = "Some of your ranged has been leeched by a curse.";
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    PrayerManager.updateAdjustments(player);
                    PrayerManager.updateAdjustments(victim);
                    return false;
                }
            };
            player.registerTick(tick);
            if (player.retrieveTick(CURSE_RANGED_NORMALIZE) == null) {
                PlayerNormalizeTick nRanged = new PlayerNormalizeTick(CURSE_RANGED_NORMALIZE) {
                    @Override
                    public boolean execute() {
                        pManager.additionalRangedBoostPercentage -= 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (pManager.additionalRangedBoostPercentage < 0) {
                            pManager.additionalRangedBoostPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(player);
                        return true;
                    }
                };
                player.registerTick(nRanged);
            }
            if (victim.retrieveTick(CURSE_RANGED_RESTORE) == null) {
                PlayerRestoreTick rRanged = new PlayerRestoreTick(CURSE_RANGED_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedRangedPercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedRangedPercentage > 0) {
                            vManager.additionalDrainedRangedPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(rRanged);
            }
        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            super.executeNPCVictimCurse(player, victim);
            // TODO npc prayer adjustment
        }

        @Override
        public void initialize(Player player) {
            boostPercentage = (float) player.getPrayerManager().additionalRangedBoostPercentage;
            super.initialize(player);
        }

        @Override
        public void terminate() {
            for (EntityCurseTick t : activeEntities) {
                if (t.entity instanceof Player) {
                    Player p = (Player) t.entity;
                    p.getPrayerManager().initialDrainedRangedPercentage = 0;
                    PrayerManager.updateAdjustments(p);
                }
            }
            super.terminate();
        }

    }

    private class LeechMagicCounter extends LeechCurseCounter {

        public LeechMagicCounter(int id) {
            super(id);
        }

        @Override
        public void onHit(Player player, Player victim) {
            victim.getPrayerManager().initialDrainedMagicPercentage = INITIAL_DRAIN;
            super.onHit(player, victim);
        }

        @Override
        protected void executePlayerVictimCurse(final Player player, final Player victim) {
            super.executePlayerVictimCurse(player, victim);
            final PrayerManager pManager = player.getPrayerManager();
            final PrayerManager vManager = victim.getPrayerManager();
            player.getCombat().executeAnimation(12575, 0, false, false);
            Static.world.sendProjectile(player, victim, 2240, player.getLocation(), victim.getLocation(), 30, 35, 30, 45, 25, 0, player.getSize());
            int distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = 40 + 20 + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            Tick tick = new Tick(null, (int) hpDrainDelay) {
                @Override
                public boolean execute() {
                    pManager.additionalMagicBoostPercentage = boostPercentage;
                    vManager.additionalDrainedMagicPercentage += drainPercentage;
                    if (vManager.additionalDrainedMagicPercentage < OVERTIME_MAX_DRAIN)
                        vManager.additionalDrainedMagicPercentage = OVERTIME_MAX_DRAIN;
                    victim.getCombat().executeGraphics(2242, 0, 0);
                    String pString;
                    String vString;
                    if (pManager.additionalMagicBoostPercentage >= OVERTIME_MAX_BOOST)
                        pString = "You can't leech any more magic from your victim.";
                    else
                        pString = "You leech some of your victim's magic.";
                    if (vManager.additionalDrainedMagicPercentage <= OVERTIME_MAX_DRAIN)
                        vString = "Your magic can't be leeched any further by the opponent's curse, his curse is at maximum power";
                    else
                        vString = "Some of your magic has been leeched by a curse.";
                    Static.proto.sendMessage(player, pString);
                    Static.proto.sendMessage(victim, vString);
                    PrayerManager.updateAdjustments(player);
                    PrayerManager.updateAdjustments(victim);
                    return false;
                }
            };
            player.registerTick(tick);
            if (player.retrieveTick(CURSE_MAGIC_NORMALIZE) == null) {
                PlayerNormalizeTick nMagic = new PlayerNormalizeTick(CURSE_MAGIC_NORMALIZE) {
                    @Override
                    public boolean execute() {
                        pManager.additionalMagicBoostPercentage -= 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (pManager.additionalMagicBoostPercentage < 0) {
                            pManager.additionalMagicBoostPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(player);
                        return true;
                    }
                };
                player.registerTick(nMagic);
            }
            if (victim.retrieveTick(CURSE_MAGIC_RESTORE) == null) {
                PlayerRestoreTick rMagic = new PlayerRestoreTick(CURSE_MAGIC_RESTORE) {
                    @Override
                    public boolean execute() {
                        vManager.additionalDrainedMagicPercentage += 0.01f + (r.nextInt(9) == 0 ? 0.01f : 0.0f);
                        if (vManager.additionalDrainedMagicPercentage > 0) {
                            vManager.additionalDrainedMagicPercentage = 0;
                            return false;
                        }
                        PrayerManager.updateAdjustments(victim);
                        return true;
                    }
                };
                victim.registerTick(rMagic);
            }
        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            super.executeNPCVictimCurse(player, victim);
            // TODO npc prayer adjustment
        }

        @Override
        public void initialize(Player player) {
            boostPercentage = (float) player.getPrayerManager().additionalMagicBoostPercentage;
            super.initialize(player);
        }

        @Override
        public void terminate() {
            for (EntityCurseTick t : activeEntities) {
                if (t.entity instanceof Player) {
                    Player p = (Player) t.entity;
                    p.getPrayerManager().initialDrainedMagicPercentage = 0;
                    PrayerManager.updateAdjustments(p);
                }
            }
            super.terminate();
        }

    }

    private class LeechSpecialCounter extends LeechCurseCounter {

        public LeechSpecialCounter(int id) {
            super(id);
        }

        @Override
        protected void executePlayerVictimCurse(final Player player, final Player victim) {
            player.getCombat().executeAnimation(12575, 0, false, false);
            Static.world.sendProjectile(player, victim, 2256, player.getLocation(), victim.getLocation(), 30, 35, 30, 45, 25, 0, player.getSize());
            int distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = 40 + 20 + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            Tick tick = new Tick(null, (int) hpDrainDelay) {
                @Override
                public boolean execute() {
                    player.getCombat().getSpecialEnergy().increase(10);
                    victim.getCombat().getSpecialEnergy().increaseDrain(10);
                    victim.doGraphics(2257);
                    return false;
                }
            };
            player.registerTick(tick);

        }

        @Override
        protected void executeNPCVictimCurse(Player player, NPC victim) {
            // TODO npc prayer adjustment
        }

    }

}
