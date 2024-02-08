package com.runescape.io;

import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Lazaro
 */
public class XMLSession {
    private final XStream xStream;

    public XMLSession() {
        xStream = new XStream();
        xStream.setMode(XStream.NO_REFERENCES);
    }

    /**
     * Loads the class aliases that simplify XML output.
     *
     * @param filePath The path of the XML file which stores the aliases.
     * @throws IOException            Error reading file.
     * @throws ClassNotFoundException Could not find the class specified in the XML file.
     */
    public void loadAliases(String filePath) throws IOException, ClassNotFoundException {
        Properties aliases = new Properties();
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream inputStream = new FileInputStream(file);
            aliases.loadFromXML(inputStream);
        }
        for (Enumeration<?> e = aliases.propertyNames(); e.hasMoreElements(); ) {
            String alias = (String) e.nextElement();
            Class<?> aliasClass = Class.forName((String) aliases.get(alias));
            xStream.alias(alias, aliasClass);
        }
    }

    /**
     * Reads an <code>Object</code> from an XML file.
     *
     * @param filePath The path of the XML file.
     * @return The object.
     * @throws IOException Error reading file.
     */
    @SuppressWarnings("unchecked")
    public <T> T readObject(String filePath) throws IOException {
        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);
        if (filePath.endsWith(".gz")) {
            inputStream = new GZIPInputStream(inputStream);
        }
        try {
            return (T) xStream.fromXML(inputStream);
        } finally {
            inputStream.close();
        }
    }

    /**
     * Writes an XML file from an <code>Object</code>.
     *
     * @param object   The object to write.
     * @param filePath The path to the XML file.
     * @throws IOException Error writing file.
     */
    public void writeObject(Object object, String filePath) throws IOException {
        File file = new File(filePath);
        OutputStream outputStream = new FileOutputStream(file);
        if (filePath.endsWith(".gz")) {
            outputStream = new GZIPOutputStream(outputStream);
        }
        xStream.toXML(object, outputStream);
        outputStream.close();
    }
}
