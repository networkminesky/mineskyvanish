package net.mineskyvanish.plugin;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class LayeredPermissionChecker {

    private final MineSkyVanish plugin;
    private final FileConfiguration settings;

    public LayeredPermissionChecker(MineSkyVanish plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings();
    }

    /**
     * @return TRUE if sender has *permission* to use /vanish on, else FALSE; TRUE doesn't mean that sender can
     * actually use /vanish on
     */
    public boolean hasPermissionToVanish(CommandSender sender) {
        if (settings.getBoolean(
                "IndicationFeatures.LayeredPermissions.LayeredSeeAndUsePermissions", false)) {
            if (sender.hasPermission("mineskyvanish.use")) return true;
            int permissionLevel;
            if (sender instanceof Player)
                permissionLevel = plugin.getVanishPlayer((Player) sender).getUsePermissionLevel();
            else permissionLevel = getLayeredPermissionLevel(sender, "use");
            return permissionLevel > 0 && sender.hasPermission("mineskyvanish.use.level" + permissionLevel);
        } else return sender.hasPermission("mineskyvanish.use");
    }

    public boolean hasPermissionToSee(Player viewer, Player viewed) {
        if (viewer == null)
            throw new IllegalArgumentException("viewer cannot be null");
        if (viewer == viewed) return true;
        if (settings.getBoolean(
                "IndicationFeatures.LayeredPermissions.LayeredSeeAndUsePermissions", false)) {
            VanishPlayer vanishViewer = plugin.getVanishPlayer(viewer);
            VanishPlayer vanishViewed = plugin.getVanishPlayer(viewed);
            int viewerLevel = vanishViewer.getSeePermissionLevel();
            if (viewerLevel == 0) return false;
            int viewedLevel = Math.max(1, vanishViewed.getUsePermissionLevel());
            return viewerLevel >= viewedLevel;
        } else {
            boolean enableSeePermission = settings
                    .getBoolean("IndicationFeatures.LayeredPermissions.EnableSeePermission", true);
            return enableSeePermission && viewer.hasPermission("mineskyvanish.see");
        }
    }

    public int getLayeredPermissionLevel(CommandSender sender, String permission) {
        boolean enableSeePermission = settings
                .getBoolean("IndicationFeatures.LayeredPermissions.EnableSeePermission", true);
        if (!enableSeePermission && permission.equalsIgnoreCase("see"))
            return 0;
        int maxLevel = settings.getInt("IndicationFeatures.LayeredPermissions.MaxLevel", 100);
        int level = sender.hasPermission("mineskyvanish." + permission) ? 1 : 0;
        for (int i = 1; i <= maxLevel; i++)
            if (sender.hasPermission("mineskyvanish." + permission + ".level" + i))
                level = i;
        return level;
    }
}
