package com.ziotic.io.rs2cache;

/**
 * @author Lazaro
 */
public interface RS2CacheIndex {
    public int getId();

    public RS2CacheFile getFile(int fileId);

    public RS2CacheFile getFileForName(String fileName);

    public byte[] getArchivedFile(int filedId, int childId);

    public int getLength();
}
