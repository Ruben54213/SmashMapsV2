package net.Ruben54213.Listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class FlowStopper implements Listener {

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Material blockType = event.getBlock().getType();

        // Stoppe Wasserfluss
        if (blockType == Material.WATER || blockType == Material.WATER_BUCKET) {
            event.setCancelled(true);
        }

        // Stoppe Lavafluss
        if (blockType == Material.LAVA || blockType == Material.LAVA_BUCKET) {
            event.setCancelled(true);
        }
    }
}