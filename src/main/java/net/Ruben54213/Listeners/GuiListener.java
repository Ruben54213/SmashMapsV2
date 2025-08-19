package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.GUIs.MapCreationGui;
import net.Ruben54213.GUIs.MapOverviewGui;
import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.Utility.ChatInputManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GuiListener implements Listener {

    private final SmashMapsV2 plugin;

    public GuiListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();

        // Prevent taking items from any plugin GUI
        if (isPluginGui(inventoryTitle)) {
            event.setCancelled(true);
        }

        // Check if it's the map creation GUI
        if (isMapCreationGui(inventoryTitle)) {
            handleMapCreationGui(event, player);
        }
        // Check if it's the map overview GUI
        else if (isMapOverviewGui(inventoryTitle)) {
            handleMapOverviewGui(event, player);
        }
    }

    private boolean isPluginGui(String title) {
        // Check against all possible GUI titles from config
        String mapCreationTitle = plugin.getConfigManager().getGuiTitle();
        String mapOverviewTitle = plugin.getConfigManager().getMapOverviewTitle()
                .replace("%player%", ""); // Remove player placeholder for general check

        return title.equals(mapCreationTitle) ||
                title.contains(mapOverviewTitle.trim()) ||
                title.contains("Karten von"); // Fallback for hardcoded German title
    }

    private boolean isMapCreationGui(String title) {
        return title.equals(plugin.getConfigManager().getGuiTitle());
    }

    private boolean isMapOverviewGui(String title) {
        String configTitle = plugin.getConfigManager().getMapOverviewTitle()
                .replace("%player%", ""); // Remove placeholder
        return title.contains(configTitle.trim()) || title.contains("Karten von");
    }

    private void handleMapCreationGui(InventoryClickEvent event, Player player) {
        // Check if clicked item is the map creation item
        if (event.getCurrentItem() != null &&
                plugin.getItemManager().isMapCreationItem(event.getCurrentItem())) {

            // Check if player can create more maps
            if (!plugin.getMapManager().canCreateMap(player)) {
                int limit = plugin.getMapManager().getPlayerMapLimit(player);
                String message = plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("map_limit_reached").replace("%limit%", String.valueOf(limit));
                player.sendMessage(message);
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
                player.closeInventory();
                return;
            }

            // Close inventory and request map name
            player.closeInventory();

            String message = plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("map_name_request");
            player.sendMessage(message);

            // Show title for map name input
            String nameTitle = plugin.getConfigManager().getMessage("map_name_title");
            String nameSubtitle = plugin.getConfigManager().getMessage("map_name_subtitle");
            player.sendTitle(nameTitle, nameSubtitle, 10, 100, 20);

            // Register player for chat input
            ChatInputManager.addPlayerWaitingForInput(player.getUniqueId());
        }
    }

    private void handleMapOverviewGui(InventoryClickEvent event, Player player) {
        // Check if clicked item has a map ID
        if (event.getCurrentItem() != null) {
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(
                    new org.bukkit.NamespacedKey(plugin, "map_id"),
                    PersistentDataType.INTEGER)) {

                int mapId = meta.getPersistentDataContainer().get(
                        new org.bukkit.NamespacedKey(plugin, "map_id"),
                        PersistentDataType.INTEGER);

                // Find the map and teleport player
                SmashMap map = plugin.getMapManager().getMapById(mapId);
                if (map != null && map.getOwnerUUID().equals(player.getUniqueId())) {
                    player.closeInventory();

                    // Teleport player to map
                    plugin.getWorldManager().teleportToMap(player, map);

                    String teleportMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_teleported").replace("%name%", map.getName());
                    player.sendMessage(teleportMessage);
                } else if (map == null) {
                    // Map not found error
                    String errorMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_not_found");
                    player.sendMessage(errorMessage);
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
                } else {
                    // Not owner error
                    String errorMessage = plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_not_owner");
                    player.sendMessage(errorMessage);
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
                }
            }
        }
    }
}