package net.mineskyvanish.diagnostic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiagnosticRecorder {
    private final JavaPlugin plugin;
    private final Path debugFile;
    private final AtomicBoolean warnedWriteFailure = new AtomicBoolean();

    public DiagnosticRecorder(JavaPlugin plugin) {
        this.plugin = plugin;
        this.debugFile = plugin.getDataFolder().toPath().resolve("presence-debug.log");
    }

    public Path debugFile() {
        return this.debugFile;
    }

    @SuppressWarnings("deprecation")
    public synchronized void startNewRun() {
        List<String> header = List.of(
                "# mienskyvanish adaptive presence diagnostics",
                "# This file is rewritten on startup and appended to when optional surfaces fail.",
                "# It records public-API probes and runtime failure points only; it does not touch NMS, CraftBukkit, or packets.",
                "time=" + Instant.now(),
                "plugin=" + this.plugin.getDescription().getName() + " " + this.plugin.getDescription().getVersion(),
                "server=" + Bukkit.getName() + " " + Bukkit.getVersion(),
                "");
        writeLines(header, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public void recordStartupReport(CompatibilityProbeReport report) {
        appendBlock("startup-probe", report.lines());
        append("startup-probe", "result", report.result() + " score=" + report.score());
    }

    public void attempt(String area, String action, String detail) {
        append(area, "attempt", action + " | " + detail);
    }

    public void result(String area, String action, String detail) {
        append(area, "result", action + " | " + detail);
    }

    public void issue(String area, String action, String issue, Throwable throwable, boolean coreAffected) {
        StringBuilder builder = new StringBuilder()
                .append(action)
                .append(" | issue=")
                .append(issue)
                .append(" | coreAffected=")
                .append(coreAffected);

        if (throwable != null) {
            builder.append(" | exception=")
                    .append(throwable.getClass().getName());
            if (throwable.getMessage() != null && !throwable.getMessage().isBlank()) {
                builder.append(": ")
                        .append(throwable.getMessage().replace(System.lineSeparator(), " "));
            }
        }

        append(area, "issue", builder.toString());

        if (throwable != null) {
            appendBlock(area + "-stacktrace", stackTraceLines(throwable));
        }
    }

    private synchronized void appendBlock(String area, List<String> lines) {
        StringBuilder builder = new StringBuilder()
                .append(System.lineSeparator())
                .append("[")
                .append(Instant.now())
                .append("] ")
                .append(area)
                .append(System.lineSeparator());
        for (String line : lines) {
            builder.append(line).append(System.lineSeparator());
        }
        writeString(builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }

    private synchronized void append(String area, String type, String detail) {
        writeString("[" + Instant.now() + "] " + area + " " + type + ": " + detail + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }

    private void writeLines(List<String> lines, StandardOpenOption... options) {
        writeString(String.join(System.lineSeparator(), lines) + System.lineSeparator(), options);
    }

    private void writeString(String text, StandardOpenOption... options) {
        try {
            Files.createDirectories(this.debugFile.getParent());
            Files.writeString(this.debugFile, text, StandardCharsets.UTF_8, options);
        } catch (IOException | UncheckedIOException exception) {
            warnWriteFailure(exception);
        }
    }

    private void warnWriteFailure(Exception exception) {
        if (this.warnedWriteFailure.compareAndSet(false, true)) {
            this.plugin.getLogger().log(Level.WARNING, "Could not write StealthVanish diagnostic file: " + this.debugFile, exception);
        }
    }

    private static List<String> stackTraceLines(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().lines().toList();
    }
}
