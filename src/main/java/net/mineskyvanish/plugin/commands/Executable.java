package net.mineskyvanish.plugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface Executable {

    void execute(Command cmd, CommandSender sender, String[] args, String label);
}
