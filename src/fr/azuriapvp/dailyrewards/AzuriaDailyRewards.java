package fr.azuriapvp.dailyrewards;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class AzuriaDailyRewards extends JavaPlugin {
    private DatabaseManager dbManager;
    private FileConfiguration messages;
    private File messagesFile;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveDefaultMessages();
        reloadMessages();
        dbManager = new DatabaseManager(this);
        dbManager.init();
        
        getCommand("dailyrewards").setExecutor(new DailyRewardCommand(this));
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
    }
    
    @Override
    public void onDisable() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
    
    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }
    
    public long getCooldown(String uuid) {
        long cooldown = 0;
        try {
            Connection conn = dbManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT cooldown FROM cooldowns WHERE uuid = ?");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cooldown = rs.getLong("cooldown");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cooldown;
    }
    
    public void reloadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "messages.yml");
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public FileConfiguration getMessages() {
        if (messages == null) {
            reloadMessages();
        }
        return messages;
    }
    
    public void saveDefaultMessages() {
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
    }
    
    public void reloadAll() {
        reloadConfig();
        reloadMessages();
    }
}
