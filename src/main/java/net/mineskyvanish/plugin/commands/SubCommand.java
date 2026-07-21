package net.mineskyvanish.plugin.commands;

import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public abstract class SubCommand implements Executable {

    protected final MineSkyVanish plugin;

    public SubCommand(MineSkyVanish plugin) {
        this.plugin = plugin;
    }

    protected Collection<UUID> getAllVanishedPlayers() {
        return plugin.getVanishStateMgr().getVanishedPlayers();
    }

    protected Collection<UUID> getOnlineVanishedPlayers() {
        return plugin.getVanishStateMgr().getOnlineVanishedPlayers();
    }

    public void hidePlayer(Player player) {
        plugin.getVisibilityChanger().hidePlayer(player);
    }

    public void showPlayer(Player player) {
        plugin.getVisibilityChanger().showPlayer(player);
    }


    public boolean isVanished(UUID uuid) {
        return plugin.getVanishStateMgr().isVanished(uuid);
    }

    public boolean canDo(CommandSender sender, CommandAction action, boolean sendErrors) {
        if (!(sender instanceof Player))
            if (!action.usableByConsole()) {
                if (sendErrors)
                    plugin.sendMessage(sender, "InvalidSender", sender);
                return false;
            }
        if (!action.checkPermission(sender, plugin)) {
            if (sendErrors)
                plugin.sendMessage(sender, "NoPermission", sender);
            return false;
        }
        return true;
    }
}