package net.mineskyvanish.plugin.visibility;

import net.mineskyvanish.plugin.MineSkyVanishPlugin;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public abstract class VanishStateMgr {

    protected final MineSkyVanishPlugin plugin;

    public VanishStateMgr(MineSkyVanishPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract boolean isVanished(final UUID uuid);

    public abstract void setVanishedState(final UUID uuid, String name, boolean hide, String causeName);

    public final void setVanishedState(final UUID uuid, String name, boolean hide) {
        setVanishedState(uuid, name, hide, null);
    }

    public abstract Set<UUID> getVanishedPlayers();

    public abstract Collection<UUID> getOnlineVanishedPlayers();
}
