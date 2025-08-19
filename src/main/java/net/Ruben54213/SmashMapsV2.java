package net.Ruben54213;

import net.Ruben54213.Manager.ConfigManager;
import net.Ruben54213.Manager.NavigationItemManager;
import net.Ruben54213.Listeners.GuiListener;
import net.Ruben54213.Listeners.ItemListener;
import net.Ruben54213.Listeners.NavigationListener;
import net.Ruben54213.Listeners.PlayerChatListener;
import net.Ruben54213.Listeners.WorldProtectionListener;
import net.Ruben54213.Listeners.IconDropListener;
import net.Ruben54213.Manager.ItemManager;
import net.Ruben54213.Manager.MapManager;
import net.Ruben54213.Manager.WorldManager;
import net.Ruben54213.Commands.ReloadCommand;
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

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.mapManager = new MapManager(this);
        this.worldManager = new WorldManager(this);
        this.itemManager = new ItemManager(this);
        this.navigationItemManager = new NavigationItemManager(this);

        // Register BungeeCord plugin messaging channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        // Give navigation items to online players immediately (no delay needed)
        for (Player player : Bukkit.getOnlinePlayers()) {
            navigationItemManager.giveNavigationItem(player);
        }

        getLogger().info("SmashMapsV2 has been enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister BungeeCord plugin messaging channel
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");

        getLogger().info("SmashMapsV2 has been disabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new IconDropListener(this), this);
        getServer().getPluginManager().registerEvents(new NavigationListener(this), this);
        getServer().getPluginManager().registerEvents(itemManager, this);
    }

    private void registerCommands() {
        getCommand("smashmaps").setExecutor(new ReloadCommand(this));
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
}