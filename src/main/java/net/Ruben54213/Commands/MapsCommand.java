package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.GUIs.MapOverviewGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapsCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public MapsCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Command kann nur von Spielern verwendet werden!");
            return true;
        }

        Player player = (Player) sender;

        // Play sound and open GUI
        player.playSound(player.getLocation(), plugin.getConfigManager().getSound("gui_open"), 1.0f, 1.0f);
        new MapOverviewGui(plugin, player).open();

        return true;
    }
}