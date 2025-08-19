package net.Ruben54213.Manager;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapManager {

    private final SmashMapsV2 plugin;
    private final Map<UUID, List<SmashMap>> playerMaps;
    private final File mapsFile;
    private FileConfiguration mapsConfig;
    private int nextMapId;

    public MapManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
        this.playerMaps = new HashMap<>();
        this.mapsFile = new File(plugin.getDataFolder(), "maps.yml");
        loadMaps();
    }

    private void loadMaps() {
        if (!mapsFile.exists()) {
            try {
                mapsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create maps.yml file!");
                e.printStackTrace();
            }
        }

        mapsConfig = YamlConfiguration.loadConfiguration(mapsFile);
        nextMapId = mapsConfig.getInt("next-id", 1);

        // Load existing maps
        if (mapsConfig.contains("maps")) {
            for (String mapId : mapsConfig.getConfigurationSection("maps").getKeys(false)) {
                String ownerUUID = mapsConfig.getString("maps." + mapId + ".owner");
                String mapName = mapsConfig.getString("maps." + mapId + ".name");
                String worldName = mapsConfig.getString("maps." + mapId + ".world");

                // Load icon data
                String iconMaterialString = mapsConfig.getString("maps." + mapId + ".icon_material", "GRASS_BLOCK");
                String iconDisplayName = mapsConfig.getString("maps." + mapId + ".icon_display_name");

                org.bukkit.Material iconMaterial;
                try {
                    iconMaterial = org.bukkit.Material.valueOf(iconMaterialString);
                } catch (IllegalArgumentException e) {
                    iconMaterial = org.bukkit.Material.GRASS_BLOCK;
                }

                SmashMap map = new SmashMap(Integer.parseInt(mapId), UUID.fromString(ownerUUID), mapName, worldName, iconMaterial, iconDisplayName);

                UUID playerUUID = UUID.fromString(ownerUUID);
                playerMaps.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(map);
            }
        }
    }

    public SmashMap createMap(Player player, String mapName) {
        int mapId = nextMapId++;
        String worldName = String.valueOf(mapId);

        SmashMap map = new SmashMap(mapId, player.getUniqueId(), mapName, worldName);

        // Save to memory
        playerMaps.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(map);

        // Save to file
        saveMaps();

        return map;
    }

    public List<SmashMap> getPlayerMaps(UUID playerUUID) {
        return playerMaps.getOrDefault(playerUUID, new ArrayList<>());
    }

    public int getPlayerMapCount(UUID playerUUID) {
        return getPlayerMaps(playerUUID).size();
    }

    public int getPlayerMapLimit(Player player) {
        if (player.hasPermission("smashmaps.maplimit.unlimited")) {
            return Integer.MAX_VALUE;
        }

        for (int i = 50; i >= 1; i--) {
            if (player.hasPermission("smashmaps.maplimit." + i)) {
                return i;
            }
        }

        return 1; // Default limit
    }

    public boolean canCreateMap(Player player) {
        return getPlayerMapCount(player.getUniqueId()) < getPlayerMapLimit(player);
    }

    /**
     * Check if ANY player already has a map with the given name (global check)
     * @param mapName Name to check
     * @return true if any player already has a map with this name
     */
    public boolean hasMapWithName(String mapName) {
        for (List<SmashMap> maps : playerMaps.values()) {
            for (SmashMap map : maps) {
                if (map.getName().equalsIgnoreCase(mapName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMapOwner(UUID playerUUID, String worldName) {
        List<SmashMap> maps = getPlayerMaps(playerUUID);
        return maps.stream().anyMatch(map -> map.getWorldName().equals(worldName));
    }

    public SmashMap getMapByWorld(String worldName) {
        for (List<SmashMap> maps : playerMaps.values()) {
            for (SmashMap map : maps) {
                if (map.getWorldName().equals(worldName)) {
                    return map;
                }
            }
        }
        return null;
    }

    public SmashMap getMapById(int mapId) {
        for (List<SmashMap> maps : playerMaps.values()) {
            for (SmashMap map : maps) {
                if (map.getId() == mapId) {
                    return map;
                }
            }
        }
        return null;
    }

    public void updateMap(SmashMap map) {
        // Map is updated by reference, just save to file
        saveMaps();
    }

    private void saveMaps() {
        mapsConfig.set("next-id", nextMapId);

        // Clear existing maps
        mapsConfig.set("maps", null);

        // Save all maps
        for (List<SmashMap> maps : playerMaps.values()) {
            for (SmashMap map : maps) {
                String path = "maps." + map.getId();
                mapsConfig.set(path + ".owner", map.getOwnerUUID().toString());
                mapsConfig.set(path + ".name", map.getName());
                mapsConfig.set(path + ".world", map.getWorldName());
                mapsConfig.set(path + ".icon_material", map.getIconMaterial().toString());
                mapsConfig.set(path + ".icon_display_name", map.getIconDisplayName());
            }
        }

        try {
            mapsConfig.save(mapsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save maps.yml!");
            e.printStackTrace();
        }
    }
}