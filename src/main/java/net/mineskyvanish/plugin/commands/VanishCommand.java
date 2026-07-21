package net.mineskyvanish.plugin.commands;

import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class VanishCommand {

    private final MineSkyVanish plugin;
    private final SubCommandMgr subCommandMgr;

    public VanishCommand(MineSkyVanish plugin) {
        this.plugin = plugin;
        subCommandMgr = new SubCommandMgr(plugin);
    }

    public void execute(Command command, CommandSender sender, String commandLabel, String[] args) {
        subCommandMgr.execute(command, sender, args, commandLabel);
    }

    public List<String> tabComplete(Command command, CommandSender sender, String alias, String[] args)
            throws IllegalArgumentException {
        return subCommandMgr.onTabComplete(command, sender, alias, args);
    }
}
