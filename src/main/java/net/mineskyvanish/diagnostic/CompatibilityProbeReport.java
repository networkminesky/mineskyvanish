package net.mineskyvanish.diagnostic;

import java.util.List;

public record CompatibilityProbeReport(String result, int score, List<String> lines) {
}
