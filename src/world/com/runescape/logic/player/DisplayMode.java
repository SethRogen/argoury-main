/**
 *
 */
package com.runescape.logic.player;

/**
 * @author Lazaro
 */
public enum DisplayMode {
    FIXED(1, 548), FULL_SCREEN(3, 746), RESIZABLE(2, 746);

    public static DisplayMode forValue(int value) {
        switch (value) {
            case 1:
                return FIXED;
            case 2:
                return RESIZABLE;
            case 3:
                return FULL_SCREEN;
        }
        return null;
    }

    private int value;
    private int interfaceId;

    private DisplayMode(int value, int interfaceId) {
        this.value = value;
        this.interfaceId = interfaceId;
    }

    public int getInterfaceId() {
        return interfaceId;
    }

    public int intValue() {
        return value;
    }
}
