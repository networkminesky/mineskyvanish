package net.mineskyvanish.plugin;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;


/**
 * Holds additional information about players in the context of vanishing
 */
public class VanishPlayer {

    private final MineSkyVanish plugin;
    private final UUID playerUUID;
    private boolean itemPickUps;
    private int seePermissionLevel, usePermissionLevel;

    VanishPlayer(Player player, MineSkyVanish plugin, boolean itemPickUps) {
        this.plugin = plugin;
        this.playerUUID = player.getUniqueId();
        this.itemPickUps = itemPickUps;
        if (plugin.getSettings().getBoolean("IndicationFeatures.LayeredPermissions.LayeredSeeAndUsePermissions", false)) {
            seePermissionLevel = plugin.getLayeredPermissionLevel(player, "see");
            usePermissionLevel = plugin.getLayeredPermissionLevel(player, "use");
        }
    }

    public boolean isOnlineVanished() {
        return plugin.getVanishStateMgr().isVanished(playerUUID);
    }

    public boolean hasItemPickUpsEnabled() {
        return itemPickUps;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getSeePermissionLevel() {
        return seePermissionLevel;
    }

    public int getUsePermissionLevel() {
        return usePermissionLevel;
    }

    public void setItemPickUps(boolean itemPickUps) {
        this.itemPickUps = itemPickUps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VanishPlayer that = (VanishPlayer) o;
        return Objects.equals(playerUUID, that.playerUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID);
    }
}
