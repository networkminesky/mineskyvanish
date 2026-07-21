package net.mineskyvanish.plugin.features;

import net.mineskyvanish.api.vanish.events.PlayerShowEvent;
import net.mineskyvanish.api.vanish.events.PostPlayerHideEvent;
import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Broadcast extends Feature {

    public Broadcast(MineSkyVanish plugin) {
        super(plugin);
    }

    public static void announceSilentJoin(Player vanished, MineSkyVanish plugin) {
        if (plugin.getSettings().getBoolean("MessageOptions.AnnounceRealJoinQuitToAdmins", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (vanished == onlinePlayer)
                    continue;
                if (plugin.canSee(onlinePlayer, vanished)) {
                    plugin.sendMessage(onlinePlayer, "SilentJoinMessageForAdmins", vanished, onlinePlayer);
                }
            }
        }
    }

    public static void announceSilentDeath(Player p, MineSkyVanish plugin, String deathMessage) {
        if (plugin.getSettings().getBoolean("MessageOptions.AnnounceDeathToAdmins", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (p == onlinePlayer)
                    continue;
                if (plugin.canSee(onlinePlayer, p)) {
                    String message = plugin.getMessage("SilentDeathMessage")
                            .replace("%deathmsg%", deathMessage);
                    plugin.sendMessage(onlinePlayer, message, p, onlinePlayer);
                }
            }
        }
    }

    public static void announceSilentQuit(Player p, MineSkyVanish plugin) {
        if (plugin.getSettings().getBoolean("MessageOptions.AnnounceRealJoinQuitToAdmins", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (p == onlinePlayer)
                    continue;
                if (plugin.canSee(onlinePlayer, p)) {
                    plugin.sendMessage(onlinePlayer, "SilentQuitMessageForAdmins", p, onlinePlayer);
                }
            }
        }
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("MessageOptions.FakeJoinQuitMessages.BroadcastFakeQuitOnVanish")
                || plugin.getSettings().getBoolean("MessageOptions.FakeJoinQuitMessages" +
                ".BroadcastFakeQuitOnReappear");
    }

    @EventHandler
    public void onVanish(PostPlayerHideEvent e) {
        final Player p = e.getPlayer();
        if (plugin.getSettings().getBoolean("MessageOptions.FakeJoinQuitMessages.BroadcastFakeQuitOnVanish")
                && !e.isSilent()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!plugin.canSee(onlinePlayer, p)) {
                    if (!plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToAdmins"))
                        plugin.sendMessage(onlinePlayer, "VanishMessage", p, onlinePlayer);
                } else if (!plugin.getSettings().getBoolean(
                        "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToUsers"))
                    if (!plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.AnnounceVanishReappearToAdmins"))
                        plugin.sendMessage(onlinePlayer, "VanishMessage", p, onlinePlayer);
                    else if (onlinePlayer == p && !plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToAdmins"))
                        plugin.sendMessage(onlinePlayer, "VanishMessage", p, onlinePlayer);
                    else if (onlinePlayer != p)
                        plugin.sendMessage(onlinePlayer, "VanishMessageWithPermission", p, onlinePlayer);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        Player p = e.getPlayer();
        if (plugin.getSettings().getBoolean(
                "MessageOptions.FakeJoinQuitMessages.BroadcastFakeJoinOnReappear") && !e.isSilent()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!plugin.canSee(onlinePlayer, p)) {
                    if (!plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToAdmins"))
                        plugin.sendMessage(onlinePlayer, "ReappearMessage", p, onlinePlayer);
                } else if (!plugin.getSettings().getBoolean(
                        "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToUsers"))
                    if (!plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.AnnounceVanishReappearToAdmins"))
                        plugin.sendMessage(onlinePlayer, "ReappearMessageWithPermission", p, onlinePlayer);
                    else if (onlinePlayer == p && !plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToAdmins"))
                        plugin.sendMessage(onlinePlayer, "ReappearMessage", p, onlinePlayer);
                    else if (onlinePlayer != p)
                        plugin.sendMessage(onlinePlayer, "ReappearMessageWithPermission", p, onlinePlayer);
            }
        }
    }
}
