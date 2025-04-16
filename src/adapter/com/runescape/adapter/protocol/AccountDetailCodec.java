package com.runescape.adapter.protocol;
 
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
 
import com.runescape.Static;
import com.runescape.network.FrameBuilder;
import com.runescape.utility.Logging;
import com.runescape.utility.Streams;
import com.runescape.utility.crypt.XTEA;
 
/**
 * @author Seth Rogen
 */
 
 
public class AccountDetailCodec {
	
    private static final Logger logger = Logging.log();
    
    private static final String DB_URL = "jdbc:mysql://localhost:3307/argoury";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void decodeDetails(IoSession session, IoBuffer buffer) { 
        int rsaBlockSize, rsaHeaderKey, affId, lang, game, uuid,  additionalInfo, age;
        String password, email;
        boolean subscribe;
        int[] xteaKey;
        byte[] xteaBlock;
        long userFlow;
        XTEA xtea;
        IoBuffer buffer2;
        	int size = buffer.getShort(); //PacketSize
            if (size > buffer.remaining()) {
                return;
            }
            int clientRevision = buffer.getShort();
            if (clientRevision != Static.clientConf.getClientVersion()) {
            }
            rsaBlockSize = buffer.getShort(); // RSA block size
            if (rsaBlockSize > buffer.remaining()) {
                return;
            }
            rsaHeaderKey = buffer.get(); // RSA header key
            if (rsaHeaderKey != 10) {
                logger.warn("Incorrect RSA header key: " + rsaHeaderKey);
            }
            xteaKey = new int[4];
            for (int i = 0; i < xteaKey.length; i++) {
                xteaKey[i] = buffer.getInt();
            }
            buffer.skip(4 * 10 + 2);
            xteaBlock = new byte[buffer.remaining()];
            buffer.get(xteaBlock);
            xtea = new XTEA(xteaKey);
            xtea.decrypt(xteaBlock, 0, xteaBlock.length);
            buffer2 = IoBuffer.wrap(xteaBlock);
            email = Streams.readString(buffer2);
            affId = buffer2.getShort();
            password = Streams.readString(buffer2);
            userFlow = buffer2.getLong();
            lang = buffer2.get();
            game = buffer2.get();
            for (int i = 0; i < 3; i++) {
                uuid = buffer2.get();
            }
            additionalInfo = buffer2.get();
            age = buffer2.get();
            subscribe = buffer2.get() == 1;
            long currentTime = System.currentTimeMillis() / 1000;  // Convert to seconds
            System.out.println("Account Creation Email = " + email + ", " + currentTime + ", " + age + ", " + password);
            /**
             * SQL query to insert new user into the database
             */
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            	
            	String getMaxIdQuery = "SELECT MAX(member_id) FROM members";
            	int nextId = 0;
            	try (Statement idStmt = connection.createStatement();
            	     ResultSet rs = idStmt.executeQuery(getMaxIdQuery)) {
            	    if (rs.next()) {
            	        nextId = rs.getInt(1) + 1;
            	    }
            	}
                String hashedPassword;

                try {
                    hashedPassword = hashPassword(password);
                } catch (Exception e) {
                    logger.error("Error hashing password", e);
                    session.write(new FrameBuilder(1).writeByte(21).toFrame()).addListener(IoFutureListener.CLOSE);
                    return;
                }

                String insertQuery = "INSERT INTO members (username, password, member_rights, displayname, age, country, email, register_ip, register_date, last_ip, gender, member_banned) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			     try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
			         String generatedUsername = generateRandomName();  // Generates something like "user1234"
			         String registerDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // SQL DATETIME format
			         statement.setString(1, generatedUsername);       // username
			         statement.setString(2, hashedPassword);          // password (MD5)
			         statement.setInt(3, 4);                          // member_rights (e.g. normal member)
			         statement.setString(4, generatedUsername);       // displayname = same as username
			         statement.setInt(5, age);                    // dob (optional: format like "2000-01-01")
			         statement.setInt(6, 0);                          // country (0 = unknown/default)
			         statement.setString(7, email);                   // email
			         statement.setString(8, "127.0.0.1");             // register_ip
			         statement.setString(9, registerDate);            // register_date
			         statement.setString(10, "127.0.0.1");            // last_ip (can be same as register_ip)
			         statement.setInt(11, 0);                         // gender (0 = unspecified)
			         statement.setInt(12, 0);                         // member_banned (0 = not banned)
			         int rowsInserted = statement.executeUpdate();
			         if (rowsInserted > 0) {
			             logger.info("User successfully registered: " + generatedUsername + " (" + email + ")");
			             session.write(new FrameBuilder(1).writeByte(2).toFrame()); // success
			         } else {
			             session.write(new FrameBuilder(1).writeByte(21).toFrame()); // general error
			         }
			     }
            } catch (SQLException e) {
                logger.error("Database error during registration", e);
                session.write(new FrameBuilder(1).writeByte(21).toFrame()).addListener(IoFutureListener.CLOSE);
            }
            //if (Static.isLink())  { 
            	//Static.currentLink().getDBLoader().createMember("", 0, email, currentTime, "127.0.0.1", "", 0, 0, age, "", "", "", password, false);
            //}
            session.write(new FrameBuilder(1).writeByte(2).toFrame()).addListener(IoFutureListener.CLOSE);
    		}
    
    private static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    private static String generateRandomName() {
        String prefix = "user";
        int number = new Random().nextInt(9000) + 1000; // 4-digit number from 1000–9999
        return prefix + number;
    }
    private static String hashPassword(String password) throws Exception {
        return md5(password);
    }
}