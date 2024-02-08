package com.runescape.content.skill.member.summoning;

import com.runescape.Static;
import com.runescape.logic.npc.summoning.Familiar;
import com.runescape.logic.player.Player;
import com.runescape.utility.Logging;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author 'Mystic Flow
 */
public class SummoningPouch {

    private static Map<Integer, SummoningPouch> pouches;

    public static void load() throws IOException {
        pouches = Static.xml.readObject("data/world/npc/familiars.xml");
        Logging.log().info("Loaded " + pouches.size() + " familiar(s)");
    }


    public static SummoningPouch get(int pouchId) {
        return pouches.get(pouchId);
    }

    public static SummoningPouch forInterfaceId(int id) {
        for (SummoningPouch pouch : pouches.values()) {
            if (pouch.interfacePouchId == id) {
                return pouch;
            }
        }
        return null;
    }

    public static Collection<SummoningPouch> getPouches() {
        return pouches.values();
    }

    private int pouchId;
    private int interfacePouchId;
    private double experience;
    private String familiarName;
    private int npcId;
    private int scrollId;
    private int summonCost;
    
    public int getScrollId() {
    	return scrollId;
    }

    public int getPouchId() {
        return pouchId;
    }

    public double getExperience() {
        return experience;
    }

    public String getFamiliarName() {
        return familiarName;
    }

    public int getInterfacePouch() {
        return interfacePouchId;
    }

    public int getNPCId() {
        return npcId;
    }
    
    public int summonCost() {
    	return summonCost;
    }

    public Familiar createFamiliar(Player player) {
        //TODO Check if this can create any bottlenecks ?
        try {
            return (Familiar) Class.forName("com.runescape.logic.npc.summoning.impl." + familiarName).getConstructor(Player.class).newInstance(player);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
