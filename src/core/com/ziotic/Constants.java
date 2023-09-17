package com.ziotic;

/**
 * @author Lazaro
 */
public class Constants {
    public static class Equipment {
        public static final int AMULET_SLOT = 2;
        public static final int ARROWS_SLOT = 13;
        public static final int BOOTS_SLOT = 10;
        public static final int BOTTOMS_SLOT = 7;
        public static final int CAPE_SLOT = 1;
        public static final int CHEST_SLOT = 4;
        public static final int GLOVES_SLOT = 9;
        public static final int HELM_SLOT = 0;
        public static final int RING_SLOT = 12;
        public static final int SHIELD_SLOT = 5;
        public static final int WEAPON_SLOT = 3;
    }

    public static class Inputs {
        public static final int CC_PREFIX = 1;
        public static final int BANK_WITHDRAW_X = 2;
        public static final int BANK_DEPOSIT_X = 3;
        public static final int TRADING_OFFER_X = 4;
        public static final int TRADING_REMOVE_X = 5;
    }

    public static final int GAME_TICK_INTERVAL = 600;
    public static final int FAST_GAME_TICK_INTERVAL = 50;

    public static final String CONFIGURATION_FILE = "%WORK_DIR%/world/config.ini";
    public static final String MAP_XTEA_FILE = "%WORK_DIR%/world/mapData/mapxtea.dat";
    public static final String MAP_XTEA_DIR = "%WORK_DIR%/world/mapData/mapxtea/";

    public static final int[] REGION_SIZE = {104, 120, 136, 168};
}
