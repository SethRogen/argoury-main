package com.runescape.content.combat;

import java.util.Random;

import com.runescape.content.combat.misc.CombatUtilities;
import com.runescape.content.combat.misc.CombatUtilities.Styles;
import com.runescape.logic.Entity;
import com.runescape.logic.HPHandlerTick.HPRemoval;
import com.runescape.logic.item.EquipmentDefinition.Bonuses;
import com.runescape.logic.item.EquipmentDefinition.WeaponStyles;
import com.runescape.logic.mask.Splat;
import com.runescape.logic.mask.SplatNode;
import com.runescape.logic.mask.Splat.SplatCause;
import com.runescape.logic.mask.Splat.SplatType;
import com.runescape.logic.npc.NPC;
import com.runescape.logic.player.Levels;
import com.runescape.logic.player.Player;

public class Melee {

    private static final Random RANDOM = new Random();

    /**
     * @param victim
     * @param special
     * @param strengthMultiplier
     * @return Whether the opponent has died
     */
    public int doMeleeHit(final Entity entity, final Entity victim, boolean special, double strengthMultiplier, double accuracyMultiplier, int amountOfHits, int[] damages, int[] delays) {
        int hp = victim.getHP();
        int totalDamage = 0;
        if (entity instanceof Player) {
	        for (int i = 0; i < (special ? amountOfHits : 1); i++) {
	            double maxHit = getMaxMeleeHit(entity, special, strengthMultiplier);
	            double hitChance = 0;
	            SplatType damageSplat = SplatType.DAMAGE;
	            SplatType absorbSplat = null;
	            if (victim instanceof Player) {
	                Player player = (Player) entity;
	                Player victim_ = (Player) victim;
	                hitChance = Combat.getAccuracy(entity, victim, accuracyMultiplier);
	                if (hitChance <= RANDOM.nextDouble()) {
	                    maxHit = 0;
	                    damageSplat = SplatType.BLOCK;
	                } else {
	                    if ((victim_.getPrayerManager().protectMelee() || victim_.getPrayerManager().deflectMelee()) && !CombatUtilities.wearsArmourSet(player, CombatUtilities.VERACS))
	                        maxHit *= 0.6;
	                }
	                double hitMultiplier = Combat.getHitMultiplier(hitChance);
	                int damage = maxHit > 0 ? (damages == null ? ((int) (maxHit * hitMultiplier)) : damages[i]) : 0;
	                if (damage > maxHit)
	                	damage = (int) maxHit;
	                int damageExcess = 0;
	                double absorbAmount = 0;
	                if (damage > 200) {
	                    damageExcess = damage - 200;
	                }
	                absorbAmount = damageExcess * (victim_.getBonuses()[Bonuses.ABSORB_MELEE] / 100);
	                if (absorbAmount > 0) {
	                    absorbSplat = SplatType.SOAK;
	                    absorbAmount = Math.ceil(absorbAmount);
	                    damage -= absorbAmount;
	                }
	                boolean dead = false;
	                if (damage < 1 && maxHit != 0)
	                    damage = 1;
	                if (damage > hp) {
	                    damage = hp;
	                    dead = true;
	                }
	                if (dead) {
	                    damage += absorbAmount;
	                    if (damage > hp)
	                        damage = hp;
	                }
	                hp -= damage;
	                if (damage == 0) {
	                    damageSplat = SplatType.BLOCK;
	                }
	                SplatNode node;
	                final int damage_ = damage;
	                final boolean criticalHit = damage + (0.03 * damage) >= maxHit - absorbAmount;
	                int delay = delays[i];
	                if (absorbSplat != null) {
	                    node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MELEE, criticalHit, 0), new Splat(victim, entity, (int) absorbAmount, absorbSplat, SplatCause.NONE, false, 0));
	                } else
	                    node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MELEE, criticalHit, 0));
	                victim.registerHPTick(new HPRemoval(victim, damage_, 1/*Combat.getHPDrainDelay(delays[i])*/, node));
	                totalDamage += damage_;
	                player.getPrayerManager().dealHit(player, victim_, damage_, WeaponStyles.MELEE, delay, 0);
	                victim_.getPrayerManager().takeHit(victim_, player, damage_, WeaponStyles.MELEE, delay, 0);
	            } else {
	            	Player player = (Player) entity;
	            	NPC victim_ = (NPC) victim;
	                hitChance = Combat.getAccuracy(entity, victim, accuracyMultiplier);
	                if (hitChance <= RANDOM.nextDouble()) {
	                    maxHit = 0;
	                    damageSplat = SplatType.BLOCK;
	                } else {
//	                    if ((victim_.getPrayerManager().protectMelee() || victim_.getPrayerManager().deflectMelee()) && !CombatUtilities.wearsArmourSet(player, CombatUtilities.VERACS))
//	                        maxHit *= 0.6; TODO prayers for npc's
	                }
	                double hitMultiplier = Combat.getHitMultiplier(hitChance);
	                int damage = maxHit > 0 ? (damages == null ? ((int) (maxHit * hitMultiplier)) : damages[i]) : 0;
	                if (damage > maxHit)
	                	damage = (int) maxHit;
	                int damageExcess = 0;
	                double absorbAmount = 0;
	                if (damage > 200) {
	                    damageExcess = damage - 200;
	                }
	                absorbAmount = damageExcess * (victim_.getBonuses()[Bonuses.ABSORB_MELEE] / 100);
	                if (absorbAmount > 0) {
	                    absorbSplat = SplatType.SOAK;
	                    absorbAmount = Math.ceil(absorbAmount);
	                    damage -= absorbAmount;
	                }
	                boolean dead = false;
	                if (damage < 1 && maxHit != 0)
	                    damage = 1;
	                if (damage > hp) {
	                    damage = hp;
	                    dead = true;
	                }
	                if (dead) {
	                    damage += absorbAmount;
	                    if (damage > hp)
	                        damage = hp;
	                }
	                hp -= damage;
	                if (damage == 0) {
	                    damageSplat = SplatType.BLOCK;
	                }
	                SplatNode node;
	                final int damage_ = damage;
	                final boolean criticalHit = damage + (0.03 * damage) >= maxHit - absorbAmount;
	                int delay = delays[i];
	                if (absorbSplat != null) {
	                    node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MELEE, criticalHit, 0), new Splat(victim, entity, (int) absorbAmount, absorbSplat, SplatCause.NONE, false, 0));
	                } else
	                    node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MELEE, criticalHit, 0));
	                victim.registerHPTick(new HPRemoval(victim, damage_, 1/*Combat.getHPDrainDelay(delays[i])*/, node));
	                totalDamage += damage_;
	                player.getPrayerManager().dealHit(player, victim_, damage_, WeaponStyles.MELEE, delay, 0);
	//                victim_.getPrayerManager().takeHit(victim_, player, damage_, WeaponStyles.MELEE, delay, 0); // TODO prayer stuff for npc's
	            }
	        }
        } else {
        	NPC npc = (NPC) entity;
        	double maxHit = npc.getDefinition().meleeMaxHit;
        	maxHit *= strengthMultiplier;
        	double hitChance = Combat.getAccuracy(npc, victim, accuracyMultiplier);
        	SplatType damageSplat = SplatType.DAMAGE;
            SplatType absorbSplat = null;
            boolean victimIsPlayer = victim instanceof Player;
        	if (hitChance <= RANDOM.nextDouble()) {
                maxHit = 0;
                damageSplat = SplatType.BLOCK;
            } else if (victimIsPlayer) {
                if ((((Player)victim).getPrayerManager().protectMelee() || ((Player) victim).getPrayerManager().deflectMelee()))
                    maxHit *= 0.6;
            }
            double hitMultiplier = Combat.getHitMultiplier(hitChance);
            int damage = maxHit > 0 ? (damages == null ? ((int) (maxHit * hitMultiplier)) : damages[0]) : 0;
            if (damage > maxHit)
            	damage = (int) maxHit;
            int damageExcess = 0;
            double absorbAmount = 0;
            if (damage > 200) {
                damageExcess = damage - 200;
            }
            absorbAmount = damageExcess * (victim.getBonuses()[Bonuses.ABSORB_MELEE] / 100);
            if (absorbAmount > 0) {
                absorbSplat = SplatType.SOAK;
                absorbAmount = Math.ceil(absorbAmount);
                damage -= absorbAmount;
            }
            boolean dead = false;
            if (damage < 1 && maxHit != 0)
                damage = 1;
            if (damage > hp) {
                damage = hp;
                dead = true;
            }
            if (dead) {
                damage += absorbAmount;
                if (damage > hp)
                    damage = hp;
            }
            hp -= damage;
            if (damage == 0) {
                damageSplat = SplatType.BLOCK;
            }
            SplatNode node;
            final int damage_ = damage;
            final boolean criticalHit = damage + (0.03 * damage) >= maxHit - absorbAmount;
            int delay = delays[0];
            if (absorbSplat != null) {
                node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MELEE, criticalHit, 0), new Splat(victim, entity, (int) absorbAmount, absorbSplat, SplatCause.NONE, false, 0));
            } else
                node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MELEE, criticalHit, 0));
            victim.registerHPTick(new HPRemoval(victim, damage_, 1/*Combat.getHPDrainDelay(delays[i])*/, node));
            totalDamage += damage_;
            if (victimIsPlayer)
            	((Player) victim).getPrayerManager().takeHit(victim, entity, damage_, WeaponStyles.MELEE, delay, 0);
        }
        return totalDamage;
    }

    public int getMaxMeleeHit(Entity entity, boolean special, double specialBonus) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            double otherBonus = getSpecialStrengthBonus(player);
            double effectiveStrength = (int) ((double) player.getLevels().getCurrentLevel(Levels.STRENGTH)
                    * player.getPrayerManager().getStrengthMultiplier() * otherBonus) + (player.getCombat().weapon.style == Styles.STYLE_AGGRESSIVE ? 3 : player.getCombat().weapon.style == Styles.STYLE_CONTROLLED ? 1 : 0); // + (agressive ? 3 : controlled ? 1 : 0);
            double baseDamage = 5 + (((effectiveStrength + 8) * ((double) player.getBonuses()[Bonuses.OFFENSIVE_STRENGTH] + 64)) / 64);
            double maxHit;
            if (special)
                maxHit = (int) Math.floor(baseDamage * specialBonus);
            else
                maxHit = (int) baseDamage;
            return (int) maxHit;
        }
        return 0;
    }

    public double getSpecialStrengthBonus(Player player) {
        if (CombatUtilities.wearsArmourSet(player, CombatUtilities.DHAROKS)) {
            int lp = player.getLevels().getLevel(Levels.CONSTITUTION) * 10;
            int hp = player.getHP();
            int difference = lp - hp;
            return 1 + (difference * 0.001d);
        }
        return 1;
    }

}
