package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class PositionHotbarListener implements Listener {

    private final SmashMapsV2 plugin;

    public PositionHotbarListener(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHeldChange(PlayerItemHeldEvent event) {
        // Nur in Map-Welten relevant
        if (!plugin.getWorldManager().isMapWorld(event.getPlayer().getWorld())) return;

        // Schild-Anzeige nur im Bearbeitungsmodus
        if (!plugin.getItemManager().isInEditMode(event.getPlayer())) return;

        ItemStack newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        boolean holdingSign = plugin.getItemManager().isPositionsSetItem(newItem);

        plugin.getPositionDisplayManager().handleHeldItem(event.getPlayer(), holdingSign);
    }
}
