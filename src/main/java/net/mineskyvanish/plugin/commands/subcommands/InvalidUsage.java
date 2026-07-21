package net.mineskyvanish.plugin.commands.subcommands;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.commands.CommandAction;
import net.mineskyvanish.plugin.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class InvalidUsage extends SubCommand {

    public InvalidUsage(MineSkyVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender sender, String[] args, String label) {
        if (!CommandAction.hasAnyCmdPermission(sender, plugin)) {
            plugin.sendMessage(sender, "NoPermission", sender);
            return;
        }
        plugin.sendMessage(sender, "InvalidUsage", sender);
    }
}
