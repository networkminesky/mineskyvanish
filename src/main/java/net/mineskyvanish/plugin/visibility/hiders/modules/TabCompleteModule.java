package net.mineskyvanish.plugin.visibility.hiders.modules;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.visibility.hiders.PlayerHider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TabCompleteModule extends PacketAdapter {
    private final PlayerHider hider;
    private final MineSkyVanish plugin;

    private boolean errorLogged = false;

    public TabCompleteModule(MineSkyVanish plugin, PlayerHider hider) {
        super(plugin, ListenerPriority.HIGH, PacketType.Play.Server.TAB_COMPLETE);
        this.plugin = plugin;
        this.hider = hider;
    }

    public static void register(MineSkyVanish plugin, PlayerHider hider) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new TabCompleteModule(plugin, hider));
    }


    @Override
    public void onPacketSending(PacketEvent event) {
        try {
            if (plugin.getVersionUtil().isOneDotXOrHigher(13)) {
                try {
                    Suggestions suggestions = event.getPacket().getSpecificModifier(Suggestions.class).read(0);
                    Iterator<Suggestion> iterator = suggestions.getList().iterator();
                    boolean containsHiddenPlayer = false;
                    while (iterator.hasNext()) {
                        Suggestion suggestion = iterator.next();
                        String completion = suggestion.getText();
                        if (completion.contains("/")) continue;
                        if (hider.isHidden(completion, event.getPlayer())) {
                            iterator.remove();
                            containsHiddenPlayer = true;
                        }
                    }
                    if (containsHiddenPlayer) {
                        event.getPacket().getSpecificModifier(Suggestions.class).write(0, suggestions);
                    }
                } catch (FieldAccessException e) {
                    if (errorLogged) return;
                    plugin.getLogger().warning("Could not intercept tab-completions using ProtocolLib: "
                            + e.getMessage());
                    errorLogged = true;
                }
            } else {
                String[] suggestions = event.getPacket().getStringArrays().read(0);
                boolean containsHiddenPlayer = false;
                List<String> suggestionList = new ArrayList<>(Arrays.asList(suggestions));
                for (String suggestion : suggestions) {
                    if (suggestion.contains("/")) continue;
                    if (hider.isHidden(suggestion, event.getPlayer())) {
                        suggestionList.remove(suggestion);
                        containsHiddenPlayer = true;
                    }
                }
                if (containsHiddenPlayer) {
                    event.getPacket().getStringArrays().write(0,
                            suggestionList.toArray(new String[suggestionList.size()]));
                }
            }
        } catch (Exception | NoClassDefFoundError e) {
            if (e.getMessage() == null
                    || !e.getMessage().endsWith("is not supported for temporary players.")) {
                if (errorLogged) return;
                plugin.logException(e);
                plugin.getLogger().warning("IMPORTANT: Please make sure that you are using the latest " +
                        "dev-build of ProtocolLib and that your server is up-to-date! This error likely " +
                        "happened inside of ProtocolLib code which is out of MineSkyVanish's control. It's part " +
                        "of an optional invisibility module and can be removed safely by disabling " +
                        "ModifyTabCompletePackets in the config. Please report this " +
                        "error if you can reproduce it on an up-to-date server with only latest " +
                        "ProtocolLib and latest MSV installed.");
                errorLogged = true;
            }
        }
    }
}
