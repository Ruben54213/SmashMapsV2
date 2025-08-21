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

public class SaveCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public SaveCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + plugin.getConfigManager().getMessage("not_player"));
            return true;
        }

        Player player = (Player) sender;
        World currentWorld = player.getWorld();

        // Prüfen ob Spieler in einer Map-Welt ist
        if (!plugin.getWorldManager().isMapWorld(currentWorld)) {
            String message = plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("not_in_map_world");
            player.sendMessage(message);
            return true;
        }

        // Map anhand der Welt finden
        String worldName = currentWorld.getName().replace("maps/", "");
        SmashMap map = plugin.getMapManager().getMapByWorld(worldName);

        if (map == null) {
            String message = plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("map_not_found_save");
            player.sendMessage(message);
            return true;
        }

        // Prüfen ob Spieler der Besitzer der Map ist
        if (!map.getOwnerUUID().equals(player.getUniqueId())) {
            String message = plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("not_map_owner_save");
            player.sendMessage(message);
            return true;
        }

        // Speichern in MinIO starten (asynchron)
        String loadingMessage = plugin.getConfigManager().getPrefix() +
                plugin.getConfigManager().getMessage("saving_map").replace("%name%", map.getName());
        player.sendMessage(loadingMessage);

        // Asynchron ausführen um Server-Lag zu vermeiden
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = plugin.getMinIOManager().saveMapToMinIO(map);

            // Zurück zum Haupt-Thread für Player-Nachrichten
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    // Erfolgsnachricht
                    String successMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_saved_success").replace("%name%", map.getName());
                    player.sendMessage(successMessage);

                    // Cooler Sound für Erfolg
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);

                    // Erfolgs-Title
                    String successTitle = ChatColor.translateAlternateColorCodes('&', "&a&lErfolgreich");
                    String successSubtitle = ChatColor.translateAlternateColorCodes('&', "&7Die Map wurde gespeichert");
                    player.sendTitle(successTitle, successSubtitle, 10, 60, 20);

                } else {
                    // Fehlernachricht
                    String errorMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_save_error");
                    player.sendMessage(errorMessage);

                    // Error Sound
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5f, 1.0f);

                    // Fehler-Title
                    String errorTitle = ChatColor.translateAlternateColorCodes('&', "&c&lFehlgeschlagen");
                    String errorSubtitle = ChatColor.translateAlternateColorCodes('&', "&7Es ist ein Problem aufgetreten");
                    player.sendTitle(errorTitle, errorSubtitle, 10, 60, 20);
                }
            });
        });

        return true;
    }
}