package com.ziotic.io.rs2cache;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Lazaro
 */
public interface RS2Cache {
    public void load(File dir) throws IOException;

    public RS2CacheIndex getIndex(int id);

    public RS2CacheFile getFile(int indexId, int fileId);

	public FileChannel getFileChannel();


}
