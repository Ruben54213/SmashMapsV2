package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class NavigationListener implements Listener {

    private final SmashMapsV2 plugin;

    public NavigationListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle navigation item click
     */
    private void handleExitMapClick(Player player) {
        // Clear inventory except plugin items when leaving map
        plugin.getItemManager().clearInventoryExceptPluginItems(player);

        // Execute /spawn command
        player.performCommand("spawn");

        // Send message
        String message = plugin.getConfigManager().getPrefix() +
                plugin.getConfigManager().getMessage("map_exited");
        player.sendMessage(message);

        // Play sound
        player.playSound(player.getLocation(), plugin.getConfigManager().getSound("teleport"), 1.0f, 1.0f);

        // Give standard lobby items after small delay
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getItemManager().giveInventoryItem(player);
        }, 10L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if it's a navigation item
        if (plugin.getNavigationItemManager().isExitMapItem(item) ||
                plugin.getNavigationItemManager().isLobbyItem(item)) {

            event.setCancelled(true);

            if (event.getAction().toString().contains("RIGHT_CLICK") ||
                    event.getAction().toString().contains("LEFT_CLICK")) {

                if (plugin.getNavigationItemManager().isExitMapItem(item)) {
                    handleExitMapClick(player);
                } else {
                    plugin.getNavigationItemManager().handleNavigationItemClick(player, item);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Prevent moving navigation items
        if (plugin.getNavigationItemManager().isExitMapItem(item) ||
                plugin.getNavigationItemManager().isLobbyItem(item) ||
                plugin.getNavigationItemManager().isExitMapItem(cursorItem) ||
                plugin.getNavigationItemManager().isLobbyItem(cursorItem)) {

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        // Prevent dropping navigation items
        if (plugin.getNavigationItemManager().isExitMapItem(item) ||
                plugin.getNavigationItemManager().isLobbyItem(item)) {

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Give navigation item with a small delay to ensure player is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getNavigationItemManager().giveNavigationItem(player);
        }, 10L); // 0.5 second delay to ensure player inventory is ready
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            Player player = event.getPlayer();

            // Give navigation item with delay when player successfully logs in
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    plugin.getNavigationItemManager().giveNavigationItem(player);
                }
            }, 20L); // 1 second delay
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // Clear inventory except plugin items when changing worlds
        plugin.getItemManager().clearInventoryExceptPluginItems(player);

        // Update navigation item when player changes world
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getNavigationItemManager().giveNavigationItem(player);

            // If not in map world, give lobby items
            if (!plugin.getWorldManager().isMapWorld(player.getWorld())) {
                plugin.getItemManager().giveInventoryItem(player);
            }
        }, 5L); // Small delay to ensure world change is complete
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Give navigation item again after respawn
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getNavigationItemManager().giveNavigationItem(player);
        }, 10L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        ItemStack item = event.getOldCursor();

        // Prevent dragging navigation items
        if (plugin.getNavigationItemManager().isExitMapItem(item) ||
                plugin.getNavigationItemManager().isLobbyItem(item)) {

            event.setCancelled(true);
        }
    }
}