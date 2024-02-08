package com.runescape.logic;

import com.runescape.utility.Attributes;

/**
 * @author Lazaro
 */
public abstract class Node {
    private int index = -1;
    private boolean valid = false;
    private Attributes attributes = null;

    public final int getIndex() {
        return index;
    }

    public final boolean isValid() {
        return valid;
    }

    public final void setIndex(int index) {
        this.index = index;
    }

    public final void setValid(boolean valid) {
        this.valid = valid;
    }

    public final Attributes getAttributes() {
        if (attributes == null) {
            attributes = new Attributes();
        }
        return attributes;
    }
}
