package com.runescape.io.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.runescape.utility.Configuration;
import com.runescape.utility.Poolable;

/**
 * @author Lazaro
 */
public class SQLSession implements Poolable {
    private static boolean loadedDriver = false;

    @SuppressWarnings("unused")
    private Configuration cfg;
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public Configuration cfg() {
        return cfg;
    }

    public synchronized Statement createStatement() throws SQLException {
        try {
            Statement statement = connection.createStatement();
            return statement;
        } catch (SQLException e) {
            throw e;
        }
    }

    public void init(Configuration configuration) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        this.cfg = configuration;

        // Set up the properties
        Properties properties = new Properties();
        properties.put("user", configuration.getString("sql_user"));
        properties.put("password", configuration.getString("sql_pass"));
        properties.put("autoReconnect", configuration.getString("sql_reconnect"));
        properties.put("maxReconnects", configuration.getString("sql_max_reconnects"));

        if (!loadedDriver) {
            // Load the SQL driver
            Class.forName(configuration.getString("sql_driver")).newInstance();
            loadedDriver = true;
        }

        // Connect to the server
        connection = DriverManager.getConnection(configuration.getString("sql_host"), properties);

        createStatement().execute("use " + configuration.getString("sql_database"));
    }

    @Override
    public boolean expired() {
        try {
            return connection.isClosed() || !connection.isValid(100);
        } catch (SQLException ex) {
            return false;
        }
    }

    @Override
    public void recycle() {
    }
}
