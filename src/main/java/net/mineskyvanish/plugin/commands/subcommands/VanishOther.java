package net.mineskyvanish.plugin.commands.subcommands;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.commands.CommandAction;
import net.mineskyvanish.plugin.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VanishOther extends SubCommand {

    private Player specifiedPlayer;

    public VanishOther(MineSkyVanish plugin) {
        this(null, plugin);
    }

    public VanishOther(Player specifiedPlayer, MineSkyVanish plugin) {
        super(plugin);
        this.specifiedPlayer = specifiedPlayer;
    }

    @Override
    public void execute(Command cmd, CommandSender sender, String[] args, String label) {
        if (canDo(sender, CommandAction.VANISH_OTHER, true)) {
            boolean hide = false, offline = false, silent = false;
            Player target;
            String name;
            UUID uuid;
            if (specifiedPlayer == null) {
                if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")
                        || args[0].equalsIgnoreCase("vanish"))
                    hide = true;
                target = Bukkit.getPlayer(args[1]);
                name = target == null ? args[1] : target.getName();
            } else {
                target = specifiedPlayer;
                name = specifiedPlayer.getName();
                hide = !isVanished(target.getUniqueId());
            }
            if (target == null) {
                offline = true;
                uuid = plugin.getVanishStateMgr().getVanishedUUIDFromNameOnFile(name);
                if (uuid == null) {
                    plugin.sendMessage(sender, "PlayerNonExistent", sender, name);
                    return;
                }
            } else {
                name = target.getName();
                uuid = target.getUniqueId();
            }
            if (!offline && sender instanceof Player && sender != target
                    && target.hasPermission("mineskyvanish.notoggle")) {
                plugin.sendMessage(sender, "CannotHideOtherPlayer", sender, name);
                return;
            }
            if (plugin.getSettings().getBoolean(
                    "IndicationFeatures.LayeredPermissions.HideInvisibleInCommands", false) && target != null
                    && sender instanceof Player && !plugin.hasPermissionToSee((Player) sender, target)) {
                plugin.sendMessage(sender, "PlayerNonExistent", sender, name);
                return;
            }
            if (hide && (offline ? isVanished(uuid) : isVanished(uuid))) {
                plugin.sendMessage(sender, "AlreadyInvisibleMessage", sender, name);
                return;
            } else if (!hide && !(offline ? isVanished(uuid) : isVanished(uuid))) {
                plugin.sendMessage(sender, "AlreadyVisibleMessage", sender, name);
                return;
            }
            if (args.length == 3)
                silent = args[2].equalsIgnoreCase("-s");
            else if (args.length == 2)
                silent = args[1].equalsIgnoreCase("-s");
            if (!offline) {
                if (hide) {
                    plugin.getVisibilityChanger().hidePlayer(target, sender.getName(), silent);
                    plugin.sendMessage(sender, "HideOtherMessage", sender, name);
                } else {
                    plugin.getVisibilityChanger().showPlayer(target, sender.getName());
                    plugin.sendMessage(sender, "ShowOtherMessage", sender, name, silent);
                }
            } else {
                if (hide) {
                    plugin.getVanishStateMgr().setVanishedState(uuid, name, true, sender.getName());
                    plugin.sendMessage(sender, "HideOtherMessage", sender, name);
                } else {
                    plugin.getVanishStateMgr().setVanishedState(uuid, name, false, sender.getName());
                    plugin.sendMessage(sender, "ShowOtherMessage", sender, name);
                }
            }
        }
    }
}
