package net.mineskyvanish.plugin.visibility.hiders.modules;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.google.common.collect.ImmutableList;
import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.visibility.hiders.PlayerHider;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * This is currently unused on Minecraft 1.19 or higher
 */
public class PlayerInfoModule extends PacketAdapter {

    private final PlayerHider hider;
    private final MineSkyVanish plugin;

    private boolean errorLogged = false;

    public PlayerInfoModule(MineSkyVanish plugin, PlayerHider hider) {
        super(plugin, ListenerPriority.HIGH, PacketType.Play.Server.PLAYER_INFO);
        this.plugin = plugin;
        this.hider = hider;
    }

    public static void register(MineSkyVanish plugin, PlayerHider hider) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PlayerInfoModule(plugin, hider));
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        // multiple events share same packet object
        event.setPacket(event.getPacket().shallowClone());
        try {
            List<PlayerInfoData> infoDataList = new ArrayList<>(event.getPacket().getPlayerInfoDataLists().read(0));

            Player receiver = event.getPlayer();
            for (PlayerInfoData infoData : ImmutableList.copyOf(infoDataList)) {
                if (hider.isHidden(infoData.getProfile().getUUID(), receiver)) {
                    infoDataList.remove(infoData);
                }
            }
            if (infoDataList.isEmpty()) {
                event.setCancelled(true);
            }
            event.getPacket().getPlayerInfoDataLists().write(0, infoDataList);
        } catch (Exception | NoClassDefFoundError e) {
            if (e.getMessage() == null
                    || !e.getMessage().endsWith("is not supported for temporary players.")) {
                if (errorLogged) return;
                plugin.logException(e);
                plugin.getLogger().warning("IMPORTANT: Please make sure that you are using the latest " +
                        "dev-build of ProtocolLib and that your server is up-to-date! This error likely " +
                        "happened inside of ProtocolLib code which is out of MineSkyVanish's control. It's part " +
                        "of an optional invisibility module and can be removed safely by disabling " +
                        "ModifyTablistPackets in the config. Please report this " +
                        "error if you can reproduce it on an up-to-date server with only latest " +
                        "ProtocolLib and latest MSV installed.");
                errorLogged = true;
            }
        }
    }
}
