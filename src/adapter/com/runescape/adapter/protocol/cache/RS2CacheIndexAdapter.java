package com.runescape.adapter.protocol.cache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.runescape.io.rs2cache.RS2Cache;
import com.runescape.io.rs2cache.RS2CacheFile;
import com.runescape.io.rs2cache.RS2CacheIndex;

/**
 * @author Lazaro
 */
public class RS2CacheIndexAdapter implements RS2CacheIndex {

	private RS2Cache rs2Cache;
	private int id;
	private FileChannel indexFileChannel;

	public byte[][][] archivedFiles = null;

	public RS2CacheIndexAdapter(RS2Cache rs2Cache, int id, FileChannel indexFileChannel, FIT fit) {
		this.rs2Cache = rs2Cache;
		this.id = id;
		this.indexFileChannel = indexFileChannel;

		if (id != 255 && fit != null) {
			archivedFiles = new byte[fit.entries.length][][];
		}
	}

	public int getId() {
		return id;
	}

	public RS2CacheFile getFile(int fileId) {
		try {
			ByteBuffer tempBuffer = ByteBuffer.allocateDirect(520);
			tempBuffer.limit(6);
			indexFileChannel.read(tempBuffer, fileId * 6);
			tempBuffer.flip();
			int length = ((tempBuffer.get() & 0xFF) << 16) | ((tempBuffer.get() & 0xFF) << 8) | (tempBuffer.get() & 0xFF);
			int position = ((tempBuffer.get() & 0xFF) << 16) | ((tempBuffer.get() & 0xFF) << 8) | (tempBuffer.get() & 0xFF);
			if (length == 0) {
				return null;
			}
			ByteBuffer buffer = ByteBuffer.allocate(length);
			int remaining = length;
			int offset = 0;
			while (remaining > 0) {
				int amount = remaining;
				if (amount > 512) {
					amount = 512;
				}
                tempBuffer.position(0).limit(amount + 8);
                rs2Cache.getFileChannel().read(tempBuffer, position * 520);
                tempBuffer.flip();

                int currentFileId = tempBuffer.getShort() & 0xffff;
                int currentOffset = tempBuffer.getShort() & 0xffff;
                position = ((tempBuffer.get() & 0xFF) << 16) | ((tempBuffer.get() & 0xFF) << 8) | (tempBuffer.get() & 0xFF);
                int currentIndexId = tempBuffer.get() & 0xff;
                
				/* Header checks */
				if (currentOffset != offset) {
					throw new IOException("Invalid offset read!");
				}
				if (position < 0) {
					throw new IOException("Unexpected block position!");
				}
				buffer.put(tempBuffer);
				remaining -= amount;
				
				/* Checks for next block details */
				if (currentFileId != fileId) {
					throw new IOException("Invalid file id read! [cache=" + id + ", file=" + fileId + ", block's file id=" + currentFileId + "]");
				}
				if (currentIndexId != id) {
					throw new IOException("Invalid index id read! [cache=" + id + ", file=" + fileId + ", block's index id=" + currentIndexId + "]");
				}
				offset++;
			}
			buffer.flip();
			return new RS2CacheFileAdapter(rs2Cache, id, fileId, buffer.array());
		} catch (Exception e) {
			return null;
		}
	}

	public byte[] getArchivedFile(int fileId, int childId) {
		if (archivedFiles[fileId] == null) {
			archivedFiles[fileId] = getFile(fileId).getArchivedFiles();
		}

		return archivedFiles[fileId][childId];
	}

	public RS2CacheFile getFileForName(String fileName) {
		FIT fit = ((RS2CacheAdapter) rs2Cache).getFIT(id);
		if (fit == null) {
			return null;
		}
		FITEntry entry = fit.fileEntryMap.get(FIT.hashName(fileName));
		if (entry == null) {
			return null;
		}
		return getFile(entry.id);
	}

	public int getLength() {
		try {
			return (int) (indexFileChannel.size() / 6);
		} catch (IOException e) {
			return 0;
		}
	}
}
