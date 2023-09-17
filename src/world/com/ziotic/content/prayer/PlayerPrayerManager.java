package com.ziotic.content.prayer;

import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.Entity;
import com.ziotic.logic.HPHandlerTick.HPAddition;
import com.ziotic.logic.HPHandlerTick.HPRemoval;
import com.ziotic.logic.item.EquipmentDefinition.WeaponStyles;
import com.ziotic.logic.mask.Splat;
import com.ziotic.logic.mask.Splat.SplatCause;
import com.ziotic.logic.mask.Splat.SplatType;
import com.ziotic.logic.mask.SplatNode;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

public class PlayerPrayerManager extends PrayerManager {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logging.log();

    public PlayerPrayerManager(Book book) {
        super(book);
    }

    public PlayerPrayerManager() {
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        load();
        system.registerButtonHandler(new int[]{271, 749}, this);
    }

    @Override
    public void takeHit(Entity owner, Entity enemy, int damage,
                        WeaponStyles type, int splatDelay, int hpDrainDelay) {
        final Player player = (Player) owner;
        if (book == Book.ANCIENT_CURSES) {
            if (deflectMelee && type == WeaponStyles.MELEE) {
                double deflect = Math.round((((double) damage / 6) * 10) * 0.1);
                if (deflect > 0) {
                    SplatNode node = new SplatNode(new Splat(enemy, player, (int) deflect, SplatType.DAMAGE, SplatCause.DEFLECT, false, splatDelay * 2));
                    //enemy.getMasks().submitSplat(node);
                    player.getCombat().executeAnimation(12573, splatDelay, false, false);
                    player.getCombat().executeGraphics(2230, splatDelay, 0);
                    //enemy.addHPHandler(new HPRemoval((int) deflect, 1));
                    enemy.registerHPTick(new HPRemoval(enemy, (int) deflect, 1, node));
                }
            }
            if (deflectMagic && type == WeaponStyles.MAGIC) {
                double deflect = Math.round((((double) damage / 6) * 10) * 0.1);
                if (deflect > 0) {
                    SplatNode node = new SplatNode(new Splat(enemy, player, (int) deflect, SplatType.DAMAGE, SplatCause.DEFLECT, false, splatDelay * 2));
                    //enemy.getMasks().submitSplat(node);
                    player.registerTick(new Tick(null, hpDrainDelay) {
                        @Override
                        public boolean execute() {
                            player.getCombat().executeAnimation(12573, 0, false, false);
                            player.getCombat().executeGraphics(2229, 0, 0);
                            return false;
                        }
                    });
                    //enemy.addHPHandler(new HPRemoval((int) deflect, hpDrainDelay * 2));
                    enemy.registerHPTick(new HPRemoval(enemy, (int) deflect, hpDrainDelay * 2, node));
                }
            }
            if (deflectRanged && type == WeaponStyles.RANGED) {
                double deflect = Math.round((((double) damage / 6) * 10) * 0.1);
                if (deflect > 0) {
                    SplatNode node = new SplatNode(new Splat(enemy, player, (int) deflect, SplatType.DAMAGE, SplatCause.DEFLECT, false, splatDelay * 2));
                    //enemy.getMasks().submitSplat(node);
                    player.registerTick(new Tick(null, hpDrainDelay) {
                        @Override
                        public boolean execute() {
                            player.getCombat().executeAnimation(12573, 0, false, false);
                            player.getCombat().executeGraphics(2229, 0, 0);
                            return false;
                        }
                    });
                    //enemy.addHPHandler(new HPRemoval((int) deflect, hpDrainDelay * 2));
                    enemy.registerHPTick(new HPRemoval(enemy, (int) deflect, hpDrainDelay * 2, node));
                }
            }
            if (enemy instanceof Player) {
                Player victim = (Player) enemy;
                for (int i : LEECH_SAP_IDS) {
                    AncientCurse c = player.getPrayerManager().curses.get(i);
                    if (c.isOn) {
                        c.hit(player, victim);
                    }
                }
                PrayerManager.updateAdjustments(victim);
            }
            PrayerManager.updateAdjustments(player);
        } else if (book == Book.PRAYER) {

        }

    }

