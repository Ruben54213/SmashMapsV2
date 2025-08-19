package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public ReloadCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smashmaps.reload")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }

        // Reload config
        plugin.getConfigManager().reloadConfig();

        // Update all player items
        if (plugin.getItemManager() != null) {
            plugin.getItemManager().updateAllPlayersItems();
        } else {
            // Fallback: Update items manually for all online players
            for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                // Clear and give new items
                player.getInventory().clear();
                org.bukkit.inventory.ItemStack item = createMapCreationItemFallback();
                player.getInventory().setItem(plugin.getConfigManager().getCreateMapSlot(), item);
                player.updateInventory();
            }
        }

        sender.sendMessage(plugin.getConfigManager().getPrefix() + "§aKonfiguration wurde neu geladen und Items aktualisiert!");

        return true;
    }

    private org.bukkit.inventory.ItemStack createMapCreationItemFallback() {
        org.bukkit.Material material;
        try {
            material = org.bukkit.Material.valueOf(plugin.getConfigManager().getCreateMapMaterial());
        } catch (IllegalArgumentException e) {
            material = org.bukkit.Material.DIAMOND;
        }

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String name = plugin.getConfigManager().getCreateMapName();
            meta.setDisplayName(name);

            String[] loreArray = plugin.getConfigManager().getCreateMapLore();
            java.util.List<String> lore = java.util.Arrays.asList(loreArray);
            meta.setLore(lore);

            // Add custom NBT tag
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "map_creation_item"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN,
                    true
            );

            item.setItemMeta(meta);
        }

        return item;
    }
}