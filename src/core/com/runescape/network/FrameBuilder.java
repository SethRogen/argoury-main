package com.runescape.network;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import com.runescape.utility.Text;

/**
 * @author Lazaro
 */
public final class FrameBuilder {
    private final int opcode;
    private Frame.FrameType type;
    private byte[] buffer;
    private int pos = 0;
    private int bitPos = -1;

    public FrameBuilder(int estimatedSize) {
        this(-1, Frame.FrameType.FIXED, estimatedSize);
    }

    public FrameBuilder(int opcode, int size) {
        this.opcode = opcode;
        this.type = Frame.FrameType.FIXED;
        this.buffer = new byte[size + 1];
        writeByte(opcode);
    }

    public FrameBuilder(int opcode, Frame.FrameType type, int estimatedSize) {
        this.opcode = opcode;
        this.type = type;
        this.buffer = new byte[estimatedSize];
        if (!isRaw()) {
            writeByte(opcode);
            switch (type) {
                case VAR_BYTE:
                    pos++;
                    break;
                case VAR_SHORT:
                    pos += 2;
                    break;
            }
        }
    }

    public Frame toFrame() {
        if (!isRaw()) {
            int len = pos - 1;
            if (type == Frame.FrameType.VAR_SHORT) {
                len -= 2;
            } else if (type == Frame.FrameType.VAR_BYTE) {
                len--;
            }
            int origPos = pos;
            pos = 1;
            switch (type) {
                case VAR_BYTE:
                    writeByte(len);
                    break;
                case VAR_SHORT:
                    writeShort(len);
                    break;
            }
            pos = origPos;
        }
        return new Frame(opcode, type, buffer, pos);
    }

    public int getOpcode() {
        return opcode;
    }

