package net.Ruben54213;

import net.Ruben54213.Manager.ConfigManager;
import net.Ruben54213.Manager.NavigationItemManager;
import net.Ruben54213.Manager.ScoreboardManager;
import net.Ruben54213.Manager.MinIOManager;
import net.Ruben54213.Listeners.GuiListener;
import net.Ruben54213.Listeners.ItemListener;
import net.Ruben54213.Listeners.NavigationListener;
import net.Ruben54213.Listeners.PlayerChatListener;
import net.Ruben54213.Listeners.WorldProtectionListener;
import net.Ruben54213.Listeners.IconDropListener;
import net.Ruben54213.Listeners.ScoreboardListener;
import net.Ruben54213.Manager.ItemManager;
import net.Ruben54213.Manager.MapManager;
import net.Ruben54213.Manager.WorldManager;
import net.Ruben54213.Commands.ReloadCommand;
import net.Ruben54213.Commands.ApproveCommand;
import net.Ruben54213.Commands.SaveCommand;
import net.Ruben54213.Commands.CreateMapCommand;
import net.Ruben54213.Commands.MapsCommand;
import net.Ruben54213.Tasks.TitleReminderTask;
import net.Ruben54213.Tasks.ScoreboardUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmashMapsV2 extends JavaPlugin {

    private static SmashMapsV2 instance;
    private ConfigManager configManager;
    private MapManager mapManager;
    private WorldManager worldManager;
    private ItemManager itemManager;
    private NavigationItemManager navigationItemManager;
    private ScoreboardManager scoreboardManager;
    private GuiListener guiListener;
    private MinIOManager minIOManager;
    private TitleReminderTask titleReminderTask;
    private ScoreboardUpdateTask scoreboardUpdateTask;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.mapManager = new MapManager(this);
        this.worldManager = new WorldManager(this);
        this.itemManager = new ItemManager(this);
        this.navigationItemManager = new NavigationItemManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.minIOManager = new MinIOManager(this);

        // Register BungeeCord plugin messaging channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        // Start tasks
        startTitleReminderTask();
        startScoreboardUpdateTask();

        // Give navigation items and scoreboards to online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            navigationItemManager.giveNavigationItem(player);
            // Show scoreboard with small delay to ensure player is fully loaded
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.isOnline()) {
                    scoreboardManager.showScoreboard(player);
                }
            }, 10L);
        }

        getLogger().info("SmashMapsV2 has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel tasks
        if (titleReminderTask != null) {
            titleReminderTask.cancel();
        }
        if (scoreboardUpdateTask != null) {
            scoreboardUpdateTask.cancel();
        }

        // Clean up scoreboards
        if (scoreboardManager != null) {
            scoreboardManager.cleanup();
        }

        // Unregister BungeeCord plugin messaging channel
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");

        getLogger().info("SmashMapsV2 has been disabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        this.guiListener = new GuiListener(this);
        getServer().getPluginManager().registerEvents(this.guiListener, this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new IconDropListener(this), this);
        getServer().getPluginManager().registerEvents(new NavigationListener(this), this);
        getServer().getPluginManager().registerEvents(new ScoreboardListener(this), this);
        getServer().getPluginManager().registerEvents(itemManager, this);
    }

    private void registerCommands() {
        getCommand("smashmaps").setExecutor(new ReloadCommand(this));
        getCommand("approve").setExecutor(new ApproveCommand(this));
        getCommand("save").setExecutor(new SaveCommand(this));
        getCommand("createmap").setExecutor(new CreateMapCommand(this));
        getCommand("maps").setExecutor(new MapsCommand(this));
    }

    private void startTitleReminderTask() {
        this.titleReminderTask = new TitleReminderTask(this);
        this.titleReminderTask.runTaskTimer(this, 100L, 100L);
    }

    private void startScoreboardUpdateTask() {
        this.scoreboardUpdateTask = new ScoreboardUpdateTask(this);
        // Update scoreboards every 5 seconds (100 ticks)
        this.scoreboardUpdateTask.runTaskTimer(this, 100L, 100L);
    }

    public static SmashMapsV2 getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public NavigationItemManager getNavigationItemManager() {
        return navigationItemManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public GuiListener getGuiListener() {
        return guiListener;
    }

    public MinIOManager getMinIOManager() {
        return minIOManager;
    }
}