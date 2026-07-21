package net.mineskyvanish.plugin.utils;

import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.entity.Player;

public class BukkitPlayerHidingUtil {

    private BukkitPlayerHidingUtil() {
    }

    public static void hidePlayer(Player player, Player viewer, MineSkyVanish plugin) {
        if (isNewPlayerHidingAPISupported(plugin))
            viewer.hidePlayer(plugin, player);
        else
            //noinspection deprecation
            viewer.hidePlayer(player);
    }

    public static void showPlayer(Player player, Player viewer, MineSkyVanish plugin) {
        if (isNewPlayerHidingAPISupported(plugin))
            viewer.showPlayer(plugin, player);
        else
            //noinspection deprecation
            viewer.showPlayer(player);
    }

    public static boolean isNewPlayerHidingAPISupported(MineSkyVanish plugin) {
        return plugin.getVersionUtil().isOneDotXOrHigher(19);
    }
}
