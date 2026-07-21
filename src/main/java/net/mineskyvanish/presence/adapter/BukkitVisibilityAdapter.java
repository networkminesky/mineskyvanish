package net.mineskyvanish.presence.adapter;

import net.mineskyvanish.presence.PresenceAdapter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class BukkitVisibilityAdapter implements PresenceAdapter {
    private final JavaPlugin plugin;

    public BukkitVisibilityAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "Bukkit visibility";
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void hide(Player viewer, Player target) {
        // Stable core: Paper/Bukkit owns the client visibility and tracking details.
        viewer.hidePlayer(this.plugin, target);
    }

    @Override
    public void show(Player viewer, Player target) {
        viewer.showPlayer(this.plugin, target);
    }
}
