package com.runescape.logic.npc;

import com.runescape.adapter.protocol.cache.format.NPCDefinitionAdapter;
import com.runescape.logic.dialogue.Conversation;
import com.runescape.logic.dialogue.Dialogue;
import com.runescape.logic.npc.NPCXMLDefinition.AttackType;
import com.runescape.logic.npc.NPCXMLDefinition.MeleeStyle;
import com.runescape.utility.Logging;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public final class NPCDefinition {
    private static final Logger logger = Logging.log();

    private static final double[] DEFAULT_BONUSES = new double[18];
    private static final int[] DEFAULT_LEVELS = new int[] { 1, 1, 1, 1, 1, 1, 1 };

    private static Map<Integer, NPCDefinition> cachedDefinitions = new HashMap<Integer, NPCDefinition>();

    public static NPCDefinition forId(int id) {
        synchronized (cachedDefinitions) {
        	try {
	            NPCDefinition def = cachedDefinitions.get(id);
	            if (def == null) {
	                try {
	                    def = NPCDefinitionAdapter.forId(id);
	                } catch (Exception e) {
	                    logger.error("Failed to load NPC definition for id : " + id/*, e*/);
	                    def = new NPCDefinition();
	                }
	
	                NPCXMLDefinition xmlDef = NPCXMLDefinition.getXMLDefinitions().get(id);
	                if (xmlDef != null) {
	                    def.hp = xmlDef.hp;
	                    def.examine = xmlDef.examine;
	                    def.aggressive = xmlDef.aggressive;
	                    def.attackSpeed = xmlDef.attackSpeed;
	                    def.attackEmote = xmlDef.attackEmote;
	                    def.defenceEmote = xmlDef.defenceEmote;
	                    def.deathEmote = xmlDef.deathEmote;
	                    def.meleeMaxHit = xmlDef.meleeMaxHit;
	                    def.rangedMaxHit = xmlDef.rangedMaxHit;
	                    def.magicMaxHit = xmlDef.magicMaxHit;
	                    def.loopScript = xmlDef.loopScript;
	                    def.bonuses = xmlDef.bonuses;
	                    if (def.bonuses == null) {
	                        def.bonuses = DEFAULT_BONUSES;
	                    }
	                    def.levels = xmlDef.levels;
	                    if (def.levels == null) {
	                    	def.levels = DEFAULT_LEVELS;
	                    }
	                    def.poisonous = xmlDef.poisonous;
	                    def.attackType = xmlDef.attackType;
	                    if (def.attackType == null)
	                    	def.attackType = AttackType.MELEE;
	                    def.meleeStyle = xmlDef.meleeStyle;
	                    if (def.meleeStyle == null)
	                    	def.meleeStyle = MeleeStyle.SLASH;
	                    def.aggressiveRange = xmlDef.aggressiveRange;
	                    def.uniqueBehaviour = xmlDef.uniqueBehaviour;
	                    def.behaviourScript = xmlDef.behaviourScript;
	                    def.attackable = xmlDef.attackable;
	                    if (def.behaviourScript == null) {
	                    	def.behaviourScript = "default";
	                    }
	                    if (xmlDef.dialogueScript != null) {
	                        def.dialogue = Conversation.loadDialogue(xmlDef.dialogueScript);
	                    }
	                }
	
	                cachedDefinitions.put(id, def);
	            }
	            return def;
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	return null;
        }
    }

    public int id;

    public String name;
    public int combatLevel;
    public String[] options;
    public int renderId;
    public int size;
    public int[] modelIds;
    public short[] modifiedModelColors;
    public short[] originalModelColors;

    public boolean aBoolean3169;
    public boolean aBoolean3172;
    public boolean aBoolean3187;
    public boolean aBoolean3190;
    public boolean aBoolean3191;
    public boolean aBoolean3196;
    public boolean aBoolean3210;
    public boolean aBoolean3221;
    public byte aByte3165;
    public byte aByte3174;
    public byte aByte3175;
    public byte aByte3193;
    public byte aByte3194;
    public byte aByte3207;
    public byte aByte3215;
    public byte aByte3225;
    public byte aByte3233;
    public byte[] aByteArray3205;
    public int anInt3162;
    public int anInt3164 = -1;
    public int anInt3167;
    public int anInt3171;
    public int anInt3173;
    public int anInt3178 = -1;
    public int anInt3179;
    public int anInt3181;
    public int anInt3182;
    public int anInt3184;
    public int anInt3185;
    public int anInt3189;
    public int anInt3200;
    public int anInt3203;
    public int anInt3208;
    public int anInt3209;
    public int anInt3212;
    public int anInt3214;
    public int anInt3216;
    public int anInt3222;
    public int anInt3223;
    public int anInt3226;
    public int anInt3227;
    public int anInt3228;
    public int anInt3235;
    public int[] anIntArray3192;
    public int[] anIntArray3202;
    public int[] anIntArray3219;
    public int[][] anIntArrayArray3211;
    public int[][] anIntArrayArray3220;
    public short aShort3213;
    public short aShort3237;
    public short[] aShortArray3183;
    public short[] aShortArray3204;

    public int hp = 100;
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
    public Map<Integer, Dialogue> dialogue = null;
    public double[] bonuses = null;
    public int[] levels = null;
    public boolean poisonous = false;
    
    public AttackType attackType = null;
    public MeleeStyle meleeStyle = null;
    public int aggressiveRange = 0;
    public boolean uniqueBehaviour = false;
    public String behaviourScript = null;
    public boolean attackable = false;

    public boolean corrupted = false;

    public NPCDefinition() {
        this.aBoolean3172 = true;
        this.options = new String[5];
        this.aBoolean3169 = true;
        this.anInt3185 = 0;
        this.anInt3162 = 0;
        this.aBoolean3196 = false;
        this.anInt3200 = -1;
        this.anInt3173 = -1;
        this.anInt3167 = -1;
        this.anInt3179 = -1;
        this.renderId = -1;
        this.aByte3193 = (byte) -16;
        this.anInt3184 = 0;
        this.anInt3214 = -1;
        this.aBoolean3210 = false;
        this.aByte3207 = (byte) 0;
        this.aByte3215 = (byte) -96;
        this.anInt3181 = -1;
        this.anInt3208 = -1;
        this.anInt3216 = 255;
        this.aBoolean3191 = true;
        this.anInt3212 = -1;
        this.anInt3226 = -1;
        this.anInt3203 = -1;
        this.anInt3228 = 128;
        this.anInt3189 = 128;
        this.aBoolean3187 = false;
        this.anInt3227 = -1;
        this.anInt3223 = -1;
        this.aByte3225 = (byte) 4;
        this.anInt3171 = -1;
        this.combatLevel = -1;
        this.aByte3194 = (byte) 0;
        this.aBoolean3221 = true;
        this.anInt3222 = -1;
        this.anInt3235 = 32;
        this.aShort3213 = (short) 0;
        this.aByte3233 = (byte) -1;
        this.name = "null";
        this.anInt3182 = -1;
        this.size = 1;
        this.aShort3237 = (short) 0;
    }

    public boolean isAttackable() {
        return hp != -1;
    }
}
