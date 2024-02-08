package com.runescape.utility;

/**
 * @author Lazaro
 */
public interface Poolable {
    /**
     * If the object is still useful.
     *
     * @return If the object is still useful.
     */
    public boolean expired();

    /**
     * Resets all the variables and data in the implemented object.
     */
    public void recycle();
}
