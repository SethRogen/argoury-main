package com.runescape.utility;

import org.apache.mina.core.buffer.IoBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author Lazaro
 */
public class Streams {
    public static String readRS2String(ByteBuffer buffer) {
        StringBuffer string = new StringBuffer();
        int i;
        while ((i = buffer.get() & 0xff) != 10) {
            string.append((char) i);
        }
        return string.toString();
    }

    public static String readRS2String(IoBuffer buffer) {
        StringBuffer string = new StringBuffer();
        int i;
        while ((i = buffer.get() & 0xff) != 10) {
            string.append((char) i);
        }
        return string.toString();
    }

    public static String readRS2String(InputStream in) throws IOException {
        StringBuffer string = new StringBuffer();
        int i;
        while ((i = in.read()) != 10 && i != -1) {
            string.append((char) i);
        }
        return string.toString();
    }

    public static int readSmart(InputStream in) throws IOException {
        int value = in.read();
        if (value == -1)
            throw new IOException("EOF");
        if (value < 128)
            return value;
        int value2 = in.read();
        if (value == -1)
            throw new IOException("EOF");
        return value << 8 | value2 - 32768;
    }

    public static int readSmart(ByteBuffer buffer) {
        int value = buffer.get() & 0xff;
        if (value < 128)
            return value;
        int value2 = buffer.get() & 0xff;
        return (value << 8 | value2) - 32768;
    }

    public static int readSmart2(ByteBuffer buffer) {
        int value = 0;
        int i;
        for (i = readSmart(buffer); i == 32767; i = readSmart(buffer))
            value += 32767;
        value += i;
        return value;
    }

    public static String readString(ByteBuffer buffer) {
        StringBuffer string = new StringBuffer();
        int i;
        while ((i = buffer.get() & 0xff) != 0) {
            string.append((char) i);
        }
        return string.toString();
    }

    public static String readString(IoBuffer buffer) {
        StringBuffer string = new StringBuffer();
        int i;
        while ((i = buffer.get() & 0xff) != 0) {
            string.append((char) i);
        }
        return string.toString();
    }

    public static String readString(InputStream in) throws IOException {
        StringBuffer string = new StringBuffer();
        int i;
        while ((i = in.read()) != 0 && i != -1) {
            string.append((char) i);
        }
        return string.toString();
    }

    public static void writeString(String string, OutputStream out) throws IOException {
        out.write(string.getBytes());
        out.write(0);
    }

    public static void writeVarString(String string, OutputStream out) throws IOException {
        byte[] stringBytes = string.getBytes("UTF-8");
        out.write(stringBytes.length >> 8);
        out.write(stringBytes.length & 0xff);
        out.write(stringBytes);
    }

    public static String writeVarString(InputStream in) throws IOException {
        int value = in.read();
        if (value == -1)
            throw new IOException("EOF");
        int value2 = in.read();
        if (value == -1)
            throw new IOException("EOF");
        byte[] stringBytes = new byte[(value & 0xff) << 8 | value2 & 0xff];
        in.read(stringBytes);
        return new String(stringBytes, "UTF-8");
    }

    public static void writeInt(int val, int index, byte[] buffer) {
        buffer[index++] = (byte) (val >> 24);
        buffer[index++] = (byte) (val >> 16);
        buffer[index++] = (byte) (val >> 8);
        buffer[index++] = (byte) val;
    }

    public static int readInt(int index, byte[] buffer) {
        return ((buffer[index++] & 0xff) << 24) | ((buffer[index++] & 0xff) << 16) | ((buffer[index++] & 0xff) << 8) | (buffer[index++] & 0xff);
    }
}
