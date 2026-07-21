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

    // New: Map-Hotbar items (configurable)
    public int getMapInfoSlot() { return config.getInt("items.map.map_info.slot", 2); }
    public String getMapInfoMaterial() { return config.getString("items.map.map_info.material", "BOOK"); }
    public String getMapInfoName() { return ChatColor.translateAlternateColorCodes('&', config.getString("items.map.map_info.name", "&e&lKarteninfo &8&l| &7Rechtsklick")); }

    public int getPositionsViewSlot() { return config.getInt("items.map.positions_view.slot", 4); }
    public String getPositionsViewMaterial() { return config.getString("items.map.positions_view.material", "ENDER_EYE"); }
    public String getPositionsViewName() { return ChatColor.translateAlternateColorCodes('&', config.getString("items.map.positions_view.name", "&5&lPositionen Anzeigen &8&l| &7Rechtsklick")); }

    public int getEditModeSlot() { return config.getInt("items.map.edit_mode.slot", 6); }
    public String getEditModeMaterial() { return config.getString("items.map.edit_mode.material", "CRAFTING_TABLE"); }
    public String getEditModeName() { return ChatColor.translateAlternateColorCodes('&', config.getString("items.map.edit_mode.name", "&9&lBearbeitungsmodus &8&l| &7Rechtsklick")); }

    public int getPositionsSetSlot() { return config.getInt("items.map.positions_set.slot", 7); }
    public String getPositionsSetMaterial() { return config.getString("items.map.positions_set.material", "OAK_SIGN"); }
    public String getPositionsSetName() { return ChatColor.translateAlternateColorCodes('&', config.getString("items.map.positions_set.name", "&9&lPositionen Setzen &8&l| &7Rechtsklick")); }

    public int getMapSettingsSlot() { return config.getInt("items.map.map_settings.slot", 8); }
    public String getMapSettingsMaterial() { return config.getString("items.map.map_settings.material", "COMMAND_BLOCK"); }
    public String getMapSettingsName() { return ChatColor.translateAlternateColorCodes('&', config.getString("items.map.map_settings.name", "&e&lKarteneinstellungen &8&l| &7Rechtsklick")); }

    // Edit mode items
    public String getWorldEditAxeMaterial() { return config.getString("items.map.edit_mode_items.axe_material", "WOODEN_AXE"); }
    public String getWorldEditAxeName() { return ChatColor.translateAlternateColorCodes('&', config.getString("items.map.edit_mode_items.axe_name", "&dWorldedit")); }
    public String getEditModeExitMaterial() { return config.getString("items.map.edit_mode_items.exit_material", "BARRIER"); }
    public String getEditModeExitName() { return ChatColor.translateAlternateColorCodes('&', config.getString("items.map.edit_mode_items.exit_name", "&c&lBearbeitungsmodus beenden &8&l| &7Rechtsklick")); }

    // GUI titles
    public String getMapInfoGuiTitle() { return ChatColor.translateAlternateColorCodes('&', config.getString("gui.map_info.title", "&8Karteninformationen")); }
    public String getMapSettingsGuiTitle() { return ChatColor.translateAlternateColorCodes('&', config.getString("gui.map_settings.title", "&8Karteneinstellungen")); }

    // Database / Storage (MinIO) Configuration
    public String getDatabaseEndpoint() {
        return config.getString("database.endpoint", "http://localhost:9000");
    }

    public String getDatabaseAccessKey() {
        return config.getString("database.access_key", "");
    }

    public String getDatabaseSecretKey() {
        return config.getString("database.secret_key", "");
    }

    public String getDatabaseBucketName() {
        return config.getString("database.bucket_name", "smashmaps");
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