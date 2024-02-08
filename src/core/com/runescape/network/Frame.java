package com.runescape.network;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

/**
 * @author Lazaro
 */
public final class Frame {
    public static final int MAX_OPCODE = 255;

    public static enum FrameType {
        RECIEVED, FIXED, VAR_BYTE, VAR_SHORT
    }

    public static final int[] BIT_MASK = new int[32];

    static {
        for (int i = 0; i < BIT_MASK.length; i++) {
            BIT_MASK[i] = (1 << i) - 1;
        }
    }

    private int opcode;
    private FrameType type;
    private byte[] buffer;
    private int len;
    private int pos = 0;
    private int bitPos = -1;

    public Frame(int opcode, FrameType type, byte[] buffer, int length) {
        this.opcode = opcode;
        this.type = type;
        this.buffer = buffer;
        this.len = length;
        if (type != null) {
            pos = (!isRaw() && type != FrameType.RECIEVED) ? (type == FrameType.VAR_SHORT ? 3 : (type == FrameType.VAR_BYTE ? 2 : 1)) : 0;
        }
    }

    public int getOpcode() {
        return opcode;
    }

    public FrameType getType() {
        return type;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getPosition() {
        return pos;
    }

    public int getLength() {
        return len;
    }

    public boolean isRaw() {
        return opcode == -1;
    }

    public int remaining() {
        return getLength() - getPosition();
    }

    /**
     * This is to be called if you are adding bits when the offset is not 0.
     *
     * @return
     */
    public void calculateBitPosition() {
        bitPos = pos * 8;
    }

    /**
     * Gets the data in this message in bytes.
     *
     * @return The data.
     */
    public byte[] getBytes() {
        byte[] array = new byte[len];
        System.arraycopy(buffer, 0, array, 0, len);
        return array;
    }

    /*
     * Start of signed reading methods.
     */

    public byte read() {
        return buffer[pos++];
    }

    public byte readA() {
        return (byte) (readUnsigned() - 128);
    }

    public byte readC() {
        return (byte) (-readUnsigned());
    }

    public byte readS() {
        return (byte) (128 - readUnsigned());
    }

    /*
     * End of signed reading methods.
     */

    /*
     * Start of unsigned reading methods.
     */

    public int readUnsigned() {
        return buffer[pos++] & 0xff;
    }

    public int readUnsignedA() {
        return readA() & 0xff;
    }

    public int readUnsignedC() {
        return readC() & 0xff;
    }

    public int readUnsignedS() {
        return readS() & 0xff;
    }

    /*
     * End of unsigned reading methods.
     */

    /*
     * Start of array reading methods.
     */

    public void read(byte[] bytes) {
        read(bytes, 0, bytes.length);
    }

    public void read(byte[] bytes, int offset, int length) {
        System.arraycopy(buffer, pos, bytes, offset, length);
    }

    /*
     * End of array reading methods.
     */

    /*
     * Start of 16 bit reading methods.
     */

    public int readShort() {
        int i = (readUnsigned() << 8) | readUnsigned();
        if (i > 32767) {
            i -= 0x10000;
        }
        return i;
    }

    public int readShortA() {
        int i = (readUnsigned() << 8) | readUnsignedA();
        if (i > 32767) {
            i -= 0x10000;
        }
        return i;
    }

    public int readLEShort() {
        int i = readUnsigned() | (readUnsigned() << 8);
        if (i > 32767) {
            i -= 0x10000;
        }
        return i;
    }

    public int readLEShortA() {
        int i = readUnsignedA() | (readUnsigned() << 8);
        if (i > 32767) {
            i -= 0x10000;
        }
        return i;
    }

    public int readUnsignedShort() {
        return readShort() & 0xffff;
    }

    public int readUnsignedShortA() {
        return readShortA() & 0xffff;
    }

    public int readUnsignedLEShort() {
        return readLEShort() & 0xffff;
    }

    public int readUnsignedLEShortA() {
        return readLEShortA() & 0xffff;
    }

    /*
     * End of 16 bit reading methods.
     */

    /*
     * Start of 32 bit reading methods.
     */

    public int readInt() {
        return (readUnsigned() << 24) | (readUnsigned() << 16) | (readUnsigned() << 8) | readUnsigned();
    }

    public int readInt1() {
        return (readUnsigned() << 8) | readUnsigned() | (readUnsigned() << 24) | (readUnsigned() << 16);
    }

    public int readInt2() {
        return (readUnsigned() << 16) | (readUnsigned() << 24) | readUnsigned() | (readUnsigned() << 8);
    }

    public int readLEInt() {
        return readUnsigned() | (readUnsigned() << 8) | (readUnsigned() << 16) | (readUnsigned() << 24);
    }

    /*
     * End of 32 bit reading methods.
     */

    /*
     * Start of 64 bit reading methods.
     */

    public long readLong() {
        return (readUnsigned() << 56) | (readUnsigned() << 48) | (readUnsigned() << 40) | (readUnsigned() << 32) | (readUnsigned() << 24) | (readUnsigned() << 16) | (readUnsigned() << 8) | readUnsigned();
    }

    /*
     * End of 64 bit reading methods.
     */

    /*
     * Start of variable sized reading methods.
     */

    public int readSmart() {
        int value = readUnsigned();
        if (value < 128) {
            return value;
        }
        return value << 8 | readUnsigned() - 32768;
    }

    public int readBits(int numBits) {
        if (bitPos == -1) {
            calculateBitPosition();
        }

        int bytePos = bitPos >> 3;
        int bitOffset = 8 - (bitPos & 7);
        int value = 0;
        bitPos += numBits;
        pos = (bitPos + 7) / 8;
        for (; numBits > bitOffset; bitOffset = 8) {
            value += (buffer[bytePos++] & BIT_MASK[bitOffset]) << numBits - bitOffset;
            numBits -= bitOffset;
        }
        if (numBits == bitOffset)
            value += buffer[bytePos] & BIT_MASK[bitOffset];
        else
            value += buffer[bytePos] >> bitOffset - numBits & BIT_MASK[numBits];
        return value;
    }

    public String readString() {
        StringBuffer string = new StringBuffer();
        int i;
        while ((i = read() & 0xff) != 0) {
            string.append((char) i);
        }
        return string.toString();
    }

    public String readRS2String() {
        StringBuffer string = new StringBuffer();
        int i;
        while ((i = read() & 0xff) != 10) {
            string.append((char) i);
        }
        return string.toString();
    }

    public Object readObject() {
        try {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(buffer));
            Object object = stream.readObject();
            stream.close();
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * End of variable sized reading methods.
     */

    @Override
    public String toString() {
        byte[] data = new byte[getLength() > 16 ? 16 : getLength()];
        System.arraycopy(buffer, 0, data, 0, data.length);
        String dataPreview = Arrays.toString(data);
        if (getLength() > 16) {
            dataPreview = dataPreview.substring(0, dataPreview.length() - 1) + ", ...]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("opcode:").append(opcode).append(", length:").append(getLength()).append(", data:").append(dataPreview);
        return sb.toString();
    }
}
