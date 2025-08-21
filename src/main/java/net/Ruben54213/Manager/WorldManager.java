package net.Ruben54213.Manager;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.Generator.VoidWorldGenerator;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

public class WorldManager {

    private final SmashMapsV2 plugin;
    private final File mapsDirectory;
    private final Set<Integer> pregeneratingWorlds;
    private final Set<Integer> pregeneratedWorlds;

    public WorldManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
        this.pregeneratingWorlds = ConcurrentHashMap.newKeySet();
        this.pregeneratedWorlds = ConcurrentHashMap.newKeySet();

        // Create maps directory in server root
        this.mapsDirectory = new File("maps");
        if (!mapsDirectory.exists()) {
            if (mapsDirectory.mkdirs()) {
                plugin.getLogger().info("Created maps directory: " + mapsDirectory.getAbsolutePath());
            } else {
                plugin.getLogger().severe("Failed to create maps directory: " + mapsDirectory.getAbsolutePath());
            }
        }

        // Start pregeneration system with delay
        plugin.getServer().getScheduler().runTaskLater(plugin, this::startPregenerationSystem, 100L);
    }

    private void startPregenerationSystem() {
        // Start pregeneration task that runs every 10 seconds
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::pregenerateWorlds, 0L, 200L);
    }

    private void pregenerateWorlds() {
        int nextMapId = plugin.getMapManager().getNextMapId();
        int pregeneratedCount = 0;

        // Count how many worlds are already pregenerated or being pregenerated
        for (int i = 0; i < 10; i++) { // Check next 10 IDs
            int worldId = nextMapId + i;
            if (pregeneratedWorlds.contains(worldId) || pregeneratingWorlds.contains(worldId) || worldExists(String.valueOf(worldId))) {
                pregeneratedCount++;
            }
        }

        // Pregenerate worlds if we have less than 5
        if (pregeneratedCount < 5) {
            for (int i = 0; i < 10 && pregeneratedCount < 5; i++) {
                int worldId = nextMapId + i;
                if (!pregeneratedWorlds.contains(worldId) &&
                        !pregeneratingWorlds.contains(worldId) &&
                        !worldExists(String.valueOf(worldId))) {

                    pregeneratingWorlds.add(worldId);

                    // Schedule world creation on main thread with staggered delay
                    long delay = (long) pregeneratedCount * 100L; // 5 second delay between each world
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        createPregeneratedWorld(worldId);
                    }, delay);

                    pregeneratedCount++;
                }
            }
        }
    }

    private void createPregeneratedWorld(int worldId) {
        try {
            String worldName = "maps/" + worldId;

            // Create world creator with void generator
            WorldCreator creator = new WorldCreator(worldName);
            creator.generator(new VoidWorldGenerator());
            creator.type(WorldType.NORMAL);
            creator.generateStructures(false);
            creator.environment(World.Environment.NORMAL);

            // Create the world
            World world = creator.createWorld();

            if (world != null) {
                // Configure world settings
                configureWorld(world);

                // Create spawn platform in chunks over multiple ticks
                createSpawnPlatformAsync(world, () -> {
                    // Unload world to save memory, but keep files
                    Bukkit.unloadWorld(world, true);

                    // Mark as pregenerated
                    pregeneratingWorlds.remove(worldId);
                    pregeneratedWorlds.add(worldId);

                    plugin.getLogger().info("Pregenerated and unloaded world: " + worldId);
                });
            } else {
                pregeneratingWorlds.remove(worldId);
                plugin.getLogger().severe("Failed to pregenerate world: " + worldId);
            }

        } catch (Exception e) {
            pregeneratingWorlds.remove(worldId);
            plugin.getLogger().severe("Error pregenerating world " + worldId + ": " + e.getMessage());
        }
    }

    private void createSpawnPlatformAsync(World world, Runnable callback) {
        // Get platform material and size from config
        Material platformMaterial;
        try {
            platformMaterial = Material.valueOf(plugin.getConfigManager().getSpawnPlatformMaterial());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid spawn platform material in config, using STONE");
            platformMaterial = Material.STONE;
        }

        int platformSize = plugin.getConfigManager().getSpawnPlatformSize();
        int halfSize = platformSize / 2;

        // Load spawn chunk
        world.getChunkAt(0, 0).load(true);

        // Create platform blocks spread over multiple ticks to avoid lag
        final Material finalMaterial = platformMaterial;
        final int[] currentX = {-halfSize};
        final int[] currentZ = {-halfSize};

        // Task that places blocks gradually
        plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            private int taskId = -1;

            @Override
            public void run() {
                int blocksPlaced = 0;

                // Place up to 9 blocks per tick (entire platform = 9 blocks for 3x3)
                while (blocksPlaced < 9 && currentX[0] <= halfSize) {
                    if (currentZ[0] <= halfSize) {
                        Location blockLocation = new Location(world, currentX[0], 64, currentZ[0]);
                        blockLocation.getBlock().setType(finalMaterial);
                        blocksPlaced++;
                        currentZ[0]++;
                    } else {
                        currentZ[0] = -halfSize;
                        currentX[0]++;
                    }
                }

                // If we're done with all blocks
                if (currentX[0] > halfSize) {
                    plugin.getLogger().info("Created " + platformSize + "x" + platformSize + " spawn platform in world: " + world.getName());
                    if (callback != null) {
                        callback.run();
                    }
                    // Cancel this task
                    if (taskId != -1) {
                        Bukkit.getScheduler().cancelTask(taskId);
                    }
                }
            }

            public void setTaskId(int id) {
                this.taskId = id;
            }
        }, 1L, 1L); // Run every tick
    }

    private boolean worldExists(String worldName) {
        File worldDir = new File(mapsDirectory, worldName);
        return worldDir.exists() && worldDir.isDirectory();
    }

    public World createMapWorld(SmashMap map) {
        String worldName = "maps/" + map.getWorldName();
        int mapId = map.getId();

        // Check if world already exists and is loaded
        World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            return existingWorld;
        }

        // If this world was pregenerated, just load it
        if (pregeneratedWorlds.contains(mapId) || worldExists(map.getWorldName())) {
            WorldCreator creator = new WorldCreator(worldName);
            creator.generator(new VoidWorldGenerator());
            creator.type(WorldType.NORMAL);
            creator.generateStructures(false);
            creator.environment(World.Environment.NORMAL);

            World world = creator.createWorld();
            if (world != null) {
                configureWorld(world);
                world.setKeepSpawnInMemory(true); // Keep active worlds in memory
                pregeneratedWorlds.remove(mapId); // Remove from pregenerated set since it's now active
                plugin.getLogger().info("Loaded pregenerated world: " + worldName);
                return world;
            }
        }

        // Fallback: Create world immediately if pregeneration failed
        return createWorldImmediately(map);
    }

    private World createWorldImmediately(SmashMap map) {
        String worldName = "maps/" + map.getWorldName();

        // Create world creator with void generator
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new VoidWorldGenerator());
        creator.type(WorldType.NORMAL);
        creator.generateStructures(false);
        creator.environment(World.Environment.NORMAL);

        // Create the world
        World world = creator.createWorld();

        if (world != null) {
            configureWorld(world);
            createSpawnPlatformSync(world);
            plugin.getLogger().info("Created map world immediately: " + worldName);
        } else {
            plugin.getLogger().severe("Failed to create map world: " + worldName);
        }

        return world;
    }

    private void createSpawnPlatformSync(World world) {
        // Get platform material and size from config
        Material platformMaterial;
        try {
            platformMaterial = Material.valueOf(plugin.getConfigManager().getSpawnPlatformMaterial());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid spawn platform material in config, using STONE");
            platformMaterial = Material.STONE;
        }

        int platformSize = plugin.getConfigManager().getSpawnPlatformSize();
        int halfSize = platformSize / 2;

        // Ensure the spawn chunk is loaded
        world.getChunkAt(0, 0).load(true);

        // Create 3x3x1 platform at Y=64
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Location blockLocation = new Location(world, x, 64, z);
                blockLocation.getBlock().setType(platformMaterial);
            }
        }

        plugin.getLogger().info("Created " + platformSize + "x" + platformSize + " spawn platform in world: " + world.getName());
    }

    private void configureWorld(World world) {
        // Configure world settings for building
        world.setSpawnFlags(false, false);
        world.setAutoSave(true);
        world.setDifficulty(Difficulty.PEACEFUL);

        // Set world border
        WorldBorder border = world.getWorldBorder();
        int borderSize = plugin.getConfigManager().getBorderSize() * 2; // Diameter
        border.setSize(borderSize);
        border.setCenter(0.5, 0.5);

        // Configure game rules for optimal building experience
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

        // Set optimal conditions
        world.setTime(6000); // Noon
        world.setStorm(false);
        world.setThundering(false);

        // Set spawn location on the platform
        world.setSpawnLocation(0, 65, 0);
    }

    public World getMapWorld(SmashMap map) {
        String worldName = "maps/" + map.getWorldName();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            // World not loaded, try to create/load it
            world = createMapWorld(map);
        }

        return world;
    }

    public boolean isMapWorld(World world) {
        if (world == null) return false;
        return world.getName().startsWith("maps/");
    }

    public void teleportToMap(Player player, SmashMap map) {
        World world = getMapWorld(map);

        if (world == null) {
            String errorMessage = plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("world_not_found");
            player.sendMessage(errorMessage);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            return;
        }

        // Clear inventory except plugin items
        plugin.getItemManager().clearInventoryExceptPluginItems(player);

        // Teleport to the center of the spawn platform with safe coordinates
        Location spawnLocation = new Location(world, 0.5, 65.0, 0.5, 0.0f, 0.0f);

        // Ensure the spawn area is loaded
        world.getChunkAt(0, 0).load(true);

        player.teleport(spawnLocation);

        // Set creative mode
        player.setGameMode(GameMode.CREATIVE);

        // Allow flight
        player.setAllowFlight(true);
        player.setFlying(true);

        // Give navigation items with delay
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getNavigationItemManager().giveNavigationItem(player);
        }, 10L);

        player.playSound(player.getLocation(), plugin.getConfigManager().getSound("teleport"), 1.0f, 1.0f);
    }

    public void unloadMapWorld(String worldName) {
        String fullWorldName = "maps/" + worldName;
        World world = Bukkit.getWorld(fullWorldName);

        if (world != null) {
            // Remove all players from the world first
            for (Player player : world.getPlayers()) {
                // Teleport to main world spawn
                Location mainWorldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
                player.teleport(mainWorldSpawn);
            }

            // Unload the world and save it
            Bukkit.unloadWorld(world, true);
            plugin.getLogger().info("Unloaded map world: " + fullWorldName);
        }
    }

    public File getMapWorldDirectory(SmashMap map) {
        return new File(mapsDirectory, map.getWorldName());
    }

    public boolean deleteMapWorld(SmashMap map) {
        // Remove from pregenerated sets
        pregeneratedWorlds.remove(map.getId());
        pregeneratingWorlds.remove(map.getId());

        // First unload the world
        unloadMapWorld(map.getWorldName());

        // Wait a bit for the world to fully unload
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Delete the world directory
            File worldDir = getMapWorldDirectory(map);
            if (worldDir.exists()) {
                boolean deleted = deleteDirectory(worldDir);
                if (deleted) {
                    plugin.getLogger().info("Deleted world directory: " + worldDir.getName());
                } else {
                    plugin.getLogger().warning("Failed to delete world directory: " + worldDir.getName());
                }
            }
        }, 20L); // 1 second delay

        return true;
    }

    private boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                }
            }
        }
        return directory.delete();
    }

    public File getMapsDirectory() {
        return mapsDirectory;
    }
}