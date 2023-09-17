package com.ziotic.logic.npc;

import com.ziotic.Static;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * @author Lazaro
 */
public class NPCXMLDefinition {
    private static final Logger logger = Logging.log();

    private static Map<Integer, NPCXMLDefinition> xmlDefinitions = null;

    public static void load() throws IOException {
        xmlDefinitions = Static.xml.readObject(Static.parseString("%WORK_DIR%/world/npcData/npcdefs.xml"));
        logger.info("Loaded " + xmlDefinitions.size() + " XML NPC definitions");
    }

    public static Map<Integer, NPCXMLDefinition> getXMLDefinitions() {
        return xmlDefinitions;
    }

    public int hp = -1;
    public String examine = "Nothing interesting.";
    public boolean aggressive = false;
    public int attackSpeed = 5;
    public int attackEmote = -1;
    public int defenceEmote = -1;
    public int deathEmote = -1;
    public int meleeMaxHit = 10;
    public int rangedMaxHit = 10;
    public int magicMaxHit = 10;
    public String loopScript = null;
    public String dialogueScript = null;
    public double[] bonuses = null;
    public int[] levels = null;
    public boolean poisonous = false;

    public static enum AttackType {

        MELEE(true, false, false),
        MELEE_MAGIC(true, true, false),
        MELEE_RANGE(true, false, true),
        MELEE_MAGIC_RANGE(true, true, true),
        MAGIC(false, true, false),
        MAGIC_RANGE(false, true, true),
        RANGE(false, false, true);
        
        public boolean usesMelee;
        public boolean usesMagic;
        public boolean usesRanged;

        private AttackType(boolean melee, boolean magic, boolean ranged) {
            this.usesMelee = melee;
            this.usesMagic = magic;
            this.usesRanged = ranged;
        }

    }
    
    public static enum MeleeStyle {
    	SLASH,
    	CRUSH,
    	STAB
    }

    protected AttackType attackType = null;
    protected MeleeStyle meleeStyle = null;
    protected int aggressiveRange = 0;
    protected boolean uniqueBehaviour = false;
    protected String behaviourScript = null;
    protected boolean attackable = false;
    
}
