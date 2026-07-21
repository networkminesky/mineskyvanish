package net.mineskyvanish.diagnostic;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import net.mineskyvanish.api.MineSkyVanishAPI;
import net.mineskyvanish.api.VanishAPI;
import net.mineskyvanish.visibility.VanishService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class CompatibilityProbeService {
    private final JavaPlugin plugin;
    private final VanishService vanishService;

    public CompatibilityProbeService(JavaPlugin plugin, VanishService vanishService) {
        this.plugin = plugin;
        this.vanishService = vanishService;
    }

    @SuppressWarnings({"deprecation", "removal"})
    public CompatibilityProbeReport runStartupProbe() {
        List<String> lines = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        int score = 0;

        lines.add("$ stealth-presence auto-probe");
        lines.add("mode=startup; no lifecycle internals touched; no fake quit event fired");
        lines.add("plugin=" + this.plugin.getDescription().getName() + " " + this.plugin.getDescription().getVersion());
        lines.add("server=" + Bukkit.getName() + " " + Bukkit.getVersion());
        lines.add("online-players-now=" + Bukkit.getOnlinePlayers().size());

        score += probeDataFolder(lines, reasons, this.plugin.getDataFolder().toPath());
        score += probeCommand(lines, reasons, "Command /stealth", "stealth", true);
        score += probeMethod(lines, reasons, "Public VanishAPI.isInvisible(Player)", VanishAPI.class, "isInvisible", true, Player.class);
        score += probeMethod(lines, reasons, "Public VanishAPI.hidePlayer(Player)", VanishAPI.class, "hidePlayer", true, Player.class);
        score += probeMethod(lines, reasons, "Public VanishAPI.showPlayer(Player)", VanishAPI.class, "showPlayer", true, Player.class);
        score += probeMethod(lines, reasons, "Public VanishAPI.isInvisible(String)", VanishAPI.class, "isInvisible", true, String.class);
        score += probeMethod(lines, reasons, "Public VanishAPI.shouldHideNameFrom(CommandSender,String)", VanishAPI.class, "shouldHideNameFrom", true, org.bukkit.command.CommandSender.class, String.class);
        score += probeMethod(lines, reasons, "Service StealthVanishApi.isInvisible(UUID)", MineSkyVanishAPI.class, "isInvisible", true, UUID.class);
        score += probeMethod(lines, reasons, "Service StealthVanishApi.hidePlayer(Player)", MineSkyVanishAPI.class, "hidePlayer", true, Player.class);
        score += probeMethod(lines, reasons, "Service StealthVanishApi.showPlayer(Player)", MineSkyVanishAPI.class, "showPlayer", true, Player.class);
        score += probeMethod(lines, reasons, "Service StealthVanishApi.isInvisibleName(String)", MineSkyVanishAPI.class, "isInvisibleName", true, String.class);
        score += probeCommandGuardConfig(lines, reasons);
        score += probeClass(lines, reasons, "Folia EntityScheduler API", "io.papermc.paper.threadedregions.scheduler.EntityScheduler", true);
        score += probeMethod(lines, reasons, "Bukkit hidePlayer API", Player.class, "hidePlayer", true, Plugin.class, Player.class);
        score += probeMethod(lines, reasons, "Bukkit showPlayer API", Player.class, "showPlayer", true, Plugin.class, Player.class);
        score += probeMethod(lines, reasons, "Paper unlistPlayer API", Player.class, "unlistPlayer", false, Player.class);
        score += probeMethod(lines, reasons, "Paper listPlayer API", Player.class, "listPlayer", false, Player.class);
        score += probeMethod(lines, reasons, "Server ping sample iterator", ServerListPingEvent.class, "iterator", false);
        score += probeMethod(lines, reasons, "Tab-complete mutable completions", TabCompleteEvent.class, "getCompletions", false);
        score += probeMethod(lines, reasons, "AsyncScheduler access", Bukkit.class, "getAsyncScheduler", true);
        score += probeMethod(lines, reasons, "RegionScheduler access", Bukkit.class, "getRegionScheduler", true);
        score += probeMethod(lines, reasons, "GlobalRegionScheduler access", Bukkit.class, "getGlobalRegionScheduler", true);

        lines.add("adapters:");
        for (String statusLine : this.vanishService.presenceStatusLines()) {
            lines.add("  " + statusLine);
            if (statusLine.toLowerCase(Locale.ROOT).contains("unavailable")) {
                score += 8;
                reasons.add("optional adapter unavailable: " + statusLine);
            }
            if (statusLine.toLowerCase(Locale.ROOT).contains("disabled")) {
                score += 20;
                reasons.add("adapter disabled: " + statusLine);
            }
        }

        lines.add("failure-point recognition:");
        lines.add("  lifecycle splice: blocked by design; Folia removal retires scheduler, closes packet processing, removes entity/maps");
        lines.add("  fake PlayerQuitEvent: blocked by design; plugins may clear state while player remains online");
        lines.add("  Bukkit API fraud: blocked by design; normal plugins call Bukkit statics directly");
        lines.add("  packet polish: optional only; packets do not change server/plugin truth");
        lines.add("  plugin-specific adapters: safe target for future compatibility automation");
        lines.add("  command respect guard: safe middle-ground; blocks configured player-targeting commands before plugins see vanished targets");

        String result;
        if (score >= 70) {
            result = "FAIL";
        } else if (score >= 25) {
            result = "PASS_WITH_WARNINGS";
        } else {
            result = "PASS";
        }

        if (reasons.isEmpty()) {
            reasons.add("public API probes and adapter registry look healthy");
        }

        lines.add("auto-probe result: " + result + " (score=" + score + ")");
        for (String reason : reasons.stream().distinct().toList()) {
            lines.add("auto-probe reason: " + reason);
        }
        lines.add("auto-probe complete; optional unsupported surfaces stay disabled");

        return new CompatibilityProbeReport(result, score, List.copyOf(lines));
    }

    private static int probeClass(List<String> lines, List<String> reasons, String label, String className, boolean required) {
        lines.add("attempt: find " + label + " at class " + className);
        try {
            Class.forName(className);
            lines.add("probe: " + label + "=OK");
            return 0;
        } catch (ClassNotFoundException exception) {
            lines.add("probe: " + label + "=" + (required ? "FAIL" : "WARN") + " missing class " + className);
            lines.add("issue: " + label + " missing; direct target=" + className + "; coreAffected=" + required);
            reasons.add(label + " missing");
            return required ? 60 : 10;
        }
    }

    private static int probeMethod(List<String> lines, List<String> reasons, String label, Class<?> owner, String methodName, boolean required, Class<?>... parameterTypes) {
        lines.add("attempt: find " + label + " at method " + owner.getName() + "#" + methodName);
        try {
            Method method = owner.getMethod(methodName, parameterTypes);
            lines.add("probe: " + label + "=OK (" + owner.getSimpleName() + "#" + method.getName() + ")");
            return 0;
        } catch (NoSuchMethodException exception) {
            lines.add("probe: " + label + "=" + (required ? "FAIL" : "WARN") + " missing " + owner.getSimpleName() + "#" + methodName);
            lines.add("issue: " + label + " missing; direct target=" + owner.getName() + "#" + methodName + "; coreAffected=" + required);
            reasons.add(label + " missing");
            return required ? 60 : 10;
        }
    }

    private int probeDataFolder(List<String> lines, List<String> reasons, Path folder) {
        lines.add("attempt: create/check plugin data folder at " + folder);
        try {
            Files.createDirectories(folder);
            if (!Files.isWritable(folder)) {
                lines.add("probe: Plugin data folder=FAIL not writable");
                lines.add("issue: Plugin data folder is not writable; direct target=" + folder + "; coreAffected=true");
                reasons.add("plugin data folder not writable");
                return 60;
            }

            lines.add("probe: Plugin data folder=OK");
            return 0;
        } catch (IOException | RuntimeException exception) {
            lines.add("probe: Plugin data folder=FAIL " + exception.getClass().getSimpleName());
            lines.add("issue: Plugin data folder could not be prepared; direct target=" + folder + "; coreAffected=true");
            reasons.add("plugin data folder unavailable");
            return 60;
        }
    }

    private int probeCommand(List<String> lines, List<String> reasons, String label, String commandName, boolean required) {
        lines.add("attempt: find " + label + " in plugin.yml");
        if (this.plugin.getCommand(commandName) != null) {
            lines.add("probe: " + label + "=OK");
            return 0;
        }

        lines.add("probe: " + label + "=" + (required ? "FAIL" : "WARN") + " missing command " + commandName);
        lines.add("issue: " + label + " missing from plugin.yml; direct target=plugin.yml command " + commandName + "; coreAffected=" + required);
        reasons.add(label + " missing");
        return required ? 60 : 10;
    }

    private int probeCommandGuardConfig(List<String> lines, List<String> reasons) {
        lines.add("attempt: read respect.command-guard config");
        if (!this.plugin.getConfig().getBoolean("respect.command-guard.enabled", true)) {
            lines.add("probe: Respect command guard=WARN disabled by config");
            reasons.add("respect command guard disabled by config");
            return 8;
        }

        List<String> patterns = this.plugin.getConfig().getStringList("respect.command-guard.patterns");
        if (patterns.isEmpty()) {
            lines.add("probe: Respect command guard=WARN no command patterns configured");
            reasons.add("respect command guard has no configured patterns");
            return 10;
        }

        boolean scanAllCommands = this.plugin.getConfig().getBoolean("respect.command-guard.scan-all-commands.enabled", true);
        List<String> ignoredRoots = this.plugin.getConfig().getStringList("respect.command-guard.scan-all-commands.ignored-command-roots");
        lines.add("probe: Respect command guard=OK patterns=" + patterns.size()
                + " scanAllCommands=" + scanAllCommands
                + " ignoredRoots=" + ignoredRoots.size());
        return 0;
    }
}
