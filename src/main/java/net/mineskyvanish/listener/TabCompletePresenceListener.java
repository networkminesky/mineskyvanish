package net.mineskyvanish.listener;

import net.mineskyvanish.visibility.VanishService;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

public final class TabCompletePresenceListener implements Listener {
    private final VanishService vanishService;

    public TabCompletePresenceListener(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        CommandSender sender = event.getSender();
        if (sender.hasPermission("stealthvanish.see")) {
            return;
        }

        if (this.vanishService.vanishedNames().isEmpty()) {
            return;
        }

        try {
            event.getCompletions().removeIf(completion -> this.vanishService.shouldHideNameFrom(sender, completion));
        } catch (RuntimeException exception) {
            this.vanishService.warnSurfaceUnavailableOnce("tab-complete-completions", exception);
        }
    }
}
