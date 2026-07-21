package net.mineskyvanish.api;

import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Stable integration contract for plugins that want to respect StealthVanish.
 *
 * <p>This API intentionally exposes vanish state and player-facing state changes
 * only. It does not fake disconnect events, rewrite Bukkit online-player
 * collections, or depend on Minecraft internals. Integrations should filter
 * their own surfaces with {@link #canSee(CommandSender, Player)} or
 * {@link #shouldHideFrom(CommandSender, Player)}.
 */
public interface MineSkyVanishAPI {
    boolean isInvisible(UUID playerId);

    /**
     * Name-based state check for surfaces that only expose player-name strings.
     *
     * <p>This is intentionally best-effort and uses the names StealthVanish has
     * seen for currently vanished players. Prefer UUID or Player checks when a
     * real Player object is already available.
     */
    boolean isInvisibleName(String playerName);

    /**
     * Vanishes a player using the same durable state and presence surfaces as /stealth.
     *
     * <p>The implementation is responsible for Folia-safe scheduling. Callers
     * should treat this as a requested state change, not as direct packet work.
     */
    void hidePlayer(Player player);

    /**
     * Reveals a player using the same durable state and presence surfaces as /stealth.
     *
     * <p>The implementation is responsible for Folia-safe scheduling. Callers
     * should treat this as a requested state change, not as direct packet work.
     */
    void showPlayer(Player player);

    boolean canSee(CommandSender viewer, UUID targetId);

    default boolean isInvisible(Player player) {
        return player != null && isInvisible(player.getUniqueId());
    }

    default boolean canSee(CommandSender viewer, Player target) {
        return target == null || canSee(viewer, target.getUniqueId());
    }

    default boolean shouldHideFrom(CommandSender viewer, Player target) {
        return target != null && isInvisible(target) && !canSee(viewer, target);
    }

    default boolean shouldHideNameFrom(CommandSender viewer, String playerName) {
        if (playerName == null || !isInvisibleName(playerName)) {
            return false;
        }

        if (viewer == null) {
            return true;
        }

        if (viewer.hasPermission("stealthvanish.see")) {
            return false;
        }

        return !(viewer instanceof Player viewerPlayer && viewerPlayer.getName().equalsIgnoreCase(playerName));
    }

    /**
     * Backward-compatible wording for older code. New integrations should use
     * {@link #isInvisible(UUID)} so the public API matches the respect contract.
     */
    @Deprecated(forRemoval = false)
    default boolean isVanished(UUID playerId) {
        return isInvisible(playerId);
    }
}
