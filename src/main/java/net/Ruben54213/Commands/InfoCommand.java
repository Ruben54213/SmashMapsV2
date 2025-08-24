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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public InfoCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Dieser Command kann nur von Spielern verwendet werden!");
            return true;
        }

        Player player = (Player) sender;
        boolean isAdmin = player.hasPermission("smashmaps.info.admin");

        if (args.length == 0) {
            // Prüfen ob Spieler auf einer Map ist
            World currentWorld = player.getWorld();
            if (!plugin.getWorldManager().isMapWorld(currentWorld)) {
                if (isAdmin) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Du &cmusst &7auf einer &eMap &7sein oder eine &eID&7/&eName &7angeben!"));
                    player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Verwendung: &e/info &7<&eID oder Name&7>"));
                } else {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Du &cmusst &7auf einer &eMap &7sein, um Informationen zu erhalten!"));
                }
                return true;
            }

            // Map anhand der Welt finden
            String worldName = currentWorld.getName().replace("maps/", "");
            SmashMap map = plugin.getMapManager().getMapByWorld(worldName);

            if (map == null) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Die &eMap &7konnte &cnicht &7gefunden werden!"));
                return true;
            }

            // Map-Informationen anzeigen
            showMapInfo(player, map);

        } else if (args.length == 1) {
            // Nur Admin kann Maps mit ID oder Name abfragen
            if (!isAdmin) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Du hast &ckeine &7Berechtigung, Maps mit &eID&7/&eName &7abzufragen!"));
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Verwendung: &e/info &7auf einer &eMap&7."));
                return true;
            }

            String identifier = args[0];
            SmashMap map = null;

            // Versuche zuerst als ID zu parsen
            try {
                int mapId = Integer.parseInt(identifier);
                map = plugin.getMapManager().getMapById(mapId);
            } catch (NumberFormatException e) {
                // Wenn nicht als ID parsbar, als Name suchen
                map = plugin.getMapManager().getMapByName(identifier);
            }

            if (map == null) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Die &eMap &7mit ID/Name '&e" + identifier + "&7' wurde &cnicht &7gefunden!"));
                return true;
            }

            // Map-Informationen anzeigen
            showMapInfo(player, map);

        } else {
            if (isAdmin) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Verwendung: &e/info &7[&eID oder Name&7]"));
            } else {
                player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.translateAlternateColorCodes('&', "&7Verwendung: &e/info &7auf einer &eMap&7."));
            }
        }

        return true;
    }

    private void showMapInfo(Player player, SmashMap map) {
        // Ersteller-Name ermitteln
        String creatorName = Bukkit.getOfflinePlayer(map.getOwnerUUID()).getName();
        if (creatorName == null) {
            creatorName = "Unbekannt";
        }

        // Erstellungsdatum ermitteln (basierend auf World-Ordner)
        String creationDate = getMapCreationDate(map);

        // Approved Status
        String approvedStatus = map.isApproved() ? "§aJa" : "§cNein";

        // Info-Box anzeigen
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + "═══════════════════════════════════");
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.AQUA + "§a§lMap-Informationen");
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GRAY + "");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() + ChatColor.WHITE + "§7Name: " + ChatColor.GOLD + map.getName()));
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.WHITE + "§7ID: " + ChatColor.YELLOW + map.getId());
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.WHITE + "§7Ersteller: " + ChatColor.GREEN + creatorName);
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.WHITE + "§7Erstellungsdatum: " + ChatColor.LIGHT_PURPLE + creationDate);
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.WHITE + "§7Genehmigt: " + approvedStatus);
        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.WHITE + "§7Welt: " + ChatColor.GRAY + map.getWorldName());

        // Icon-Informationen (falls verfügbar)
        if (map.getIconDisplayName() != null && !map.getIconDisplayName().isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.WHITE + "§7Icon: &e" + map.getIconDisplayName());
        }

        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + "═══════════════════════════════════");

        // Success Sound
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.0f);
    }

    private String getMapCreationDate(SmashMap map) {
        try {
            // Versuche das Erstellungsdatum anhand des World-Ordners zu ermitteln
            File worldFolder = new File(Bukkit.getWorldContainer(), "maps/" + map.getWorldName());
            if (!worldFolder.exists()) {
                worldFolder = new File(Bukkit.getWorldContainer(), map.getWorldName());
            }

            if (worldFolder.exists()) {
                long lastModified = worldFolder.lastModified();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                return sdf.format(new Date(lastModified));
            } else {
                return "§cUnbekannt";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not determine creation date for map " + map.getName() + ": " + e.getMessage());
            return "§cFehler beim Ermitteln";
        }
    }
}