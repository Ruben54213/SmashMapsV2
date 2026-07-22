package net.Ruben54213.Manager;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

/**
 * Verwaltet persönliche Anzeige von Positions-Markern und persistiert Änderungen über den MapManager in der maps.yml.
 * - Enderauge toggelt dauerhafte Anzeige (bis zum erneuten Klick)
 * - Schild zeigt nur während es gehalten wird (und nur im Bearbeitungsmodus via Listener)
 *
 * Verwendet ItemDisplay/TextDisplay (statt ArmorStand-Tricks) für die Marker,
 * da diese speziell für genau diesen Zweck existieren und garantiert vom Client
 * gerendert werden (kein Invisible/Marker-Flag-Fallstrick).
 */
public class PositionDisplayManager {

    private final SmashMapsV2 plugin;

    // Scoreboard-Tag zur eindeutigen Kennzeichnung unserer Marker/Hologramme
    private static final String MARKER_TAG = "smv2_marker";

    // Sichtbare Marker pro Spieler
    private final Map<UUID, List<Entity>> visibleMarkers = new HashMap<>();

    // Toggle-Status des Enderauges pro Spieler
    private final Set<UUID> eyeToggle = new HashSet<>();

    public PositionDisplayManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
    }

    // Toggle über Enderauge
    public void togglePositions(Player player) {
        UUID id = player.getUniqueId();
        if (eyeToggle.contains(id)) {
            eyeToggle.remove(id);
            hidePositions(player);
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GRAY + "Positionen ausgeblendet.");
        } else {
            eyeToggle.add(id);
            showPositions(player);
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GRAY + "Positionen eingeblendet.");
        }
    }

    public boolean isEyeToggled(Player p) { return eyeToggle.contains(p.getUniqueId()); }

    // Autosteuerung beim Slot-Wechsel (Schild)
    public void handleHeldItem(Player player, boolean holdingSign) {
        if (holdingSign) {
            showPositions(player);
        } else if (!isEyeToggled(player)) {
            hidePositions(player);
        }
    }

    public void showPositions(Player player) {
        hidePositions(player); // Safety

        String worldName = player.getWorld().getName().replace("maps/", "");
        List<Entity> spawned = new ArrayList<>();

        try {
            // Itemspawns: Marker (Kiste) + Hologramm
            for (Location loc : plugin.getMapManager().getItemSpawns(worldName)) {
                Location base = ensureWorld(centerOfBlock(loc).add(0, 0.1, 0), player.getWorld());
                spawned.add(spawnMarker(base, new ItemStack(Material.CHEST)));
                spawned.add(spawnHologram(base.clone().add(0, 1.0, 0), ChatColor.GREEN + "" + ChatColor.BOLD + "Item"));
            }

            // Spielerspawns: Marker (Steve-Kopf) + Hologramm
            for (Location loc : plugin.getMapManager().getPlayerSpawns(worldName)) {
                Location base = ensureWorld(centerOfBlock(loc).add(0, 0.1, 0), player.getWorld());
                spawned.add(spawnMarker(base, new ItemStack(Material.PLAYER_HEAD)));
                spawned.add(spawnHologram(base.clone().add(0, 1.0, 0), ChatColor.AQUA + "" + ChatColor.BOLD + "Spawn"));
            }

            // Center: Marker (Beacon) + Hologramm
            Location center = plugin.getMapManager().getCenter(worldName);
            if (center != null) {
                Location base = ensureWorld(centerOfBlock(center).add(0, 0.1, 0), player.getWorld());
                spawned.add(spawnMarker(base, new ItemStack(Material.BEACON)));
                spawned.add(spawnHologram(base.clone().add(0, 1.0, 0), ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Center"));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Anzeigen der Positionen für " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Sichtbarkeit festlegen
        for (Entity e : spawned) {
            // Besitzer explizit zeigen (failsafe für Clients)
            player.showEntity(plugin, e);
            // Für alle anderen verstecken
            for (Player other : player.getWorld().getPlayers()) {
                if (!other.getUniqueId().equals(player.getUniqueId())) {
                    other.hideEntity(plugin, e);
                }
            }
        }
        visibleMarkers.put(player.getUniqueId(), spawned);
    }

    public void hidePositions(Player player) {
        List<Entity> list = visibleMarkers.remove(player.getUniqueId());
        if (list != null) {
            for (Entity e : list) e.remove();
        }
    }

    private Location centerOfBlock(Location loc) {
        return new Location(loc.getWorld(),
                loc.getBlockX() + 0.5,
                loc.getBlockY(),
                loc.getBlockZ() + 0.5);
    }

    private Location ensureWorld(Location loc, World fallback) {
        if (loc.getWorld() == null) {
            return new Location(fallback, loc.getX(), loc.getY(), loc.getZ());
        }
        return loc;
    }

    private TextDisplay spawnHologram(Location loc, String text) {
        return loc.getWorld().spawn(loc, TextDisplay.class, td -> {
            td.setText(text);
            td.setBillboard(Display.Billboard.CENTER);
            td.setSeeThrough(true);
            td.setShadowed(false);
            td.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            try { td.addScoreboardTag(MARKER_TAG); } catch (Throwable ignored) {}
        });
    }

    private ItemDisplay spawnMarker(Location loc, ItemStack displayItem) {
        return loc.getWorld().spawn(loc, ItemDisplay.class, id -> {
            id.setItemStack(displayItem);
            id.setBillboard(Display.Billboard.CENTER);
            id.setTransformation(new Transformation(
                    new Vector3f(0f, 0f, 0f),
                    new AxisAngle4f(0f, 0f, 0f, 1f),
                    new Vector3f(0.6f, 0.6f, 0.6f),
                    new AxisAngle4f(0f, 0f, 0f, 1f)
            ));
            try { id.addScoreboardTag(MARKER_TAG); } catch (Throwable ignored) {}
        });
    }

    // Entfernen nächstgelegener Position beim Linksklick mit dem Schild
    public void removeNearestPosition(Player player) {
        String worldName = player.getWorld().getName().replace("maps/", "");
        Location here = player.getLocation();

        Location nearest = null;
        String type = null;

        for (Location l : plugin.getMapManager().getItemSpawns(worldName)) {
            if (nearest == null || l.distanceSquared(here) < nearest.distanceSquared(here)) { nearest = l; type = "item"; }
        }
        for (Location l : plugin.getMapManager().getPlayerSpawns(worldName)) {
            if (nearest == null || l.distanceSquared(here) < nearest.distanceSquared(here)) { nearest = l; type = "spawn"; }
        }
        Location c = plugin.getMapManager().getCenter(worldName);
        if (c != null && (nearest == null || c.distanceSquared(here) < nearest.distanceSquared(here))) { nearest = c; type = "center"; }

        if (nearest == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Keine Position in der Nähe gefunden.");
            return;
        }

        boolean changed = false;
        if ("item".equals(type)) changed = plugin.getMapManager().removeItemSpawn(worldName, nearest);
        if ("spawn".equals(type)) changed = plugin.getMapManager().removePlayerSpawn(worldName, nearest);
        if ("center".equals(type)) { plugin.getMapManager().clearCenter(worldName); changed = true; }

        if (changed) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Position entfernt.");
        }
        hidePositions(player);
        boolean holdingSign = plugin.getItemManager().isPositionsSetItem(player.getInventory().getItemInMainHand());
        if (isEyeToggled(player) || holdingSign) showPositions(player);

        // Scoreboards aktualisieren
        plugin.getScoreboardManager().updateAllScoreboards();

        // Async Upload
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try { plugin.getMinIOManager().uploadMapsYmlToMinIO(); } catch (Exception ignored) {}
        });
    }

    // Setter für GUI "Position setzen"
    public void addItemSpawn(String worldName, Location loc, Player p) {
        boolean ok = plugin.getMapManager().addItemSpawn(worldName, loc);
        p.closeInventory();
        if (ok) {
            p.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GREEN + "Itemspawn gesetzt.");
            handleHeldItem(p, true); // wenn Schild in der Hand, direkt zeigen
            plugin.getScoreboardManager().updateAllScoreboards();
        } else {
            p.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Maximal 30 Itemspawns erlaubt.");
        }
    }

    public void addPlayerSpawn(String worldName, Location loc, Player p) {
        boolean ok = plugin.getMapManager().addPlayerSpawn(worldName, loc);
        p.closeInventory();
        if (ok) {
            p.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GREEN + "Spielerspawn gesetzt.");
            handleHeldItem(p, true);
            plugin.getScoreboardManager().updateAllScoreboards();
        } else {
            p.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.RED + "Maximal 30 Spielerspawns erlaubt.");
        }
    }

    public void setCenter(String worldName, Location loc, Player p) {
        plugin.getMapManager().setCenter(worldName, loc);
        p.closeInventory();
        p.sendMessage(plugin.getConfigManager().getPrefix() + ChatColor.GREEN + "Center gesetzt.");
        handleHeldItem(p, true);
        plugin.getScoreboardManager().updateAllScoreboards();
    }

    public void cleanupPlayer(Player player) {
        hidePositions(player);
    }

    public void resetPlayer(Player player) {
        hidePositions(player);
        eyeToggle.remove(player.getUniqueId());
    }

    // Startup safety: remove ALL eigenen Marker/Hologramme in ALL worlds
    public void cleanupAllMarkersOnStartup() {
        // Clear internal state first (in case of /reload with players online later)
        visibleMarkers.clear();
        eyeToggle.clear();

        // Immediate pass
        for (World world : Bukkit.getWorlds()) {
            removeAllMarkerEntities(world);
        }

        // Delayed follow-up to catch entities that appeared shortly after startup
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                removeAllMarkerEntities(world);
            }
        }, 100L);
    }

    private void removeAllMarkerEntities(World world) {
        for (Entity e : world.getEntitiesByClass(ItemDisplay.class)) {
            if (shouldRemove(e)) e.remove();
        }
        for (Entity e : world.getEntitiesByClass(TextDisplay.class)) {
            if (shouldRemove(e)) e.remove();
        }
        // Aufräumen alter ArmorStand-Marker aus früheren Plugin-Versionen
        for (Entity e : world.getEntitiesByClass(org.bukkit.entity.ArmorStand.class)) {
            try {
                if (e.getScoreboardTags().contains(MARKER_TAG)) e.remove();
            } catch (Throwable ignored) {}
        }
    }

    public void cleanupWorld(World world) {
        removeAllMarkerEntities(world);
    }

    public void cleanupChunk(Chunk chunk) {
        for (Entity e : chunk.getEntities()) {
            if ((e instanceof ItemDisplay || e instanceof TextDisplay) && shouldRemove(e)) {
                e.remove();
            }
        }
    }

    private boolean shouldRemove(Entity e) {
        try {
            if (e.getScoreboardTags().contains(MARKER_TAG)) return true;
        } catch (Throwable ignored) {}

        // Match our known hologram texts without color codes (Fallback für alte Marker)
        try {
            if (e instanceof TextDisplay) {
                String text = ((TextDisplay) e).getText();
                if (text != null) {
                    String plain = ChatColor.stripColor(text);
                    if (plain != null && (plain.contains("Item") || plain.contains("Spawn") || plain.contains("Center"))) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }
}
