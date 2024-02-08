package com.runescape.adapter.protocol;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
 
import com.runescape.Static;
import com.runescape.io.sql.SQLSession;
import com.runescape.network.FrameBuilder;
import com.runescape.utility.Logging;
import com.runescape.utility.Pool;
import com.runescape.utility.Streams;
import com.runescape.utility.crypt.XTEA;
 
/**
 * @author Seth Rogen
 */
 
 
public class AccountConnectCodec {
	
	private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/argoury";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final Logger logger = Logging.log();
    
    public static void decodeDetails(IoSession session, IoBuffer buffer) { 
        int rsaBlockSize, rsaHeaderKey, lang;
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
                //
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
            lang = buffer2.get();
            /**
             * Checks to make sure its a actual email or not.
             */
            if(!isValidEmail(email)) { 
            	session.write(new FrameBuilder(1).writeByte(21).toFrame()).addListener(IoFutureListener.CLOSE);
            }
            /** 
             * SQL query to check if email exists in database
             */
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String query = "SELECT COUNT(*) FROM members WHERE email = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setString(1, email);
                        try (ResultSet resultSet = statement.executeQuery()) {
                            if (resultSet.next()) {
                                int count = resultSet.getInt(1);
                                if (count > 0) {
                                	session.write(new FrameBuilder(1).writeByte(20).toFrame()).addListener(IoFutureListener.CLOSE);
                                    System.out.println("Email exists in database.");
                                } else {
                                    System.out.println("Email does not exist in database.");
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            System.out.println("Email Address: " + email);
            session.write(new FrameBuilder(1).writeByte(2).toFrame()).addListener(IoFutureListener.CLOSE);
    	}

    	public static boolean isValidEmail(String email) {
    		Pattern pattern = Pattern.compile(EMAIL_REGEX);
    		Matcher matcher = pattern.matcher(email);
    		return matcher.matches();
    }
}