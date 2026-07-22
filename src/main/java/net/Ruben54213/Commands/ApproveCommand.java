package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
// ... existing code ...
import net.Ruben54213.Models.SmashMap;
import org.bukkit.ChatColor;

public class ApproveCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public ApproveCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("smashmaps.approve")) {
            String message = plugin.getConfigManager().getPrefix() +
                    "§cDu hast keine Berechtigung für diesen Befehl!";
            player.sendMessage(message);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cVerwendung: /approve <ID|Mapname>");
            return true;
        }

        // Unterstützt Namen mit Leerzeichen und numerische IDs
        String joined = String.join(" ", args).trim();

        // Wenn nur Ziffern -> als ID behandeln
        if (joined.matches("^\\d+$")) {
            int id = Integer.parseInt(joined);
            SmashMap map = plugin.getMapManager().getMapById(id);
            if (map != null) {
                boolean wasApproved = map.isApproved();
                if (!wasApproved) {
                    map.setApproved(true);
                    plugin.getMapManager().updateMap(map);
                }
                String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                        "§aMap mit ID §e" + id + " §a('§e" + map.getName() + "§a') wurde erfolgreich approved!");
                player.sendMessage(message);
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("map_created"), 1.0f, 1.0f);
                if (!wasApproved) {
                    onMapApproved(map, player);
                }
            } else {
                String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                        "§cEs wurde keine Map mit der ID '§e" + id + "§c' gefunden!");
                player.sendMessage(message);
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            }
            return true;
        }

        // Andernfalls: vollständigen Namen verwenden (inkl. Leerzeichen),
        // aber Farb-/Formatcodes beim Vergleich ignorieren.
        String userInput = joined;
        String normalizedInput = normalizeName(userInput);

        SmashMap matched = plugin.getMapManager().getAllMaps().stream()
                .filter(m -> normalizeName(m.getName()).equalsIgnoreCase(normalizedInput))
                .findFirst()
                .orElse(null);

        if (matched != null) {
            boolean wasApproved = matched.isApproved();
            if (!wasApproved) {
                matched.setApproved(true);
                plugin.getMapManager().updateMap(matched);
            }
            String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                    "§aMap '§e" + matched.getName() + "§a' wurde erfolgreich approved!");
            player.sendMessage(message);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("map_created"), 1.0f, 1.0f);
            if (!wasApproved) {
                onMapApproved(matched, player);
            }
        } else {
            // Fallback: vorhandene Logik versuchen (falls MapManager intern andere Regeln hat)
            if (plugin.getMapManager().approveMap(userInput)) {
                String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                        "§aMap '§e" + userInput + "§a' wurde erfolgreich approved!");
                player.sendMessage(message);
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("map_created"), 1.0f, 1.0f);
                SmashMap approvedMap = plugin.getMapManager().getMapByName(userInput);
                if (approvedMap != null) {
                    onMapApproved(approvedMap, player);
                }
            } else {
                String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                        "§cMap '§e" + userInput + "§c' wurde nicht gefunden!");
                player.sendMessage(message);
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            }
        }

        return true;
    }

    // Wird ausgeführt, nachdem eine Map frisch approved wurde:
    // löst automatisch einen Save aus und bringt Spieler, die sich gerade in der
    // Map-Welt befinden (z.B. noch im Bearbeitungsmodus), auf einen sauberen Item-Stand,
    // damit u.a. "Positionen Anzeigen" wieder funktioniert.
    private void onMapApproved(SmashMap map, Player approver) {
        // Spieler, die aktuell in der Map-Welt stehen, aus dem Bearbeitungsmodus holen
        // und ihre Items neu vergeben (sonst fehlt z.B. das "Positionen Anzeigen"-Item).
        org.bukkit.World world = plugin.getServer().getWorld("maps/" + map.getWorldName());
        if (world != null) {
            for (Player online : world.getPlayers()) {
                plugin.getItemManager().restoreMapWorldItems(online);
            }
        }

        // Automatischen Save auslösen
        String savingMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                plugin.getConfigManager().getMessage("saving_map").replace("%name%", map.getName()));
        approver.sendMessage(savingMessage);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = plugin.getMinIOManager().saveMapToMinIO(map);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    String successMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_saved_success").replace("%name%", map.getName()));
                    approver.sendMessage(successMessage);
                } else {
                    String errorMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_save_error"));
                    approver.sendMessage(errorMessage);
                }
            });
        });
    }

    // Entfernt Farb-/Formatcodes (§ oder &-basierend) und normalisiert den Namen
    private String normalizeName(String input) {
        if (input == null) return "";
        String withSection = ChatColor.translateAlternateColorCodes('&', input);
        String stripped = ChatColor.stripColor(withSection);
        return stripped == null ? "" : stripped.trim().toLowerCase();
    }
}