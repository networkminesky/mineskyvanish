package net.mineskyvanish.plugin.commands.subcommands;

import com.google.common.collect.ImmutableList;
import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.commands.CommandAction;
import net.mineskyvanish.plugin.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class VanishedList extends SubCommand {

    public VanishedList(MineSkyVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, final CommandSender sender, final String[] args, String label) {
        if (canDo(sender, CommandAction.VANISHED_LIST, true)) {
            String listMessage = plugin.getMessage("ListMessagePrefix");
            StringBuilder stringBuilder = new StringBuilder();
            List<UUID> allInvisiblePlayerUUIDs = ImmutableList.copyOf(getAllVanishedPlayers());
            if (allInvisiblePlayerUUIDs.isEmpty()) {
                stringBuilder.append("none");
            }
            for (int i = 0; i < allInvisiblePlayerUUIDs.size(); i++) {
                UUID playerUUID = allInvisiblePlayerUUIDs.get(i);
                String name = Bukkit.getOfflinePlayer(playerUUID).getName();
                if (Bukkit.getPlayer(playerUUID) == null) {
                    name = name + ChatColor.RED + "[offline]" + ChatColor.WHITE;
                }
                stringBuilder.append(name);
                if (i != allInvisiblePlayerUUIDs.size() - 1) {
                    stringBuilder.append(ChatColor.GREEN).append(", ").append(ChatColor.WHITE);
                }
            }
            listMessage = listMessage.replace("%l", stringBuilder.toString());
            plugin.sendMessage(sender, listMessage, sender);
        }
    }
}
