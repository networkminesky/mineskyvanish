package net.mineskyvanish.plugin.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.mineskyvanish.api.vanish.events.PlayerHideEvent;
import net.mineskyvanish.api.vanish.events.PostPlayerShowEvent;
import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.commands.CommandAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class EssentialsHook extends PluginHook {

    private final Set<UUID> preVanishHiddenPlayers = new HashSet<>();
    private Essentials essentials;
    private Object activeTask = null;

    public EssentialsHook(MineSkyVanish mineSkyVanish) {
        super(mineSkyVanish);
    }

    @Override
    public void onPluginEnable(Plugin plugin) {
        essentials = (Essentials) plugin;
        startForcedInvisibilityTask();
    }

    @Override
    public void onPluginDisable(Plugin plugin) {
        essentials = null;
        startForcedInvisibilityTask();
    }

    private void runInvisibilityCheck() {
        try {
            if (!Bukkit.getPluginManager().isPluginEnabled("Essentials")) return;
            for (UUID uuid : mineSkyVanish.getVanishStateMgr().getOnlineVanishedPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                User user = essentials.getUser(p);
                if (user == null) continue;
                if (!user.isHidden()) {
                    user.setHidden(true);
                }
            }
        } catch (Exception e) {
            stopForcedInvisibilityTask();
            mineSkyVanish.logException(e);
        }
    }

    private void startForcedInvisibilityTask() {
            this.activeTask = mineSkyVanish.getServer().getGlobalRegionScheduler()
                    .runAtFixedRate(mineSkyVanish, task -> runInvisibilityCheck(), 1L, 100L);

        runInvisibilityCheck();
    }

    private void stopForcedInvisibilityTask() {
        if (activeTask == null) return;

        if (activeTask instanceof org.bukkit.scheduler.BukkitTask) {
            ((org.bukkit.scheduler.BukkitTask) activeTask).cancel();
        } else {
            try {
                ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) activeTask).cancel();
            } catch (Throwable ignored) {
            }
        }
        activeTask = null;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {
        User user = essentials.getUser(e.getPlayer());
        if (user == null) return;
        if (mineSkyVanish.getVanishStateMgr().isVanished(e.getPlayer().getUniqueId()) && !user.isHidden())
            user.setHidden(true);
        else user.setHidden(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        User user = essentials.getUser(e.getPlayer());
        if (user == null) return;
        if (user.isVanished()) user.setVanished(false);
        preVanishHiddenPlayers.remove(e.getPlayer().getUniqueId());
        user.setHidden(true);
    }

    @EventHandler
    public void onReappear(PostPlayerShowEvent e) {
        User user = essentials.getUser(e.getPlayer());
        if (user == null) return;
        user.setHidden(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(final PlayerCommandPreprocessEvent e) {
        if (!CommandAction.VANISH_SELF.checkPermission(e.getPlayer(), mineSkyVanish)) return;
        if (mineSkyVanish.getVanishStateMgr().isVanished(e.getPlayer().getUniqueId())) return;
        String command = e.getMessage().toLowerCase(Locale.ENGLISH).split(" ")[0].replace("/", "")
                .toLowerCase(Locale.ENGLISH);
        if (command.split(":").length > 1) command = command.split(":")[1];
        if (command.equals("mineskyvanish") || command.equals("msv")
                || command.equals("v") || command.equals("vanish")) {
            final User user = essentials.getUser(e.getPlayer());
            if (user == null || !user.isAfk()) return;
            user.setHidden(true);
            preVanishHiddenPlayers.add(e.getPlayer().getUniqueId());
            Runnable delayedTask = () -> {
                if (preVanishHiddenPlayers.remove(e.getPlayer().getUniqueId())) {
                    user.setHidden(false);
                }
            };

            mineSkyVanish.getServer().getGlobalRegionScheduler()
                    .runDelayed(mineSkyVanish, task -> delayedTask.run(), 1L);
        }
    }
}
