package net.mineskyvanish.api;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class VanishAPI {
    private VanishAPI() {
    }

    /**
     * Public respect hook for other plugins.
     *
     * <p>This intentionally answers only the stable StealthVanish state:
     * "should this player be treated as invisible?" It does not fake Bukkit's
     * online-player lists, fire quit events, or depend on Minecraft internals.
     */
    public static boolean isInvisible(Player player) {
        return player != null && isInvisible(player.getUniqueId());
    }

    public static boolean isInvisible(UUID playerId) {
        if (playerId == null) {
            return false;
        }

        MineSkyVanishAPI api = service();
        return api != null && api.isInvisible(playerId);
    }

    public static boolean isInvisible(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return false;
        }

        MineSkyVanishAPI api = service();
        return api != null && api.isInvisibleName(playerName);
    }

    /**
     * Request that StealthVanish hide/vanish this player.
     *
     * <p>This is the stable public entry point for other plugins. The backing
     * service handles Folia scheduling and durable UUID state; callers do not
     * need packets, reflection, or server lifecycle tricks.
     */
    public static void hidePlayer(Player player) {
        if (player == null) {
            return;
        }

        MineSkyVanishAPI api = service();
        if (api != null) {
            api.hidePlayer(player);
        }
    }

    /**
     * Request that StealthVanish reveal/unvanish this player.
     *
     * <p>This uses the same core path as /stealth off and restores public API
     * presence surfaces that are available on the running server.
     */
    public static void showPlayer(Player player) {
        if (player == null) {
            return;
        }

        MineSkyVanishAPI api = service();
        if (api != null) {
            api.showPlayer(player);
        }
    }

    /**
     * True when a viewer is allowed to know about or see the target.
     *
     * <p>Use this when filtering command output, GUI rows, map markers,
     * placeholders, chat mentions, or any plugin-specific player surface.
     */
    public static boolean canSee(CommandSender viewer, Player target) {
        if (target == null) {
            return true;
        }

        MineSkyVanishAPI api = service();
        return api == null || api.canSee(viewer, target);
    }

    public static boolean shouldHideFrom(CommandSender viewer, Player target) {
        return target != null && isInvisible(target) && !canSee(viewer, target);
    }

    public static boolean shouldHideNameFrom(CommandSender viewer, String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return false;
        }

        MineSkyVanishAPI api = service();
        return api != null && api.shouldHideNameFrom(viewer, playerName);
    }

    private static MineSkyVanishAPI service() {
        return Bukkit.getServicesManager().load(MineSkyVanishAPI.class);
    }
}
