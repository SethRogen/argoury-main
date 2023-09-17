package com.ziotic.content.prayer;

import com.ziotic.Static;
import com.ziotic.content.prayer.definitions.AbstractPrayerDefinition;
import com.ziotic.content.prayer.definitions.AncientCurseDefinition;
import com.ziotic.content.prayer.definitions.PrayerDefinition;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.item.EquipmentDefinition.Bonuses;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;

public abstract class AbstractPrayer {

    public boolean isOn = false;
    protected AbstractPrayerDefinition definition;

    protected Tick drainTick;

    public AbstractPrayer(AbstractPrayerDefinition definition) {
        this.definition = definition;
    }

    protected void switchStatus(final Player player, boolean turnOn) {
        if (turnOn && !isOn) {
            isOn = true;
            drainTick = new Tick(null) {
                @Override
                public boolean execute() {
                    final double drain = (1 / definition.getDrainrate()) * 0.6 * (Math.pow(0.965, player.getBonuses()[Bonuses.PRAYER]));
                    double newPrayer = player.getLevels().getCurrentPrayer() - drain < 0 ? 0 : player.getLevels().getCurrentPrayer() - drain;
                    player.getLevels().setCurrentPrayer(newPrayer);
                    Static.proto.sendLevel(player, Levels.PRAYER);
                    if (newPrayer == 0) {
                        PrayerManager.resetPrayers(player);
                        Static.proto.sendMessage(player, "You have run out of player points. You can recharge your prayer at an altar.");
                    }
                    return isOn;
                }
            };
            player.registerTick(drainTick);
            if (this instanceof AncientCurse) {
                AncientCurseDefinition def = (AncientCurseDefinition) definition;
                player.getPrayerManager().addStrengthMultiplier(definition.getStrengthMultiplier());
                player.getPrayerManager().addAttackMultiplier(definition.getAttackMultiplier());
                player.getPrayerManager().addDefenceMultiplier(definition.getDefenceMultiplier());
                player.getPrayerManager().addMagicMultiplier(definition.getMagicMultiplier());
                player.getPrayerManager().addRangedMultiplier(definition.getRangedMultiplier());
                player.getPrayerManager().deflectMelee(def.isDeflectMelee());
                player.getPrayerManager().deflectMagic(def.isDeflectMagic());
                player.getPrayerManager().deflectRanged(def.isDeflectRanged());
                player.getItemsOnDeathManager().itemProtect(def.isProtectItem());
                if (def.isBerserker()) {
                    player.getPrayerManager().berserker(def.isBerserker());
                    Tick tick = player.retrieveTick("level_normalize");
                    tick.setInterval(115);
                    tick.setCounter((int) (tick.getCounter() * 0.15));
                }
                player.getPrayerManager().wrath(def.isWrath());
                if (def.isSoulSplit())
                    player.getPrayerManager().soulSplit(true);
                if (def.isTurmoil()) {
                    player.getPrayerManager().turmoil(true);
                    player.getPrayerManager().additionalAttackBoostPercentage = 0;
                    player.getPrayerManager().additionalStrengthBoostPercentage = 0;
                    player.getPrayerManager().additionalDefenceBoostPercentage = 0;
                }
                switch (def.getId()) {
                    case 1:
                    case 2:
                    case 3:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 16:
                        player.getPrayerManager().curses.get(def.getId()).initializeCurseCounter(player);
                        break;
                }

            } else if (this instanceof Prayer) {
                PrayerDefinition def = (PrayerDefinition) definition;
                player.getPrayerManager().addStrengthMultiplier(definition.getStrengthMultiplier());
                player.getPrayerManager().addAttackMultiplier(definition.getAttackMultiplier());
                player.getPrayerManager().addDefenceMultiplier(definition.getDefenceMultiplier());
                player.getPrayerManager().addMagicMultiplier(definition.getMagicMultiplier());
                player.getPrayerManager().addRangedMultiplier(definition.getRangedMultiplier());
                player.getPrayerManager().protectMelee(def.isProtectFromMelee());
                player.getPrayerManager().protectMagic(def.isProtectFromMagic());
                player.getPrayerManager().protectRanged(def.isProtectFromRanged());
                player.getItemsOnDeathManager().itemProtect(def.isProtectItem());
                if (def.isRapidRestore()) {
                    player.getPrayerManager().rapidRestore(def.isRapidRestore());
                    Tick tick = player.retrieveTick("level_restore");
                    int counter = tick.getCounter();
                    if (counter - tick.interval() / 2 <= 0)
                        tick.setCounter(0);
                    else
                        tick.setCounter(counter - tick.interval() / 2);
                    tick.setInterval(tick.interval() / 2);
                }
                if (def.isRapidHeal()) {
                    player.getPrayerManager().rapidHeal(def.isRapidHeal());
                    Tick tick = player.retrieveTick("hp_restore");
                    int counter = tick.getCounter();
                    if (counter - tick.interval() / 2 <= 0) {
                        tick.setCounter(0);
                    }
                    tick.setInterval(tick.interval() / 2);
                }
                if (def.isRapidRenewal()) {
                    player.getPrayerManager().rapidRenewal(def.isRapidRenewal());
                    Tick tick = player.retrieveTick("hp_restore");
                    int counter = tick.getCounter();
                    if (counter - tick.interval() / 5 <= 0) {
                        tick.setCounter(0);
                    }
                    tick.setInterval(tick.interval() / 5);
                }
                player.getPrayerManager().retribution(def.isRedemption());
                player.getPrayerManager().redemption(def.isRedemption());
                player.getPrayerManager().smite(def.isSmite());

            }
        } else {
            isOn = false;
            if (drainTick != null) {
                drainTick.stop();
            }
            drainTick = null;
            if (this instanceof AncientCurse) {
                AncientCurseDefinition def = (AncientCurseDefinition) definition;
                player.getPrayerManager().addStrengthMultiplier(-definition.getStrengthMultiplier());
                player.getPrayerManager().addAttackMultiplier(-definition.getAttackMultiplier());
                player.getPrayerManager().addDefenceMultiplier(-definition.getDefenceMultiplier());
                player.getPrayerManager().addMagicMultiplier(-definition.getMagicMultiplier());
                player.getPrayerManager().addRangedMultiplier(-definition.getRangedMultiplier());
                if (def.isDeflectMelee())
                    player.getPrayerManager().deflectMelee(false);
                if (def.isDeflectMagic())
                    player.getPrayerManager().deflectMagic(false);
                if (def.isDeflectRanged())
                    player.getPrayerManager().deflectRanged(false);
                if (def.isProtectItem())
                    player.getItemsOnDeathManager().itemProtect(false);
                if (def.isBerserker()) {
                    player.getPrayerManager().berserker(false);
                    Tick tick = player.retrieveTick("level_normalize");
                    tick.setInterval(100);
                }
                if (def.isWrath())
                    player.getPrayerManager().wrath(false);
                if (def.isSoulSplit())
                    player.getPrayerManager().soulSplit(false);
                if (def.isTurmoil()) {
                    player.getPrayerManager().turmoil(false);
                    player.getPrayerManager().turmoilAttackBoost = 0;
                    player.getPrayerManager().turmoilStrengthBoost = 0;
                    player.getPrayerManager().turmoilDefenceBoost = 0;
                }
                switch (def.getId()) {
                    case 1:
                    case 2:
                    case 3:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 16:
                        player.getPrayerManager().curses.get(def.getId()).terminateCurseCounter();
                        break;
                }
            } else if (this instanceof Prayer) {
                PrayerDefinition def = (PrayerDefinition) definition;
                player.getPrayerManager().addStrengthMultiplier(-definition.getStrengthMultiplier());
                player.getPrayerManager().addAttackMultiplier(-definition.getAttackMultiplier());
                player.getPrayerManager().addDefenceMultiplier(-definition.getDefenceMultiplier());
                player.getPrayerManager().addMagicMultiplier(-definition.getMagicMultiplier());
                player.getPrayerManager().addRangedMultiplier(-definition.getRangedMultiplier());
                if (def.isProtectFromMelee())
                    player.getPrayerManager().protectMelee(false);
                if (def.isProtectFromMagic())
                    player.getPrayerManager().protectMagic(false);
                if (def.isProtectFromRanged())
                    player.getPrayerManager().protectRanged(false);
                if (def.isProtectItem())
                    player.getItemsOnDeathManager().itemProtect(false);
                if (def.isRapidRestore()) {
                    player.getPrayerManager().rapidRestore(false);
                    Tick tick = player.retrieveTick("level_restore");
                    tick.setInterval(tick.interval() * 2);
                }
                if (def.isRapidHeal()) {
                    player.getPrayerManager().rapidHeal(false);
                    Tick tick = player.retrieveTick("hp_restore");
                    tick.setInterval(tick.interval() * 2);
                }
                if (def.isRapidRenewal()) {
                    player.getPrayerManager().rapidRenewal(false);
                    Tick tick = player.retrieveTick("hp_restore");
                    tick.setInterval(tick.interval() * 5);
                }
                if (def.isRedemption())
                    player.getPrayerManager().redemption(false);
                if (def.isRetribution())
                    player.getPrayerManager().retribution(false);
                if (def.isSmite())
                    player.getPrayerManager().smite(false);
            }
        }
    }
}
