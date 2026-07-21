package net.mineskyvanish.respect;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HiddenNameCommandScanner {
    private static final Pattern USERNAME_TOKEN = Pattern.compile("(?<![A-Za-z0-9_])([A-Za-z0-9_]{3,16})(?![A-Za-z0-9_])");

    private final Set<String> ignoredCommandRoots;

    public HiddenNameCommandScanner(List<String> ignoredCommandRoots) {
        this.ignoredCommandRoots = ignoredCommandRoots.stream()
                .filter(root -> root != null && !root.isBlank())
                .map(HiddenNameCommandScanner::normalizeRoot)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public Optional<String> firstHiddenName(String command, Predicate<String> hiddenNamePredicate) {
        if (command == null || command.isBlank() || isIgnoredCommand(command)) {
            return Optional.empty();
        }

        Matcher matcher = USERNAME_TOKEN.matcher(command);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (hiddenNamePredicate.test(name)) {
                return Optional.of(name);
            }
        }

        return Optional.empty();
    }

    public boolean isIgnoredCommand(String command) {
        String root = commandRoot(command);
        if (root == null) {
            return true;
        }

        String normalizedRoot = normalizeRoot(root);
        return this.ignoredCommandRoots.contains(normalizedRoot) || this.ignoredCommandRoots.contains(stripNamespace(normalizedRoot));
    }

    private static String commandRoot(String command) {
        String trimmed = command.trim();
        if (trimmed.isEmpty() || trimmed.charAt(0) != '/') {
            return null;
        }

        String withoutSlash = trimmed.substring(1).trim();
        if (withoutSlash.isEmpty()) {
            return null;
        }

        int firstSpace = withoutSlash.indexOf(' ');
        if (firstSpace == -1) {
            return withoutSlash;
        }

        return withoutSlash.substring(0, firstSpace);
    }

    private static String normalizeRoot(String root) {
        String normalized = root.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static String stripNamespace(String root) {
        int namespaceSeparator = root.indexOf(':');
        if (namespaceSeparator == -1) {
            return root;
        }
        return root.substring(namespaceSeparator + 1);
    }
}
