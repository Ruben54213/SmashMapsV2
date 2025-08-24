package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfirmCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public ConfirmCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Command kann nur von Spielern verwendet werden!");
            return true;
        }

        Player player = (Player) sender;

        // Prüfen ob Spieler eine ausstehende Löschung hat
        SmashMap pendingMap = plugin.getMapManager().getPendingDeletion(player.getUniqueId());
        if (pendingMap == null) {
            player.sendMessage(ChatColor.RED + "§7Du hast §ckeine§7 ausstehende Bestätigung!");
            return true;
        }

        // Bestätigung verarbeiten

        // Get the delete command and perform deletion
        if (plugin.getCommand("delete").getExecutor() instanceof DeleteCommand) {
            DeleteCommand deleteCommand = (DeleteCommand) plugin.getCommand("delete").getExecutor();
            deleteCommand.performMapDeletion(player, pendingMap);
        } else {
            player.sendMessage(ChatColor.RED + "§cFehler§7 beim Ausführen der §cLöschung§7!");
        }

        return true;
    }
}
