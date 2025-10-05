package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public SpawnCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfigManager().getPrefix();

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getConfigManager().getMessage("not_player")));
            return true;
        }

        Player player = (Player) sender;
        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "§cSpawn ist nicht gesetzt."));
            return true;
        }

        player.teleport(spawn);
        player.playSound(player.getLocation(), plugin.getConfigManager().getSound("teleport"), 1.0f, 1.0f);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "§7Du wurdest zum §eSpawn §7teleportiert."));
        return true;
    }
}
