package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public DeleteCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Dieser Command kann nur von Spielern verwendet werden!");
            return true;
        }

        Player player = (Player) sender;

        boolean isAdmin = player.hasPermission("smashmaps.delete.admin");

        if (args.length == 0) {
            // Prüfen ob Spieler auf einer Map ist
            World currentWorld = player.getWorld();
            if (!plugin.getWorldManager().isMapWorld(currentWorld)) {
                if (isAdmin) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Du &cmusst&7 auf einer &eMap &7 sein oder eine &eID&7/&eName&7 angeben!"));
                    player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&',"&7Verwendung: &e/delete &7<&eID oder Name&7>"));
                } else {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&',"&7Du &cmusst &7auf einer &eMap &7sein um sie &clöschen&7 zu können!"));
                }
                return true;
            }

            // Map anhand der Welt finden
            String worldName = currentWorld.getName().replace("maps/", "");
            SmashMap map = plugin.getMapManager().getMapByWorld(worldName);

            if (map == null) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Die &eMap &7konnte &cnicht&7 gefunden werden!"));
                return true;
            }

            // Prüfen ob Spieler der Besitzer ist
            if (!map.getOwnerUUID().equals(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&',"&7Du kannst nur deine &eeigenen&7 Maps löschen!"));
                return true;
            }

            // Approved maps dürfen nicht gelöscht werden
            if (map.isApproved()) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Diese Map ist &aapproved&7 und kann &cnicht &7gelöscht werden!"));
                return true;
            }

            // Map löschen
            deleteMapWithConfirmation(player, map);

        } else if (args.length == 1) {
            // Löschen nur auf der eigenen Map ohne Argumente erlaubt
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Verwendung: &e/delete&7 auf deiner &eMap&7 (ohne Argumente)."));
            return true;

        } else {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&',"&7Verwendung: &e/delete&7 auf deiner &eMap&7."));
        }

        return true;
    }

    private void deleteMapWithConfirmation(Player player, SmashMap map) {
        // Sicherheitscheck: Approved Maps dürfen nicht gelöscht werden
        if (map.isApproved()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Diese Map ist &aapproved&7 und kann &cnicht &7gelöscht werden!"));
            return;
        }
        // Bestätigungsnachricht senden
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + "═══════════════════════════════════");
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "⚠ " + ChatColor.BOLD + "WARNUNG" + ChatColor.RESET + ChatColor.RED + " ⚠");
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GRAY + "§7Du bist dabei, die §eMap§7 zu §clöschen§7:");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.getConfigManager().getPrefix() + ChatColor.WHITE + "Name: " + ChatColor.GOLD + map.getName()));
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.WHITE + "ID: " + ChatColor.GOLD + map.getId());
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "§7Diese §eAktion §7kann §cNICHT §7rückgängig gemacht werden!");
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + "═══════════════════════════════════");
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GREEN + "Gib §a/confirm §7ein, um zu§a bestätigen§7.");
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "§7Warte §e30 Sekunden§7, um §cabzubrechen§7.");

        // Warning Sound
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 0.8f);

        // Confirmation timeout nach 30 Sekunden
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getMapManager().hasPendingDeletion(player.getUniqueId())) {
                plugin.getMapManager().removePendingDeletion(player.getUniqueId());
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GRAY + "§7 Der §cLöschvorgang §7 wurde nun §cabgebrochen §7(§eZeitüberschreitung§7).");
            }
        }, 600L); // 30 Sekunden = 600 Ticks

        // Map für Bestätigung speichern
        plugin.getMapManager().addPendingDeletion(player.getUniqueId(), map);
    }

    public void performMapDeletion(Player player, SmashMap map) {
        // Sicherheitscheck: Approved Maps dürfen nicht gelöscht werden (Failsafe bei /confirm)
        if (map.isApproved()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Approved &eMaps &7können &cnicht &7gelöscht werden!"));
            // Eventuelles Pending aufräumen
            plugin.getMapManager().removePendingDeletion(player.getUniqueId());
            return;
        }
        // Spieler zum Spawn teleportieren
        player.performCommand("spawn");

        // Kurze Verzögerung, dann Map löschen
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + "§cLösche §eMap §7'§e" + map.getName() + "§7'..."));

            // Alle Spieler aus der Map-Welt teleportieren
            World mapWorld = Bukkit.getWorld(map.getWorldName());
            if (mapWorld != null) {
                for (Player worldPlayer : mapWorld.getPlayers()) {
                    if (!worldPlayer.equals(player)) {
                        worldPlayer.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "§7Die Map '§e" + map.getName() + "§7' wird §cgelöscht§7. §7Du wirst nun zum §eSpawn §7teleportiert.");
                        worldPlayer.performCommand("spawn");
                    }
                }
            }

            // Weitere Verzögerung für Teleportation
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Map aus dem System löschen
                boolean success = plugin.getMapManager().deleteMap(map.getId());

                if (success) {
                    // Erfolgreiche Löschung
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() + ChatColor.GREEN + "✓§7 Map '§e" + map.getName() + "§7' wurde §aerfolgreich §7gelöscht!"));

                    // Success Sound
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);

                    // Success Title
                    player.sendTitle(
                            ChatColor.GREEN + "" + ChatColor.BOLD + "Map gelöscht",
                            ChatColor.GRAY + "Die Map wurde erfolgreich entfernt",
                            10, 60, 20
                    );

                    // Asynchron MinIO updaten (simuliert /save Funktionalität)
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            // Maps.yml in MinIO aktualisieren
                            plugin.getMinIOManager().uploadMapsYmlToMinIO();
                            plugin.getLogger().info("maps.yml successfully updated in MinIO after map deletion");
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to update maps.yml in MinIO after map deletion: " + e.getMessage());
                        }
                    });

                    // Scoreboards für alle Spieler aktualisieren
                    plugin.getScoreboardManager().updateAllScoreboards();

                } else {
                    // Fehler beim Löschen
                    player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "✗ Fehler beim Löschen der Map '" + map.getName() + "'!");

                    // Error Sound
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5f, 1.0f);

                    // Error Title
                    player.sendTitle(
                            ChatColor.RED + "" + ChatColor.BOLD + "Fehler",
                            ChatColor.GRAY + "Die Map konnte nicht gelöscht werden",
                            10, 60, 20
                    );
                }

                // Pending deletion entfernen
                plugin.getMapManager().removePendingDeletion(player.getUniqueId());

            }, 40L); // 2 Sekunden Verzögerung für Teleportation
        }, 20L); // 1 Sekunde Verzögerung
    }

    // Methode für Bestätigung (wird von ChatListener aufgerufen)
    public void handleConfirmation(Player player, String message) {
        if (!message.equalsIgnoreCase("/delete confirm")) {
            return;
        }

        SmashMap pendingMap = plugin.getMapManager().getPendingDeletion(player.getUniqueId());
        if (pendingMap == null) {
            return;
        }

        performMapDeletion(player, pendingMap);
    }
}