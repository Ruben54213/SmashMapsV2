package net.Ruben54213.GUIs;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glassPane);
        }
    }

    private void showNoMapsItem() {
        ItemStack noMapsItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = noMapsItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lKeine Karten gefunden");
            meta.setLore(Arrays.asList(
                    "§7Du hast noch keine Karten erstellt.",
                    "§7Erstelle eine neue Karte mit dem",
                    "§7§lDiamant §7in deinem Inventar!"
            ));
            noMapsItem.setItemMeta(meta);
        }

        // Place in center of inventory
        int centerSlot = inventory.getSize() / 2;
        inventory.setItem(centerSlot, noMapsItem);
    }

    private void displayPlayerMaps(List<SmashMap> playerMaps) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        // Define slots to place maps in (avoiding borders)
        int[] mapSlots;
        if (inventory.getSize() == 45) { // 5 rows
            mapSlots = new int[]{
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34
            };
        } else if (inventory.getSize() == 36) { // 4 rows
            mapSlots = new int[]{
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25
            };
        } else { // Default for other sizes
            mapSlots = new int[]{10, 11, 12, 13, 14, 15, 16};
        }

        int slotIndex = 0;
        for (SmashMap map : playerMaps) {
            if (slotIndex >= mapSlots.length) break; // Don't exceed available slots

            ItemStack mapItem = createMapItem(map, dateFormat);
            inventory.setItem(mapSlots[slotIndex], mapItem);
            slotIndex++;
        }
    }

    private ItemStack createMapItem(SmashMap map, SimpleDateFormat dateFormat) {
        ItemStack item = new ItemStack(map.getIconMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Get display name with color support - no italic formatting
            String displayName;
            if (map.getIconDisplayName() != null && !map.getIconDisplayName().isEmpty()) {
                displayName = ChatColor.translateAlternateColorCodes('&', map.getIconDisplayName());
            } else {
                displayName = ChatColor.translateAlternateColorCodes('&', map.getName());
            }

            String ownerName = Bukkit.getOfflinePlayer(map.getOwnerUUID()).getName();
            if (ownerName == null) ownerName = "Unknown";

            // Set the display name with status indicator and ID - same format as AllMapsGui
            meta.setDisplayName("§r" + displayName + " §8(§c✘§8) §7(#" + String.format("%04d", map.getId()) + "/0)");

            String formattedDate = dateFormat.format(new Date(map.getCreationTime()));
            meta.setLore(Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Typ: &aSmash"),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Ersteller: &e" + ownerName),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Status: " + (map.isApproved() ? "&aApproved" : "&cNicht approved")),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Spiele: &e?"),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Aktualisiert: &e" + formattedDate),
                    "",
                    ChatColor.translateAlternateColorCodes('&', "&6Klicke zum Teleportieren!")
            ));

            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "map_id"),
                    org.bukkit.persistence.PersistentDataType.INTEGER,
                    map.getId());

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