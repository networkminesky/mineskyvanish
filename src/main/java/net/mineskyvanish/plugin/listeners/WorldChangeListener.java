package net.mineskyvanish.plugin.listeners;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.commands.CommandAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeListener implements Listener {

    private final MineSkyVanish plugin;

    public WorldChangeListener(MineSkyVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        try {
            final Player p = e.getPlayer();
            if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
                return;
            // check auto-reappear option
            if (plugin.getSettings().getBoolean("VanishStateFeatures.ReappearOnWorldChange")
                    || plugin.getSettings().getBoolean("VanishStateFeatures.CheckPermissionOnWorldChange")
                    && !CommandAction.VANISH_SELF.checkPermission(p, plugin)) {
                plugin.getVisibilityChanger().showPlayer(p);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}