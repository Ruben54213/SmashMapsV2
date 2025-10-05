package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class SetSpawnCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public SetSpawnCommand(SmashMapsV2 plugin) {
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

        if (!player.hasPermission("yukismash.setspawn")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }

        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "§cKonnte die Welt nicht bestimmen."));
            return true;
        }

        File spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(spawnFile);

        cfg.set("spawn.world", world.getName());
        cfg.set("spawn.x", loc.getX());
        cfg.set("spawn.y", loc.getY());
        cfg.set("spawn.z", loc.getZ());
        cfg.set("spawn.yaw", loc.getYaw());
        cfg.set("spawn.pitch", loc.getPitch());

        try {
            cfg.save(spawnFile);
            String msg = String.format("§7Spawn gesetzt in §e%s §7bei §e%.2f§7/§e%.2f§7/§e%.2f §7(§eyaw§7=§e%.1f§7, §epitch§7=§e%.1f§7)",
                    world.getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save spawn.yml: " + e.getMessage());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "§cFehler beim Speichern von spawn.yml!"));
        }

        return true;
    }
}
