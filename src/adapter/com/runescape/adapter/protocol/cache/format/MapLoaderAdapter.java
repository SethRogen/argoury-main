/**
 *
 */
package com.runescape.adapter.protocol.cache.format;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.runescape.Static;
import com.runescape.adapter.protocol.cache.RS2CacheFileAdapter;
import com.runescape.logic.map.MapLoader;
import com.runescape.logic.map.Region;
import com.runescape.utility.Logging;
import com.runescape.utility.Streams;

/**
 * @author Lazaro
 */
public class MapLoaderAdapter implements MapLoader {
	private static Logger logger = Logging.log();

	@Override
	public boolean loadMap(Region region, int x, int y) {
		boolean failed = false;

		ByteBuffer str1 = null, str2 = null;

		byte[] f1 = null;
		byte[] f2 = null;
		try {
			f1 = Static.serverCache.getFile("l" + x + "_" + y);
			f2 = Static.serverCache.getFile("m" + x + "_" + y);
		} catch (IOException e) {
		}

		if (f1 == null || f2 == null) {
			RS2CacheFileAdapter landscapeMap = (RS2CacheFileAdapter) Static.rs2Cache.getIndex(5).getFileForName("m" + x + "_" + y);
			RS2CacheFileAdapter objectMap = (RS2CacheFileAdapter) Static.rs2Cache.getIndex(5).getFileForName("l" + x + "_" + y);
			if (landscapeMap == null && objectMap == null) {
				// logger.warn("Map [" + x + ", " + y +
				// "] was not found in the cache!");
				return false;
			}
			if (landscapeMap != null) {
				landscapeMap.extract();
			}
			if (objectMap != null) {
				int[] key = Static.mapXTEA.getKey(x << 8 | y);
				if (key == null) {
					key = new int[4];
					// logger.warn("No mapdata for region : " + (x << 8 | y));
				}
				if (!objectMap.extract(key)) {
					failed = true;
				}
			}

			try {
				if (objectMap.isExtracted())
					Static.serverCache.writeFile("l" + x + "_" + y, objectMap
							.getData());
				if (landscapeMap.isExtracted())
					Static.serverCache.writeFile("m" + x + "_" + y,
							landscapeMap.getData());
			} catch (IOException e) {
				logger.warn("Unable to write to server cache!");
			}

			if (landscapeMap != null && landscapeMap.isExtracted()) {
				str2 = ByteBuffer.wrap(landscapeMap.getData());
			}
			if (objectMap != null && objectMap.isExtracted()) {
				str1 = ByteBuffer.wrap(objectMap.getData());
			}
		} else {
			str1 = ByteBuffer.wrap(f1);
			str2 = ByteBuffer.wrap(f2);
		}
		byte[][][] landscapeData = new byte[4][64][64];
		if (str2 != null) {
			for (int i = 0; i < 4; i++) {
				for (int i2 = 0; i2 < 64; i2++) {
					for (int i3 = 0; i3 < 64; i3++) {
						while (true) {
							int v = str2.get() & 0xff;
							if (v == 0) {
								break;
							} else if (v == 1) {
								str2.get();
								break;
							} else if (v <= 49) {
								str2.get();
							} else if (v <= 81) {
								landscapeData[i][i2][i3] = (byte) (v - 49);
							}
						}
					}
				}
			}
			for (int i = 0; i < 4; i++) {
				for (int x2 = 0; x2 < 64; x2++) {
					for (int y2 = 0; y2 < 64; y2++) {
						if ((landscapeData[i][x2][y2] & 1) == 1) {
							int z = i;
							if ((landscapeData[1][x2][y2] & 2) == 2) {
								z--;
							}
							if (z >= 0 && z <= 3) {
								region.clip(x2, y2, z, 0x200000);
							}
						}
					}
				}
			}
		}
		if (str1 != null) {
			int objectId = -1;
			int incr;
			while ((incr = Streams.readSmart2(str1)) != 0) {
				objectId += incr;
				int location = 0;
				int incr2;
				while ((incr2 = Streams.readSmart(str1)) != 0) {
					location += incr2 - 1;
					int x2 = (location >> 6 & 0x3f);
					int y2 = (location & 0x3f);
					int z = location >> 12;
					int objectData = str1.get() & 0xff;
					int type = objectData >> 2;
					int direction = objectData & 0x3;
					if (x2 < 0 || x2 >= 64 || y2 < 0 || y2 >= 64) {
						continue;
					}
					if ((landscapeData[1][x2][y2] & 2) == 2) {
						z--;
					}
					if (z >= 0 && z <= 3) {
						region.addObject(objectId, x2, y2, z, type, direction, true);
					}
				}
			}
		}
		return !failed;
	}
}
