package com.runescape.adapter.protocol.cache;

/**
 * @author Lazaro
 */
public class FITEntry {
    public static class FITSubEntry {
        public int pointer = 0;
        public int nameHash = 0;
    }

    public int id;
    public boolean exists = false;
    public int crc = 0;
    public int revision = 0; // TODO Apparently this is not the file revision!
    public int nameHash = 0;
    public byte[] whirlpoolChecksum = null;
    public FITSubEntry[] subEntries = null;
    public int realSubEntryCount = -1;

    public FITEntry(int fileId) {
        this.id = fileId;
    }
}