    protected static final int[] LEECH_SAP_IDS = new int[]{1, 2, 3, 10, 11, 12, 13, 14, 16};

    @Override
    public void dealHit(Entity owner, Entity enemy, int damage,
                        final WeaponStyles type, int splatDelay, int hpDrainDelay) {
        final Player player = (Player) owner;
        if (book == Book.ANCIENT_CURSES) {
            if (enemy instanceof Player) {
                final Player victim = (Player) enemy;
                for (int i : LEECH_SAP_IDS) {
                    AncientCurse c = player.getPrayerManager().curses.get(i);
                    if (c.isOn) {
                        c.hit(player, victim);
                    }
                }
                if (turmoil) {
                    double tAttack = (double) Math.floor(((double) victim.getLevels().getLevel(Levels.ATTACK) * 0.15d)) / 100;
                    double tStrength = (double) Math.floor(((double) victim.getLevels().getLevel(Levels.STRENGTH) * 0.10d)) / 100;
                    double tDefence = (double) Math.floor(((double) victim.getLevels().getLevel(Levels.DEFENCE) * 0.15d)) / 100;
                    player.getPrayerManager().setTurmoilAttackBoost(tAttack);
                    player.getPrayerManager().setTurmoilStrengthBoost(tStrength);
                    player.getPrayerManager().setTurmoilDefenceBoost(tDefence);
                }
                if (soulSplit) {
                    if (damage > 0) {
                        victim.getCombat().executeGraphics(2264, (int) Math.round((double) splatDelay * 0.3), 0);
                        Static.world.sendProjectile(player, victim, 2263, player.getLocation(), victim.getLocation(), 25, 30, 20, 0, 15, 0, victim.getSize());
                        final double drain = Math.round((double) damage * 0.02);
                        final int hp = (int) Math.round((double) damage * 0.2);
                        Tick tick = new Tick(null, hpDrainDelay - 1 < 0 ? 0 : hpDrainDelay - 1) {
                            @Override
                            public boolean execute() {

                                Static.world.sendProjectile(victim, player, 2263, victim.getLocation(), player.getLocation(), 25, 30, 20, 0, 15, 0, victim.getSize());
                                victim.getLevels().removePrayer(drain);
                                Static.proto.sendLevel(victim, Levels.PRAYER);
                                double delay;
                                if (type == WeaponStyles.MELEE) {
                                    delay = 0;
                                } else {
                                    int distance = victim.getLocation().distance(player.getLocation());
                                    delay = ((20d + (double) distance * 5d) * 12d) / 600d;
                                }
                                Tick t2 = new Tick(null, (int) delay) {
                                    @Override
                                    public boolean execute() {
                                        if (player.getHP() != 0 && !player.isDead()) {
                                            //player.addHPHandler(new HPAddition(hp, 0, 0));
                                            player.registerHPTick(new HPAddition(player, hp, 0, 0, null));
                                        }
                                        return false;
                                    }
                                };
                                player.registerTick(t2);
                                return false;
                            }
                        };
                        player.registerTick(tick);
                    }
                }
                PrayerManager.updateAdjustments(player);
                PrayerManager.updateAdjustments(victim);
            }
        } else if (book == Book.PRAYER) {
            if (enemy instanceof Player) {
                Player victim = (Player) enemy;
                if (smite) {
                    int drain = (int) Math.round((double) damage * 0.025);
                    int newPrayer = victim.getLevels().getCurrentLevel(Levels.PRAYER) - drain < 0 ? 0 : victim.getLevels().getCurrentLevel(Levels.PRAYER) - drain;
                    victim.getLevels().setCurrentLevel(Levels.PRAYER, newPrayer);
                    Static.proto.sendLevel(victim, Levels.PRAYER);
                }
            }
        }
    }

}
