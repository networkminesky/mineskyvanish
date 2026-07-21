package net.mineskyvanish.plugin.config;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.MineSkyVanishPlugin;

import java.util.HashMap;
import java.util.Map;

public class FileMgr {

    private final MineSkyVanishPlugin plugin;
    private Map<String, PluginFile<?>> files;

    public FileMgr(MineSkyVanishPlugin plugin) {
        this.plugin = plugin;
        files = new HashMap<>();
    }

    public PluginFile<?> addFile(String name, FileType type) {
        if (name == null)
            throw new IllegalArgumentException("The file name cannot be null!");
        if (type == FileType.STORAGE) {
            StorageFile file = new StorageFile(name, (MineSkyVanish) plugin);
            files.put(name, file);
            return file;
        } else if (type == FileType.CONFIG) {
            ConfigurableFile file = new ConfigurableFile(name, (MineSkyVanish) plugin);
            files.put(name, file);
            return file;
        } else {
            throw new IllegalArgumentException("The FileType cannot be null!");
        }
    }

    public void reloadFile(String fileName) {
        PluginFile<?> file = files.get(fileName);
        if (file != null)
            file.reload();
        else
            throw new IllegalArgumentException("Specified file doesn't exist!");
    }

    public void reloadAll() {
        for (String fileName : files.keySet())
            reloadFile(fileName);
    }

    public enum FileType {
        STORAGE, CONFIG
    }
}
