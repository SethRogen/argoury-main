package com.ziotic.adapter.protocol.cache;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import com.ziotic.io.rs2cache.RS2Cache;
import com.ziotic.io.rs2cache.RS2CacheFile;
import com.ziotic.utility.Streams;
import com.ziotic.utility.crypt.XTEA;
import com.ziotic.utility.zip.CBZip2InputStream;

/**
 * @author Lazaro
 */
public class RS2CacheFileAdapter implements RS2CacheFile {

    public static final int COMPRESSION_NONE = 0;
    public static final int COMPRESSION_GZIP = 2;
    public static final int COMPRESSION_BZIP2 = 1;
    private RS2Cache rs2Cache;
    private int indexId;
    private int id;
    private byte[] data;
    private boolean extracted = false;

    public RS2CacheFileAdapter(RS2Cache rs2Cache, int indexId, int id, byte[] data) {
        this.rs2Cache = rs2Cache;
        this.indexId = indexId;
        this.id = id;
        this.data = data;
    }

    public int getIndexId() {
        return indexId;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getData(int offset) {
        byte[] subData = new byte[data.length - offset];
        System.arraycopy(data, offset, subData, 0, subData.length);
        return subData;
    }

    public byte[][] getArchivedFiles() {
        if (!extract()) {
            return null;
        }

        FITEntry entry = ((RS2CacheAdapter) rs2Cache).getFIT(indexId).entries[id];
        FITEntry.FITSubEntry[] subEntries = entry.subEntries;
        int subFileCount = entry.subEntries.length;
        int realSubFileCount = entry.realSubEntryCount;
        if (indexId == 255 || subFileCount == 0 || realSubFileCount == 0) {
            throw new UnsupportedOperationException("This file does not contain any archived files!");
        }

        byte[][] archivedFiles = new byte[realSubFileCount][];

        byte[] dataArray = getData();
        int pos;
        int length = dataArray.length;
        int i_19_ = dataArray[--length] & 0xff;
        length -= i_19_ * (subFileCount * 4);
        pos = length;
        int[] subWritePos = new int[subFileCount];
        for (int i_21_ = 0; i_21_ < i_19_; i_21_++) {
            int i_22_ = 0;
            for (int subIndex = 0; subIndex < subFileCount; subIndex++) {
                i_22_ += Streams.readInt(pos, dataArray);
                pos += 4;
                subWritePos[subIndex] += i_22_;
            }
        }
        byte[][] subData = new byte[subFileCount][];
        for (int subIndex = 0; subIndex < subFileCount; subIndex++) {
            subData[subIndex] = new byte[subWritePos[subIndex]];
            subWritePos[subIndex] = 0;
        }
        int readPos = 0;
        pos = length;
        for (int i_27_ = 0; i_27_ < i_19_; i_27_++) {
            int i_28_ = 0;

            for (int childPos = 0; childPos < subFileCount; childPos++) {
                i_28_ += Streams.readInt(pos, dataArray);
                pos += 4;
                System.arraycopy(dataArray, readPos, subData[childPos], subWritePos[childPos], i_28_);
                readPos += i_28_;
                subWritePos[childPos] += i_28_;
            }
        }

        for (int i = 0; i < subFileCount; i++) {
            int i_31_;
            if (subEntries != null)
                i_31_ = subEntries[i].pointer;
            else
                i_31_ = i;
            archivedFiles[i_31_] = subData[i];
        }

        return archivedFiles;
    }

    public boolean extract(int[] xteaKey) {
        if (extracted) {
            return true;
        }
        try {
            if (xteaKey != null && xteaKey[0] != 0 && xteaKey[1] != 0 && xteaKey[2] != 0 && xteaKey[3] != 0) {
                XTEA xtea = new XTEA(xteaKey);
                xtea.decrypt(data, 5, Streams.readInt(1, data));
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int compression = buffer.get() & 0xff;
            int length = buffer.getInt();
            int decompressedLength = compression != COMPRESSION_NONE ? buffer.getInt() : length;
            byte[] out = new byte[decompressedLength];
            byte[] inputData = new byte[length];
            buffer.get(inputData);
            InputStream input = new ByteArrayInputStream(inputData);
            switch (compression) {
                case COMPRESSION_GZIP:
                    input = new GZIPInputStream(input);
                    break;
                case COMPRESSION_BZIP2:
                    input = new CBZip2InputStream(input);
                    break;
            }
            BufferedInputStream inputStream = new BufferedInputStream(input, decompressedLength);
			for (int i = 0; i < decompressedLength; i++) {
				int val = inputStream.read();
				if (val == -1) {
					throw new IOException("EOF");
				}
				out[i] = (byte) val;
			}
			data = out;
            extracted = true;
            return true;
        } catch (Throwable e) {
            //logger.error("Error extracting file [index=" + indexId + ", file="
            //		+ id + "]", e);
            return false;
        }
    }

    public boolean extract() {
        return extract(null);
    }

    public boolean isExtracted() {
        return extracted;
    }
}
