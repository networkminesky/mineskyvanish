package net.mineskyvanish.plugin.visibility.hiders;

import com.google.common.collect.ImmutableSet;
import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PlayerHider implements Listener {

    protected final MineSkyVanish plugin;
    protected final Map<Player, Set<Player>> playerHiddenFromPlayersMap = new ConcurrentHashMap<>();

    public PlayerHider(MineSkyVanish plugin) {
        this.plugin = plugin;
        registerQuitListener();
    }

    public abstract String getName();

    public boolean isHidden(Player player, Player viewer) {
        return !player.getUniqueId().equals(viewer.getUniqueId())
                && playerHiddenFromPlayersMap.containsKey(player)
                && playerHiddenFromPlayersMap.get(player).contains(viewer);
    }

    public boolean isHidden(UUID playerUUID, Player viewer) {
        if (playerUUID.equals(viewer.getUniqueId())) return false;
        for (Player p : playerHiddenFromPlayersMap.keySet()) {
            if (p.getUniqueId().equals(playerUUID)) {
                return playerHiddenFromPlayersMap.get(p).contains(viewer);
            }
        }
        return false;
    }

    public boolean isHidden(String playerName, Player viewer) {
        if (playerName.equalsIgnoreCase(viewer.getName())) return false;
        for (Player p : playerHiddenFromPlayersMap.keySet()) {
            if (p.getName().equalsIgnoreCase(playerName)) {
                return playerHiddenFromPlayersMap.get(p).contains(viewer);
            }
        }
        return false;
    }

    /**
     * @return TRUE if the operation changed the state, FALSE if it did not
     */
    public boolean setHidden(Player player, Player viewer, boolean hidden) {
        if (!playerHiddenFromPlayersMap.containsKey(player))
            playerHiddenFromPlayersMap.put(player, new HashSet<>());
        if (viewer == player) return false;
        Set<Player> hiddenFromPlayers = playerHiddenFromPlayersMap.get(player);
        if (hidden && !hiddenFromPlayers.contains(viewer)) {
            hiddenFromPlayers.add(viewer);
            return true;
        } else if (!hidden && hiddenFromPlayers.contains(viewer)) {
            hiddenFromPlayers.remove(viewer);
            return true;
        }
        return false;
    }

    public Set<Player> getHiddenPlayerKeys() {
        return playerHiddenFromPlayersMap.keySet();
    }

    private void registerQuitListener() {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler(priority = EventPriority.MONITOR)
            public void onQuit(final PlayerQuitEvent e) {
                Runnable cleanupTask = () -> {
                    playerHiddenFromPlayersMap.remove(e.getPlayer());
                    for (Player p : ImmutableSet.copyOf(playerHiddenFromPlayersMap.keySet())) {
                        Set<Player> viewers = playerHiddenFromPlayersMap.get(p);
                        if (viewers != null) {
                            viewers.remove(e.getPlayer());
                        }
                    }
                };

                plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> cleanupTask.run(), 1L);
            }
        }, plugin);
    }
}
