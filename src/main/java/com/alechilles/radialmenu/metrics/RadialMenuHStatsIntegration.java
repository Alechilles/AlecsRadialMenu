package com.alechilles.radialmenu.metrics;

import java.nio.file.Path;
import java.util.logging.Level;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

/**
 * Bootstraps HStats metrics reporting for Alec's Radial Menu.
 */
public final class RadialMenuHStatsIntegration {

    private static final String RADIAL_MENU_HSTATS_UUID = "775d797e-2618-4d5b-b710-dfd48154ce0f";
    private static final Path HSTATS_SERVER_UUID_FILE = Path.of("hstats-server-uuid.txt");

    private final JavaPlugin plugin;
    private boolean initialized;

    public RadialMenuHStatsIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (initialized || plugin == null) {
            return;
        }
        String version = resolvePluginVersion();
        try {
            new HStats(RADIAL_MENU_HSTATS_UUID, version);
            initialized = true;
            if (HStatsServerUuidFile.readEnabledServerUuid(HSTATS_SERVER_UUID_FILE) == null) {
                plugin.getLogger().at(Level.INFO).log(
                        "Radial Menu metrics are disabled by server config (hstats-server-uuid.txt)."
                );
                return;
            }
            plugin.getLogger().at(Level.INFO).log(
                    "Radial Menu metrics enabled via HStats. Server owners can opt out in hstats-server-uuid.txt."
            );
        } catch (Exception ex) {
            plugin.getLogger().at(Level.WARNING).withCause(ex)
                    .log("Radial Menu metrics failed to initialize; continuing without HStats.");
        }
    }

    private String resolvePluginVersion() {
        PluginManifest manifest = plugin.getManifest();
        if (manifest == null) {
            return "Unknown";
        }
        Semver version = manifest.getVersion();
        if (version == null) {
            return "Unknown";
        }
        return version.toString();
    }
}
