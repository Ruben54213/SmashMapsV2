package net.Ruben54213.Tasks;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Utility.ChatInputManager;
import net.Ruben54213.Manager.IconSelectionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class TitleReminderTask extends BukkitRunnable {

    private final SmashMapsV2 plugin;

    public TitleReminderTask(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Check for players waiting for chat input
        for (UUID playerUUID : ChatInputManager.getPlayersWaitingForInput()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                String nameTitle = plugin.getConfigManager().getMessage("map_name_title");
                String nameSubtitle = plugin.getConfigManager().getMessage("map_name_subtitle");
                player.sendTitle(nameTitle, nameSubtitle, 10, 60, 10);
            }
        }

        // Check for players waiting for icon selection
        for (UUID playerUUID : IconSelectionManager.getPlayersWaitingForIcon()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                String iconTitle = plugin.getConfigManager().getMessage("icon_selection_title");
                String iconSubtitle = plugin.getConfigManager().getMessage("icon_selection_subtitle");
                player.sendTitle(iconTitle, iconSubtitle, 10, 60, 10);
            }
        }
    }
}