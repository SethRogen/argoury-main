package com.runescape;

/**
 * @author Lazaro
 */
public interface Application {
    public static enum AppType {
        GAME, LOBBY, LINK, GAME_AND_LOBBY, UPDATER
    }

    public void main(String[] args) throws Throwable;
}
