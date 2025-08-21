package net.Ruben54213.Manager;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import net.Ruben54213.SmashMapsV2;

public class ConfigManager {

    private final SmashMapsV2 plugin;
    private FileConfiguration config;

    public ConfigManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "&e&lSmash &8>> &7"));
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages." + path, "Message not found: " + path));
    }

    public String getGuiTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.title", "&8Karte erstellen"));
    }

    public int getGuiSize() {
        return config.getInt("gui.size", 9);
    }

    // Map Overview Item Configuration (Crafting Table)
    public int getMapOverviewSlot() {
        return config.getInt("items.map_overview.slot", 4);
    }

    public String getMapOverviewMaterial() {
        return config.getString("items.map_overview.material", "CRAFTING_TABLE");
    }

    public String getMapOverviewName() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("items.map_overview.name", "&6&lKarte &7bearbeiten"));
    }

    public String[] getMapOverviewLore() {
        return config.getStringList("items.map_overview.lore")
                .stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .toArray(String[]::new);
    }

    // Map Overview GUI Configuration
    public String getMapOverviewTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.overview.title", "&8Karten von &e%player%"));
    }

    public int getMapOverviewSize() {
        return config.getInt("gui.overview.size", 45);
    }

    // Inventory Item Configuration (Diamond that opens GUI)
    public int getInventoryItemSlot() {
        return config.getInt("items.inventory_item.slot", 5);
    }

    public String getInventoryItemMaterial() {
        return config.getString("items.inventory_item.material", "DIAMOND");
    }

    public String getInventoryItemName() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("items.inventory_item.name", "&e&lKarte &7Erstellen"));
    }

    public String[] getInventoryItemLore() {
        return config.getStringList("items.inventory_item.lore")
                .stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .toArray(String[]::new);
    }

    // Create Map Item Configuration (Golden Pickaxe in GUI)
    public int getCreateMapSlot() {
        return config.getInt("items.create_map.slot", 13);
    }

    public String getCreateMapMaterial() {
        return config.getString("items.create_map.material", "GOLDEN_PICKAXE");
    }

    public String getCreateMapName() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("items.create_map.name", "&6&lSmash Map"));
    }

    public String[] getCreateMapLore() {
        return config.getStringList("items.create_map.lore")
                .stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .toArray(String[]::new);
    }

    // All Maps Item Configuration (Compass)
    public int getAllMapsSlot() {
        return config.getInt("items.all_maps.slot", 0);
    }

    public String getAllMapsMaterial() {
        return config.getString("items.all_maps.material", "COMPASS");
    }

    public String getAllMapsName() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("items.all_maps.name", "&6&lAlle Karten"));
    }

    public String[] getAllMapsLore() {
        return config.getStringList("items.all_maps.lore")
                .stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .toArray(String[]::new);
    }

    // All Maps GUI Configuration
    public String getAllMapsTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.all_maps.title", "&6Alle Karten"));
    }

    public int getAllMapsSize() {
        return config.getInt("gui.all_maps.size", 54);
    }

    public int getBorderSize() {
        return config.getInt("world.border_size", 75);
    }

    public String getSpawnPlatformMaterial() {
        return config.getString("world.spawn_platform.material", "STONE");
    }

    public int getSpawnPlatformSize() {
        return config.getInt("world.spawn_platform.size", 3);
    }

    public Sound getSound(String soundType) {
        String soundName = config.getString("sounds." + soundType, "UI_BUTTON_CLICK");
        try {
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(soundName.toLowerCase());
            Sound sound = org.bukkit.Registry.SOUNDS.get(key);
            return sound != null ? sound : Sound.UI_BUTTON_CLICK;
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid sound: " + soundName + ", using default UI_BUTTON_CLICK");
            return Sound.UI_BUTTON_CLICK;
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public org.bukkit.configuration.file.FileConfiguration getConfig() {
        return config;
    }
}