package net.Ruben54213.Listeners;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class WeatherDisabler implements Listener {

    private final SmashMapsV2 plugin;

    public WeatherDisabler(SmashMapsV2 plugin) {
        this.plugin = plugin;

        // Beim Start alle bereits geladenen Welten konfigurieren
        for (World world : plugin.getServer().getWorlds()) {
            applyNoWeather(world);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        // Verhindere, dass Regen startet
        if (event.toWeatherState()) {
            event.setCancelled(true);
            applyNoWeather(event.getWorld());
        }
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        // Verhindere, dass Gewitter startet
        if (event.toThunderState()) {
            event.setCancelled(true);
            applyNoWeather(event.getWorld());
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        // Neue/geladene Welt direkt konfigurieren
        applyNoWeather(event.getWorld());
    }

    private void applyNoWeather(World world) {
        try {
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(0);
            world.setThunderDuration(0);
            // Langes klares Wetter setzen (Failsafe)
            world.setClearWeatherDuration(20 * 60 * 60); // 1 Stunde in Ticks
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        } catch (Throwable ignored) {
            // Fallback: Ignoriere Fehler leise, um Kompatibilität zu bewahren
        }
    }
}
