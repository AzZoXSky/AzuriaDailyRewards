package fr.azuriapvp.dailyrewards;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private Connection connection;
    private AzuriaDailyRewards plugin;
    
    public DatabaseManager(AzuriaDailyRewards plugin) {
        this.plugin = plugin;
    }
    
    public void init() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File databaseFile = new File(dataFolder, "userdata.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS cooldowns (uuid TEXT PRIMARY KEY, cooldown INTEGER)");
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Impossible d'établir la connexion SQLite");
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void close() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
