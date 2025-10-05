package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpawnListener implements Listener {

    private final SmashMapsV2 plugin;

    public SpawnListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    private boolean isInSpawnWorld(Player player) {
        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn == null) return false;
        return player.getWorld().equals(spawn.getWorld());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn == null) return;
        // Delay one tick to ensure player fully spawned
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player p = event.getPlayer();
            if (p.isOnline()) {
                p.teleport(spawn);
            }
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInSpawnWorld(player)) return;
        if (player.isOp() || player.hasPermission("smashmaps.admin")) return;
        event.setCancelled(true);
        player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("world_protected"));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isInSpawnWorld(player)) return;
        if (player.isOp() || player.hasPermission("smashmaps.admin")) return;
        event.setCancelled(true);
        player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("world_protected"));
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        if (!isInSpawnWorld(damager)) return;
        if (damager.isOp() || damager.hasPermission("smashmaps.admin")) return;
        event.setCancelled(true);
        damager.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("world_protected"));
    }
}
