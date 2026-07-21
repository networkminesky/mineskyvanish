package net.mineskyvanish.plugin.config;

import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.logging.Level;

import static net.mineskyvanish.plugin.MineSkyVanish.NON_REQUIRED_MESSAGES_UPDATES;
import static net.mineskyvanish.plugin.MineSkyVanish.NON_REQUIRED_SETTINGS_UPDATES;

public class ConfigMgr {

    private final MineSkyVanish plugin;
    private final FileMgr fileMgr;
    private boolean settingsUpdateRequired, messagesUpdateRequired;
    private FileConfiguration settings, messages, playerData;
    private ConfigurableFile messagesFile, settingsFile;
    private StorageFile playerDataFile;

    public ConfigMgr(MineSkyVanish plugin) {
        this.plugin = plugin;
        fileMgr = new FileMgr(plugin);
    }

    public void prepareFiles() {
        // messages
        messagesFile = (ConfigurableFile) fileMgr.addFile("messages", FileMgr.FileType.CONFIG);
        messages = messagesFile.getConfig();
        // settings
        settingsFile = (ConfigurableFile) fileMgr.addFile("config", FileMgr.FileType.CONFIG);
        settings = settingsFile.getConfig();
        // data
        playerDataFile = (StorageFile) fileMgr.addFile("data", FileMgr.FileType.STORAGE);
        playerData = playerDataFile.getConfig();
        playerData.addDefault("InvisiblePlayers", Collections.emptyList());
        playerData.options().copyDefaults(true);
        playerData.options().header("MineSkyVanish v" + plugin.getDescription().getVersion() + " - Data file");
        playerDataFile.save();

        checkFilesForLeftOvers();
    }

    public void checkFilesForLeftOvers() {
        try {
            String currentSettingsVersion = settings.getString("ConfigVersion");
            String newestVersion = plugin.getDescription().getVersion();
            String currentMessagesVersion = messages.getString("MessagesVersion");
            messagesUpdateRequired = fileRequiresRecreation(currentMessagesVersion, false);
            settingsUpdateRequired = fileRequiresRecreation(currentSettingsVersion, true);
            if (newestVersion.equals(currentSettingsVersion))
                settingsUpdateRequired = false;
            if (newestVersion.equals(currentMessagesVersion))
                messagesUpdateRequired = false;
            if (settingsUpdateRequired || messagesUpdateRequired) {
                String currentVersion = plugin.getDescription().getVersion();
                boolean isDismissed = playerData.getBoolean("PlayerData.CONSOLE.dismissed."
                        + currentVersion.replace(".", "_"), false);
                if (!isDismissed) plugin.log(Level.WARNING, "At least one config file is outdated, " +
                        "it's recommended to regenerate it using '/msv recreatefiles'");
            }
            if (currentSettingsVersion.startsWith("1.5.") || currentSettingsVersion.startsWith("1.4.")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "You have a very outdated " +
                        "config file, your settings will not work until you regenerate your MSV-files " +
                        "using /msv recreatefiles");
            }
        } catch (Exception e) {
            plugin.logException(e);
        }
    }

    private boolean fileRequiresRecreation(String currentVersion, boolean isSettingsFile) {
        if (currentVersion == null) return true;
        for (String ignoredVersion : isSettingsFile ? NON_REQUIRED_SETTINGS_UPDATES
                : NON_REQUIRED_MESSAGES_UPDATES) {
            if (currentVersion.equalsIgnoreCase(ignoredVersion)) return false;
        }
        return true;
    }

    public FileMgr getFileMgr() {
        return fileMgr;
    }

    public FileConfiguration getPlayerData() {
        return playerData;
    }

    public StorageFile getPlayerDataFile() {
        return playerDataFile;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public ConfigurableFile getMessagesFile() {
        return messagesFile;
    }

    public ConfigurableFile getSettingsFile() {
        return settingsFile;
    }

    public FileConfiguration getSettings() {
        return settings;
    }

    public boolean isSettingsUpdateRequired() {
        return settingsUpdateRequired;
    }

    public boolean isMessagesUpdateRequired() {
        return messagesUpdateRequired;
    }

    public void setSettings(FileConfiguration settings) {
        this.settings = settings;
    }

    public void setMessages(FileConfiguration messages) {
        this.messages = messages;
    }

    public void setPlayerData(FileConfiguration playerData) {
        this.playerData = playerData;
    }
}
