package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnapproveCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public UnapproveCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Permission check
        if (!player.hasPermission("smashmaps.unapprove")) {
            String message = plugin.getConfigManager().getPrefix() +
                    "§cDu hast keine Berechtigung für diesen Befehl!";
            player.sendMessage(message);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            return true;
        }

        SmashMap targetMap = null;

        // Case 1: No args provided -> only allowed when player is currently in a map world
        if (args.length == 0) {
            World world = player.getWorld();
            if (!plugin.getWorldManager().isMapWorld(world)) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cVerwendung: /unapprove <ID|Mapname>");
                return true;
            }
            String worldName = world.getName(); // e.g. "maps/123" where 123 is map.getWorldName()
            // Our maps use the logical worldName (map.getWorldName()) without the "maps/" prefix in MapManager
            String logical = worldName.startsWith("maps/") ? worldName.substring("maps/".length()) : worldName;
            targetMap = plugin.getMapManager().getMapByWorld(logical);
        } else {
            // Args provided: support numeric ID or full map name (including spaces)
            String joined = String.join(" ", args).trim();
            if (joined.matches("^\\d+$")) {
                int id = Integer.parseInt(joined);
                targetMap = plugin.getMapManager().getMapById(id);
            } else {
                // Try exact name match first (case-insensitive)
                // If that fails, try normalized comparison like ApproveCommand
                SmashMap byName = plugin.getMapManager().getMapByName(joined);
                if (byName != null) {
                    targetMap = byName;
                } else {
                    String normalizedInput = normalizeName(joined);
                    targetMap = plugin.getMapManager().getAllMaps().stream()
                            .filter(m -> normalizeName(m.getName()).equalsIgnoreCase(normalizedInput))
                            .findFirst().orElse(null);
                }
            }
        }

        if (targetMap == null) {
            String inputInfo = args.length == 0 ? "deiner aktuellen Welt" : ("'§e" + String.join(" ", args) + "§7'");
            String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                    "§cEs wurde keine Map zu " + inputInfo + " §cgefunden!");
            player.sendMessage(message);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            return true;
        }

        // Only allow if already approved
        if (!targetMap.isApproved()) {
            String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                    "§7Die Map '§e" + targetMap.getName() + "§7' ist §cnoch nicht approved§7!");
            player.sendMessage(message);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            return true;
        }

        // Unapprove the map and persist to maps.yml
        targetMap.setApproved(false);
        plugin.getMapManager().updateMap(targetMap);

        String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                "§aMap '§e" + targetMap.getName() + "§a' wurde erfolgreich §cunapproved§a!");
        player.sendMessage(message);
        player.playSound(player.getLocation(), plugin.getConfigManager().getSound("map_created"), 1.0f, 1.0f);

        return true;
    }

    private String normalizeName(String input) {
        if (input == null) return "";
        String withSection = ChatColor.translateAlternateColorCodes('&', input);
        String stripped = ChatColor.stripColor(withSection);
        return stripped == null ? "" : stripped.trim().toLowerCase();
    }
}
