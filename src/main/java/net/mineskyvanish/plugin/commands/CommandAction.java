package net.mineskyvanish.plugin.commands;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.utils.Validation;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public enum CommandAction {
    VANISH_SELF(
            "mineskyvanish.use",
            false,
            "/msv [on|off]",
            "Toggles your visibility") {
        @Override
        public boolean checkPermission(CommandSender sender, MineSkyVanish plugin) {
            Validation.checkNotNull(plugin, "plugin cannot be null");
            return plugin.hasPermissionToVanish(sender);
        }
    },
    VANISH_OTHER(
            "mineskyvanish.others",
            true,
            "/msv [on|off] <player>",
            "Shows or hides a player"),
    VANISHED_LIST(
            "mineskyvanish.list",
            true,
            "/msv list",
            "Shows a list of vanished players"),
    BROADCAST_LOGIN(
            "mineskyvanish.login",
            false,
            "/msv login",
            "Broadcasts a fake login message"),
    BROADCAST_LOGOUT(
            "mineskyvanish.logout",
            false,
            "/msv logout",
            "Broadcasts a fake logout message"),
    TOGGLE_ITEM_PICKUPS(
            "mineskyvanish.toggleitems",
            false,
            "/msv tipu",
            "Toggles picking up items") {
        @Override
        public boolean checkPermission(CommandSender sender, MineSkyVanish plugin) {
            return sender.hasPermission(getMainPermission()) || sender.hasPermission("mineskyvanish.toggleitempickups");
        }
    },
    RECREATE_FILES(
            "mineskyvanish.recreatefiles",
            true,
            "/msv recreatefiles",
            "Recreates the config") {
        @Override
        public boolean checkPermission(CommandSender sender, MineSkyVanish plugin) {
            return sender.hasPermission(getMainPermission()) || sender.hasPermission("mineskyvanish.updatecfg");
        }
    },
    RELOAD(
            "mineskyvanish.reload",
            true,
            "/msv reload",
            "Reloads all settings and messages"),
    PRINT_STACKTRACE(
            "mineskyvanish.stacktrace",
            true,
            "/msv stacktrace",
            "Logs info for a bug report"),
    SHOW_HELP(
            "mineskyvanish.help",
            true,
            "/msv help",
            "Shows this help page");

    /**
     * Use {@link #checkPermission(CommandSender, MineSkyVanish)} to check whether a {@link CommandSender} has
     * permission to perform an action or not
     */
    private final String mainPermission;
    private final boolean console;
    private final String usage, description;

    CommandAction(String mainPermission, boolean console, String usage, String description) {
        this.mainPermission = mainPermission;
        this.console = console;
        this.usage = usage;
        this.description = description;
    }

    public String getMainPermission() {
        return mainPermission;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    static List<String> getAvailableFirstArguments(CommandSender sender, MineSkyVanish plugin) {
        Validation.checkNotNull(plugin, "plugin cannot be null");
        List<String> list = new ArrayList<>();
        if (SHOW_HELP.checkPermission(sender, plugin))
            list.add("help");
        if (RECREATE_FILES.checkPermission(sender, plugin))
            list.add("recreatefiles");
        if (RELOAD.checkPermission(sender, plugin))
            list.add("reload");
        if (VANISHED_LIST.checkPermission(sender, plugin))
            list.add("list");
        if (VANISH_SELF.checkPermission(sender, plugin))
            list.add("on");
        if (VANISH_SELF.checkPermission(sender, plugin))
            list.add("off");
        if (TOGGLE_ITEM_PICKUPS.checkPermission(sender, plugin))
            list.add("tipu");
        if (PRINT_STACKTRACE.checkPermission(sender, plugin))
            list.add("stacktrace");
        return list;
    }

    public static boolean hasAnyCmdPermission(CommandSender sender, MineSkyVanish plugin) {
        for (CommandAction action : CommandAction.values())
            if (action.checkPermission(sender, plugin))
                return true;
        return false;
    }

    public boolean checkPermission(CommandSender sender, MineSkyVanish plugin) {
        return sender.hasPermission(getMainPermission());
    }

    public boolean usableByConsole() {
        return console;
    }
}