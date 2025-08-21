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

            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "map_overview_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN,
                    true
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack createAllMapsItem() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getAllMapsMaterial());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid all maps material in config, using COMPASS");
            material = Material.COMPASS;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String name = plugin.getConfigManager().getAllMapsName();
            meta.setDisplayName(name);

            String[] loreArray = plugin.getConfigManager().getAllMapsLore();
            List<String> lore = Arrays.asList(loreArray);
            meta.setLore(lore);

            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "all_maps_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN,
                    true
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    public void clearInventoryExceptPluginItems(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (!isPluginItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }
    }

    public void giveInventoryItem(Player player) {
        ItemStack inventoryItem = createInventoryItem();
        ItemStack overviewItem = createMapOverviewItem();
        ItemStack allMapsItem = createAllMapsItem();

        clearInventoryExceptPluginItems(player);

        int allMapsSlot = plugin.getConfigManager().getAllMapsSlot();
        int inventorySlot = plugin.getConfigManager().getInventoryItemSlot();
        int overviewSlot = plugin.getConfigManager().getMapOverviewSlot();

        player.getInventory().setItem(allMapsSlot, allMapsItem);
        player.getInventory().setItem(inventorySlot, inventoryItem);
        player.getInventory().setItem(overviewSlot, overviewItem);

        plugin.getNavigationItemManager().giveNavigationItem(player);
        player.updateInventory();
    }

    public boolean isAllMapsItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "all_maps_item");
        return meta.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    public boolean isPluginItem(ItemStack item) {
        return isInventoryItem(item) ||
                isMapOverviewItem(item) ||
                isMapCreationItem(item) ||
                isAllMapsItem(item) ||
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

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "map_creation_item");
        return meta.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            giveInventoryItem(player);
        }, 20L);
    }

    public void updateAllPlayersItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            giveInventoryItem(player);
        }
    }
}