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

    // Additional protection events for comprehensive coverage
    @EventHandler
    public void onPlayerBucketEmpty(org.bukkit.event.player.PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (!canModifyWorld(player)) {
            event.setCancelled(true);
            sendProtectionMessage(player);
        }
    }

    @EventHandler
    public void onPlayerBucketFill(org.bukkit.event.player.PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (!canModifyWorld(player)) {
            event.setCancelled(true);
            sendProtectionMessage(player);
        }
    }

    @EventHandler
    public void onHangingBreak(org.bukkit.event.hanging.HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            Player player = (Player) event.getRemover();
            if (!canModifyWorld(player)) {
                event.setCancelled(true);
                sendProtectionMessage(player);
            }
        }
    }

    @EventHandler
    public void onHangingPlace(org.bukkit.event.hanging.HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (player != null && !canModifyWorld(player)) {
            event.setCancelled(true);
            sendProtectionMessage(player);
        }
    }

    private boolean canModifyWorld(Player player) {
        // Check if it's a map world first
        if (!plugin.getWorldManager().isMapWorld(player.getWorld())) {
            return true; // Not a map world, allow everything
        }

        // If the current map is approved, disallow any modification for everyone
        String worldNameCheck = player.getWorld().getName();
        if (worldNameCheck.startsWith("maps/")) {
            worldNameCheck = worldNameCheck.replace("maps/", "");
        }
        net.Ruben54213.Models.SmashMap approvedMapCheck = plugin.getMapManager().getMapByWorld(worldNameCheck);
        if (approvedMapCheck != null && approvedMapCheck.isApproved()) {
            return false;
        }

        // Allow if player is OP
        if (player.isOp()) {
            return true;
        }

        // Allow if player has admin permission
        if (player.hasPermission("smashmaps.admin") || 
            player.hasPermission("smashmaps.build.admin")) {
            return true;
        }

        // Get the world name for map checking
        String worldName = player.getWorld().getName();
        // Remove "maps/" prefix if present for consistency
        if (worldName.startsWith("maps/")) {
            worldName = worldName.replace("maps/", "");
        }

        // Check if player owns this map
        return plugin.getMapManager().isMapOwner(player.getUniqueId(), worldName);
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