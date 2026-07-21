/**
 * Public integration API for StealthVanish.
 *
 * <p>Other plugins should use {@link net.mineskyvanish.api.VanishAPI}
 * for simple static calls, or load {@link net.mineskyvanish.api.MineSkyVanishAPI}
 * from Bukkit's ServicesManager when they want a service object. Both paths are
 * designed as respect hooks: they expose invisibility state and safe state
 * changes without pretending the player actually disconnected from the server.
 */
package net.mineskyvanish.api;
