package net.mineskyvanish.presence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.mineskyvanish.diagnostic.DiagnosticRecorder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PresenceAdapterRegistry {
    private final JavaPlugin plugin;
    private final DiagnosticRecorder diagnostics;
    private final List<RegisteredAdapter> adapters = new ArrayList<>();

    public PresenceAdapterRegistry(JavaPlugin plugin, DiagnosticRecorder diagnostics) {
        this.plugin = plugin;
        this.diagnostics = diagnostics;
    }

    public void register(PresenceAdapter adapter) {
        this.diagnostics.attempt("presence-adapter", "register " + adapter.name(), "required=" + adapter.required());
        boolean enabled;
        String status;
        try {
            enabled = adapter.isAvailable();
            status = enabled ? "enabled" : "not available";
        } catch (LinkageError | RuntimeException exception) {
            enabled = false;
            status = "availability check failed: " + exception.getClass().getSimpleName();
            this.diagnostics.issue("presence-adapter", "register " + adapter.name(), "adapter availability check failed; surface will stay disabled", exception, adapter.required());
        }

        this.adapters.add(new RegisteredAdapter(adapter, enabled, status));
        this.diagnostics.result("presence-adapter", "register " + adapter.name(), status);

        if (enabled) {
            this.plugin.getLogger().info("Presence adapter enabled: " + adapter.name());
        } else if (adapter.required()) {
            this.plugin.getLogger().severe("Required presence adapter unavailable: " + adapter.name() + " (" + status + ")");
            this.diagnostics.issue("presence-adapter", "register " + adapter.name(), "required adapter is not available on this server API", null, true);
        } else {
            this.plugin.getLogger().info("Presence adapter unavailable: " + adapter.name() + " (" + status + ")");
        }
    }

    public void hide(Player viewer, Player target) {
        apply(viewer, target, false);
    }

    public void show(Player viewer, Player target) {
        apply(viewer, target, true);
    }

    public List<String> statusLines() {
        return this.adapters.stream()
                .map(adapter -> adapter.adapter().name() + ": " + adapter.status())
                .toList();
    }

    private void apply(Player viewer, Player target, boolean visible) {
        for (int index = 0; index < this.adapters.size(); index++) {
            RegisteredAdapter registered = this.adapters.get(index);
            if (!registered.enabled()) {
                continue;
            }

            try {
                if (visible) {
                    registered.adapter().show(viewer, target);
                } else {
                    registered.adapter().hide(viewer, target);
                }
            } catch (NoSuchMethodError | UnsupportedOperationException exception) {
                disable(index, registered, exception);
            } catch (RuntimeException exception) {
                disable(index, registered, exception);
            }
        }
    }

    private void disable(int index, RegisteredAdapter registered, Throwable exception) {
        if (registered.adapter().required()) {
            this.plugin.getLogger().log(Level.SEVERE, "Required presence adapter " + registered.adapter().name() + " failed.", exception);
            this.diagnostics.issue("presence-adapter", "apply " + registered.adapter().name(), "required adapter failed while updating viewer visibility", exception, true);
            return;
        }

        String status = "disabled after failure: " + exception.getClass().getSimpleName();
        this.adapters.set(index, new RegisteredAdapter(registered.adapter(), false, status));

        this.plugin.getLogger().log(Level.WARNING, "Presence adapter " + registered.adapter().name() + " " + status, exception);
        this.diagnostics.issue("presence-adapter", "apply " + registered.adapter().name(), "optional adapter disabled; core Bukkit visibility remains active", exception, false);
    }

    private record RegisteredAdapter(PresenceAdapter adapter, boolean enabled, String status) {
    }
}
