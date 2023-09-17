package com.ziotic.io.rs2cache;

/**
 * @author Lazaro
 */
public interface RS2CacheFile {
    public int getIndexId();

    public int getId();

    public byte[] getData();

    public byte[] getData(int offset);

    public byte[][] getArchivedFiles();
}
