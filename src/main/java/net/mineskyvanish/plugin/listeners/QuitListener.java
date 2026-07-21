package net.mineskyvanish.plugin.listeners;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.commands.CommandAction;
import net.mineskyvanish.plugin.features.Broadcast;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

public class QuitListener implements EventExecutor, Listener {

    private final MineSkyVanish plugin;

    public QuitListener(MineSkyVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Listener l, Event event) {
        try {
            if (event instanceof PlayerQuitEvent) {
                PlayerQuitEvent e = (PlayerQuitEvent) event;
                FileConfiguration config = plugin.getConfig();
                Player p = e.getPlayer();
                // if is invisible
                if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                    // remove action bar
                    if (plugin.getActionBarMgr() != null && plugin.getSettings().getBoolean(
                            "MessageOptions.DisplayActionBar")) {
                        plugin.getActionBarMgr().removeActionBar(p);
                    }
                    // check auto-reappear-option
                    boolean noMsg = false;
                    if (plugin.getSettings().getBoolean("VanishStateFeatures.ReappearOnQuit")
                            || plugin.getSettings().getBoolean("VanishStateFeatures.CheckPermissionOnQuit")
                            && !CommandAction.VANISH_SELF.checkPermission(p, plugin)) {
                        plugin.getVanishStateMgr().setVanishedState(p.getUniqueId(), p.getName(), false, null);
                        // check if it should handle the quit msg
                        if (!config.getBoolean("MessageOptions.ReappearOnQuitHideLeaveMsg"))
                            noMsg = true;
                    }
                    // check remove-quit-msg option
                    if (!noMsg && config.getBoolean("MessageOptions.HideRealJoinQuitMessages")) {
                        e.setQuitMessage(null);
                        Broadcast.announceSilentQuit(p, plugin);
                    }
                }
                // remove VanishPlayer
                plugin.removeVanishPlayer(plugin.getVanishPlayer(p));
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}