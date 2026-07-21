package net.mineskyvanish.presence;

import org.bukkit.entity.Player;

public interface PresenceAdapter {
    String name();

    boolean required();

    boolean isAvailable();

    void hide(Player viewer, Player target);

    void show(Player viewer, Player target);
}
