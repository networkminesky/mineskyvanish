package net.mineskyvanish.plugin.visibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask; // Import da task do Folia
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.mineskyvanish.plugin.MineSkyVanish;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class ActionBarMgr {

    private final MineSkyVanish plugin;
    private final List<Player> actionBars = new CopyOnWriteArrayList<>();
    private ScheduledTask task;

    public ActionBarMgr(MineSkyVanish plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {
        this.task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> {

            for (Player p : actionBars) {
                if (p == null || !p.isOnline()) {
                    actionBars.remove(p);
                    continue;
                }

                try {
                    sendActionBar(p, plugin.replacePlaceholders(plugin.getMessage("ActionBarMessage"), p));
                } catch (Exception | NoSuchMethodError | NoClassDefFoundError e) {
                    scheduledTask.cancel(); // Cancela a Task nativa do Folia
                    plugin.logException(e);
                    plugin.getLogger().warning("IMPORTANT: Please make sure that you are using the latest " +
                            "dev-build of ProtocolLib and that your server is up-to-date! This error likely " +
                            "happened inside of ProtocolLib code which is out of MineSkyVanish's control. It's part " +
                            "of an optional feature module and can be removed safely by disabling " +
                            "DisplayActionBar in the config file. Please report this " +
                            "error if you can reproduce it on an up-to-date server with only latest " +
                            "ProtocolLib and latest MSV installed.");
                }
            }

        }, 50, 2000, TimeUnit.MILLISECONDS);
    }

    private void sendActionBar(Player p, String bar) {
        try {
            Class.forName("net.md_5.bungee.api.chat.ComponentBuilder");
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(bar));
        } catch (ClassNotFoundException | NoSuchMethodError | NoClassDefFoundError er) {
            String json = "{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', bar) + "\"}";
            WrappedChatComponent msg = WrappedChatComponent.fromJson(json);
            PacketContainer chatMsg = new PacketContainer(PacketType.Play.Server.CHAT);
            chatMsg.getChatComponents().write(0, msg);
            if (plugin.getVersionUtil().isOneDotXOrHigher(12))
                try {
                    chatMsg.getChatTypes().write(0, EnumWrappers.ChatType.GAME_INFO);
                } catch (NoSuchMethodError e) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText
                            ("MineSkyVanish: Please update ProtocolLib"));
                }
            else
                chatMsg.getBytes().write(0, (byte) 2);
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, chatMsg);
        }
    }

    public void addActionBar(Player p) {
        if (!actionBars.contains(p)) {
            actionBars.add(p);
        }
    }

    public void removeActionBar(Player p) {
        actionBars.remove(p);
    }

    public void stopTask() {
        if (this.task != null) {
            this.task.cancel();
        }
    }
}