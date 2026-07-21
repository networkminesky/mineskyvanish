package net.mineskyvanish.plugin.listeners;

import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TabCompleteListener implements Listener {

    private final MineSkyVanish plugin;

    private boolean errorLogged = false;

    public TabCompleteListener(MineSkyVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent e) {
        try {
            if (!(e.getSender() instanceof Player)) return;
            Player p = (Player) e.getSender();
            Set<String> hiddenNames = plugin.getVanishStateMgr().getOnlineVanishedPlayers().stream()
                    .map(Bukkit::getPlayer).filter(Objects::nonNull)
                    .filter(vanishedPlayer -> !plugin.canSee(p, vanishedPlayer))
                    .map(HumanEntity::getName)
                    .map(name -> name.toLowerCase(Locale.ENGLISH))
                    .collect(Collectors.toSet());
            Iterator<String> it = e.getCompletions().iterator();
            while (it.hasNext()) {
                String completion = it.next();
                boolean allowedCompletion = !hiddenNames.contains(completion.toLowerCase(Locale.ENGLISH));
                if (!allowedCompletion) {
                    it.remove();
                }
            }
        } catch (UnsupportedOperationException uoe) {
            if (!errorLogged) {
                plugin.getLogger().warning("UnsupportedOperationException while modifying TabCompleteEvent");
                errorLogged = true;
            }
        } catch (Exception er) {
            if (!errorLogged) {
                plugin.logException(er);
                errorLogged = true;
            }
        }
    }

}
