package com.ziotic.adapter.protocol.cache.format;

import com.ziotic.Static;
import com.ziotic.logic.npc.NPCDefinition;
import com.ziotic.utility.Streams;

import java.nio.ByteBuffer;

/**
 * @author Lazaro
 */
public class NPCDefinitionAdapter {
    public static NPCDefinition forId(int id) {
        NPCDefinition def = new NPCDefinition();
        def.id = id;

        byte[] data = Static.rs2Cache.getIndex(18).getArchivedFile(id >>> 7, id & 0x7f);
        load(def, ByteBuffer.wrap(data));

        return def;
    }

    private static void load(NPCDefinition def, ByteBuffer buffer) {
        int opcode;
        while ((opcode = (buffer.get() & 0xff)) != 0) {
            parseOpcode(def, opcode, buffer);
        }
    }

    @SuppressWarnings("unused")
    private static void parseOpcode(NPCDefinition def, int opcode, ByteBuffer data) {
        if (opcode != 1) {
            if (opcode == 2)
                def.name = Streams.readString(data);
            else if (opcode != 12) {
                if (opcode < 30 || opcode >= 35) {
                    if (opcode == 40) {
                        int i_1_ = data.get() & 0xff;
                        def.modifiedModelColors = new short[i_1_];
                        def.originalModelColors = new short[i_1_];
                        for (int i_2_ = 0; i_2_ < i_1_; i_2_++) {
                            def.originalModelColors[i_2_] = data.getShort();
                            def.modifiedModelColors[i_2_] = data.getShort();
                        }
                    } else if (opcode != 41) {
                        if (opcode != 42) {
                            if (opcode != 60) {
                                if (opcode == 93)
                                    def.aBoolean3191 = false;
                                else if (opcode == 95)
                                    def.combatLevel = data.getShort() & 0xffff;
                                else if (opcode != 97) {
                                    if (opcode != 98) {
                                        if (opcode == 99)
                                            def.aBoolean3210 = true;
                                        else if (opcode == 100)
                                            def.anInt3162 = data.get();
                                        else if (opcode == 101)
                                            def.anInt3185 = (data.get());
                                        else if (opcode == 102)
                                            def.anInt3222 = data.getShort() & 0xffff;
                                        else if (opcode == 103)
                                            def.anInt3235 = data.getShort() & 0xffff;
                                        else if (opcode != 106 && opcode != 118) {
                                            if (opcode != 107) {
                                                if (opcode == 109)
                                                    def.aBoolean3169 = false;
                                                else if (opcode != 111) {
                                                    if (opcode == 113) {
                                                        def.aShort3213 = data.getShort();
                                                        def.aShort3237 = data.getShort();
                                                    } else if (opcode == 114) {
                                                        def.aByte3215 = (data.get());
                                                        def.aByte3193 = (data.get());
                                                    } else if (opcode != 119) {
                                                        if (opcode != 121) {
                                                            if (opcode != 122) {
                                                                if (opcode == 123)
                                                                    def.anInt3203 = data.getShort() & 0xffff;
                                                                else if (opcode != 125) {
                                                                    if (opcode == 127) {
                                                                        def.renderId = data.getShort() & 0xffff;
                                                                    } else if (opcode != 128) {
                                                                        if (opcode == 134) {
                                                                            def.anInt3173 = data.getShort() & 0xffff;
                                                                            if (def.anInt3173 == 65535)
                                                                                def.anInt3173 = -1;
                                                                            def.anInt3212 = data.getShort() & 0xffff;
                                                                            if (def.anInt3212 == 65535)
                                                                                def.anInt3212 = -1;
                                                                            def.anInt3226 = data.getShort() & 0xffff;
                                                                            if (def.anInt3226 == 65535)
                                                                                def.anInt3226 = -1;
                                                                            def.anInt3179 = data.getShort() & 0xffff;
                                                                            if (def.anInt3179 == 65535)
                                                                                def.anInt3179 = -1;
                                                                            def.anInt3184 = data.get() & 0xff;
                                                                        } else if (opcode == 135) {
                                                                            def.anInt3214 = data.get() & 0xff;
                                                                            def.anInt3178 = data.getShort() & 0xffff;
                                                                        } else if (opcode != 136) {
                                                                            if (opcode == 137) {
                                                                                def.anInt3223 = data.getShort() & 0xffff;
                                                                            } else if (opcode == 138)
                                                                                def.anInt3167 = data.getShort() & 0xffff;
                                                                            else if (opcode != 139) {
                                                                                if (opcode == 140)
                                                                                    def.anInt3216 = data.get() & 0xff;
                                                                                else if (opcode == 141)
                                                                                    def.aBoolean3187 = true;
                                                                                else if (opcode == 142)
                                                                                    def.anInt3200 = data.getShort() & 0xffff;
                                                                                else if (opcode != 143) {
                                                                                    if (opcode < 150 || opcode >= 155) {
                                                                                        if (opcode == 155) {
                                                                                            def.aByte3165 = data.get();
                                                                                            def.aByte3175 = data.get();
                                                                                            def.aByte3174 = data.get();
                                                                                            def.aByte3194 = data.get();
                                                                                        } else if (opcode != 158) {
                                                                                            if (opcode == 159)
                                                                                                def.aByte3233 = (byte) 0;
                                                                                            else if (opcode != 160) {
                                                                                                if (opcode != 161) {
                                                                                                    if (opcode == 249) {
                                                                                                        int len = (data.get() & 0xff);
                                                                                                        for (int i = 0; i < len; i++) {
                                                                                                            boolean string = (data.get() & 0xff) == 1;
                                                                                                            int opcode2 = (data.get() & 0xff) << 16 | (data.get() & 0xff) << 8 | (data.get() & 0xff);
                                                                                                            if (!string) {
                                                                                                                int val = data.getInt();
                                                                                                            } else {
                                                                                                                String val = Streams.readString(data);
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                } else
                                                                                                    def.aBoolean3190 = true;
                                                                                            } else {
                                                                                                int i_7_ = data.get() & 0xff;
                                                                                                def.anIntArray3219 = new int[i_7_];
                                                                                                for (int i_8_ = 0; i_7_ > i_8_; i_8_++)
                                                                                                    def.anIntArray3219[i_8_] = data.getShort() & 0xffff;
                                                                                            }
                                                                                        } else
                                                                                            def.aByte3233 = (byte) 1;
                                                                                    } else {
                                                                                        def.options[opcode - 150] = Streams.readString(data);
                                                                                    }
                                                                                } else
                                                                                    def.aBoolean3196 = true;
                                                                            } else
                                                                                def.anInt3164 = data.getShort() & 0xffff;
                                                                        } else {
                                                                            def.anInt3181 = data.get() & 0xff;
                                                                            def.anInt3227 = data.getShort() & 0xffff;
                                                                        }
                                                                    } else
                                                                        data.get();
                                                                } else
                                                                    def.aByte3225 = data.get();
                                                            } else
                                                                def.anInt3182 = data.getShort() & 0xffff;
                                                        } else {
                                                            def.anIntArrayArray3211 = (new int[def.modelIds.length][]);
                                                            int i_9_ = data.get() & 0xff;
                                                            for (int i_10_ = 0; (i_9_ > i_10_); i_10_++) {
                                                                int i_11_ = data.get() & 0xff;
                                                                int[] is = (def.anIntArrayArray3211[i_11_] = new int[3]);
                                                                is[0] = data.get();
                                                                is[1] = data.get();
                                                                is[2] = data.get();
                                                            }
                                                        }
                                                    } else
                                                        def.aByte3207 = data.get();
                                                } else
                                                    def.aBoolean3172 = false;
                                            } else
                                                def.aBoolean3221 = false;
                                        } else {
                                            def.anInt3171 = data.getShort() & 0xffff;
                                            if (def.anInt3171 == 65535)
                                                def.anInt3171 = -1;
                                            def.anInt3208 = data.getShort() & 0xffff;
                                            if (def.anInt3208 == 65535)
                                                def.anInt3208 = -1;
                                            int i_12_ = -1;
                                            if (opcode == 118) {
                                                i_12_ = data.getShort() & 0xffff;
                                                if (i_12_ == 65535)
                                                    i_12_ = -1;
                                            }
                                            int i_13_ = data.get() & 0xff;
                                            def.anIntArray3202 = new int[2 + i_13_];
                                            for (int i_14_ = 0; i_14_ <= i_13_; i_14_++) {
                                                def.anIntArray3202[i_14_] = data.getShort() & 0xffff;
                                                if ((def.anIntArray3202[i_14_]) == 65535)
                                                    def.anIntArray3202[i_14_] = -1;
                                            }
                                            def.anIntArray3202[i_13_ + 1] = i_12_;
                                        }
                                    } else
                                        def.anInt3189 = data.getShort() & 0xffff;
                                } else
                                    def.anInt3228 = data.getShort() & 0xffff;
                            } else {
                                int i_15_ = data.get() & 0xff;
                                def.anIntArray3192 = new int[i_15_];
                                for (int i_16_ = 0; i_16_ < i_15_; i_16_++)
                                    def.anIntArray3192[i_16_] = data.getShort() & 0xffff;
                            }
                        } else {
                            int i_17_ = data.get() & 0xff;
                            def.aByteArray3205 = new byte[i_17_];
                            for (int i_18_ = 0; i_18_ < i_17_; i_18_++)
                                def.aByteArray3205[i_18_] = data.get();
                        }
                    } else {
                        int i_19_ = data.get() & 0xff;
                        def.aShortArray3183 = new short[i_19_];
                        def.aShortArray3204 = new short[i_19_];
                        for (int i_20_ = 0; i_20_ < i_19_; i_20_++) {
                            def.aShortArray3183[i_20_] = data.getShort();
                            def.aShortArray3204[i_20_] = data.getShort();
                        }
                    }
                } else
                    def.options[opcode - 30] = Streams.readString(data);
            } else {
                def.size = data.get() & 0xff;
            }
        } else {
            int modelCount = data.get() & 0xff;
            def.modelIds = new int[modelCount];
            for (int i = 0; i < modelCount; i++) {
                def.modelIds[i] = data.getShort() & 0xffff;
                if (def.modelIds[i] == 65535)
                    def.modelIds[i] = -1;
            }
        }
    }
}
