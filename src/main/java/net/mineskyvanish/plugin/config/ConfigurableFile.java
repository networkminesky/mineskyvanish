package net.mineskyvanish.plugin.config;

import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class ConfigurableFile implements PluginFile<FileConfiguration> {

    private final MineSkyVanish plugin;
    private String name;
    private File file;
    private FileConfiguration fileConfiguration;
    private FileConfiguration defaultFileConfiguration;

    public ConfigurableFile(String name, MineSkyVanish plugin) {
        this.name = name + ".yml";
        this.plugin = plugin;
        setup();
    }

    @Override
    public String getName() {
        return name;
    }

    private void setup() {
        file = new File(plugin.getDataFolder(), name);
        try (Reader reader = new InputStreamReader(plugin.getResource(name))) {
            defaultFileConfiguration = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        save();
    }

    @Override
    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reload();
        }
        return fileConfiguration;
    }

    @Override
    public void save() {
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }

    public FileConfiguration getDefaultConfig() {
        return defaultFileConfiguration;
    }
}
