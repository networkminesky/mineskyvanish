package net.mineskyvanish.plugin.commands.subcommands;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.VanishPlayer;
import net.mineskyvanish.plugin.commands.CommandAction;
import net.mineskyvanish.plugin.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleItemPickups extends SubCommand {

    public ToggleItemPickups(MineSkyVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender sender, String[] args, String label) {
        if (canDo(sender, CommandAction.TOGGLE_ITEM_PICKUPS, true)) {
            Player p = (Player) sender;
            plugin.sendMessage(p, plugin.getMessage("ToggledPickingUpItems"
                    + (toggleState(plugin.getVanishPlayer(p)) ? "On" : "Off")), p);
        }
    }

    private boolean toggleState(VanishPlayer vp) {
        boolean hasEnabled = plugin.getPlayerData().getBoolean("PlayerData."
                + vp.getPlayerUUID() + ".itemPickUps");
        plugin.getPlayerData().set("PlayerData." + vp.getPlayerUUID() + ".itemPickUps", !hasEnabled);
        vp.setItemPickUps(!hasEnabled);
        plugin.getConfigMgr().getPlayerDataFile().save();
        return !hasEnabled;
    }
}
