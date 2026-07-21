package net.mineskyvanish.listener;

import net.mineskyvanish.visibility.VanishService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerVisibilityListener implements Listener {
    private final VanishService vanishService;

    public PlayerVisibilityListener(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (this.vanishService.isInvisible(event.getPlayer().getUniqueId())) {
            event.joinMessage(null);
        }

        this.vanishService.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.vanishService.isInvisible(event.getPlayer().getUniqueId())) {
            event.quitMessage(null);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        // Some client state is rebuilt during dimension changes. Reapply the API-only visibility rules.
        this.vanishService.refreshViewer(event.getPlayer());
        this.vanishService.refreshVanishedPlayer(event.getPlayer().getUniqueId());
    }
}
