package net.Ruben54213.GUIs;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MapOverviewGui {

    private final SmashMapsV2 plugin;
    private final Player player;
    private final Inventory inventory;

    public MapOverviewGui(SmashMapsV2 plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        String title = plugin.getConfigManager().getMapOverviewTitle()
                .replace("%player%", player.getName());
        int size = plugin.getConfigManager().getMapOverviewSize();

        this.inventory = Bukkit.createInventory(null, size, title);
        setupGui();
    }

    private void setupGui() {
        // Fill with gray glass panes first
        fillWithGlass();

        // Get player's maps
        List<SmashMap> playerMaps = plugin.getMapManager().getPlayerMaps(player.getUniqueId());

        if (playerMaps.isEmpty()) {
            showNoMapsItem();
            return;
        }

        // Display maps in the center area (avoiding borders)
        displayPlayerMaps(playerMaps);
    }

    private void fillWithGlass() {
        ItemStack glassPane = createGlassPane();

        // Fill all slots with glass
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glassPane);
        }
    }

    private ItemStack createGlassPane() {
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        return glassPane;
    }

    private void showNoMapsItem() {
        ItemStack noMapsItem = new ItemStack(Material.BARRIER);
        ItemMeta noMapsMeta = noMapsItem.getItemMeta();
        if (noMapsMeta != null) {
            noMapsMeta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getMessage("no_maps_title")));
            noMapsMeta.setLore(Arrays.asList(
                    org.bukkit.ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfigManager().getMessage("no_maps_line1")),
                    org.bukkit.ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfigManager().getMessage("no_maps_line2"))
            ));
            noMapsItem.setItemMeta(noMapsMeta);
        }

        // Place in center of GUI (for 54 slots = 5x9, center is slot 22)
        int centerSlot = 22;
        inventory.setItem(centerSlot, noMapsItem);
    }

    private void displayPlayerMaps(List<SmashMap> playerMaps) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        int slot = 10; // Start after top border (row 1) and left border
        int mapsDisplayed = 0;
        int maxMapsPerPage = getMaxMapsPerPage();

        for (SmashMap map : playerMaps) {
            if (mapsDisplayed >= maxMapsPerPage) break;

            // Skip border slots (first and last column)
            if (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
                if (slot % 9 == 0) slot++; // Skip to next row if we hit right border
                continue;
            }

            // Skip bottom border row
            if (slot >= inventory.getSize() - 9) break;

            ItemStack mapItem = createMapItem(map, dateFormat);
            inventory.setItem(slot, mapItem);

            slot++;
            mapsDisplayed++;
        }
    }

    private int getMaxMapsPerPage() {
        // Calculate based on GUI size, excluding borders
        int rows = inventory.getSize() / 9;
        int availableRows = Math.max(1, rows - 2); // Exclude top and bottom border
        int availableColumns = 7; // Exclude left and right border
        return availableRows * availableColumns;
    }

    private ItemStack createMapItem(SmashMap map, SimpleDateFormat dateFormat) {
        ItemStack item = new ItemStack(map.getIconMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set display name with formatting like in the image
            String displayName = map.getIconDisplayName() != null ? map.getIconDisplayName() : map.getName();
            meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    "&e" + displayName + " &8(&c✘&8) &7(#" + String.format("%04d", map.getId()) + "/0)"));

            // Create lore matching the image format
            String formattedDate = dateFormat.format(new Date(map.getCreationTime()));
            meta.setLore(Arrays.asList(
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', "&8→ &7Typ: &aSmash"),
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', "&8→ &7Sichtbarkeit: &e?"),
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', "&8→ &7Aktualisiert: &e" + formattedDate),
                    "",
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', "&6Klicke zum Teleportieren!"),
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', "&eminercraft:teleport"),
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', "&7NBT: &e2 tag(s)")
            ));

            // Add map ID to persistent data for identification
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "map_id"),
                    org.bukkit.persistence.PersistentDataType.INTEGER,
                    map.getId()
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }
}