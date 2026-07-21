package net.mineskyvanish;

import net.mineskyvanish.api.MineSkyVanishAPI;
import net.mineskyvanish.command.VanishCommand;
import net.mineskyvanish.diagnostic.CompatibilityProbeReport;
import net.mineskyvanish.diagnostic.CompatibilityProbeService;
import net.mineskyvanish.diagnostic.DiagnosticRecorder;
import net.mineskyvanish.listener.PlayerVisibilityListener;
import net.mineskyvanish.listener.ServerPingPresenceListener;
import net.mineskyvanish.listener.TabCompletePresenceListener;
import net.mineskyvanish.presence.PresenceAdapterRegistry;
import net.mineskyvanish.presence.adapter.BukkitVisibilityAdapter;
import net.mineskyvanish.presence.adapter.PaperPlayerListAdapter;
import net.mineskyvanish.respect.PlayerCommandRespectGuard;
import net.mineskyvanish.storage.VanishStorage;
import net.mineskyvanish.visibility.VanishService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MineSkyVanish extends JavaPlugin {
    private VanishService vanishService;
    private DiagnosticRecorder diagnostics;

    @Override
    public void onEnable() {
        this.diagnostics = new DiagnosticRecorder(this);
        this.diagnostics.startNewRun();
        saveDefaultConfig();

        VanishStorage storage = new VanishStorage(this, this.diagnostics);
        PresenceAdapterRegistry presenceAdapters = new PresenceAdapterRegistry(this, this.diagnostics);
        presenceAdapters.register(new BukkitVisibilityAdapter(this));
        presenceAdapters.register(new PaperPlayerListAdapter());

        this.vanishService = new VanishService(this, storage, presenceAdapters, this.diagnostics);
        this.vanishService.load();

        runStartupCompatibilityAutomation();

        VanishCommand vanishCommand = new VanishCommand(this.vanishService);
        PluginCommand command = Objects.requireNonNull(getCommand("vanish"), "Comando /vanish faltando na plugin.yml");
        command.setExecutor(vanishCommand);
        command.setTabCompleter(vanishCommand);

        getServer().getPluginManager().registerEvents(new PlayerVisibilityListener(this.vanishService), this);
        getServer().getPluginManager().registerEvents(new ServerPingPresenceListener(this.vanishService), this);
        getServer().getPluginManager().registerEvents(new TabCompletePresenceListener(this.vanishService), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandRespectGuard(this, this.vanishService, this.diagnostics), this);
        getServer().getServicesManager().register(MineSkyVanishAPI.class, this.vanishService, this, ServicePriority.Normal);
        getLogger().info("MineSkyVanish enabled with " + this.vanishService.vanishedCount() + " persisted vanished player(s).");
        getLogger().info("MineSkyVanish diagnostics: " + this.diagnostics.debugFile());
    }

    private void runStartupCompatibilityAutomation() {
        CompatibilityProbeService probeService = new CompatibilityProbeService(this, this.vanishService);
        CompatibilityProbeReport report = probeService.runStartupProbe();
        this.diagnostics.recordStartupReport(report);
        getLogger().info("Adaptive presence startup probe: " + report.result() + " (score=" + report.score() + ")");
        for (String line : report.lines()) {
            getLogger().fine("[presence-probe] " + line);
        }
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
        if (this.vanishService != null) {
            this.vanishService.saveNow();
        }
    }

    public VanishService vanishService() {
        return this.vanishService;
    }
}
