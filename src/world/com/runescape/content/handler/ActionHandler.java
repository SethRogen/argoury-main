package com.runescape.content.handler;

/**
 * @author Lazaro
 */
public interface ActionHandler {
    public void load(ActionHandlerSystem system) throws Exception;

    public boolean explicitlyForMembers();
}
