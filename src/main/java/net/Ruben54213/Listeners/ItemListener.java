package net.Ruben54213.Listeners;

import net.Ruben54213.GUIs.AllMapsGui;
import net.Ruben54213.GUIs.MapCreationGui;
import net.Ruben54213.GUIs.MapInfoGui;
import net.Ruben54213.GUIs.MapOverviewGui;
import net.Ruben54213.GUIs.MapSettingsGui;
import net.Ruben54213.SmashMapsV2;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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

    private boolean isPluginGuiTitle(String title) {
        if (title == null) return false;
        return title.equals(plugin.getConfigManager().getMapInfoGuiTitle())
                || title.equals(plugin.getConfigManager().getGuiTitle())
                || title.equals(plugin.getConfigManager().getMapOverviewTitle())
                || title.equals(plugin.getConfigManager().getAllMapsTitle())
                || title.equals(plugin.getConfigManager().getMapSettingsGuiTitle())
                || title.equals(ChatColor.translateAlternateColorCodes('&', "&8Position setzen"));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (plugin.getItemManager().isInventoryItem(item)) {
            event.setCancelled(true);

            // Only trigger on RIGHT_CLICK
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("gui_open"), 1.0f, 1.0f);
                new MapCreationGui(plugin, player).open();
            }
        }

        if (plugin.getItemManager().isMapOverviewItem(item)) {
            event.setCancelled(true);

            // Only trigger on RIGHT_CLICK
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("gui_open"), 1.0f, 1.0f);
                new MapOverviewGui(plugin, player).open();
            }
        }

        if (plugin.getItemManager().isAllMapsItem(item)) {
            event.setCancelled(true);

            // Only trigger on RIGHT_CLICK
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                player.playSound(player.getLocation(), plugin.getConfigManager().getSound("gui_open"), 1.0f, 1.0f);

                AllMapsGui gui = new AllMapsGui(plugin, player);
                // Register the GUI with the GuiListener
                plugin.getGuiListener().registerAllMapsGui(player, gui);
                gui.open();
            }
        }

        // --- Map-spezifische Items ---
        if (plugin.getItemManager().isMapInfoBook(item)) {
            event.setCancelled(true);
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                new MapInfoGui(plugin, player).open();
            }
        }

        if (plugin.getItemManager().isPositionsViewItem(item)) {
            event.setCancelled(true);
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                plugin.getPositionDisplayManager().togglePositions(player);
            }
        }

        if (plugin.getItemManager().isEditModeToggle(item)) {
            event.setCancelled(true);
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                // Inhaber prüfen
                String worldName = player.getWorld().getName().replace("maps/", "");
                net.Ruben54213.Models.SmashMap map = plugin.getMapManager().getMapByWorld(worldName);
                if (map != null && map.getOwnerUUID() != null && map.getOwnerUUID().equals(player.getUniqueId())) {
                    plugin.getItemManager().giveEditModeItems(player);
                } else {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + org.bukkit.ChatColor.RED + "Nur der Inhaber kann den Bearbeitungsmodus verwenden.");
                }
            }
        }

        if (plugin.getItemManager().isEditModeExit(item)) {
            event.setCancelled(true);
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                plugin.getItemManager().restoreMapWorldItems(player);
            }
        }

        if (plugin.getItemManager().isMapSettingsItem(item)) {
            event.setCancelled(true);
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                new MapSettingsGui(plugin, player).open();
            }
        }

        // Positions-Setzen GUI öffnet sich via Rechtsklick auf Schild
        if (plugin.getItemManager().isPositionsSetItem(item)) {
            event.setCancelled(true);
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                MapSettingsGui.openPositionTypeSelection(plugin, player);
            }
            // Entfernen per Linksklick auf vorhandene Marker – Logik in PositionDisplayManager
            if (event.getAction().toString().contains("LEFT_CLICK")) {
                plugin.getPositionDisplayManager().removeNearestPosition(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        // Vollständige Sperre in Plugin-GUIs (inkl. MapInfo)
        String title = event.getView().getTitle();
        if (isPluginGuiTitle(title)) {
            event.setCancelled(true);
            return;
        }

        // Zahlentasten / Offhand-Swap verhindern, wenn Plugin-Items involviert sind
        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbar = event.getHotbarButton();
            if (hotbar >= 0) {
                Player p = (Player) event.getWhoClicked();
                ItemStack hotbarItem = p.getInventory().getItem(hotbar);
                if (plugin.getItemManager().isPluginItem(hotbarItem)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (event.getClick() == ClickType.SWAP_OFFHAND) {
            ItemStack off = ((Player) event.getWhoClicked()).getInventory().getItemInOffHand();
            if (plugin.getItemManager().isPluginItem(off)) {
                event.setCancelled(true);
                return;
            }
        }

        // Genereller Schutz für Plugin-Items
        ItemStack item = event.getCurrentItem();

        if (plugin.getItemManager().isInventoryItem(item) ||
                plugin.getItemManager().isMapOverviewItem(item) ||
                plugin.getItemManager().isAllMapsItem(item) ||
                plugin.getItemManager().isMapInfoBook(item) ||
                plugin.getItemManager().isPositionsViewItem(item) ||
                plugin.getItemManager().isEditModeToggle(item) ||
                plugin.getItemManager().isPositionsSetItem(item) ||
                plugin.getItemManager().isMapSettingsItem(item) ||
                plugin.getItemManager().isEditModeAxe(item) ||
                plugin.getItemManager().isEditModeExit(item)) {
            event.setCancelled(true);
        }

        ItemStack cursorItem = event.getCursor();
        if (plugin.getItemManager().isInventoryItem(cursorItem) ||
                plugin.getItemManager().isMapOverviewItem(cursorItem) ||
                plugin.getItemManager().isAllMapsItem(cursorItem) ||
                plugin.getItemManager().isMapInfoBook(cursorItem) ||
                plugin.getItemManager().isPositionsViewItem(cursorItem) ||
                plugin.getItemManager().isEditModeToggle(cursorItem) ||
                plugin.getItemManager().isPositionsSetItem(cursorItem) ||
                plugin.getItemManager().isMapSettingsItem(cursorItem) ||
                plugin.getItemManager().isEditModeAxe(cursorItem) ||
                plugin.getItemManager().isEditModeExit(cursorItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // In Plugin-GUIs grundsätzlich keine Drags erlauben
        String title = event.getView().getTitle();
        if (isPluginGuiTitle(title)) {
            event.setCancelled(true);
            return;
        }

        ItemStack item = event.getOldCursor();
        if (plugin.getItemManager().isInventoryItem(item) ||
                plugin.getItemManager().isMapOverviewItem(item) ||
                plugin.getItemManager().isAllMapsItem(item) ||
                plugin.getItemManager().isMapInfoBook(item) ||
                plugin.getItemManager().isPositionsViewItem(item) ||
                plugin.getItemManager().isEditModeToggle(item) ||
                plugin.getItemManager().isPositionsSetItem(item) ||
                plugin.getItemManager().isMapSettingsItem(item) ||
                plugin.getItemManager().isEditModeAxe(item) ||
                plugin.getItemManager().isEditModeExit(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (plugin.getItemManager().isInventoryItem(item) ||
                plugin.getItemManager().isMapOverviewItem(item) ||
                plugin.getItemManager().isAllMapsItem(item) ||
                plugin.getItemManager().isMapInfoBook(item) ||
                plugin.getItemManager().isPositionsViewItem(item) ||
                plugin.getItemManager().isEditModeToggle(item) ||
                plugin.getItemManager().isPositionsSetItem(item) ||
                plugin.getItemManager().isMapSettingsItem(item) ||
                plugin.getItemManager().isEditModeAxe(item) ||
                plugin.getItemManager().isEditModeExit(item)) {
            event.setCancelled(true);
        }
    }
}