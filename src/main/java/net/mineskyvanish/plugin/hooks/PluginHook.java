package net.mineskyvanish.plugin.hooks;

import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Represents a hook into a plugin to support its functionality or make it support MineSkyVanish's
 * functionality
 */
public abstract class PluginHook implements Listener {

    protected final MineSkyVanish mineSkyVanish;
    protected Plugin plugin;

    public PluginHook(MineSkyVanish mineSkyVanish) {
        this.mineSkyVanish = mineSkyVanish;
    }

    public void onPluginEnable(Plugin plugin) {
    }

    public void onPluginDisable(Plugin plugin) {
    }

    public Plugin getPlugin() {
        return plugin;
    }

    void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
}
