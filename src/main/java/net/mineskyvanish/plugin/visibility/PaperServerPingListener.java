package net.mineskyvanish.plugin.visibility;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PaperServerPingListener implements Listener {

    private boolean errorLogged = false;

    private final MineSkyVanish plugin;

    public PaperServerPingListener(MineSkyVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerListPing(PaperServerListPingEvent e) {
        try {
            final FileConfiguration settings = plugin.getSettings();

            boolean adjustAmount = settings.getBoolean("ExternalInvisibility.ServerList.AdjustAmountOfOnlinePlayers", true);
            boolean adjustList = settings.getBoolean("ExternalInvisibility.ServerList.AdjustListOfLoggedInPlayers", true);

            if (!adjustAmount && !adjustList) {
                return;
            }

            Collection<UUID> onlineVanishedPlayers = plugin.getVanishStateMgr().getOnlineVanishedPlayers();
            int vanishedPlayersCount = onlineVanishedPlayers.size();
            int playerCount = Bukkit.getOnlinePlayers().size();

            if (adjustAmount) {
                e.setNumPlayers(Math.max(0, playerCount - vanishedPlayersCount));
            }

            if (adjustList && e.getPlayerSample() != null) {
                List<PlayerProfile> playerSample = e.getPlayerSample();
                playerSample.removeIf(profile -> profile.getId() != null && onlineVanishedPlayers.contains(profile.getId()));
            }
        } catch (Exception er) {
            if (!errorLogged) {
                plugin.logException(er);
                errorLogged = true;
            }
        }
    }
}