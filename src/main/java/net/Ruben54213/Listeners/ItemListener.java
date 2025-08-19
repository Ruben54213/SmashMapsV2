package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.GUIs.MapCreationGui;
import net.Ruben54213.GUIs.MapOverviewGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    private final SmashMapsV2 plugin;

    public ItemListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if it's the inventory item (Diamond that opens GUI)
        if (plugin.getItemManager().isInventoryItem(item)) {
            event.setCancelled(true);

            if (event.getAction().toString().contains("RIGHT_CLICK") ||
                    event.getAction().toString().contains("LEFT_CLICK")) {
                // Play GUI open sound
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("gui_open"), 1.0f, 1.0f);

                // Open map creation GUI
                new MapCreationGui(plugin, player).open();
            }
        }

        // Check if it's the map overview item (Crafting Table that opens overview)
        if (plugin.getItemManager().isMapOverviewItem(item)) {
            event.setCancelled(true);

            if (event.getAction().toString().contains("RIGHT_CLICK") ||
                    event.getAction().toString().contains("LEFT_CLICK")) {
                // Play GUI open sound
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("gui_open"), 1.0f, 1.0f);

                // Open map overview GUI
                new MapOverviewGui(plugin, player).open();
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        // Prevent moving the inventory item (Diamond)
        if (plugin.getItemManager().isInventoryItem(item)) {
            event.setCancelled(true);
        }

        // Prevent moving the map overview item (Crafting Table)
        if (plugin.getItemManager().isMapOverviewItem(item)) {
            event.setCancelled(true);
        }

        // Also check clicked item
        ItemStack cursorItem = event.getCursor();
        if (plugin.getItemManager().isInventoryItem(cursorItem) ||
                plugin.getItemManager().isMapOverviewItem(cursorItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        ItemStack item = event.getOldCursor();
        if (plugin.getItemManager().isInventoryItem(item) ||
                plugin.getItemManager().isMapOverviewItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (plugin.getItemManager().isInventoryItem(item) ||
                plugin.getItemManager().isMapOverviewItem(item)) {
            event.setCancelled(true);
        }
    }
}