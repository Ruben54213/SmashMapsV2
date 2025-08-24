package net.Ruben54213.Manager;

import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final SmashMapsV2 plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;

    public ScoreboardManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
    }

    /**
     * Create and show scoreboard for a player
     */
    public void showScoreboard(Player player) {
        // Check if player is in a map by world name
        String worldName = player.getWorld().getName();

        // Debug: Log the actual world name
        plugin.getLogger().info("DEBUG: Player " + player.getName() + " is in world: '" + worldName + "'");

        // If world name is "world", player is in lobby - show standard scoreboard
        if ("world".equals(worldName)) {
            plugin.getLogger().info("DEBUG: Showing standard scoreboard (lobby world)");
            showStandardScoreboard(player);
            return;
        }

        // Check if world name starts with "maps/" and extract ID
        String mapIdString = worldName;
        if (worldName.startsWith("maps/")) {
            mapIdString = worldName.substring(5); // Remove "maps/" prefix
            plugin.getLogger().info("DEBUG: Extracted map ID: '" + mapIdString + "' from world: '" + worldName + "'");
        }

        // Try to parse world name as map ID and get map data
        try {
            int mapId = Integer.parseInt(mapIdString);
            SmashMap currentMap = plugin.getMapManager().getMapById(mapId);

            plugin.getLogger().info("DEBUG: Looking for map with ID: " + mapId + ", found: " + (currentMap != null ? currentMap.getName() : "null"));

            if (currentMap != null) {
                plugin.getLogger().info("DEBUG: Showing map scoreboard for map: " + currentMap.getName());
                showMapScoreboard(player, currentMap);
            } else {
                plugin.getLogger().info("DEBUG: Map not found, showing standard scoreboard");
                showStandardScoreboard(player);
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().info("DEBUG: Could not parse '" + mapIdString + "' as number, showing standard scoreboard");
            showStandardScoreboard(player);
        }
    }

    /**
     * Create and show the standard scoreboard (when not in a map)
     */
    private void showStandardScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // Create objective with title
        Objective objective = scoreboard.registerNewObjective(
                "mapstats",
                "dummy",
                "§3§lMaps"
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Get statistics
        int playerMapsCount = plugin.getMapManager().getPlayerMapCount(player.getUniqueId());
        int playerMapLimit = plugin.getMapManager().getPlayerMapLimit(player);
        int totalMaps = plugin.getMapManager().getAllMaps().size();
        int approvedMaps = plugin.getMapManager().getApprovedMaps().size();

        // Create empty lines with different colors to avoid conflicts
        String emptyLine1 = "§r ";
        String emptyLine2 = "§f ";
        String emptyLine3 = "§0 ";

        // Create teams for colored text
        Team createdTitle = scoreboard.registerNewTeam("createdTitle");
        createdTitle.setPrefix("§e§lErstellt");
        createdTitle.addEntry("§a");

        Team createdValue = scoreboard.registerNewTeam("createdValue");
        String limitText = (playerMapLimit == Integer.MAX_VALUE) ? "∞" : String.valueOf(playerMapLimit);
        createdValue.setPrefix("§7» §e" + playerMapsCount + "§7/§6" + limitText);
        createdValue.addEntry("§e");

        Team totalTitle = scoreboard.registerNewTeam("totalTitle");
        totalTitle.setPrefix("§b§lInsgesamt");
        totalTitle.addEntry("§b");

        Team totalValue = scoreboard.registerNewTeam("totalValue");
        totalValue.setPrefix("§7» §f" + String.valueOf(totalMaps));
        totalValue.addEntry("§d");

        Team approvedTitle = scoreboard.registerNewTeam("approvedTitle");
        approvedTitle.setPrefix("§a§lGenehmigt");
        approvedTitle.addEntry("§2");

        Team approvedValue = scoreboard.registerNewTeam("approvedValue");
        approvedValue.setPrefix("§7» §f" + String.valueOf(approvedMaps));
        approvedValue.addEntry("§c");

        // Set scores (higher numbers appear higher on scoreboard)
        objective.getScore(emptyLine1).setScore(11);
        objective.getScore("§a").setScore(10);     // Erstellt title
        objective.getScore("§e").setScore(9);      // Erstellt value
        objective.getScore(emptyLine2).setScore(8);
        objective.getScore("§b").setScore(7);      // Insgesamt title
        objective.getScore("§d").setScore(6);      // Insgesamt value
        objective.getScore(emptyLine3).setScore(5);
        objective.getScore("§2").setScore(4);      // Genehmigt title
        objective.getScore("§c").setScore(3);      // Genehmigt value

        // Store and set scoreboard
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
    }

    /**
     * Create and show the map-specific scoreboard (when in a map)
     */
    private void showMapScoreboard(Player player, SmashMap map) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // Convert color codes and create title with (Smash)
        String mapName = ChatColor.translateAlternateColorCodes('&', map.getName());

        // Create objective with map name and (Smash) as title
        Objective objective = scoreboard.registerNewObjective(
                "mapinfo",
                "dummy",
                "§6§l" + mapName + " §7(Smash)"
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Create empty lines with different colors to avoid conflicts
        String emptyLine1 = "§r ";
        String emptyLine2 = "§f ";
        String emptyLine3 = "§0 ";

        // Create teams for colored text
        Team creatorTitle = scoreboard.registerNewTeam("creatorTitle");
        creatorTitle.setPrefix("§e§lErsteller:");
        creatorTitle.addEntry("§a");

        Team creatorValue = scoreboard.registerNewTeam("creatorValue");
        String creatorName = Bukkit.getOfflinePlayer(map.getOwnerUUID()).getName();
        creatorValue.setPrefix("§7» §f" + (creatorName != null ? creatorName : "Unbekannt"));
        creatorValue.addEntry("§e");

        Team statusTitle = scoreboard.registerNewTeam("statusTitle");
        statusTitle.setPrefix("§a§lStatus");
        statusTitle.addEntry("§b");

        // Spielbar? -> mind. 8 Items, 8 Spawns, 1 Center
        String wName = map.getWorldName();
        int items = plugin.getMapManager().getItemSpawns(wName).size();
        int spawns = plugin.getMapManager().getPlayerSpawns(wName).size();
        boolean hasCenter = plugin.getMapManager().getCenter(wName) != null;
        boolean playable = items >= 8 && spawns >= 8 && hasCenter;

        Team statusValue = scoreboard.registerNewTeam("statusValue");
        statusValue.setPrefix("§7» " + (playable ? "§aSpielbar" : "§cUnspielbar"));
        statusValue.addEntry("§d");

        Team approvedTitle = scoreboard.registerNewTeam("approvedTitle");
        approvedTitle.setPrefix("§a§lApproved");
        approvedTitle.addEntry("§2");

        Team approvedValue = scoreboard.registerNewTeam("approvedValue");
        String approvedText = map.isApproved() ? "§aJa" : "§cNein";
        approvedValue.setPrefix("§7» " + approvedText);
        approvedValue.addEntry("§c");

        // Set scores (higher numbers appear higher on scoreboard)
        objective.getScore(emptyLine1).setScore(10);
        objective.getScore("§a").setScore(9);      // Ersteller title
        objective.getScore("§e").setScore(8);      // Ersteller value
        objective.getScore(emptyLine2).setScore(7);
        objective.getScore("§b").setScore(6);      // Status title
        objective.getScore("§d").setScore(5);      // Status value
        objective.getScore(emptyLine3).setScore(4);
        objective.getScore("§2").setScore(3);      // Approved title
        objective.getScore("§c").setScore(2);      // Approved value

        // Store and set scoreboard
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
    }

    /**
     * Update scoreboard for a player
     */
    public void updateScoreboard(Player player) {
        if (hasScoreboard(player)) {
            showScoreboard(player); // Recreate with updated values
        }
    }

    /**
     * Remove scoreboard from a player
     */
    public void removeScoreboard(Player player) {
        if (hasScoreboard(player)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            playerScoreboards.remove(player.getUniqueId());
        }
    }

    /**
     * Check if player has a scoreboard
     */
    public boolean hasScoreboard(Player player) {
        return playerScoreboards.containsKey(player.getUniqueId());
    }

    /**
     * Toggle scoreboard for a player
     */
    public void toggleScoreboard(Player player) {
        if (hasScoreboard(player)) {
            removeScoreboard(player);
        } else {
            showScoreboard(player);
        }
    }

    /**
     * Update all active scoreboards
     */
    public void updateAllScoreboards() {
        for (UUID playerUUID : playerScoreboards.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                updateScoreboard(player);
            } else {
                playerScoreboards.remove(playerUUID);
            }
        }
    }

    /**
     * Clean up scoreboards on disable
     */
    public void cleanup() {
        for (UUID playerUUID : playerScoreboards.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        playerScoreboards.clear();
    }
}