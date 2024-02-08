package com.runescape.adapter.protocol.cache.format;

import com.runescape.Static;
import com.runescape.logic.item.ItemDefinition;
import com.runescape.logic.player.Levels;
import com.runescape.utility.Logging;
import com.runescape.utility.Streams;

import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * @author Lazaro
 */
public class ItemDefinitionAdapter {

    public static final int[] ITEM_REQS = {
            540, 542, 697, 538
    };

    private static final Logger LOGGER = Logging.log();

    public static ItemDefinition forId(int id) {
        ItemDefinition def = new ItemDefinition();
        def.id = id;

        byte[] data = Static.rs2Cache.getIndex(19).getArchivedFile(id >>> 8, id & 0xff);
        load(def, ByteBuffer.wrap(data));

        return def;
    }

    private static void load(ItemDefinition def, ByteBuffer buffer) {
        int opcode;
        while ((opcode = (buffer.get() & 0xff)) != 0) {
            parseOpcode(def, opcode, buffer);
        }
    }

    private static void parseOpcode(ItemDefinition def, int opcode, ByteBuffer data) {

        if (opcode != 1) {
            if (opcode != 2) {
                if (opcode != 4) {
                    do {
                        if (opcode != 5) {
                            if (opcode == 6)
                                def.anInt1477 = data.getShort() & 0xffff;
                            else {
                                if (opcode != 7) {
                                    if (opcode != 8) {
                                        if (opcode == 11)
                                            def.anInt1456 = 1;
                                        else if (opcode != 12) {
                                            if (opcode != 16) {
                                                if (opcode == 23)
                                                    def.maleWieldModel = (data.getShort() & 0xffff);
                                                else if (opcode != 24) {
                                                    if (opcode == 25)
                                                        def.femaleWieldModel = (data.getShort() & 0xffff);
                                                    else if (opcode == 26)
                                                        def.anInt1435 = (data.getShort() & 0xffff);
                                                    else if (opcode < 30 || opcode >= 35) {
                                                        if (opcode >= 35 && opcode < 40)
                                                            def.options[-35 + opcode] = (Streams.readString(data));
                                                        else if (opcode == 40) {
                                                            int i_48_ = data.get() & 0xff;
                                                            def.aShortArray1457 = (new short[i_48_]);
                                                            def.aShortArray1492 = (new short[i_48_]);
                                                            for (int i_49_ = 0; i_49_ < i_48_; i_49_++) {
                                                                def.aShortArray1457[i_49_] = data.getShort();
                                                                def.aShortArray1492[i_49_] = data.getShort();
                                                            }
                                                        } else if (opcode != 41) {
                                                            if (opcode == 42) {
                                                                int i_50_ = (data.get() & 0xff);
                                                                def.aByteArray1501 = (new byte[i_50_]);
                                                                for (int i_51_ = 0; (i_51_ < i_50_); i_51_++)
                                                                    def.aByteArray1501[i_51_] = (data.get());
                                                            } else if (opcode != 65) {
                                                                if (opcode == 78)
                                                                    def.anInt1479 = (data.getShort() & 0xffff);
                                                                else if (opcode == 79)
                                                                    def.anInt1438 = (data.getShort() & 0xffff);
                                                                else if (opcode != 90) {
                                                                    if (opcode != 91) {
                                                                        if (opcode == 92)
                                                                            def.anInt1450 = data.getShort() & 0xffff;
                                                                        else if (opcode != 93) {
                                                                            if (opcode != 95) {
                                                                                if (opcode != 96) {
                                                                                    if (opcode == 97)
                                                                                        def.certID = data.getShort() & 0xffff;
                                                                                    else if (opcode == 98)
                                                                                        def.certTemplateID = data.getShort() & 0xffff;
                                                                                    else if (opcode < 100 || opcode >= 110) {
                                                                                        if (opcode == 110)
                                                                                            def.anInt1423 = data.getShort() & 0xffff;
                                                                                        else if (opcode != 111) {
                                                                                            if (opcode == 112)
                                                                                                def.anInt1480 = data.getShort() & 0xffff;
                                                                                            else if (opcode != 113) {
                                                                                                if (opcode == 114)
                                                                                                    def.anInt1439 = data.get() * 5;
                                                                                                else if (opcode == 115)
                                                                                                    def.anInt1462 = data.get() & 0xff;
                                                                                                else if (opcode != 121) {
                                                                                                    if (opcode != 122) {
                                                                                                        if (opcode == 125) {
                                                                                                            def.anInt1493 = data.get() << 2;
                                                                                                            def.anInt1465 = data.get() << 2;
                                                                                                            def.anInt1437 = data.get() << 2;
                                                                                                        } else if (opcode == 126) {
                                                                                                            def.anInt1498 = data.get() << 2;
                                                                                                            def.anInt1470 = data.get() << 2;
                                                                                                            def.anInt1446 = data.get() << 2;
                                                                                                        } else if (opcode == 127) {
                                                                                                            def.anInt1455 = data.get() & 0xff;
                                                                                                            def.anInt1426 = data.getShort() & 0xffff;
                                                                                                        } else if (opcode != 128) {
                                                                                                            if (opcode == 129) {
                                                                                                                def.anInt1433 = data.get() & 0xff;
                                                                                                                def.anInt1468 = data.getShort() & 0xffff;
                                                                                                            } else if (opcode == 130) {
                                                                                                                def.anInt1440 = data.get() & 0xff;
                                                                                                                def.anInt1483 = data.getShort() & 0xffff;
                                                                                                            } else if (opcode != 132) {
                                                                                                                if (opcode == 249) {
                                                                                                                    int len = (data.get() & 0xff);
                                                                                                                    int levelId = -1, levelReq = -1, itemId = -1;
                                                                                                                    for (int i = 0; i < len; i++) {
                                                                                                                        boolean string = (data.get() & 0xff) == 1;
                                                                                                                        int opcode2 = (data.get() & 0xff) << 16 | (data.get() & 0xff) << 8 | (data.get() & 0xff);
                                                                                                                        if (!string) {
                                                                                                                            int val = data.getInt();
                                                                                                                            if (opcode2 >= 749 && opcode2 <= 796) {
                                                                                                                                if (opcode2 % 2 == 0) {
                                                                                                                                    levelReq = val;
                                                                                                                                } else {
                                                                                                                                    levelId = val;
                                                                                                                                }
                                                                                                                                if (levelId != -1 && levelReq != -1) {
                                                                                                                                    if (def.levelRequirements == null) {
                                                                                                                                        def.levelRequirements = new HashMap<Integer, Integer>();
                                                                                                                                    }
                                                                                                                                    def.levelRequirements.put(levelId, levelReq);
                                                                                                                                    levelId = levelReq = -1;
                                                                                                                                }
                                                                                                                            }
                                                                                                                            for (int reqOpcode : ITEM_REQS) {
                                                                                                                                if (opcode2 == reqOpcode) {
                                                                                                                                    itemId = val;
                                                                                                                                } else if (opcode2 - 1 == reqOpcode && itemId != -1) {
                                                                                                                                    if (def.itemRequirements == null)
                                                                                                                                        def.itemRequirements = new HashMap<Integer, Integer>();
                                                                                                                                    def.itemRequirements.put(itemId, val);
                                                                                                                                    itemId = -1;
                                                                                                                                }
                                                                                                                            }
                                                                                                                            switch (opcode2) {
                                                                                                                                case 394:
                                                                                                                                    if (def.levelRequirements == null)
                                                                                                                                        def.levelRequirements = new HashMap<Integer, Integer>();
                                                                                                                                    def.levelRequirements.put(Levels.SUMMONING, val);
                                                                                                                                    break;
                                                                                                                                case 644:
                                                                                                                                    def.renderId = val;
                                                                                                                                    break;
                                                                                                                                case 686:
                                                                                                                                    def.weaponGroupId = val;
                                                                                                                                    break;
                                                                                                                                case 687:
                                                                                                                                    def.weaponSpecial = val == 1;
                                                                                                                                    break;
                                                                                                                                default:
                                                                                                                                    //LOGGER.info("[ItemDefinitionLoader] [opcode=249] [Unhandled subopcode=" + opcode2 + " value=" + val);
                                                                                                                            }
                                                                                                                        } else {
                                                                                                                            String val = Streams.readString(data);
                                                                                                                            //LOGGER.info("[ItemDefinitionLoader] [opcode=249] [Unhandled subopcode=" + opcode2 + " value=" + val);
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            } else {
                                                                                                                int i_56_ = data.get() & 0xff;
                                                                                                                def.anIntArray1441 = new int[i_56_];
                                                                                                                for (int i_57_ = 0; i_56_ > i_57_; i_57_++)
                                                                                                                    def.anIntArray1441[i_57_] = data.getShort() & 0xffff;
                                                                                                            }
                                                                                                        } else {
                                                                                                            def.anInt1442 = data.get() & 0xff;
                                                                                                            def.anInt1476 = data.getShort() & 0xffff;
                                                                                                        }
                                                                                                    } else
                                                                                                        def.lendedParentId2 = data.getShort() & 0xffff;
                                                                                                } else
                                                                                                    def.lendedParentId = data.getShort() & 0xffff;
                                                                                            } else
                                                                                                def.anInt1458 = data.get();
                                                                                        } else
                                                                                            def.anInt1503 = data.getShort() & 0xffff;
                                                                                    } else {
                                                                                        if (def.stackTypes == null) {
                                                                                            def.stackTypes = new int[10];
                                                                                            def.stackAmounts = new int[10];
                                                                                        }
                                                                                        def.stackTypes[-100 + opcode] = data.getShort() & 0xffff;
                                                                                        def.stackAmounts[opcode - 100] = data.getShort() & 0xffff;
                                                                                    }
                                                                                } else
                                                                                    def.anInt1443 = data.get() & 0xff;
                                                                            } else
                                                                                def.anInt1494 = data.getShort() & 0xffff;
                                                                        } else
                                                                            def.anInt1490 = data.getShort() & 0xffff;
                                                                    } else
                                                                        def.anInt1466 = data.getShort() & 0xffff;
                                                                } else
                                                                    def.anInt1454 = (data.getShort() & 0xffff);
                                                            } else
                                                                def.aBoolean1463 = true;
                                                        } else {
                                                            int i_58_ = (data.get() & 0xff);
                                                            def.aShortArray1504 = (new short[i_58_]);
                                                            def.aShortArray1488 = (new short[i_58_]);
                                                            for (int i_59_ = 0; i_59_ < i_58_; i_59_++) {
                                                                def.aShortArray1488[i_59_] = data.getShort();
                                                                def.aShortArray1504[i_59_] = data.getShort();
                                                            }
                                                        }
                                                    } else
                                                        def.aStringArray1485[opcode + -30] = (Streams.readString(data));
                                                } else
                                                    def.anInt1449 = (data.getShort() & 0xffff);
                                            } else
                                                def.aBoolean1502 = true;
                                        } else
                                            def.shopPrice = data.getInt();
                                    } else {
                                        def.anInt1491 = data.getShort() & 0xffff;
                                        if (def.anInt1491 > 32767)
                                            def.anInt1491 -= 65536;
                                    }
                                } else {
                                    def.anInt1425 = data.getShort() & 0xffff;
                                    if (def.anInt1425 <= 32767)
                                        break;
                                    def.anInt1425 -= 65536;
                                }
                                break;
                            }
                            break;
                        }
                        def.anInt1444 = data.getShort() & 0xffff;
                    } while (false);
                } else
                    def.anInt1436 = data.getShort() & 0xffff;
            } else
                def.name = Streams.readString(data);
        } else
            def.modelId = data.getShort() & 0xffff;
    }
}
