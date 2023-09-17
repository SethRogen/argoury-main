package com.ziotic.logic.map;

import com.ziotic.Constants;
import com.ziotic.Static;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MapXTEA {
    private static final Logger logger = Logging.log();
    private Map<Integer, int[]> xteas = new HashMap<Integer, int[]>();

    public MapXTEA() {
        try {
            if (!loadPackedFile()) {
                createPackedFile();
            }
            logger.info("Loaded " + xteas.size() + " map XTEA key(s)");
        } catch (IOException e) {
            logger.error("Failed to load map xtea(s)!", e);
        }
    }

    public void createPackedFile() throws IOException {
        DataOutputStream output = new DataOutputStream(new FileOutputStream(Static.parseString(Constants.MAP_XTEA_FILE)));
        File directory = new File(Static.parseString(Constants.MAP_XTEA_DIR));
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    BufferedReader input = new BufferedReader(new FileReader(file));
                    int id = Integer.parseInt(file.getName().substring(0, file.getName().indexOf(".")));
                    output.writeInt(id);
                    int[] keys = new int[4];
                    for (int i = 0; i < 4; i++) {
                        String line = input.readLine();
                        if (line != null) {
                            keys[i] = Integer.parseInt(line);
                        } else {
                            logger.error("Corrupted XTEA file : " + id + "; line: " + line);
                            keys[i] = 0;
                        }
                        output.writeInt(keys[i]);
                    }
                    input.close();
                    xteas.put(id, keys);
                }
            }
        }
        output.close();
    }

    public int[] getKey(int region) {
        return xteas.get(region);
    }

    public Map<Integer, int[]> getXTEAs() {
        return xteas;
    }

    public boolean loadPackedFile() throws IOException {
        File file = new File(Static.parseString(Constants.MAP_XTEA_FILE));
        if (!file.exists()) {
            return false;
        }
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        for (int i = 0; i < file.length() / (4 + (4 * 4)); i++) {
            int id = in.readInt();
            int[] key = new int[4];
            for (int i2 = 0; i2 < 4; i2++) {
                key[i2] = in.readInt();
            }
            xteas.put(id, key);
        }
        return true;
    }
}
