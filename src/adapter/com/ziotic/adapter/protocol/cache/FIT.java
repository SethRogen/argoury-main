package com.ziotic.adapter.protocol.cache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class FIT {
    public static int hashName(String s) {
        int r = 0;
        for (int i = 0; i < s.length(); i++) {
            r = s.charAt(i) + ((r << 5) - r);
        }
        return r;
    }

    public int version = 0;
    public int revision = 0;
    public boolean titled = false;
    public boolean whirlpoolHashed = false;
    public FITEntry[] entries = null;
    public Map<Integer, FITEntry> fileEntryMap = null;

    public FIT(RS2CacheFileAdapter fitFile) throws IOException {
        if (!fitFile.extract()) {
            entries = new FITEntry[0];
            fileEntryMap = new HashMap<Integer, FITEntry>();
            return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(fitFile.getData());
        version = buffer.get() & 0xff;
        if (version == 6) {
            revision = buffer.getInt();
        } else if (version != 5) {
            throw new IOException("Invalid descriptor version : " + version + " : " + java.util.Arrays.toString(buffer.array()));
        }
        int flags = buffer.get() & 0xff;
        titled = (flags & 0x1) != 0;
        whirlpoolHashed = (flags & 0x2) != 0;
        int count = buffer.getShort() & 0xffff;
        int[] spacing = new int[count];
        int entryCount = 0;
        for (int i = 0; i < count; i++) {
            spacing[i] = entryCount += buffer.getShort() & 0xffff;
        }
        entryCount++;
        entries = new FITEntry[entryCount];
        for (int i = 0; i < entryCount; i++) {
            entries[i] = new FITEntry(i);
        }
        if (titled) {
            fileEntryMap = new HashMap<Integer, FITEntry>();
            for (int i : spacing) {
                FITEntry entry = entries[i];
                fileEntryMap.put(entry.nameHash = buffer.getInt(), entry);
            }
        }
        for (int i : spacing) {
            entries[i].exists = true;
            entries[i].crc = buffer.getInt();
        }
        if (whirlpoolHashed) {
            for (int i : spacing) {
                byte[] whirlpoolChecksum = new byte[64];
                buffer.get(whirlpoolChecksum);
                entries[i].whirlpoolChecksum = whirlpoolChecksum;
            }
        }
        for (int i : spacing) {
            entries[i].revision = buffer.getInt(); // TODO Apparently
            // this is not
            // actually a
            // revision number!
        }
        for (int i : spacing) {
            FITEntry entry = entries[i];
            FITEntry.FITSubEntry[] subEntries = new FITEntry.FITSubEntry[buffer.getShort() & 0xffff];
            for (int j = 0; j < subEntries.length; j++) {
                subEntries[j] = new FITEntry.FITSubEntry();
            }
            entry.subEntries = subEntries;
        }
        for (int i : spacing) {
            FITEntry entry = entries[i];
            entryCount = 0;
            for (int j = 0; j < entry.subEntries.length; j++) {
                int pointer = entry.subEntries[j].pointer = (entryCount += buffer.getShort() & 0xffff);
                if (entry.realSubEntryCount < pointer) {
                    entry.realSubEntryCount = pointer;
                }
            }
            entry.realSubEntryCount++;
        }
        if (titled) {
            for (int i : spacing) {
                FITEntry entry = entries[i];
                for (int j = 0; j < entry.subEntries.length; j++) {
                    entries[i].subEntries[j].nameHash = buffer.getInt();
                }
            }
        }
    }
}
