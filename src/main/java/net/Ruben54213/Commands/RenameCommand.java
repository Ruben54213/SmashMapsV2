package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RenameCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public RenameCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Dieser Command kann nur von Spielern verwendet werden!");
            return true;
        }

        Player player = (Player) sender;

        // Überprüfung, ob ein neuer Name angegeben wurde
        if (args.length == 0) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "§7Verwendung: §e/rename §7<§eneuer Name§7>");
            return true;
        }

        // Prüfen ob Spieler auf einer Map ist
        World currentWorld = player.getWorld();
        if (!plugin.getWorldManager().isMapWorld(currentWorld)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "§7Du musst auf einer §eMap §7sein, um sie umzubenennen!");
            return true;
        }

        // Map anhand der Welt finden
        String worldName = currentWorld.getName().replace("maps/", "");
        SmashMap map = plugin.getMapManager().getMapByWorld(worldName);

        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "§7Die Map konnte§c nicht §7gefunden werden!");
            return true;
        }

        // Prüfen ob Spieler der Besitzer ist
        if (!map.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "§7Du kannst nur deine §eeigenen§7 Maps umbenennen!");
            return true;
        }

        // Neuen Namen aus den Argumenten zusammensetzen
        StringBuilder newNameBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                newNameBuilder.append(" ");
            }
            newNameBuilder.append(args[i]);
        }
        String newName = newNameBuilder.toString();

        // Validierung des neuen Namens
        if (newName.length() > 32) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "§7Der Name darf§c nicht §7länger als §e32 Zeichen§7 sein!");
            return true;
        }

        if (newName.trim().isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "§7Der Name darf §cnicht §7leer sein!");
            return true;
        }

        // Prüfen ob bereits eine Map mit diesem Namen existiert
        SmashMap existingMap = plugin.getMapManager().getMapByName(newName);
        if (existingMap != null && existingMap.getId() != map.getId()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Eine Map mit dem§e Namen§7 '§e" + newName + "§7' existiert §cbereits§7!");
            return true;
        }

        // Alten Namen für Referenz speichern
        String oldName = map.getName();

        // Map direkt umbenennen
        map.setName(newName);
        plugin.getMapManager().updateMap(map);
        boolean success = true;

        if (success) {
            // Erfolgreiche Umbenennung
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GREEN + "✓ Map erfolgreich umbenannt!");
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GRAY + "Alter Name: " + ChatColor.WHITE + oldName);
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GRAY + "Neuer Name: " + ChatColor.WHITE + newName);

            // Success Sound
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);

            // Success Title anzeigen
            player.sendTitle(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Erfolgreich!",
                    ChatColor.GRAY + "Der Neue Name wurde nun gespeichert.",
                    10, 60, 20
            );

            // Asynchron MinIO updaten (simuliert /save Funktionalität)
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    // Maps.yml in MinIO aktualisieren
                    plugin.getMinIOManager().uploadMapsYmlToMinIO();
                    plugin.getLogger().info("maps.yml successfully updated in MinIO after map rename");
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to update maps.yml in MinIO after map rename: " + e.getMessage());
                }
            });

            // Scoreboards für alle Spieler aktualisieren
            plugin.getScoreboardManager().updateAllScoreboards();

        } else {
            // Fehler beim Umbenennen
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "✗ Fehler beim Umbenennen der Map!");

            // Error Sound
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5f, 1.0f);

            // Error Title
            player.sendTitle(
                    ChatColor.RED + "" + ChatColor.BOLD + "Fehler",
                    ChatColor.GRAY + "Die Map konnte nicht umbenannt werden",
                    10, 60, 20
            );
        }

        return true;
    }
}