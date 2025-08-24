package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.Manager.IconSelectionManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class IconDropListener implements Listener {

    private final SmashMapsV2 plugin;

    public IconDropListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Check if player is waiting for icon selection
        if (IconSelectionManager.isPlayerWaitingForIcon(player.getUniqueId())) {
            // DON'T cancel the event - let the item be dropped from inventory
            // But remove the dropped item entity from the world immediately

            ItemStack droppedItem = event.getItemDrop().getItemStack().clone(); // Clone to get the exact item
            SmashMap map = IconSelectionManager.getPlayerWaitingMap(player.getUniqueId());

            // Remove the dropped item entity from the world
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                event.getItemDrop().remove();
            });

            if (map != null) {
                // Set the icon for the map
                map.setIconMaterial(droppedItem.getType());

                // Get display name if available
                ItemMeta meta = droppedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    map.setIconDisplayName(meta.getDisplayName());
                }

                // Update map in storage
                plugin.getMapManager().updateMap(map);

                // Remove player from waiting list
                IconSelectionManager.removePlayerWaitingForIcon(player.getUniqueId());

                // Set player back to survival mode
                player.setGameMode(GameMode.SURVIVAL);

                // Send confirmation message
                String itemName = meta != null && meta.hasDisplayName() ?
                        meta.getDisplayName() : droppedItem.getType().toString();

                String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("map_icon_selected")
                                .replace("%item%", itemName)
                                .replace("%name%", map.getName()));
                player.sendMessage(message);

                // Play success sound
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("map_created"), 1.0f, 1.0f);

                // Teleport player to the new map
                plugin.getWorldManager().teleportToMap(player, map);

                // Send teleported message
                String teleportMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMessage("map_teleported").replace("%name%", map.getName()));
                player.sendMessage(teleportMessage);
            }
        }
    }
}