package net.mineskyvanish.visibility;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineskyvanish.api.MineSkyVanishAPI;
import net.mineskyvanish.diagnostic.DiagnosticRecorder;
import net.mineskyvanish.presence.PresenceAdapterRegistry;
import net.mineskyvanish.storage.VanishStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanishService implements MineSkyVanishAPI {
    private final JavaPlugin plugin;
    private final VanishStorage storage;
    private final PresenceAdapterRegistry presenceAdapters;
    private final DiagnosticRecorder diagnostics;
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<UUID, String> vanishedNames = new ConcurrentHashMap<>();
    private final Set<String> warnedUnavailableSurfaces = ConcurrentHashMap.newKeySet();

    public VanishService(JavaPlugin plugin, VanishStorage storage, PresenceAdapterRegistry presenceAdapters, DiagnosticRecorder diagnostics) {
        this.plugin = plugin;
        this.storage = storage;
        this.presenceAdapters = presenceAdapters;
        this.diagnostics = diagnostics;
    }

    public JavaPlugin plugin() {
        return this.plugin;
    }

    public void load() {
        this.vanishedPlayers.clear();
        this.vanishedPlayers.addAll(this.storage.load());
    }

    public void saveNow() {
        this.storage.save(Set.copyOf(this.vanishedPlayers));
    }

    public int vanishedCount() {
        return this.vanishedPlayers.size();
    }

    public List<String> presenceStatusLines() {
        return this.presenceAdapters.statusLines();
    }

    public Set<String> vanishedNames() {
        return Set.copyOf(this.vanishedNames.values());
    }

    @Override
    public boolean isInvisible(UUID playerId) {
        return playerId != null && this.vanishedPlayers.contains(playerId);
    }

    @Override
    public boolean isInvisibleName(String playerName) {
        return playerName != null && this.vanishedNames.containsValue(playerName.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean canSee(CommandSender viewer, UUID targetId) {
        if (!isInvisible(targetId)) {
            return true;
        }

        if (viewer == null) {
            return false;
        }

        if (viewer.hasPermission("mienskyvanish.see")) {
            return true;
        }

        return viewer instanceof Player viewerPlayer && viewerPlayer.getUniqueId().equals(targetId);
    }

    @Override
    public void hidePlayer(Player player) {
        requestInvisibleState(player, true, "VanishAPI.hidePlayer");
    }

    @Override
    public void showPlayer(Player player) {
        requestInvisibleState(player, false, "VanishAPI.showPlayer");
    }

    @Deprecated(forRemoval = false)
    public boolean isVanished(UUID playerId) {
        return isInvisible(playerId);
    }

    public void setVanished(Player player, boolean vanished) {
        transitionInvisibleState(player, vanished, true);
    }

    private void requestInvisibleState(Player player, boolean invisible, String source) {
        if (player == null) {
            return;
        }

        try {
            player.getScheduler().execute(this.plugin, () -> transitionInvisibleState(player, invisible, false),
                    () -> this.diagnostics.issue("public-api", source, "player scheduler retired before requested vanish state change", null, false), 1L);
        } catch (RuntimeException exception) {
            this.diagnostics.issue("public-api", source, "could not schedule requested vanish state change", exception, true);
        }
    }

    private boolean transitionInvisibleState(Player player, boolean vanished, boolean notifyActor) {
        UUID playerId = player.getUniqueId();
        boolean changed = vanished ? this.vanishedPlayers.add(playerId) : this.vanishedPlayers.remove(playerId);
        this.diagnostics.attempt("vanish-state", vanished ? "enable vanish" : "disable vanish", player.getName() + " uuid=" + playerId);

        if (!changed) {
            this.diagnostics.result("vanish-state", vanished ? "enable vanish" : "disable vanish", "no state change needed for " + player.getName());
            if (notifyActor) {
                player.sendMessage(Component.text(vanished ? "You are already vanished." : "You are already visible.", NamedTextColor.GRAY));
            }
            return false;
        }

        saveAsync();

        if (vanished) {
            this.vanishedNames.put(playerId, player.getName().toLowerCase(Locale.ROOT));
            refreshVanishedPlayer(playerId);
            if (notifyActor) {
                player.sendMessage(Component.text("Vanish enabled. Regular players saw you leave.", NamedTextColor.GREEN));
            }
            sendFakeQuit(player);
            return true;
        }

        this.vanishedNames.remove(playerId);
        refreshRevealedPlayer(playerId);
        if (notifyActor) {
            player.sendMessage(Component.text("Vansih disabled. Regular players saw you join.", NamedTextColor.YELLOW));
        }
        sendFakeJoin(player);
        return true;
    }

    public void handleJoin(Player joiningPlayer) {
        if (isInvisible(joiningPlayer.getUniqueId())) {
            this.vanishedNames.put(joiningPlayer.getUniqueId(), joiningPlayer.getName().toLowerCase(Locale.ROOT));
        }

        refreshViewer(joiningPlayer);

        if (isInvisible(joiningPlayer.getUniqueId())) {
            refreshVanishedPlayer(joiningPlayer.getUniqueId());
            joiningPlayer.sendMessage(Component.text("You joined while still vanished.", NamedTextColor.GRAY));
        }
    }

    public void refreshViewer(Player viewer) {
        for (UUID vanishedId : Set.copyOf(this.vanishedPlayers)) {
            if (viewer.getUniqueId().equals(vanishedId)) {
                continue;
            }

            Player vanished = Bukkit.getPlayer(vanishedId);
            if (vanished != null) {
                applyVisibility(viewer, vanished, false);
            }
        }
    }

    public void refreshVanishedPlayer(UUID vanishedId) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            scheduleForViewer(viewer, vanishedId, false);
        }
    }

    private void refreshRevealedPlayer(UUID revealedId) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            scheduleForViewer(viewer, revealedId, true);
        }
    }

    private void scheduleForViewer(Player viewer, UUID targetId, boolean visible) {
        try {
            viewer.getScheduler().execute(this.plugin, () -> {
                try {
                    Player target = Bukkit.getPlayer(targetId);
                    if (target == null) {
                        this.diagnostics.result("visibility", "apply viewer visibility", "target offline before update; target=" + targetId);
                        return;
                    }

                    applyVisibility(viewer, target, visible);
                } catch (RuntimeException exception) {
                    this.diagnostics.issue("visibility", "apply viewer visibility", "scheduler task failed for viewer=" + viewer.getName() + " target=" + targetId + " visible=" + visible, exception, true);
                    throw exception;
                }
            }, () -> this.diagnostics.issue("visibility", "apply viewer visibility", "viewer scheduler retired before visibility update; viewer=" + viewer.getName() + " target=" + targetId + " visible=" + visible, null, false), 1L);
        } catch (RuntimeException exception) {
            this.diagnostics.issue("visibility", "schedule viewer visibility", "could not schedule visibility update for viewer=" + viewer.getName() + " target=" + targetId + " visible=" + visible, exception, true);
            throw exception;
        }
    }

    private void applyVisibility(Player viewer, Player target, boolean visible) {
        if (viewer.getUniqueId().equals(target.getUniqueId())) {
            return;
        }

        // The viewer owns the client connection that receives the presence update.
        // Keep every adapter public-API based so the core survives internal changes.
        if (visible || canSee(viewer, target.getUniqueId())) {
            this.presenceAdapters.show(viewer, target);
        } else {
            this.presenceAdapters.hide(viewer, target);
        }
    }

    private void sendFakeQuit(Player vanishedPlayer) {
        Component message = Component.text(vanishedPlayer.getName() + " left the game", NamedTextColor.YELLOW);
        sendToRegularViewers(vanishedPlayer.getUniqueId(), message);
    }

    private void sendFakeJoin(Player revealedPlayer) {
        Component message = Component.text(revealedPlayer.getName() + " joined the game", NamedTextColor.YELLOW);
        sendToRegularViewers(revealedPlayer.getUniqueId(), message);
    }

    private void sendToRegularViewers(UUID actorId, Component message) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            try {
                viewer.getScheduler().execute(this.plugin, () -> {
                    try {
                        if (!viewer.getUniqueId().equals(actorId) && !viewer.hasPermission("mienskyvanish.see")) {
                            viewer.sendMessage(message);
                        }
                    } catch (RuntimeException exception) {
                        this.diagnostics.issue("fake-presence-message", "send viewer message", "scheduler task failed for viewer=" + viewer.getName() + " actor=" + actorId, exception, false);
                        throw exception;
                    }
                }, () -> this.diagnostics.issue("fake-presence-message", "send viewer message", "viewer scheduler retired before fake join/quit message; viewer=" + viewer.getName() + " actor=" + actorId, null, false), 1L);
            } catch (RuntimeException exception) {
                this.diagnostics.issue("fake-presence-message", "schedule viewer message", "could not schedule fake join/quit message for viewer=" + viewer.getName() + " actor=" + actorId, exception, false);
            }
        }
    }

    private void saveAsync() {
        Set<UUID> snapshot = Set.copyOf(this.vanishedPlayers);
        try {
            Bukkit.getAsyncScheduler().runNow(this.plugin, task -> this.storage.save(snapshot));
        } catch (RuntimeException exception) {
            this.diagnostics.issue("storage", "schedule async save", "async scheduler rejected vanish-state save; saving immediately as fallback", exception, false);
            this.storage.save(snapshot);
        }
    }

    public void warnSurfaceUnavailableOnce(String surfaceName, Exception exception) {
        if (this.warnedUnavailableSurfaces.add(surfaceName)) {
            this.plugin.getLogger().warning("Presence surface unavailable: " + surfaceName + " (" + exception.getClass().getSimpleName() + ")");
            this.diagnostics.issue("presence-surface", surfaceName, "optional public API surface failed and will be skipped after first warning", exception, false);
        }
    }
}
