package net.Ruben54213.GUIs;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MapCreationGui {

    private final SmashMapsV2 plugin;
    private final Player player;
    private final Inventory inventory;

    public MapCreationGui(SmashMapsV2 plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        String title = plugin.getConfigManager().getGuiTitle();
        int size = plugin.getConfigManager().getGuiSize();

        this.inventory = Bukkit.createInventory(null, size, title);
        setupGui();
    }

    private void setupGui() {
        int inventorySize = inventory.getSize();

        // Create gray glass pane for borders
        ItemStack borderGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderGlass.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" "); // Empty name
            borderGlass.setItemMeta(borderMeta);
        }

        // Only setup borders if we have a 27-slot inventory (3x9)
        if (inventorySize == 27) {
            // Fill borders with glass panes (3x9 = 27 slots)
            // Top row (0-8)
            for (int i = 0; i < 9; i++) {
                inventory.setItem(i, borderGlass);
            }

            // Bottom row (18-26)
            for (int i = 18; i < 27; i++) {
                inventory.setItem(i, borderGlass);
            }

            // Left and right borders of middle row
            inventory.setItem(9, borderGlass);   // Left border
            inventory.setItem(17, borderGlass);  // Right border
        } else if (inventorySize == 9) {
            // For 9-slot inventory, fill all except center
            for (int i = 0; i < 9; i++) {
                if (i != 4) { // Don't fill the center slot
                    inventory.setItem(i, borderGlass);
                }
            }
        }

        // Set the map creation item
        int itemSlot = plugin.getConfigManager().getCreateMapSlot();

        // Validate slot is within inventory size
        if (itemSlot >= 0 && itemSlot < inventorySize) {
            ItemStack mapCreationItem = plugin.getItemManager().createMapCreationItem();
            inventory.setItem(itemSlot, mapCreationItem);
        } else {
            plugin.getLogger().warning("Invalid item slot " + itemSlot + " for inventory size " + inventorySize + ". Using center slot instead.");
            // Use center slot as fallback
            int centerSlot = inventorySize == 27 ? 13 : 4;
            ItemStack mapCreationItem = plugin.getItemManager().createMapCreationItem();
            inventory.setItem(centerSlot, mapCreationItem);
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }
}