package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.GUIs.MapCreationGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateMapCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public CreateMapCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Command kann nur von Spielern verwendet werden!");
            return true;
        }

        Player player = (Player) sender;

        // Check if player can create maps
        if (!plugin.getMapManager().canCreateMap(player)) {
            int limit = plugin.getMapManager().getPlayerMapLimit(player);
            String message = plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("map_limit_reached").replace("%limit%", String.valueOf(limit));
            player.sendMessage(message);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            return true;
        }

        // Play sound and open GUI
        player.playSound(player.getLocation(), plugin.getConfigManager().getSound("gui_open"), 1.0f, 1.0f);
        new MapCreationGui(plugin, player).open();

        return true;
    }
}