package net.Ruben54213.Manager;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemManager implements Listener {

    private final SmashMapsV2 plugin;

    public ItemManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public ItemStack createInventoryItem() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getInventoryItemMaterial());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid inventory item material in config, using DIAMOND");
            material = Material.DIAMOND;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String name = plugin.getConfigManager().getInventoryItemName();
            meta.setDisplayName(name);

            String[] loreArray = plugin.getConfigManager().getInventoryItemLore();
            List<String> lore = Arrays.asList(loreArray);
            meta.setLore(lore);

            // Add custom NBT tag to identify the inventory item
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "inventory_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN,
                    true
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack createMapCreationItem() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getCreateMapMaterial());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid map creation material in config, using GOLDEN_PICKAXE");
            material = Material.GOLDEN_PICKAXE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String name = plugin.getConfigManager().getCreateMapName();
            meta.setDisplayName(name);

            String[] loreArray = plugin.getConfigManager().getCreateMapLore();
            List<String> lore = Arrays.asList(loreArray);
            meta.setLore(lore);

            // Add custom NBT tag to identify the map creation item
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "map_creation_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN,
                    true
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack createMapOverviewItem() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getMapOverviewMaterial());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid map overview material in config, using CRAFTING_TABLE");
            material = Material.CRAFTING_TABLE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String name = plugin.getConfigManager().getMapOverviewName();
            meta.setDisplayName(name);

            String[] loreArray = plugin.getConfigManager().getMapOverviewLore();
            List<String> lore = Arrays.asList(loreArray);
            meta.setLore(lore);

            // Add custom NBT tag to identify the overview item
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "map_overview_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN,
                    true
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    public void giveInventoryItem(Player player) {
        ItemStack inventoryItem = createInventoryItem();
        ItemStack overviewItem = createMapOverviewItem();

        // Clear all items except plugin items before giving new ones
        clearInventoryExceptPluginItems(player);

        // Set specific slots for inventory items
        int inventorySlot = plugin.getConfigManager().getInventoryItemSlot();
        int overviewSlot = plugin.getConfigManager().getMapOverviewSlot();

        player.getInventory().setItem(inventorySlot, inventoryItem);
        player.getInventory().setItem(overviewSlot, overviewItem);

        // Ensure navigation item is preserved/given (slot 8)
        plugin.getNavigationItemManager().giveNavigationItem(player);

        // Update inventory
        player.updateInventory();
    }

    /**
     * Clear all items from player inventory except plugin items
     */
    public void clearInventoryExceptPluginItems(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            // Skip if slot is empty
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            // Check if item is a plugin item
            if (!isPluginItem(item)) {
                // Not a plugin item - remove it
                player.getInventory().setItem(i, null);
            }
        }
    }

    /**
     * Check if an item is any of our plugin items
     */
    public boolean isPluginItem(ItemStack item) {
        return isInventoryItem(item) ||
                isMapOverviewItem(item) ||
                isMapCreationItem(item) ||
                plugin.getNavigationItemManager().isExitMapItem(item) ||
                plugin.getNavigationItemManager().isLobbyItem(item);
    }

    public boolean isMapOverviewItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Check for custom NBT tag first (most reliable)
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "map_overview_item");
        return meta.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    public boolean isInventoryItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Check for custom NBT tag first (most reliable)
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "inventory_item");
        return meta.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    public boolean isMapCreationItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Check for custom NBT tag first (most reliable)
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "map_creation_item");
        return meta.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Give the inventory items when player joins (without clearing inventory)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            giveInventoryItem(player);
        }, 20L); // Delay by 1 second to ensure player is fully loaded
    }

    public void updateAllPlayersItems() {
        // Update items for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            giveInventoryItem(player);
        }
    }
}