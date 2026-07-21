package net.mineskyvanish.plugin.features;

import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.event.Listener;

/**
 * Represents a toggleable feature of MineSkyVanish
 */
public abstract class Feature implements Listener {

    protected final MineSkyVanish plugin;

    public Feature(MineSkyVanish plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public abstract boolean isActive();

    protected void delay(Runnable runnable) {
        plugin.getServer().getScheduler().runTaskLater(plugin, runnable, 1);
    }
}
