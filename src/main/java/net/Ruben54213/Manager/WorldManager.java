package net.Ruben54213.Manager;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class WorldManager {

    private final SmashMapsV2 plugin;

    public WorldManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    public World createMapWorld(SmashMap map) {
        WorldCreator creator = new WorldCreator(map.getWorldName())
                .environment(World.Environment.NORMAL)
                .type(WorldType.FLAT)
                .generatorSettings("minecraft:air;minecraft:air;minecraft:air;minecraft:air") // Pure void with only bedrock at bottom
                .generateStructures(false);

        World world = creator.createWorld();

        if (world != null) {
            setupWorld(world);
            createSpawnPlatform(world);
        }

        return world;
    }

    private void setupWorld(World world) {
        // Ensure this runs on the main thread
        if (!plugin.getServer().isPrimaryThread()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> setupWorld(world));
            return;
        }

        // Set world properties
        world.setTime(1000); // Day time
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
        world.setKeepSpawnInMemory(true);

        // Set world border
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0.5, 0.5);
        border.setSize(200); // Smaller border for void world
        border.setWarningDistance(5);
        border.setWarningTime(10);

        // Set game rules
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.FALL_DAMAGE, false);
    }

    private void createSpawnPlatform(World world) {
        // Create small 3x3x1 platform at Y=100
        Material platformMaterial = Material.STONE;

        try {
            String configMaterial = plugin.getConfigManager().getSpawnPlatformMaterial();
            if (configMaterial != null && !configMaterial.isEmpty()) {
                platformMaterial = Material.valueOf(configMaterial);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid platform material in config, using STONE");
            platformMaterial = Material.STONE;
        }

        int platformY = 100;

        // Create 3x3 platform centered at 0,0
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location loc = new Location(world, x, platformY, z);
                world.getBlockAt(loc).setType(platformMaterial);
            }
        }

        // Set spawn location on top of platform center
        world.setSpawnLocation(0, platformY + 1, 0);

        plugin.getLogger().info("Created 3x3 spawn platform at Y=" + platformY + " for world: " + world.getName());
    }

    public void teleportToMap(Player player, SmashMap map) {
        World world = Bukkit.getWorld(map.getWorldName());

        if (world == null) {
            world = createMapWorld(map);
        }

        if (world != null) {
            // Teleport to center of 3x3 platform
            Location spawnLocation = new Location(world, 0.5, 101, 0.5);
            spawnLocation.setYaw(0); // Face north
            spawnLocation.setPitch(0); // Look straight ahead

            player.teleport(spawnLocation);

            // Set player to creative mode in their map
            player.setGameMode(GameMode.CREATIVE);

            // Play teleport sound
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("map_created"), 1.0f, 1.0f);

            plugin.getLogger().info("Teleported player " + player.getName() + " to map world: " + world.getName());
        } else {
            plugin.getLogger().severe("Failed to create or load world: " + map.getWorldName());
        }
    }

    public boolean isMapWorld(World world) {
        if (world == null) return false;
        return plugin.getMapManager().getMapByWorld(world.getName()) != null;
    }
}