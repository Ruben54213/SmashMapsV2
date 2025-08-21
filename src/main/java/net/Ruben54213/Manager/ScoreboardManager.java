package net.Ruben54213.Manager;

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
        String emptyLine4 = "§8 ";

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
        // Leere Zeile zwischen Titel und Erstellt
        objective.getScore(emptyLine1).setScore(11);
        // Erstellt ganz oben
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