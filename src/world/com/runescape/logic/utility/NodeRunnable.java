/**
 *
 */
package com.runescape.logic.utility;

import com.runescape.logic.Node;

/**
 * @author Lazaro
 */
public interface NodeRunnable<T extends Node> {
    public void run(T node);
}
