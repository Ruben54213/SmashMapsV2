package net.Ruben54213.Manager;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class SpawnManager {

    private final SmashMapsV2 plugin;

    public SpawnManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    public Location getSpawn() {
        File spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        if (!spawnFile.exists()) {
            return null;
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(spawnFile);
        String worldName = cfg.getString("spawn.world");
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // World not loaded or not found
            return null;
        }

        double x = cfg.getDouble("spawn.x");
        double y = cfg.getDouble("spawn.y");
        double z = cfg.getDouble("spawn.z");
        float yaw = (float) cfg.getDouble("spawn.yaw");
        float pitch = (float) cfg.getDouble("spawn.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }
}
