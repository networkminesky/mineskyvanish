package net.mineskyvanish.storage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import net.mineskyvanish.diagnostic.DiagnosticRecorder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanishStorage {
    private final JavaPlugin plugin;
    private final DiagnosticRecorder diagnostics;
    private final File file;

    public VanishStorage(JavaPlugin plugin, DiagnosticRecorder diagnostics) {
        this.plugin = plugin;
        this.diagnostics = diagnostics;
        this.file = new File(plugin.getDataFolder(), "vanished.yml");
    }

    public Set<UUID> load() {
        if (!this.file.exists()) {
            this.diagnostics.result("storage", "load vanished.yml", "no file yet; starting with empty vanish state");
            return Set.of();
        }

        this.diagnostics.attempt("storage", "load vanished.yml", this.file.getAbsolutePath());
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.file);
        List<String> rawIds = configuration.getStringList("vanished");
        Set<UUID> ids = new LinkedHashSet<>();

        for (String rawId : rawIds) {
            try {
                ids.add(UUID.fromString(rawId));
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning("Ignoring invalid UUID in vanished.yml: " + rawId);
                this.diagnostics.issue("storage", "parse vanished UUID", "invalid UUID entry in vanished.yml: " + rawId, exception, false);
            }
        }

        this.diagnostics.result("storage", "load vanished.yml", "loaded " + ids.size() + " vanished UUID(s)");
        return ids;
    }

    public void save(Set<UUID> vanishedPlayers) {
        if (!this.plugin.getDataFolder().exists() && !this.plugin.getDataFolder().mkdirs()) {
            this.plugin.getLogger().warning("Could not create plugin data folder; vanish state was not saved.");
            this.diagnostics.issue("storage", "save vanished.yml", "plugin data folder could not be created: " + this.plugin.getDataFolder(), null, false);
            return;
        }

        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("vanished", vanishedPlayers.stream().map(UUID::toString).sorted().toList());

        try {
            this.diagnostics.attempt("storage", "save vanished.yml", "writing " + vanishedPlayers.size() + " vanished UUID(s)");
            configuration.save(this.file);
            this.diagnostics.result("storage", "save vanished.yml", "saved " + vanishedPlayers.size() + " vanished UUID(s)");
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save vanished player state.", exception);
            this.diagnostics.issue("storage", "save vanished.yml", "I/O failure while saving vanish state to " + this.file.getAbsolutePath(), exception, false);
        }
    }
}
