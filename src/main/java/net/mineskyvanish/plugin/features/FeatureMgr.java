package net.mineskyvanish.plugin.features;

import net.mineskyvanish.plugin.MineSkyVanish;
import net.mineskyvanish.plugin.utils.Requirement;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class FeatureMgr {

    private static final Requirement<FeatureInfo> protocolLibInstalled = featureInfo -> Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"),
            oneDotEightOrHigher = featureInfo -> featureInfo.getPlugin().getVersionUtil().isOneDotXOrHigher(8),
            oneDotSeventeenOrHigher = featureInfo -> featureInfo.getPlugin().getVersionUtil().isOneDotXOrHigher(17),
            supportedServer = featureInfo -> Bukkit.getServer().getName().equals("Paper") || Bukkit.getServer().getName().equals("Purpur");
    private final Map<String, FeatureInfo> registeredFeatures = new HashMap<>();
    private final Set<Feature> activeFeatures = new HashSet<>();
    private final MineSkyVanish plugin;

    public FeatureMgr(MineSkyVanish plugin) {
        this.plugin = plugin;
        registeredFeatures.put("SilentOpenChest", new FeatureInfo(SilentOpenChest.class, plugin,
                Arrays.asList(protocolLibInstalled, oneDotEightOrHigher)));
        registeredFeatures.put("NightVision", new FeatureInfo(NightVision.class, plugin,
                Arrays.asList(protocolLibInstalled, oneDotEightOrHigher)));
        registeredFeatures.put("VanishIndication", new FeatureInfo(VanishIndication.class, plugin,
                Arrays.asList(protocolLibInstalled, oneDotEightOrHigher)));
        registeredFeatures.put("Broadcast", new FeatureInfo(Broadcast.class, plugin));
        registeredFeatures.put("NoSculkSensorDetection", new FeatureInfo(NoSculkSensorDetection.class, plugin,
                Collections.singletonList(oneDotSeventeenOrHigher)));
        registeredFeatures.put("NoTurtleEggBreaking", new FeatureInfo(NoTurtleEggBreaking.class, plugin,
                Collections.singletonList(oneDotSeventeenOrHigher)));
        registeredFeatures.put("NoDripLeafTilt", new FeatureInfo(NoDripLeafTilt.class, plugin,
            Collections.singletonList(oneDotSeventeenOrHigher)));
        registeredFeatures.put("NoRaidTrigger", new FeatureInfo(NoRaidTrigger.class, plugin,
            Collections.singletonList(oneDotSeventeenOrHigher)));
        registeredFeatures.put("NoMobSpawn", new FeatureInfo(NoMobSpawn.class, plugin,
            Collections.singletonList(supportedServer)));
        registeredFeatures.put("HideAdvancementMessages", new FeatureInfo(HideAdvancementMessages.class, plugin,
            Collections.singletonList(supportedServer)));
    }

    public void enableFeatures() {
        featureLoop:
        for (String id : registeredFeatures.keySet()) {
            FeatureInfo featureInfo = registeredFeatures.get(id);
            for (Requirement<FeatureInfo> requirement : featureInfo.getRequirements()) {
                if (!requirement.fulfilledBy(featureInfo)) continue featureLoop;
            }
            Feature feature;
            try {
                feature = featureInfo.getFeatureClass().getConstructor(MineSkyVanish.class).newInstance(plugin);
            } catch (NoSuchMethodException | InvocationTargetException
                     | InstantiationException | IllegalAccessException e) {
                plugin.logException(e);
                continue;
            }
            if (!feature.isActive()) continue;
            activeFeatures.add(feature);
            Bukkit.getPluginManager().registerEvents(feature, plugin);
            feature.onEnable();
        }
    }

    public void disableFeatures() {
        for (Feature feature : activeFeatures) {
            feature.onDisable();
            HandlerList.unregisterAll(feature);
        }
        activeFeatures.clear();
    }

    public <T extends Feature> T getFeature(Class<T> featureClass) {
        for (Feature feature : activeFeatures) {
            if (feature.getClass().equals(featureClass)) {
                //noinspection unchecked
                return (T) feature;
            }
        }
        return null;
    }

    public Set<Feature> getActiveFeatures() {
        return activeFeatures;
    }

    private static class FeatureInfo {
        private final Class<? extends Feature> featureClass;
        private final Collection<Requirement<FeatureInfo>> requirements;
        private final MineSkyVanish plugin;

        FeatureInfo(Class<? extends Feature> featureClass, MineSkyVanish plugin,
                    Collection<Requirement<FeatureInfo>> requirements) {
            this.featureClass = featureClass;
            this.requirements = requirements;
            this.plugin = plugin;
        }

        FeatureInfo(Class<? extends Feature> featureClass, MineSkyVanish plugin) {
            this(featureClass, plugin, Collections.emptySet());
        }

        public MineSkyVanish getPlugin() {
            return plugin;
        }

        public Class<? extends Feature> getFeatureClass() {
            return featureClass;
        }

        public Collection<Requirement<FeatureInfo>> getRequirements() {
            return requirements;
        }
    }
}
