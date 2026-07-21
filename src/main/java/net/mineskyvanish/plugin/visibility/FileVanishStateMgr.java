package net.mineskyvanish.plugin.visibility;

import net.mineskyvanish.api.vanish.events.PlayerVanishStateChangeEvent;
import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;

public class FileVanishStateMgr extends VanishStateMgr {

    private final MineSkyVanish plugin;

    public FileVanishStateMgr(MineSkyVanish plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public boolean isVanished(final UUID uuid) {
        return getVanishedPlayersOnFile().contains(uuid);
    }

    @Override
    public void setVanishedState(final UUID uuid, String name, boolean hide, String causeName) {
        PlayerVanishStateChangeEvent event = new PlayerVanishStateChangeEvent(uuid, name, hide, causeName);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        List<String> vanishedPlayerUUIDStrings = plugin.getPlayerData().getStringList("InvisiblePlayers");
        if (hide)
            vanishedPlayerUUIDStrings.add(uuid.toString());
        else
            vanishedPlayerUUIDStrings.remove(uuid.toString());
        plugin.getPlayerData().set("InvisiblePlayers", vanishedPlayerUUIDStrings);
        if (hide)
            plugin.getPlayerData().set("PlayerData." + uuid + ".information.name", name);
        plugin.getConfigMgr().getPlayerDataFile().save();
    }

    @Override
    public Set<UUID> getVanishedPlayers() {
        return getVanishedPlayersOnFile();
    }

    @Override
    public Collection<UUID> getOnlineVanishedPlayers() {
        Set<UUID> onlineVanishedPlayers = new HashSet<>();
        for (UUID vanishedUUID : getVanishedPlayers()) {
            if (Bukkit.getPlayer(vanishedUUID) != null)
                onlineVanishedPlayers.add(vanishedUUID);
        }
        return onlineVanishedPlayers;
    }

    public UUID getVanishedUUIDFromNameOnFile(String name) {
        for (UUID uuid : getVanishedPlayersOnFile()) {
            if (plugin.getPlayerData().getString("PlayerData." + uuid + ".information.name")
                    .equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        return null;
    }

    private Set<UUID> getVanishedPlayersOnFile() {
        List<String> vanishedPlayerUUIDStrings = plugin.getPlayerData().getStringList("InvisiblePlayers");
        Set<UUID> vanishedPlayerUUIDs = new HashSet<>();
        for (String uuidString : new ArrayList<>(vanishedPlayerUUIDStrings)) {
            try {
                vanishedPlayerUUIDs.add(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                vanishedPlayerUUIDStrings.remove(uuidString);
                plugin.log(Level.WARNING,
                        "The data.yml file contains an invalid player uuid," +
                                " deleting it.");
                plugin.getPlayerData().set("InvisiblePlayers", vanishedPlayerUUIDStrings);
                plugin.getConfigMgr().getPlayerDataFile().save();
            }
        }
        return vanishedPlayerUUIDs;
    }

    private void setVanishedPlayersOnFile(Set<UUID> vanishedPlayers) {
        List<String> vanishedPlayerUUIDStrings = new ArrayList<>();
        for (UUID uuid : vanishedPlayers)
            vanishedPlayerUUIDStrings.add(uuid.toString());
        plugin.getPlayerData().set("InvisiblePlayers",
                vanishedPlayerUUIDStrings);
        plugin.getConfigMgr().getPlayerDataFile().save();
    }
}
