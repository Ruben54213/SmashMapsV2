package net.Ruben54213.Commands;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class RcCommand implements CommandExecutor {

    private final SmashMapsV2 plugin;

    public RcCommand(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isPlayer = sender instanceof Player;
        String prefix = plugin.getConfigManager().getPrefix();

        // Permission-Check für Spieler
        if (isPlayer) {
            Player player = (Player) sender;
            if (!player.hasPermission("yuki.rc.command")) {
                player.sendMessage(prefix + "§cDu hast keine Berechtigung, diesen Befehl zu verwenden.");
                return true;
            }

            player.sendMessage(prefix + "Führe §e/iareload §7aus...");
            player.performCommand("iareload");
        } else {
            sender.sendMessage(prefix + "Führe /iareload aus...");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iareload");
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (isPlayer) {
                Player player = (Player) sender;
                player.sendMessage(prefix + "Führe §e/iazip §7aus...");
                player.performCommand("iazip");
            } else {
                sender.sendMessage(prefix + "Führe /iazip aus...");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iazip");
            }
        }, 40L); // 2 Sekunden Verzögerung

        return true;
    }
}
