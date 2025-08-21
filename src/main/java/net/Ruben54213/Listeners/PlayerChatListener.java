package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.GUIs.AllMapsGui;
import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.Manager.IconSelectionManager;
import net.Ruben54213.Utility.ChatInputManager;
import org.bukkit.ChatColor;
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
        String message = event.getMessage().trim();

        // Check if player is waiting for map name input
        if (ChatInputManager.isPlayerWaitingForInput(player.getUniqueId())) {
            event.setCancelled(true);

            // Process the message on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                ChatInputManager.removePlayerWaitingForInput(player.getUniqueId());

                // Allow color codes in map names (keep the raw message with &)
                String mapName = message;

                // Validate map name
                if (!isValidMapName(mapName)) {
                    String errorMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("invalid_map_name")
                                    .replace("%name%", ChatColor.translateAlternateColorCodes('&', mapName));
                    player.sendMessage(errorMessage);
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);

                    // Ask again
                    String requestMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_name_request");
                    player.sendMessage(requestMessage);

                    String nameTitle = plugin.getConfigManager().getMessage("map_name_title");
                    String nameSubtitle = plugin.getConfigManager().getMessage("map_name_subtitle");
                    player.sendTitle(nameTitle, nameSubtitle, 10, 100, 20);

                    ChatInputManager.addPlayerWaitingForInput(player.getUniqueId());
                    return;
                }

                // Check if ANY player already has a map with this name (global check)
                // Strip color codes for name comparison
                String strippedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', mapName));
                if (plugin.getMapManager().hasMapWithName(strippedName)) {
                    String errorMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_name_already_exists")
                                    .replace("%name%", ChatColor.translateAlternateColorCodes('&', mapName));
                    player.sendMessage(errorMessage);
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);

                    // Ask again
                    String requestMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_name_request");
                    player.sendMessage(requestMessage);

                    String nameTitle = plugin.getConfigManager().getMessage("map_name_title");
                    String nameSubtitle = plugin.getConfigManager().getMessage("map_name_subtitle");
                    player.sendTitle(nameTitle, nameSubtitle, 10, 100, 20);

                    ChatInputManager.addPlayerWaitingForInput(player.getUniqueId());
                    return;
                }

                // Create the map with color codes preserved
                SmashMap newMap = plugin.getMapManager().createMap(player, mapName);
                if (newMap != null) {
                    // Add player to icon selection manager
                    IconSelectionManager.addPlayerWaitingForIcon(player.getUniqueId(), newMap);

                    String successMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_created")
                                    .replace("%name%", ChatColor.translateAlternateColorCodes('&', mapName))
                                    .replace("%id%", String.valueOf(newMap.getId()));
                    player.sendMessage(successMessage);

                    // Start icon selection process
                    String iconMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_icon_request");
                    player.sendMessage(iconMessage);

                    String iconTitle = plugin.getConfigManager().getMessage("icon_selection_title");
                    String iconSubtitle = plugin.getConfigManager().getMessage("icon_selection_subtitle");
                    player.sendTitle(iconTitle, iconSubtitle, 10, 100, 20);

                    // Set player to creative mode
                    player.setGameMode(org.bukkit.GameMode.CREATIVE);
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("success"), 1.0f, 1.0f);
                } else {
                    String errorMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_creation_failed");
                    player.sendMessage(errorMessage);
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
                }
            });
        }
        // Check if player is waiting for search input
        else if (ChatInputManager.isPlayerWaitingForSearchInput(player.getUniqueId())) {
            event.setCancelled(true);

            // Process the search on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                ChatInputManager.removePlayerWaitingForSearchInput(player.getUniqueId());

                String searchQuery = message.trim();

                if (searchQuery.isEmpty()) {
                    // Open all maps GUI without search
                    AllMapsGui gui = new AllMapsGui(plugin, player);
                    plugin.getGuiListener().registerAllMapsGui(player, gui);
                    gui.open();
                } else {
                    // Open all maps GUI with search query
                    AllMapsGui gui = new AllMapsGui(plugin, player, searchQuery);
                    plugin.getGuiListener().registerAllMapsGui(player, gui);
                    gui.open();

                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                            "§7Suche nach: §e" + searchQuery);
                }

                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("gui_open"), 1.0f, 1.0f);
            });
        }
    }

    private boolean isValidMapName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // Strip color codes for length and content validation
        String strippedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));

        if (strippedName.trim().isEmpty()) {
            return false;
        }

        if (strippedName.length() > 32) {
            return false;
        }

        // Allow letters, numbers, spaces, and some special characters
        // Also allow color codes (&) and formatting codes
        return name.matches("[a-zA-Z0-9äöüÄÖÜß\\s\\-_&]+");
    }
}