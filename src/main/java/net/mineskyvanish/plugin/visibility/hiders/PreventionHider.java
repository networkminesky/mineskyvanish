package net.mineskyvanish.plugin.visibility.hiders;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.utils.BukkitPlayerHidingUtil;
import net.mineskyvanish.plugin.visibility.hiders.modules.PlayerInfoModule;
import net.mineskyvanish.plugin.visibility.hiders.modules.TabCompleteModule;
import org.bukkit.entity.Player;

public class PreventionHider extends PlayerHider implements Runnable {

    private int taskId;

    public PreventionHider(MineSkyVanish plugin) {
        super(plugin);
        if (!BukkitPlayerHidingUtil.isNewPlayerHidingAPISupported(plugin)) {
            taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 2, 2).getTaskId();
        }
        if (plugin.isUseProtocolLib() && plugin.getVersionUtil().isOneDotXOrHigher(8)
                && !plugin.getVersionUtil().isOneDotXOrHigher(19)
                && plugin.getSettings().getBoolean("InvisibilityFeatures.ModifyTablistPackets", true))
            PlayerInfoModule.register(plugin, this);
        if (plugin.isUseProtocolLib()
                && plugin.getSettings().getBoolean("InvisibilityFeatures.ModifyTabCompletePackets", true)
                && !plugin.getVersionUtil().isOneDotXOrHigher(21)) {
            // Not supported anymore on 1.21 and above (ProtocolLib broken)
            TabCompleteModule.register(plugin, this);
        }
    }

    @Override
    public boolean setHidden(Player player, Player viewer, boolean hidden) {
        if (super.setHidden(player, viewer, hidden) || BukkitPlayerHidingUtil.isNewPlayerHidingAPISupported(plugin)) {
            if (hidden) BukkitPlayerHidingUtil.hidePlayer(player, viewer, plugin);
            else BukkitPlayerHidingUtil.showPlayer(player, viewer, plugin);
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "Prevention";
    }

    @Override
    public void run() {
        for (Player hidden : playerHiddenFromPlayersMap.keySet()) {
            if (BukkitPlayerHidingUtil.isNewPlayerHidingAPISupported(plugin)) {
                plugin.getServer().getScheduler().cancelTask(taskId);
                return;
            }
            for (Player viewer : playerHiddenFromPlayersMap.get(hidden)) {
                BukkitPlayerHidingUtil.hidePlayer(hidden, viewer, plugin);
            }
        }
    }
}
