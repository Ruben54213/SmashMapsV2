package net.Ruben54213.GUIs;

import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.SmashMapsV2;
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

public class MapInfoGui {

    private final SmashMapsV2 plugin;
    private final Player player;
    private final Inventory inventory;

    public MapInfoGui(SmashMapsV2 plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        this.inventory = Bukkit.createInventory(null, 27, plugin.getConfigManager().getMapInfoGuiTitle());
        setup();
    }

    private void setup() {
        // Glasränder
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 9; i++) inventory.setItem(i, glass);
        for (int i = 18; i < 27; i++) inventory.setItem(i, glass);
        inventory.setItem(9, glass);
        inventory.setItem(17, glass);

        // Map aus Welt bestimmen
        String worldName = player.getWorld().getName().replace("maps/", "");
        SmashMap map = plugin.getMapManager().getMapByWorld(worldName);
        if (map == null) {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta bm = barrier.getItemMeta();
            if (bm != null) {
                bm.setDisplayName("§cKeine Map-Daten gefunden");
                barrier.setItemMeta(bm);
            }
            inventory.setItem(14, barrier);
            return;
        }

        // Icon-Item mit Infos
        ItemStack icon = new ItemStack(map.getIconMaterial());
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            String created = df.format(new Date(map.getCreationTime()));

            // Spielbar? -> mind. 8 Items, 8 Spawns, 1 Center
            String world = map.getWorldName();
            int itemCount = plugin.getMapManager().getItemSpawns(world).size();
            int spawnCount = plugin.getMapManager().getPlayerSpawns(world).size();
            boolean hasCenter = plugin.getMapManager().getCenter(world) != null;
            boolean playable = itemCount >= 8 && spawnCount >= 8 && hasCenter;

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lKarteninfos"));
            meta.setLore(Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Spielbar?: ") + (playable ? "§aJa" : "§cNein"),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Name: &e" + map.getName()),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Erstellt: &e" + created),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Typ: &aSmash"),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Approved?: " + (map.isApproved() ? "&a✓" : "&c✗")),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Spiele: &e?")
            ));
            icon.setItemMeta(meta);
        }
        inventory.setItem(13, icon);

        // Positions-Item mit Anzahl Items/Spawns/Zentrum
        ItemStack positions = new ItemStack(Material.ENDER_EYE);
        ItemMeta pmeta = positions.getItemMeta();
        if (pmeta != null) {
            String world = map.getWorldName();
            int itemCount = plugin.getMapManager().getItemSpawns(world).size();
            int spawnCount = plugin.getMapManager().getPlayerSpawns(world).size();
            int centerCount = plugin.getMapManager().getCenter(world) != null ? 1 : 0;

            pmeta.setDisplayName("§5§lPositionen");
            pmeta.setLore(Arrays.asList(
                    "§7↪ §fItems: §d" + itemCount,
                    "§7↪ §fSpawns: §d" + spawnCount,
                    "§7↪ §fZentrum: §d" + centerCount
            ));
            positions.setItemMeta(pmeta);
        }
        // freier Slot links neben dem Icon
        inventory.setItem(12, positions);
    }

    public void open() { player.openInventory(inventory); }

    public Inventory getInventory() { return inventory; }
}
