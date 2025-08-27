package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class MapWorldListener implements Listener {

    private final SmashMapsV2 plugin;

    public MapWorldListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Immer zuerst komplett leeren, um Konflikte zu vermeiden
        plugin.getItemManager().clearAllInventory(e.getPlayer());
        // ItemManager kümmert sich bereits im eigenen Listener – hier als Fallback direkt nachladen
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getItemManager().giveInventoryItem(e.getPlayer()), 30L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        // Zuerst immer Inventar komplett leeren
        plugin.getItemManager().clearAllInventory(e.getPlayer());
        // Marker + Toggle clean
        plugin.getPositionDisplayManager().resetPlayer(e.getPlayer());
        // Danach – je nach Welt – Items vergeben
        plugin.getItemManager().giveInventoryItem(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getPositionDisplayManager().resetPlayer(e.getPlayer());
        plugin.getItemManager().clearAllInventory(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        plugin.getPositionDisplayManager().resetPlayer(e.getPlayer());
        plugin.getItemManager().clearAllInventory(e.getPlayer());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        try {
            if (plugin.getWorldManager().isMapWorld(e.getWorld())) {
                plugin.getPositionDisplayManager().cleanupWorld(e.getWorld());
            }
        } catch (Exception ignored) {
            plugin.getPositionDisplayManager().cleanupWorld(e.getWorld());
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        try {
            if (plugin.getWorldManager().isMapWorld(e.getWorld())) {
                plugin.getPositionDisplayManager().cleanupChunk(e.getChunk());
            }
        } catch (Exception ignored) {
            plugin.getPositionDisplayManager().cleanupChunk(e.getChunk());
        }
    }
}
