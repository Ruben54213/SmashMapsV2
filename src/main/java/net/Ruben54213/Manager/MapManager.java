
package net.Ruben54213.Manager;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapManager {

    private final SmashMapsV2 plugin;
    private final Map<UUID, List<SmashMap>> playerMaps;
    private final Map<UUID, SmashMap> pendingDeletions;
    private final File mapsFile;
    private FileConfiguration mapsConfig;
    private int nextMapId;

    public MapManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
        this.playerMaps = new HashMap<>();
        this.pendingDeletions = new ConcurrentHashMap<>();
        this.mapsFile = new File(plugin.getDataFolder(), "maps.yml");
        loadMaps();
    }
    public int getNextMapId() {
        return nextMapId;
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
                boolean approved = mapsConfig.getBoolean("maps." + mapId + ".approved", false);

                // Load icon data
                String iconMaterialString = mapsConfig.getString("maps." + mapId + ".icon_material", "GRASS_BLOCK");
                String iconDisplayName = mapsConfig.getString("maps." + mapId + ".icon_display_name");

                org.bukkit.Material iconMaterial;
                try {
                    iconMaterial = org.bukkit.Material.valueOf(iconMaterialString);
                } catch (IllegalArgumentException e) {
                    iconMaterial = org.bukkit.Material.GRASS_BLOCK;
                }

                SmashMap map = new SmashMap(Integer.parseInt(mapId), UUID.fromString(ownerUUID), mapName, worldName, iconMaterial, iconDisplayName, approved);

                // Aktualisiere/Setze den Anzeigenamen des Eigentümers in der Config
                org.bukkit.OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(ownerUUID));
                String ownerName = (offlinePlayer != null && offlinePlayer.getName() != null)
                        ? offlinePlayer.getName()
                        : mapsConfig.getString("maps." + mapId + ".owner_name", "Unknown");
                mapsConfig.set("maps." + mapId + ".owner_name", ownerName);

                UUID playerUUID = UUID.fromString(ownerUUID);
                playerMaps.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(map);
            }
        }

        // Persistiere eventuell aktualisierte Owner-Namen
        try {
            mapsConfig.save(mapsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not persist updated owner names to maps.yml");
            e.printStackTrace();
        }
    }

    public SmashMap createMap(Player player, String mapName) {
        int mapId = nextMapId++;
        String worldName = String.valueOf(mapId);

        SmashMap map = new SmashMap(mapId, player.getUniqueId(), mapName, worldName);

        // Save to memory
        playerMaps.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(map);

        // Create the world immediately
        plugin.getWorldManager().createMapWorld(map);

        // Save to file
        saveMaps();

        return map;
    }

    // Neue Methode zum Löschen von Maps
    public boolean deleteMap(int mapId) {
        SmashMap map = getMapById(mapId);
        if (map == null) return false;
        // Approved maps dürfen grundsätzlich nicht gelöscht werden
        if (map.isApproved()) return false;

        // Remove from memory
        List<SmashMap> ownerMaps = playerMaps.get(map.getOwnerUUID());
        if (ownerMaps != null) {
            ownerMaps.removeIf(m -> m.getId() == mapId);
            if (ownerMaps.isEmpty()) {
                playerMaps.remove(map.getOwnerUUID());
            }
        }

        // Delete world files
        plugin.getWorldManager().deleteMapWorld(map);

        // Entferne den Abschnitt der Map vollständig aus der Config (inkl. positions)
        mapsConfig.set("maps." + mapId, null);

        // Save to file
        saveMaps();

        return true;
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

    public SmashMap getMapByName(String mapName) {
        for (List<SmashMap> maps : playerMaps.values()) {
            for (SmashMap map : maps) {
                if (map.getName().equalsIgnoreCase(mapName)) {
                    return map;
                }
            }
        }
        return null;
    }

    public List<SmashMap> getAllMaps() {
        List<SmashMap> allMaps = new ArrayList<>();
        for (List<SmashMap> maps : playerMaps.values()) {
            allMaps.addAll(maps);
        }
        return allMaps;
    }

    public List<SmashMap> getApprovedMaps() {
        return getAllMaps().stream()
                .filter(SmashMap::isApproved)
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean approveMap(String mapName) {
        SmashMap map = getMapByName(mapName);
        if (map != null) {
            map.setApproved(true);
            saveMaps();
            return true;
        }
        return false;
    }

    public void updateMap(SmashMap map) {
        // Map is updated by reference, just save to file
        saveMaps();
    }

    private void saveMaps() {
        mapsConfig.set("next-id", nextMapId);

        // NICHT mehr die komplette Sektion löschen, damit positions erhalten bleiben!

        // Save all maps (Metadaten)
        for (List<SmashMap> maps : playerMaps.values()) {
            for (SmashMap map : maps) {
                String path = "maps." + map.getId();
                mapsConfig.set(path + ".owner", map.getOwnerUUID().toString());
                // Ermittele stets den aktuellen Spielernamen des Owners
                org.bukkit.OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(map.getOwnerUUID());
                String ownerName = (offlinePlayer != null && offlinePlayer.getName() != null) ? offlinePlayer.getName() : "Unknown";
                mapsConfig.set(path + ".owner_name", ownerName);

                mapsConfig.set(path + ".name", map.getName());
                mapsConfig.set(path + ".world", map.getWorldName());
                mapsConfig.set(path + ".icon_material", map.getIconMaterial().toString());
                mapsConfig.set(path + ".icon_display_name", map.getIconDisplayName());
                mapsConfig.set(path + ".approved", map.isApproved());
                // positions-Untersektion bleibt unberührt
            }
        }

        try {
            mapsConfig.save(mapsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save maps.yml!");
            e.printStackTrace();
        }
    }

    // Methoden für Pending Deletions
    public void addPendingDeletion(UUID playerUUID, SmashMap map) {
        pendingDeletions.put(playerUUID, map);
    }

    public SmashMap getPendingDeletion(UUID playerUUID) {
        return pendingDeletions.get(playerUUID);
    }

    public void removePendingDeletion(UUID playerUUID) {
        pendingDeletions.remove(playerUUID);
    }

    public boolean hasPendingDeletion(UUID playerUUID) {
        return pendingDeletions.containsKey(playerUUID);
    }

    // Neue Methode zum Umbenennen von Maps
    public boolean renameMap(int mapId, String newName) {
        SmashMap map = getMapById(mapId);
        if (map == null) return false;

        // Setze den neuen Namen
        map.setName(newName);

        // Speichere die Änderungen
        saveMaps();

        return true;
    }

    // --------------------------
    // Positions-API (maps.yml)
    // --------------------------

    private Integer getMapIdByWorldName(String worldName) {
        SmashMap map = getMapByWorld(worldName);
        return map != null ? map.getId() : null;
    }

    private String posPath(String worldName) {
        Integer id = getMapIdByWorldName(worldName);
        return id == null ? null : "maps." + id + ".positions";
    }

    private String serializeBlock(Location loc) {
        return loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location deserializeBlock(String s, String worldName) {
        if (s == null) return null;
        try {
            String[] arr = s.split(",");
            int x = Integer.parseInt(arr[0]);
            int y = Integer.parseInt(arr[1]);
            int z = Integer.parseInt(arr[2]);
            // Welt wird ggf. später gesetzt (Anzeige im Kontext der aktuellen Welt)
            return new Location(Bukkit.getWorld("maps/" + worldName), x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringList(String path) {
        return new ArrayList<>(mapsConfig.getStringList(path));
    }

    public List<Location> getItemSpawns(String worldName) {
        String base = posPath(worldName);
        if (base == null) return Collections.emptyList();
        List<String> raw = getStringList(base + ".items");
        List<Location> list = new ArrayList<>();
        for (String s : raw) {
            Location l = deserializeBlock(s, worldName);
            if (l != null) list.add(l);
        }
        return list;
    }

    public List<Location> getPlayerSpawns(String worldName) {
        String base = posPath(worldName);
        if (base == null) return Collections.emptyList();
        List<String> raw = getStringList(base + ".spawns");
        List<Location> list = new ArrayList<>();
        for (String s : raw) {
            Location l = deserializeBlock(s, worldName);
            if (l != null) list.add(l);
        }
        return list;
    }

    public Location getCenter(String worldName) {
        String base = posPath(worldName);
        if (base == null) return null;
        String raw = mapsConfig.getString(base + ".center");
        return deserializeBlock(raw, worldName);
    }

    public boolean addItemSpawn(String worldName, Location loc) {
        String base = posPath(worldName);
        if (base == null) return false;
        List<String> raw = getStringList(base + ".items");
        if (raw.size() >= 30) return false;
        String ser = serializeBlock(loc);
        if (!raw.contains(ser)) raw.add(ser);
        mapsConfig.set(base + ".items", raw);
        saveSilent();
        return true;
    }

    public boolean addPlayerSpawn(String worldName, Location loc) {
        String base = posPath(worldName);
        if (base == null) return false;
        List<String> raw = getStringList(base + ".spawns");
        if (raw.size() >= 30) return false;
        String ser = serializeBlock(loc);
        if (!raw.contains(ser)) raw.add(ser);
        mapsConfig.set(base + ".spawns", raw);
        saveSilent();
        return true;
    }

    public void setCenter(String worldName, Location loc) {
        String base = posPath(worldName);
        if (base == null) return;
        mapsConfig.set(base + ".center", serializeBlock(loc)); // immer genau 1 Center
        saveSilent();
    }

    public boolean removeItemSpawn(String worldName, Location loc) {
        String base = posPath(worldName);
        if (base == null) return false;
        List<String> raw = getStringList(base + ".items");
        boolean removed = raw.remove(serializeBlock(loc));
        if (removed) {
            mapsConfig.set(base + ".items", raw);
            saveSilent();
        }
        return removed;
    }

    public boolean removePlayerSpawn(String worldName, Location loc) {
        String base = posPath(worldName);
        if (base == null) return false;
        List<String> raw = getStringList(base + ".spawns");
        boolean removed = raw.remove(serializeBlock(loc));
        if (removed) {
            mapsConfig.set(base + ".spawns", raw);
            saveSilent();
        }
        return removed;
    }

    public void clearCenter(String worldName) {
        String base = posPath(worldName);
        if (base == null) return;
        mapsConfig.set(base + ".center", null);
        saveSilent();
    }

    private void saveSilent() {
        try {
            mapsConfig.save(mapsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save positions to maps.yml: " + e.getMessage());
        }
    }
}