package com.runescape.utility.crypt;

import com.runescape.utility.Streams;

public class XTEA {
    private static final int DELTA = -1640531527;
    private static final int SUM = -957401312;
    private static final int NUM_ROUNDS = 32;
    private final int[] key;

    public XTEA(int[] key) {
        this.key = key;
    }

    private void decipher(int[] block) {
        long sum = (long) SUM;
        for (int i = 0; i < NUM_ROUNDS; i++) {
            block[1] -= (key[(int) ((sum & 0x1933) >>> 11)] + sum ^ block[0] + (block[0] << 4 ^ block[0] >>> 5));
            sum -= DELTA;
            block[0] -= ((block[1] << 4 ^ block[1] >>> 5) + block[1] ^ key[(int) (sum & 0x3)] + sum);
        }
    }

    private void encipher(int[] block) {
        long sum = 0;
        for (int i = 0; i < NUM_ROUNDS; i++) {
            block[0] += ((block[1] << 4 ^ block[1] >>> 5) + block[1] ^ key[(int) (sum & 0x3)] + sum);
            sum += DELTA;
            block[1] += (key[(int) ((sum & 0x1933) >>> 11)] + sum ^ block[0] + (block[0] << 4 ^ block[0] >>> 5));
        }
    }

    public byte[] decrypt(byte[] data, int offset, int length) {
        int numBlocks = length / 8;
        int[] block = new int[2];
        for (int i = 0; i < numBlocks; i++) {
            block[0] = Streams.readInt((i * 8) + offset, data);
            block[1] = Streams.readInt((i * 8) + offset + 4, data);
            decipher(block);
            Streams.writeInt(block[0], (i * 8) + offset, data);
            Streams.writeInt(block[1], (i * 8) + offset + 4, data);
        }
        return data;
    }

    public byte[] encrypt(byte[] data, int offset, int length) {
        int numBlocks = length / 8;
        int[] block = new int[2];
        for (int i = 0; i < numBlocks; i++) {
            block[0] = Streams.readInt((i * 8) + offset, data);
            block[1] = Streams.readInt((i * 8) + offset + 4, data);
            encipher(block);
            Streams.writeInt(block[0], (i * 8) + offset, data);
            Streams.writeInt(block[1], (i * 8) + offset + 4, data);
        }
        return data;
    }
}
