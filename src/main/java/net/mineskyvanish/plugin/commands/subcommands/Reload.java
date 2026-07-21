package net.mineskyvanish.plugin.commands.subcommands;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.commands.CommandAction;
import net.mineskyvanish.plugin.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {

    public Reload(MineSkyVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender p, String[] args, String label) {
        if (canDo(p, CommandAction.RELOAD, true)) {
            long before = System.currentTimeMillis();
            plugin.reload();
            if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) {
                p.sendMessage(ChatColor.RED +
                        "Failed to reload MineSkyVanish since it failed to restart itself. " +
                        "More information is in the console. ("
                        + (System.currentTimeMillis() - before) + "ms)");
                return;
            }
            plugin.sendMessage(p, plugin.getMessage("PluginReloaded").replace("%time%",
                    (System.currentTimeMillis() - before) + ""), p);
        }
    }
}
