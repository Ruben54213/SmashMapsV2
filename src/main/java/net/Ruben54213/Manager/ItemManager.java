package net.Ruben54213.Manager;

import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ItemManager implements Listener {

    private final SmashMapsV2 plugin;
    // Trackt, wer sich im Bearbeitungsmodus befindet
    private final Set<UUID> editModePlayers = new HashSet<>();

    public ItemManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // --- Lobby/Standard Items ---

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

    // --- Map Items ---

    public ItemStack createMapInfoBook() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getMapInfoMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.BOOK;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getMapInfoName());
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "map_info_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createPositionsViewItem() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getPositionsViewMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.ENDER_EYE;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getPositionsViewName());
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "positions_view_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createEditModeItem() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getEditModeMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.CRAFTING_TABLE;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getEditModeName());
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "edit_mode_toggle"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createPositionsSetItem() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getPositionsSetMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.OAK_SIGN;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getPositionsSetName());
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "positions_set_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createMapSettingsItem() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getMapSettingsMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.COMMAND_BLOCK;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getMapSettingsName());
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "map_settings_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createWorldEditAxe() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getWorldEditAxeMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.WOODEN_AXE;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getWorldEditAxeName());
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "edit_mode_axe"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createEditModeExitItem() {
        Material material;
        try {
            material = Material.valueOf(plugin.getConfigManager().getEditModeExitMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.BARRIER;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getEditModeExitName());
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "edit_mode_exit"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    // --- Inventory helpers ---

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
        // Lobby vs Map Items
        if (plugin.getWorldManager().isMapWorld(player.getWorld())) {
            giveMapWorldItems(player);
            return;
        }

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

    public boolean isInEditMode(Player player) {
        return editModePlayers.contains(player.getUniqueId());
    }

    public void clearAllInventory(Player player) {
        player.getInventory().clear();
        player.updateInventory();
    }

    public void giveMapWorldItems(Player player) {
        // komplette Hotbar leeren, damit Diamant/Werkbank verschwinden
        clearAllInventory(player);

        // Basis-Items in Map-Welt
        player.getInventory().setItem(plugin.getConfigManager().getMapInfoSlot(), createMapInfoBook());
        player.getInventory().setItem(plugin.getConfigManager().getPositionsViewSlot(), createPositionsViewItem());

        // „Alle Karten“ immer geben
        player.getInventory().setItem(plugin.getConfigManager().getAllMapsSlot(), createAllMapsItem());
        // „Karte verlassen“ in den letzten Slot
        plugin.getNavigationItemManager().giveNavigationItem(player);

        // Owner-spezifische Items
        World world = player.getWorld();
        String worldName = world.getName().replace("maps/", "");
        SmashMap map = plugin.getMapManager().getMapByWorld(worldName);
        if (map != null && !map.isApproved() && map.getOwnerUUID() != null && map.getOwnerUUID().equals(player.getUniqueId())) {
            player.getInventory().setItem(plugin.getConfigManager().getEditModeSlot(), createEditModeItem());
            // Positionen-Setzen und Einstellungen erscheinen nur im Bearbeitungsmodus, daher hier nicht automatisch setzen
        }

        player.updateInventory();
    }

    public void giveEditModeItems(Player player) {
        // Map bestimmen
        String worldName = player.getWorld().getName().replace("maps/", "");
        SmashMap map = plugin.getMapManager().getMapByWorld(worldName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + org.bukkit.ChatColor.RED + "Keine Map-Daten gefunden.");
            return;
        }
        // Approved-Maps können nicht bearbeitet werden
        if (map.isApproved()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + org.bukkit.ChatColor.RED + "Diese Map ist bereits approved. Der Bearbeitungsmodus ist deaktiviert.");
            return;
        }
        // Nur Inhaber dürfen den Modus nutzen
        if (map.getOwnerUUID() == null || !map.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + org.bukkit.ChatColor.RED + "Nur der Inhaber kann den Bearbeitungsmodus verwenden.");
            return;
        }

        editModePlayers.add(player.getUniqueId());

        // Inventar komplett leeren
        clearAllInventory(player);

        // Bearbeitungsmodus: Exit (Slot 7) und „Positionen Setzen“ (Slot 8)
        player.getInventory().setItem(7, createEditModeExitItem());
        player.getInventory().setItem(8, createPositionsSetItem());

        player.setGameMode(GameMode.CREATIVE); // Bearbeitung sinnvoll in GM 1
        player.updateInventory();
    }

    public void restoreMapWorldItems(Player player) {
        // Bearbeitungsmodus verlassen
        editModePlayers.remove(player.getUniqueId());
        // Marker-Status bereinigen
        plugin.getPositionDisplayManager().resetPlayer(player);

        // Erst alles leeren, dann ggf. neue Items vergeben
        clearAllInventory(player);
        if (plugin.getWorldManager().isMapWorld(player.getWorld())) {
            giveMapWorldItems(player);
        } else {
            giveInventoryItem(player);
        }
    }

    // --- Identifiers ---

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
                isMapInfoBook(item) ||
                isPositionsViewItem(item) ||
                isEditModeToggle(item) ||
                isPositionsSetItem(item) ||
                isMapSettingsItem(item) ||
                isEditModeAxe(item) ||
                isEditModeExit(item) ||
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

    public boolean isMapInfoBook(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "map_info_item"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    public boolean isPositionsViewItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "positions_view_item"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    public boolean isEditModeToggle(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "edit_mode_toggle"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    public boolean isPositionsSetItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "positions_set_item"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    public boolean isMapSettingsItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "map_settings_item"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    public boolean isEditModeAxe(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "edit_mode_axe"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    public boolean isEditModeExit(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "edit_mode_exit"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Erst alles leeren, dann Items vergeben (nach kleinem Delay)
        clearAllInventory(player);
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