    public Frame.FrameType getType() {
        return type;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getPosition() {
        return pos;
    }

    public boolean isRaw() {
        return opcode == -1;
    }

    /**
     * This is to be called if you are adding bits when the offset is not 0.
     *
     * @return
     */
    public FrameBuilder calculateBitPosition() {
        bitPos = pos * 8;
        return this;
    }

    private void ensure(int bytes) {
        int minimumCapacity = pos + bytes;
        if (minimumCapacity >= buffer.length) {
            int newCapacity = (buffer.length + 1) * 2;
            if (newCapacity < 0) {
                newCapacity = Integer.MAX_VALUE;
            } else if (minimumCapacity > newCapacity) {
                newCapacity = minimumCapacity;
            }

            byte[] newBuffer = new byte[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, pos);
            buffer = newBuffer;
        }
    }

    /*
     * Start of signed writing methods.
     */

    public FrameBuilder write(byte v) {
        ensure(1);
        buffer[pos++] = v;
        return this;
    }

    public FrameBuilder writeA(byte b) {
        write((byte) ((b & 0xff) + 128));
        return this;
    }

    public FrameBuilder writeC(byte b) {
        write((byte) -(b & 0xff));
        return this;
    }

    public FrameBuilder writeS(byte b) {
        write((byte) (128 - (b & 0xff)));
        return this;
    }

    /*
     * End of signed writing methods.
     */

    /*
     * Start of unsigned writing methods.
     */

    public FrameBuilder writeByte(int i) {
        write((byte) i);
        return this;
    }

    public FrameBuilder writeByteA(int i) {
        writeA((byte) i);
        return this;
    }

    public FrameBuilder writeByteC(int i) {
        writeC((byte) i);
        return this;
    }

    public FrameBuilder writeByteS(int i) {
        writeS((byte) i);
        return this;
    }

    /*
     * End of unsigned writing methods.
     */

    /*
     * Start of array writing methods.
     */

    public FrameBuilder write(byte[] bytes) {
        write(bytes, 0, bytes.length);
        return this;
    }

    public FrameBuilder write(byte[] bytes, int offset, int length) {
        ensure(length);
        System.arraycopy(bytes, offset, buffer, pos, length);
        pos += length;
        return this;
    }

    public FrameBuilder writeA(byte[] src) {
        writeA(src, 0, src.length);
        return this;
    }

    public FrameBuilder writeA(byte[] src, int offset, int length) {
        ensure(length);
        for (int i = offset; i < length; i++) {
            buffer[pos++] = (byte) ((src[i] & 0xff) + 128);
        }
        return this;
    }

    public FrameBuilder writeC(byte[] src) {
        writeC(src, 0, src.length);
        return this;
    }

    public FrameBuilder writeC(byte[] src, int offset, int length) {
        ensure(length);
        for (int i = offset; i < length; i++) {
            buffer[pos++] = (byte) -(src[i] & 0xff);
        }
        return this;
    }

    public FrameBuilder writeS(byte[] src) {
        writeS(src, 0, src.length);
        return this;
    }

    public FrameBuilder writeS(byte[] src, int offset, int length) {
        for (int i = offset; i < length; i++) {
            buffer[pos++] = (byte) (128 - (src[i] & 0xff));
        }
        return this;
    }

    public FrameBuilder writeBackwards(byte[] src) {
        writeBackwards(src, 0, src.length);
        return this;
    }

    public FrameBuilder writeBackwards(byte[] src, int offset, int length) {
        ensure(length);
        for (int i = length - 1; i >= offset; i--) {
            buffer[pos++] = src[i];
        }
        return this;
    }

    public FrameBuilder writeBackwardsA(byte[] src) {
        writeBackwardsA(src, 0, src.length);
        return this;
    }

    public FrameBuilder writeBackwardsA(byte[] src, int offset, int length) {
        ensure(length);
        for (int i = length - 1; i >= offset; i--) {
            buffer[pos++] = (byte) ((src[i] & 0xff) + 128);
        }
        return this;
    }

    public FrameBuilder writeBackwardsC(byte[] src) {
        writeBackwardsC(src, 0, src.length);
        return this;
    }

    public FrameBuilder writeBackwardsC(byte[] src, int offset, int length) {
        ensure(length);
        for (int i = length - 1; i >= offset; i--) {
            buffer[pos++] = (byte) -(src[i] & 0xff);
        }
        return this;
    }

    public FrameBuilder writeBackwardsS(byte[] src) {
        writeBackwardsS(src, 0, src.length);
        return this;
    }

    public FrameBuilder writeBackwardsS(byte[] src, int offset, int length) {
        ensure(length);
        for (int i = length - 1; i >= offset; i--) {
            buffer[pos++] = (byte) (128 - (src[i] & 0xff));
        }
        return this;
    }

    /*
     * End of array writing methods.
     */

    /*
     * Start of 16 bit writing methods.
     */

    public FrameBuilder writeShort(int i) {
        ensure(2);

        buffer[pos++] = (byte) (i >> 8);
        buffer[pos++] = (byte) i;
        return this;
    }

    public FrameBuilder writeShortA(int i) {
        ensure(2);

        buffer[pos++] = (byte) (i >> 8);
        buffer[pos++] = (byte) (i + 128);
        return this;
    }

    public FrameBuilder writeLEShort(int i) {
        ensure(2);

        buffer[pos++] = (byte) i;
        buffer[pos++] = (byte) (i >> 8);
        return this;
    }

    public FrameBuilder writeLEShortA(int i) {
        ensure(2);

        buffer[pos++] = (byte) (i + 128);
        buffer[pos++] = (byte) (i >> 8);
        return this;
    }

    /*
     * End of 16 bit writing methods.
     */

    /*
     * Start of 24 bit writing methods.
     */

    public FrameBuilder writeTriByte(int i) {
        ensure(3);

        buffer[pos++] = (byte) (i >> 16);
        buffer[pos++] = (byte) (i >> 8);
        buffer[pos++] = (byte) i;
        return this;
    }

    /*
     * End of 24 bit writing methods.
     */

    /*
     * Start of 32 bit writing methods.
     */

    public FrameBuilder writeInt(int i) {
        ensure(4);

        buffer[pos++] = (byte) (i >> 24);
        buffer[pos++] = (byte) (i >> 16);
        buffer[pos++] = (byte) (i >> 8);
        buffer[pos++] = (byte) i;
        return this;
    }

    public FrameBuilder writeInt1(int i) {
        ensure(4);

        buffer[pos++] = (byte) (i >> 8);
        buffer[pos++] = (byte) i;
        buffer[pos++] = (byte) (i >> 24);
        buffer[pos++] = (byte) (i >> 16);
        return this;
    }

    public FrameBuilder writeInt2(int i) {
        ensure(4);

        buffer[pos++] = (byte) (i >> 16);
        buffer[pos++] = (byte) (i >> 24);
        buffer[pos++] = (byte) i;
        buffer[pos++] = (byte) (i >> 8);
        return this;
    }

    public FrameBuilder writeLEInt(int i) {
        ensure(4);

        buffer[pos++] = (byte) i;
        buffer[pos++] = (byte) (i >> 8);
        buffer[pos++] = (byte) (i >> 16);
        buffer[pos++] = (byte) (i >> 24);
        return this;
    }

    /*
     * End of 32 bit writing methods.
     */

    /*
     * Start of 64 bit writing methods.
     */

    public FrameBuilder writeLong(long i) {
        ensure(8);

        buffer[pos++] = (byte) (i >> 56);
        buffer[pos++] = (byte) (i >> 48);
        buffer[pos++] = (byte) (i >> 40);
        buffer[pos++] = (byte) (i >> 32);
        buffer[pos++] = (byte) (i >> 24);
        buffer[pos++] = (byte) (i >> 16);
        buffer[pos++] = (byte) (i >> 8);
        buffer[pos++] = (byte) i;
        return this;
    }

    /*
     * End of 64 bit writing methods.
     */

    /*
     * Start of variable sized writing methods.
     */

    public FrameBuilder writeSmart(int i) {
        if (i >= 0 && i < 128) {
            writeByte(i);
        } else if (i >= 0 && i < 32768) {
            writeShort(i + 32768);
        } else {
            throw new IllegalArgumentException("Invalid smart value : " + i);
        }
        return this;
    }


    public FrameBuilder writeBits(int numBits, int value) {
        /* Prepare for adding bits */
        if (bitPos == -1) {
            calculateBitPosition();
        }
        int bytePos = bitPos >> 3;
        int bitOffset = 8 - (bitPos & 7);
        bitPos += numBits;
        ensure((numBits + 7) * 8);
        pos = (bitPos + 7) / 8;

        /* Write the bits */
        for (; numBits > bitOffset; bitOffset = 8) {
            buffer[bytePos] &= ~Frame.BIT_MASK[bitOffset];     // mask out the desired area
            buffer[bytePos++] |= (value >> (numBits - bitOffset)) & Frame.BIT_MASK[bitOffset];

            numBits -= bitOffset;
        }
        if (numBits == bitOffset) {
            buffer[bytePos] &= ~Frame.BIT_MASK[bitOffset];
            buffer[bytePos] |= value & Frame.BIT_MASK[bitOffset];
        } else {
            buffer[bytePos] &= ~(Frame.BIT_MASK[numBits] << (bitOffset - numBits));
            buffer[bytePos] |= (value & Frame.BIT_MASK[numBits]) << (bitOffset - numBits);
        }
        return this;
    }

    public FrameBuilder writeString(String string) {
        write(string.getBytes()).writeByte(0);
        return this;
    }

    public FrameBuilder writeRS2String(String string) {
        write(string.getBytes()).writeByte(10);
        return this;
    }

    public FrameBuilder writeJagString(String string) {
        byte[] packed = new byte[Text.calculateGJString2Length(string)];
        int length = Text.packGJString2(0, packed, string);
        writeByte(0).write(packed, 0, length).writeByte(0);
        return this;
    }

    public FrameBuilder writeObject(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();

            write(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /*
     * End of variable sized writing methods.
     */
}
