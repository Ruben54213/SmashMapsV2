package net.Ruben54213.Listeners;

import net.Ruben54213.GUIs.MapSettingsGui;
import net.Ruben54213.Models.SmashMap;
import net.Ruben54213.SmashMapsV2;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MapSettingsListener implements Listener {

    private final SmashMapsV2 plugin;
    private final Set<UUID> waitingForRename = new HashSet<>();

    public MapSettingsListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null) return;

        String title = event.getView().getTitle();
        if (title == null) return;

        if (title.equals(plugin.getConfigManager().getMapSettingsGuiTitle())) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            int slot = event.getRawSlot();
            if (slot == 13) {
                // Icon ändern
                MapSettingsGui.startIconSelection(plugin, (org.bukkit.entity.Player) event.getWhoClicked());
                event.getWhoClicked().closeInventory();
            } else if (slot == 15) {
                // Name ändern via Chat
                waitingForRename.add(event.getWhoClicked().getUniqueId());
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + "Gebe nun den neuen Namen im Chat ein.");
            }
        } else if (title.equals(ChatColor.translateAlternateColorCodes('&', "&8Position setzen"))) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) event.getWhoClicked();
            org.bukkit.Location loc = p.getLocation();
            String worldName = p.getWorld().getName().replace("maps/", "");
            // Platzhalter: An PositionDisplayManager delegieren
            if (slot == 11) plugin.getPositionDisplayManager().addPlayerSpawn(worldName, loc, p);
            if (slot == 13) plugin.getPositionDisplayManager().addItemSpawn(worldName, loc, p);
            if (slot == 15) plugin.getPositionDisplayManager().setCenter(worldName, loc, p);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!waitingForRename.contains(event.getPlayer().getUniqueId())) return;

        event.setCancelled(true);
        String newName = event.getMessage();
        org.bukkit.entity.Player player = event.getPlayer();

        // Validierungen wie im /rename Command
        if (newName.trim().isEmpty() || newName.length() > 32) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Ungültiger Name.");
            waitingForRename.remove(player.getUniqueId());
            return;
        }

        // Map suchen
        String worldName = player.getWorld().getName().replace("maps/", "");
        SmashMap map = plugin.getMapManager().getMapByWorld(worldName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Map nicht gefunden.");
            waitingForRename.remove(player.getUniqueId());
            return;
        }

        // Besitzer prüfen
        if (!map.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Du bist nicht der Inhaber dieser Map.");
            waitingForRename.remove(player.getUniqueId());
            return;
        }

        // Belegt?
        net.Ruben54213.Models.SmashMap existing = plugin.getMapManager().getMapByName(newName);
        if (existing != null && existing.getId() != map.getId()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Dieser Name ist bereits vergeben.");
            waitingForRename.remove(player.getUniqueId());
            return;
        }

        String oldName = map.getName();
        map.setName(newName);
        plugin.getMapManager().updateMap(map);

        player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GREEN + "✓ Map umbenannt von " + ChatColor.WHITE + oldName + ChatColor.GRAY + " zu " + ChatColor.WHITE + newName);

        // MinIO-Upload asynchron
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try { plugin.getMinIOManager().uploadMapsYmlToMinIO(); }
            catch (Exception ignored) {}
        });

        plugin.getScoreboardManager().updateAllScoreboards();
        waitingForRename.remove(player.getUniqueId());
    }
}
