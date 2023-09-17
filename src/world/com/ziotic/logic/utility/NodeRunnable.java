/**
 *
 */
package com.ziotic.logic.utility;

import com.ziotic.logic.Node;

/**
 * @author Lazaro
 */
public interface NodeRunnable<T extends Node> {
    public void run(T node);
}
