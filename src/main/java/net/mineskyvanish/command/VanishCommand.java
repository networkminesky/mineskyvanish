package net.mineskyvanish.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineskyvanish.visibility.VanishService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VanishCommand implements CommandExecutor, TabCompleter {
    private static final List<String> MODES = List.of("on", "off", "toggle", "status", "capabilities");

    private final VanishService vanishService;

    public VanishCommand(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        String mode = args.length >= 1 ? args[0].toLowerCase(Locale.ROOT) : "toggle";

        if ("capabilities".equals(mode)) {
            sendCapabilities(sender);
            return true;
        }

        if (args.length >= 2) {
            return handleOther(sender, mode, args[1]);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Console usage: /stealth <on|off|toggle|status> <player>", NamedTextColor.RED));
            return true;
        }

        return handlePlayer(sender, player, mode);
    }

    private boolean handleOther(CommandSender sender, String mode, String targetName) {
        if (!sender.hasPermission("stealthvanish.command.others")) {
            sender.sendMessage(Component.text("You do not have permission to vanish other players.", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(Component.text("That player is not online.", NamedTextColor.RED));
            return true;
        }

        target.getScheduler().execute(this.vanishService.plugin(), () -> {
            if (!this.vanishService.canSee(sender, target.getUniqueId())) {
                sendFeedback(sender, Component.text("That player is not online.", NamedTextColor.RED));
                return;
            }

            if (applyMode(target, mode)) {
                if ("status".equals(mode)) {
                    sendFeedback(sender, statusMessage(target));
                } else {
                    sendFeedback(sender, Component.text("Updated vanish state for " + target.getName() + ".", NamedTextColor.GRAY));
                }
            } else {
                sendUsage(sender);
            }
        }, null, 1L);
        return true;
    }

    private boolean handlePlayer(CommandSender sender, Player player, String mode) {
        player.getScheduler().execute(this.vanishService.plugin(), () -> {
            if (!applyMode(player, mode)) {
                sendUsage(sender);
                return;
            }

            if ("status".equals(mode)) {
                player.sendMessage(statusMessage(player));
            }
        }, null, 1L);

        return true;
    }

    private boolean applyMode(Player player, String mode) {
        switch (mode) {
            case "on" -> this.vanishService.setVanished(player, true);
            case "off" -> this.vanishService.setVanished(player, false);
            case "toggle" -> this.vanishService.setVanished(player, !this.vanishService.isInvisible(player.getUniqueId()));
            case "status" -> {
                return true;
            }
            case "capabilities" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private Component statusMessage(Player player) {
        return Component.text(
                player.getName() + " is " + (this.vanishService.isInvisible(player.getUniqueId()) ? "vanished." : "visible."),
                NamedTextColor.GRAY);
    }

    private void sendUsage(CommandSender sender) {
        sendFeedback(sender, Component.text("Usage: /stealth [on|off|toggle|status] [player]", NamedTextColor.RED));
    }

    private void sendFeedback(CommandSender sender, Component message) {
        if (sender instanceof Player player) {
            player.getScheduler().execute(this.vanishService.plugin(), () -> player.sendMessage(message), null, 1L);
            return;
        }

        sender.sendMessage(message);
    }

    private void sendCapabilities(CommandSender sender) {
        sendFeedback(sender, Component.text("Adaptive presence adapters:", NamedTextColor.GRAY));
        for (String statusLine : this.vanishService.presenceStatusLines()) {
            sendFeedback(sender, Component.text("- " + statusLine, NamedTextColor.DARK_GRAY));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length == 1) {
            return filter(MODES, args[0]);
        }

        if (args.length == 2 && sender.hasPermission("stealthvanish.command.others")) {
            List<String> names = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> this.vanishService.canSee(sender, player.getUniqueId()))
                    .map(Player::getName)
                    .toList();
            return filter(names, args[1]);
        }

        return List.of();
    }

    private static List<String> filter(List<String> options, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
