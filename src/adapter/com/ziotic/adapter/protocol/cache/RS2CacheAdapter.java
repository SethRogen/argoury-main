package com.ziotic.adapter.protocol.cache;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;

import com.ziotic.Static;
import com.ziotic.io.rs2cache.RS2Cache;
import com.ziotic.io.rs2cache.RS2CacheFile;
import com.ziotic.io.rs2cache.RS2CacheIndex;
import com.ziotic.utility.Logging;
import com.ziotic.utility.Whirlpool;

/**
 * @author Lazaro
 */
public class RS2CacheAdapter implements RS2Cache {
	private static final Logger logger = Logging.log();

	private FileChannel channel = null;

	private RS2CacheIndex[] indexes = null;
	private RS2CacheIndex fitIndex = null;
	private FIT[] fits = null;
	private RS2CacheFile fitChecksumFile = null;

	public void load(File dir) throws IOException {
		channel = new RandomAccessFile(new File(dir, "main_file_cache.dat2"), "rw").getChannel();

		RandomAccessFile fitIndexFile = new RandomAccessFile(new File(dir, "main_file_cache.idx255"), "rw");
		fitIndex = new RS2CacheIndexAdapter(Static.rs2Cache, 255, fitIndexFile.getChannel(), null);

		int cacheIndicies = fitIndex.getLength();

		indexes = new RS2CacheIndex[cacheIndicies];
		fits = new FIT[cacheIndicies];
		for (int i = 0; i < cacheIndicies; i++) {
			FileChannel indexFileChannel = new RandomAccessFile(new File(dir, "main_file_cache.idx" + i), "rw").getChannel();
			RS2CacheFileAdapter fitFile = (RS2CacheFileAdapter) fitIndex.getFile(i);
			FIT fit = null;
			if (fitFile != null) {
				fit = fits[i] = new FIT(fitFile);
			}
			indexes[i] = new RS2CacheIndexAdapter(Static.rs2Cache, i, indexFileChannel, fit);
		}
		int realIndexCount = fitIndex.getLength();
		CRC32 crc = new CRC32();
		ByteBuffer buffer = ByteBuffer.allocate((realIndexCount * 72) + 6 + 65);
		buffer.put((byte) RS2CacheFileAdapter.COMPRESSION_NONE).putInt((realIndexCount * 72) + 1 + 65).put((byte) realIndexCount);
		for (int i = 0; i < realIndexCount; i++) {
			RS2CacheFile file = fitIndex.getFile(i);
			if (file == null) {
				buffer.putInt(0).putInt(0).put(Whirlpool.whirlpool(new byte[0], 0, 0));
			} else {
				byte[] fileData = file.getData();
				crc.update(fileData);
				buffer.putInt((int) crc.getValue());
				buffer.putInt(fits[i].revision);
				buffer.put(Whirlpool.whirlpool(fileData, 0, fileData.length));
				crc.reset();
			}
		}
		int pos = buffer.position();
		buffer.rewind();
		byte[] currentData = new byte[pos];
		buffer.get(currentData).rewind().position(pos);
		buffer.put((byte) 10).put(Whirlpool.whirlpool(currentData, 5, currentData.length - 5)).rewind();
		fitChecksumFile = new RS2CacheFileAdapter(Static.rs2Cache, 255, 255, buffer.array());
		logger.info("Loaded cache with " + realIndexCount + " index(es)");
	}

	public RS2CacheIndex getIndex(int id) {
		return indexes[id];
	}

	public RS2CacheFile getFile(int indexId, int fileId) {
		if (indexId == 255 && fileId == 255) {
			return fitChecksumFile;
		}
		RS2CacheIndex index = indexId != 255 ? indexes[indexId] : fitIndex;
		if (index != null) {
			return index.getFile(fileId);
		}
		return null;
	}

	public FIT getFIT(int indexId) {
		return fits[indexId];
	}

	@Override
	public FileChannel getFileChannel() {
		return channel;
	}
}
