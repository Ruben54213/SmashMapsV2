package net.Ruben54213.Tasks;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardUpdateTask extends BukkitRunnable {

    private final SmashMapsV2 plugin;

    public ScoreboardUpdateTask(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Update all active scoreboards
        plugin.getScoreboardManager().updateAllScoreboards();
    }
}