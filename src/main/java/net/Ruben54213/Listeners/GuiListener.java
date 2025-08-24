package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.GUIs.AllMapsGui;
import net.Ruben54213.GUIs.MapCreationGui;
import net.Ruben54213.GUIs.MapOverviewGui;
import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.Utility.ChatInputManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiListener implements Listener {

    private final SmashMapsV2 plugin;
    private final Map<UUID, AllMapsGui> allMapsGuis = new HashMap<>();

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
        // Check if it's the all maps GUI
        else if (isAllMapsGui(inventoryTitle)) {
            handleAllMapsGui(event, player);
        }
    }

    private boolean isPluginGui(String title) {
        String mapCreationTitle = plugin.getConfigManager().getGuiTitle();
        String mapOverviewTitle = plugin.getConfigManager().getMapOverviewTitle()
                .replace("%player%", "");
        String allMapsTitle = plugin.getConfigManager().getAllMapsTitle();

        return title.equals(mapCreationTitle) ||
                title.contains(mapOverviewTitle.trim()) ||
                title.contains("Karten von") ||
                title.contains(allMapsTitle) ||
                title.startsWith(allMapsTitle);
    }

    private boolean isMapCreationGui(String title) {
        return title.equals(plugin.getConfigManager().getGuiTitle());
    }

    private boolean isMapOverviewGui(String title) {
        String configTitle = plugin.getConfigManager().getMapOverviewTitle()
                .replace("%player%", "");
        return title.contains(configTitle.trim()) || title.contains("Karten von");
    }

    private boolean isAllMapsGui(String title) {
        String configTitle = plugin.getConfigManager().getAllMapsTitle();
        return title.contains(configTitle) || title.startsWith(configTitle);
    }

    private void handleMapCreationGui(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() != null &&
                plugin.getItemManager().isMapCreationItem(event.getCurrentItem())) {

            if (!plugin.getMapManager().canCreateMap(player)) {
                int limit = plugin.getMapManager().getPlayerMapLimit(player);
                String message = plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("map_limit_reached").replace("%limit%", String.valueOf(limit));
                player.sendMessage(message);
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
                player.closeInventory();
                return;
            }

            player.closeInventory();

            String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("map_name_request"));
            player.sendMessage(message);

            String nameTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getMessage("map_name_title"));
            String nameSubtitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getMessage("map_name_subtitle"));
            player.sendTitle(nameTitle, nameSubtitle, 10, 100, 20);

            ChatInputManager.addPlayerWaitingForInput(player.getUniqueId());
        }
    }

    private void handleMapOverviewGui(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() != null) {
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(
                    new org.bukkit.NamespacedKey(plugin, "map_id"),
                    PersistentDataType.INTEGER)) {

                int mapId = meta.getPersistentDataContainer().get(
                        new org.bukkit.NamespacedKey(plugin, "map_id"),
                        PersistentDataType.INTEGER);

                SmashMap map = plugin.getMapManager().getMapById(mapId);
                if (map != null && map.getOwnerUUID().equals(player.getUniqueId())) {
                    player.closeInventory();

                    plugin.getWorldManager().teleportToMap(player, map);

                    String teleportMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_teleported").replace("%name%", map.getName()));
                    player.sendMessage(teleportMessage);
                } else if (map == null) {
                    String errorMessage = ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_not_found")));
                    player.sendMessage(errorMessage);
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
                } else {
                    String errorMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                            plugin.getConfigManager().getMessage("map_not_owner"));
                    player.sendMessage(errorMessage);
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
                }
            }
        }
    }

    private void handleAllMapsGui(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) return;

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null) return;

        AllMapsGui gui = allMapsGuis.get(player.getUniqueId());
        if (gui == null) return;

        // Check for navigation items
        if (meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "all_maps_nav"),
                PersistentDataType.STRING)) {

            String navAction = meta.getPersistentDataContainer().get(
                    new org.bukkit.NamespacedKey(plugin, "all_maps_nav"),
                    PersistentDataType.STRING);

            switch (navAction) {
                case "prev_page":
                    gui.previousPage();
                    break;
                case "next_page":
                    gui.nextPage();
                    break;
                case "filter":
                    gui.cycleFilter();
                    break;
                case "sort":
                    gui.cycleSort();
                    break;
                case "search":
                    player.closeInventory();
                    allMapsGuis.remove(player.getUniqueId());

                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                            "§6Gebe den Namen der Map ein, nach der du suchen möchtest:");

                    // Add player to search waiting list
                    ChatInputManager.addPlayerWaitingForSearchInput(player.getUniqueId());
                    break;
            }
            return;
        }

        // Check for map items
        if (meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "map_id"),
                PersistentDataType.INTEGER)) {

            int mapId = meta.getPersistentDataContainer().get(
                    new org.bukkit.NamespacedKey(plugin, "map_id"),
                    PersistentDataType.INTEGER);

            SmashMap map = plugin.getMapManager().getMapById(mapId);
            if (map != null) {
                player.closeInventory();
                allMapsGuis.remove(player.getUniqueId());

                plugin.getWorldManager().teleportToMap(player, map);

                String teleportMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("map_teleported").replace("%name%", map.getName()));
                player.sendMessage(teleportMessage);
            } else {
                String errorMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("map_not_found"));
                player.sendMessage(errorMessage);
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            }
        }
    }

    // Method to register AllMapsGui for a player
    public void registerAllMapsGui(Player player, AllMapsGui gui) {
        allMapsGuis.put(player.getUniqueId(), gui);
    }

    // Clean up when player closes inventory
    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            String title = event.getView().getTitle();

            if (isAllMapsGui(title)) {
                // Clean up with a small delay in case they're reopening immediately
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (!player.getOpenInventory().getTitle().equals(title)) {
                        allMapsGuis.remove(player.getUniqueId());
                    }
                }, 1L);
            }
        }
    }
}