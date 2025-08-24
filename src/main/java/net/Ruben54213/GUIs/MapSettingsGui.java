package net.Ruben54213.GUIs;

import net.Ruben54213.Manager.IconSelectionManager;
import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MapSettingsGui {

    private final SmashMapsV2 plugin;
    private final Player player;
    private final Inventory inventory;

    public MapSettingsGui(SmashMapsV2 plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, plugin.getConfigManager().getMapSettingsGuiTitle());
        setup();
    }

    private void setup() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 9; i++) inventory.setItem(i, glass);
        for (int i = 18; i < 27; i++) inventory.setItem(i, glass);
        inventory.setItem(9, glass);
        inventory.setItem(17, glass);

        // Icon ändern (Slot 13)
        ItemStack icon = new ItemStack(Material.ITEM_FRAME);
        ItemMeta im = icon.getItemMeta();
        if (im != null) {
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lIcon ändern"));
            im.setLore(Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', "&7Droppe das gewünschte Item"),
                    ChatColor.translateAlternateColorCodes('&', "&7um es als Icon zu setzen.")
            ));
            icon.setItemMeta(im);
        }
        inventory.setItem(13, icon);

        // Namen ändern (Slot 15)
        ItemStack rename = new ItemStack(Material.NAME_TAG);
        ItemMeta rm = rename.getItemMeta();
        if (rm != null) {
            rm.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lNamen ändern"));
            rm.setLore(Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', "&7Klicke und gebe den neuen"),
                    ChatColor.translateAlternateColorCodes('&', "&7Namen im Chat ein.")
            ));
            rename.setItemMeta(rm);
        }
        inventory.setItem(15, rename);
    }

    public void open() { player.openInventory(inventory); }

    public static void openPositionTypeSelection(SmashMapsV2 plugin, Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&8Position setzen"));

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = 18; i < 27; i++) inv.setItem(i, glass);
        inv.setItem(9, glass);
        inv.setItem(17, glass);

        // Links: Spielerspawn
        ItemStack spawn = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta sm = spawn.getItemMeta();
        if (sm != null) {
            sm.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aSpielerspawn"));
            sm.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7Setze an deiner aktuellen Position")));
            spawn.setItemMeta(sm);
        }
        inv.setItem(11, spawn);

        // Mitte: Itemspawn
        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta cm = chest.getItemMeta();
        if (cm != null) {
            cm.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aItem"));
            cm.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7Setze an deiner aktuellen Position")));
            chest.setItemMeta(cm);
        }
        inv.setItem(13, chest);

        // Rechts: Center
        ItemStack beacon = new ItemStack(Material.BEACON);
        ItemMeta bm = beacon.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aCenter"));
            bm.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7Setze an deiner aktuellen Position")));
            beacon.setItemMeta(bm);
        }
        inv.setItem(15, beacon);

        player.openInventory(inv);
        // Registrierung über zentralen GuiListener erfolgt bereits im Projekt;
        // Fallback: Wir können Klicks im MapSettingsListener behandeln.
    }

    // Hilfsaufrufe: vom Listener nutzbar
    public static void startIconSelection(SmashMapsV2 plugin, Player player) {
        String worldName = player.getWorld().getName().replace("maps/", "");
        SmashMap map = plugin.getMapManager().getMapByWorld(worldName);
        if (map != null) {
            IconSelectionManager.addPlayerWaitingForIcon(player.getUniqueId(), map);
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + "Droppe nun ein Item, um das Icon zu setzen.");
            player.setGameMode(org.bukkit.GameMode.CREATIVE); // nicht zurücksetzen auf GM 0
        }
    }

    public Inventory getInventory() { return inventory; }
}
