package net.mineskyvanish.presence.adapter;

import java.lang.reflect.Method;

import net.mineskyvanish.presence.PresenceAdapter;
import org.bukkit.entity.Player;

public final class PaperPlayerListAdapter implements PresenceAdapter {
    @Override
    public String name() {
        return "Paper player-list";
    }

    @Override
    public boolean required() {
        return false;
    }

    @Override
    public boolean isAvailable() {
        return hasPlayerListMethods();
    }

    @Override
    public void hide(Player viewer, Player target) {
        // Public Paper API: removes target from this viewer's player list/tab presence.
        viewer.unlistPlayer(target);
    }

    @Override
    public void show(Player viewer, Player target) {
        viewer.listPlayer(target);
    }

    private static boolean hasPlayerListMethods() {
        try {
            Method unlist = Player.class.getMethod("unlistPlayer", Player.class);
            Method list = Player.class.getMethod("listPlayer", Player.class);
            return unlist.getReturnType() == boolean.class && list.getReturnType() == boolean.class;
        } catch (NoSuchMethodException exception) {
            return false;
        }
    }
}
