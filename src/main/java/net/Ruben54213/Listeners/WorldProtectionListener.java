package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldProtectionListener implements Listener {

    private final SmashMapsV2 plugin;

    public WorldProtectionListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!canModifyWorld(player)) {
            event.setCancelled(true);
            sendProtectionMessage(player);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!canModifyWorld(player)) {
            event.setCancelled(true);
            sendProtectionMessage(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only cancel interactions that modify the world (not right-click with items)
        if (event.getAction().toString().contains("PHYSICAL") ||
                (event.getAction().toString().contains("RIGHT_CLICK_BLOCK") &&
                        event.getClickedBlock() != null &&
                        isInteractableBlock(event.getClickedBlock().getType().toString()))) {

            if (!canModifyWorld(player)) {
                event.setCancelled(true);
                sendProtectionMessage(player);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();

            if (!canModifyWorld(player)) {
                event.setCancelled(true);
                sendProtectionMessage(player);
            }
        }
    }

    private boolean canModifyWorld(Player player) {
        // Allow if player is OP
        if (player.isOp()) {
            return true;
        }

        // Allow if player is in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }

        // Check if it's a map world
        if (!plugin.getWorldManager().isMapWorld(player.getWorld())) {
            return true; // Not a map world, allow everything
        }

        // Check if player owns this map
        return plugin.getMapManager().isMapOwner(player.getUniqueId(), player.getWorld().getName());
    }

    private void sendProtectionMessage(Player player) {
        String message = plugin.getConfigManager().getPrefix() +
                plugin.getConfigManager().getMessage("world_protected");
        player.sendMessage(message);
        player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
    }

    private boolean isInteractableBlock(String material) {
        return material.contains("DOOR") ||
                material.contains("GATE") ||
                material.contains("TRAPDOOR") ||
                material.contains("BUTTON") ||
                material.contains("LEVER") ||
                material.contains("CHEST") ||
                material.contains("FURNACE") ||
                material.contains("HOPPER") ||
                material.contains("DISPENSER") ||
                material.contains("DROPPER");
    }
}