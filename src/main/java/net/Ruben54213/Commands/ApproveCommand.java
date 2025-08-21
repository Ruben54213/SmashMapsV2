
package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ApproveCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public ApproveCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("smashmaps.approve")) {
            String message = plugin.getConfigManager().getPrefix() +
                    "§cDu hast keine Berechtigung für diesen Befehl!";
            player.sendMessage(message);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cVerwendung: /approve <mapname>");
            return true;
        }

        String mapName = args[0];

        if (plugin.getMapManager().approveMap(mapName)) {
            String message = plugin.getConfigManager().getPrefix() +
                    "§aMap '§e" + mapName + "§a' wurde erfolgreich approved!";
            player.sendMessage(message);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("map_created"), 1.0f, 1.0f);
        } else {
            String message = plugin.getConfigManager().getPrefix() +
                    "§cMap '§e" + mapName + "§c' wurde nicht gefunden!";
            player.sendMessage(message);
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("error"), 1.0f, 1.0f);
        }

        return true;
    }
}