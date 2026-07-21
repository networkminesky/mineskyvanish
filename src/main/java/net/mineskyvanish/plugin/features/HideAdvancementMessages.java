package net.mineskyvanish.plugin.features;

import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.awt.*;

// This feature is paper-only because the PlayerAdvancementDoneEvent#message() method doesn't exist in Spigot
public class HideAdvancementMessages extends Feature {

    private boolean suppressErrors = false;

    public HideAdvancementMessages(MineSkyVanish plugin) {
        super(plugin);
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        try {
            Player p = e.getPlayer();
            Component message = (Component) e.message();
            if (message == null) return;
            if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
            if (e.message() == null) return;
            e.message(null);
            p.sendMessage((net.kyori.adventure.text.Component) message);
        } catch (Exception er) {
            if (!suppressErrors) {
                plugin.logException(er);
                suppressErrors = true;
            }
        }
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("MessageOptions.HideAdvancementMessages", true);
    }
}
