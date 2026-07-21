package net.mineskyvanish.plugin.listeners;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.VanishPlayer;
import net.mineskyvanish.plugin.features.Broadcast;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;


public class GeneralListener implements Listener {

    private final MineSkyVanish plugin;

    private final FileConfiguration config;

    public GeneralListener(MineSkyVanish plugin) {
        this.plugin = plugin;
        config = plugin.getSettings();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        try {
            if (!(e.getDamager() instanceof Player)) return;
            if (e.getEntity() == null) return;
            Player p = (Player) e.getDamager();
            if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                if (config.getBoolean("RestrictiveOptions.PreventHittingEntities")
                        && !p.hasPermission("mineskyvanish.damageentities") && !p.hasPermission("mineskyvanish.damage")) {
                    plugin.sendMessage(p, "EntityHitDenied", p);
                    e.setCancelled(true);
                }
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent e) {
        try {
            Player p = e.getEntity();
            if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                String deathMessage = e.getDeathMessage();
                e.setDeathMessage(null);
                if (deathMessage != null)
                    Broadcast.announceSilentDeath(p, plugin, deathMessage);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        try {
            if (e.getEntity() instanceof Player && config.getBoolean("InvisibilityFeatures.DisableHunger")) {
                Player p = (Player) e.getEntity();
                if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())
                        && e.getFoodLevel() <= p.getFoodLevel())
                    e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent e) {
        try {
            if (!(e.getEntity() instanceof Player)) return;
            Player p = (Player) e.getEntity();
            if (!config.getBoolean("InvisibilityFeatures.DisableDamage")) return;
            if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTarget(EntityTargetEvent e) {
        try {
            if (!(e.getTarget() instanceof Player)) return;
            if (!config.getBoolean("InvisibilityFeatures.DisableMobTarget")) return;
            Player p = (Player) e.getTarget();
            if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickUp(PlayerPickupItemEvent e) {
        try {
            VanishPlayer vanishPlayer = plugin.getVanishPlayer(e.getPlayer());
            if (vanishPlayer == null || !vanishPlayer.isOnlineVanished()) return;
            if (!vanishPlayer.hasItemPickUpsEnabled())
                e.setCancelled(true);
            if (plugin.getSettings().getBoolean("RestrictiveOptions.PreventModifyingOwnInventory")
                    && !e.getPlayer().hasPermission("mineskyvanish.modifyowninv")) {
                e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler
    public void onPlayerCropTrample(PlayerInteractEvent e) {
        try {
            if (!plugin.getVanishStateMgr().isVanished(e.getPlayer().getUniqueId())) return;
            if (e.getAction() != Action.PHYSICAL) return;
            if (e.getClickedBlock() != null && e.getClickedBlock().getType().toString().matches("SOIL|FARMLAND"))
                e.setCancelled(true);
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}
