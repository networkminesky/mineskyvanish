package net.mineskyvanish.respect;

import java.util.List;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineskyvanish.diagnostic.DiagnosticRecorder;
import net.mineskyvanish.visibility.VanishService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerCommandRespectGuard implements Listener {
    private static final String CONFIG_PATH = "respect.command-guard";

    private final JavaPlugin plugin;
    private final VanishService vanishService;
    private final DiagnosticRecorder diagnostics;
    private final boolean enabled;
    private final String blockedMessage;
    private final List<CommandPatternRule> rules;
    private final boolean scanAllCommands;
    private final HiddenNameCommandScanner hiddenNameScanner;

    public PlayerCommandRespectGuard(JavaPlugin plugin, VanishService vanishService, DiagnosticRecorder diagnostics) {
        this.plugin = plugin;
        this.vanishService = vanishService;
        this.diagnostics = diagnostics;

        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean(CONFIG_PATH + ".enabled", true);
        this.blockedMessage = config.getString(CONFIG_PATH + ".blocked-message", "That player is not online.");
        this.rules = loadRules(config.getStringList(CONFIG_PATH + ".patterns"));
        this.scanAllCommands = config.getBoolean(CONFIG_PATH + ".scan-all-commands.enabled", true);
        this.hiddenNameScanner = new HiddenNameCommandScanner(config.getStringList(CONFIG_PATH + ".scan-all-commands.ignored-command-roots"));

        this.diagnostics.result("respect-command-guard", "load command guard",
                "enabled=" + this.enabled + " rules=" + this.rules.size() + " scanAllCommands=" + this.scanAllCommands);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!this.enabled || this.rules.isEmpty() || event.getPlayer().hasPermission("stealthvanish.see")) {
            return;
        }

        String command = event.getMessage();
        for (CommandPatternRule rule : this.rules) {
            String targetName = rule.target(command).orElse(null);
            if (targetName == null || !this.vanishService.shouldHideNameFrom(event.getPlayer(), targetName)) {
                continue;
            }

            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text(this.blockedMessage, NamedTextColor.RED));
            this.diagnostics.result("respect-command-guard", "blocked player-targeting command",
                    "sender=" + event.getPlayer().getName() + " target=" + targetName + " pattern=" + rule.rawPattern());
            return;
        }

        if (this.scanAllCommands) {
            String targetName = this.hiddenNameScanner.firstHiddenName(command,
                    name -> this.vanishService.shouldHideNameFrom(event.getPlayer(), name)).orElse(null);
            if (targetName != null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text(this.blockedMessage, NamedTextColor.RED));
                this.diagnostics.result("respect-command-guard", "blocked command containing hidden name",
                        "sender=" + event.getPlayer().getName() + " target=" + targetName);
            }
        }
    }

    private List<CommandPatternRule> loadRules(List<String> rawPatterns) {
        return rawPatterns.stream()
                .map(this::compileRule)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<CommandPatternRule> compileRule(String rawPattern) {
        if (rawPattern == null || rawPattern.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(CommandPatternRule.compileRequired(rawPattern));
        } catch (PatternSyntaxException exception) {
            this.plugin.getLogger().warning("Ignoring invalid StealthVanish command-guard pattern: " + rawPattern);
            this.diagnostics.issue("respect-command-guard", "compile command pattern",
                    "invalid regex in config.yml: " + rawPattern, exception, false);
            return Optional.empty();
        }
    }
}
