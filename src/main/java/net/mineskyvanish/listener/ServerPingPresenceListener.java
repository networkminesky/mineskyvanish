package net.mineskyvanish.listener;

import java.util.Iterator;

import net.mineskyvanish.visibility.VanishService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public final class ServerPingPresenceListener implements Listener {
    private final VanishService vanishService;

    public ServerPingPresenceListener(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    @EventHandler
    @SuppressWarnings("removal")
    public void onServerListPing(ServerListPingEvent event) {
        try {
            // Optional surface: Bukkit currently exposes server-list player samples through this iterator.
            // If a future API removes or blocks it, this listener warns once and the core vanish still works.
            Iterator<Player> iterator = event.iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                if (this.vanishService.isInvisible(player.getUniqueId())) {
                    iterator.remove();
                }
            }
        } catch (RuntimeException exception) {
            this.vanishService.warnSurfaceUnavailableOnce("server-list-sample", exception);
        }
    }
}
