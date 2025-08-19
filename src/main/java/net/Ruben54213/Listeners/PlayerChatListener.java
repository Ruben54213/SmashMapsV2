package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.Utility.ChatInputManager;
import net.Ruben54213.Manager.IconSelectionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    private final SmashMapsV2 plugin;

    public PlayerChatListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (ChatInputManager.isPlayerWaitingForInput(player.getUniqueId())) {
            event.setCancelled(true);

            String mapName = event.getMessage().trim();

            // Validate map name
            if (!isValidMapName(mapName)) {
                String message = plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("invalid_map_name").replace("%name%", mapName);
                player.sendMessage(message);
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);

                // Ask again
                String requestMessage = plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("map_name_request");
                player.sendMessage(requestMessage);
                return;
            }

            // Check if ANY player already has a map with this name (global check)
            if (plugin.getMapManager().hasMapWithName(mapName)) {
                String message = plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("map_name_already_exists").replace("%name%", mapName);
                player.sendMessage(message);
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);

                // Ask again
                String requestMessage = plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("map_name_request");
                player.sendMessage(requestMessage);
                return;
            }

            // Remove player from chat waiting list
            ChatInputManager.removePlayerWaitingForInput(player.getUniqueId());

            // Execute map creation and icon selection synchronously on the main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // Create the map
                SmashMap map = plugin.getMapManager().createMap(player, mapName);

                // Send success message
                String message = plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("map_created").replace("%name%", mapName);
                player.sendMessage(message);

                // Start icon selection process
                String iconMessage = plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("map_icon_request");
                player.sendMessage(iconMessage);

                // Show title for icon selection
                String iconTitle = plugin.getConfigManager().getMessage("icon_selection_title");
                String iconSubtitle = plugin.getConfigManager().getMessage("icon_selection_subtitle");
                player.sendTitle(iconTitle, iconSubtitle, 10, 100, 20);

                // Set player to creative and add to icon waiting list
                player.setGameMode(org.bukkit.GameMode.CREATIVE);
                IconSelectionManager.addPlayerWaitingForIcon(player.getUniqueId(), map);
            });
        }
    }

    private boolean isValidMapName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (name.length() > 32) {
            return false;
        }

        // Only allow letters, numbers, spaces, and some special characters
        return name.matches("[a-zA-Z0-9äöüÄÖÜß\\s\\-_]+");
    }
}