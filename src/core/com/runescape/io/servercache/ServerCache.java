package com.runescape.io.servercache;

import org.apache.log4j.Logger;

import com.runescape.utility.Logging;
import com.runescape.utility.Streams;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class ServerCache {
    private static final Logger logger = Logging.log();

    private File dir;
    private RandomAccessFile dataFile = null;
    private Map<String, ServerCacheFileIndex> fileIndexMap = new HashMap<String, ServerCacheFileIndex>();

    public void load(File dir) throws IOException {
        this.dir = dir;

        validateFiles();

        dataFile = new RandomAccessFile(new File(dir, "servercache.dat"), "rw");

        loadFileIndexMap();

        logger.info("Loaded server cache with " + fileIndexMap.size() + " files");
    }

    private void validateFiles() throws IOException {
        File f1 = new File(dir, "servercache.dat");
        File f2 = new File(dir, "servercache.idx");
        if (!f1.exists()) {
            f1.createNewFile();
        }
        if (!f2.exists()) {
            f2.createNewFile();
        }
    }

    private void loadFileIndexMap() throws IOException {
        File fileIndexMapFile = new File(dir, "servercache.idx");
        DataInputStream stream = new DataInputStream(new FileInputStream(fileIndexMapFile));
        while (stream.available() > 0) {
            String name = Streams.readString(stream);
            int position = stream.readInt();
            int length = stream.readInt();

            fileIndexMap.put(name, new ServerCacheFileIndex(name, position, length));
        }
        stream.close();
    }

    private void flushFileIndexMap() throws IOException {
        File fileIndexMapFile = new File(dir, "servercache.idx");
        fileIndexMapFile.delete();

        DataOutputStream stream = new DataOutputStream(new FileOutputStream(fileIndexMapFile));
        for (ServerCacheFileIndex fileIndex : fileIndexMap.values()) {
            Streams.writeString(fileIndex.getName(), stream);
            stream.writeInt(fileIndex.getPosition());
            stream.writeInt(fileIndex.getLength());
        }
        stream.close();
    }

    public byte[] getFile(String name) throws IOException {
        ServerCacheFileIndex fileIndex = fileIndexMap.get(name);
        if (fileIndex == null) {
            return null;
        }
        byte[] data = new byte[fileIndex.getLength()];
        synchronized (dataFile) {
            dataFile.seek(fileIndex.getPosition());
            dataFile.read(data);
        }
        return data;
    }

    public void writeFile(String name, byte[] data) throws IOException {
        int position = -1;

        ServerCacheFileIndex fileIndex = fileIndexMap.get(name);
        if (fileIndex != null) {
            if (data.length < fileIndex.getLength()) {
                position = fileIndex.getPosition();
            }
        }

        synchronized (dataFile) {
            if (position == -1) {
                position = (int) dataFile.length();
            }

            dataFile.seek(position);
            dataFile.write(data);

            fileIndexMap.put(name, new ServerCacheFileIndex(name, position, data.length));

            flushFileIndexMap();
        }
    }
}
