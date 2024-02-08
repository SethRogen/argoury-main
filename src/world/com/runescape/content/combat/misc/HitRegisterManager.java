package com.runescape.content.combat.misc;

import com.runescape.Static;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.Entity;
import com.runescape.logic.player.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class HitRegisterManager {

    private HashMap<Long, HitRegister> hitRegisters = new HashMap<Long, HitRegister>();

    public void registerHit(Player player, int damage) {
        HitRegister register = hitRegisters.get(player.getIdentifier());
        if (register == null) {
            register = new HitRegister(player);
            registerHitRegister(player);
        }
        register.registerHit(damage);
    }

    private void registerHitRegister(Player player) {
        hitRegisters.put(player.getIdentifier(), new HitRegister(player));
    }

    public HitRegister[] getSortedHitRegisters() {
        HitRegister[] registers = hitRegisters.values().toArray(new HitRegister[0]);
        Arrays.sort(registers);
        return registers;
    }

    public static class HitRegister implements Comparable<HitRegister> {

        public Player player;
        public int totalDamage = 0;
        public LinkedList<Hit> hits = new LinkedList<Hit>();

        public HitRegister(Player player) {
            this.player = player;
        }

        public void registerHit(int damage) {
            hits.add(new Hit(damage, Static.world.getTime()));
            totalDamage += damage;
        }

        public void unregisterHit(Hit hit) {
            hits.remove(hit);
            totalDamage -= hit.damage;
        }

        @Override
        public int compareTo(HitRegister register) {
            return register.totalDamage - totalDamage;
        }

        public void removeExpiredHits() {
            Iterator<Hit> it = hits.iterator();
            int worldTime = Static.world.getTime();
            Hit h;
            while (it.hasNext()) {
                h = it.next();
                if (h.tickTime + 100 < worldTime) {
                    it.remove();
                } else {
                    break;
                }
            }
        }

    }

    public static class Hit {

        public int damage;
        public int tickTime;

        public Hit(int damage, int tickTime) {
            this.damage = damage;
            this.tickTime = tickTime;
        }

    }

    public static class HitRegisterManagerTick extends Tick {

        private Entity entity;

        public HitRegisterManagerTick(Entity entity) {
            super("HitRegisterManager", 15, TickPolicy.PERSISTENT);
            this.entity = entity;
        }

        @Override
        public boolean execute() {
            HitRegisterManager manager = entity.hitRegisterManager;
            Iterator<HitRegister> it = manager.hitRegisters.values().iterator();
            while (it.hasNext()) {
                it.next().removeExpiredHits();
            }
            return true;
        }

    }

}
