package net.mineskyvanish.plugin.commands.subcommands;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.commands.CommandAction;
import net.mineskyvanish.plugin.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class PrintStacktrace extends SubCommand {

    public PrintStacktrace(MineSkyVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender sender, String[] args, String label) {
        if (canDo(sender, CommandAction.PRINT_STACKTRACE, true)) {
            plugin.sendMessage(sender, "PrintedStacktrace", sender);
            plugin.logException(null);
        }
    }
}
