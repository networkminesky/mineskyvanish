package net.mineskyvanish.plugin.commands.subcommands;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.commands.CommandAction;
import net.mineskyvanish.plugin.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ShowHelp extends SubCommand {

    public ShowHelp(MineSkyVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender sender, String[] args, String label) {
        if (canDo(sender, CommandAction.SHOW_HELP, true)) {
            plugin.sendMessage(sender, "HelpHeader", sender);
            for (CommandAction action : CommandAction.values()) {
                if (canDo(sender, action, false)) {
                    plugin.sendMessage(sender, plugin.getMessage("HelpFormat")
                            .replace("%usage%", action.getUsage())
                            .replace("%description%", action.getDescription())
                            .replace("%permission%", action.getMainPermission()), sender);
                }
            }
        }
    }
}
