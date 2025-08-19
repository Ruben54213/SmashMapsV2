package net.Ruben54213.Manager;

import net.Ruben54213.SmashMapsV2;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

public class NavigationItemManager {

    private final SmashMapsV2 plugin;
    private final NamespacedKey exitMapKey;
    private final NamespacedKey lobbyKey;

    // Custom head texture for "Karte Verlassen"
    private static final String EXIT_MAP_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";

    public NavigationItemManager(SmashMapsV2 plugin) {
        this.plugin = plugin;
        this.exitMapKey = new NamespacedKey(plugin, "exit_map_item");
        this.lobbyKey = new NamespacedKey(plugin, "lobby_item");
    }

    /**
     * Give the appropriate navigation item to player (slot 8 = last hotbar slot)
     */
    public void giveNavigationItem(Player player) {
        ItemStack navigationItem;

        if (plugin.getWorldManager().isMapWorld(player.getWorld())) {
            // Player is in a map - give exit map item
            navigationItem = createExitMapItem();
        } else {
            // Player is not in a map - give lobby item
            navigationItem = createLobbyItem();
        }

        // Set item in last hotbar slot (slot 8)
        player.getInventory().setItem(8, navigationItem);
    }

    /**
     * Create the "Karte Verlassen" custom head item
     */
    private ItemStack createExitMapItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta != null) {
            // Set display name
            meta.setDisplayName(plugin.getConfigManager().getMessage("exit_map_item_name"));

            // Set lore
            meta.setLore(Arrays.asList(
                    plugin.getConfigManager().getMessage("exit_map_item_lore1"),
                    plugin.getConfigManager().getMessage("exit_map_item_lore2")
            ));

            // Set custom texture
            setCustomTexture(meta, EXIT_MAP_TEXTURE);

            // Add identifier for click detection
            meta.getPersistentDataContainer().set(exitMapKey, PersistentDataType.BOOLEAN, true);

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Create the "Zurück zur Lobby" slime ball item
     */
    private ItemStack createLobbyItem() {
        ItemStack item = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set display name
            meta.setDisplayName(plugin.getConfigManager().getMessage("lobby_item_name"));

            // Set lore
            meta.setLore(Arrays.asList(
                    plugin.getConfigManager().getMessage("lobby_item_lore1"),
                    plugin.getConfigManager().getMessage("lobby_item_lore2")
            ));

            // Add identifier for click detection
            meta.getPersistentDataContainer().set(lobbyKey, PersistentDataType.BOOLEAN, true);

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Set custom texture for skull item
     */
    private void setCustomTexture(SkullMeta skullMeta, String textureValue) {
        try {
            // Create a random UUID for the profile
            UUID profileId = UUID.randomUUID();

            // Create player profile
            PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "CustomHead");
            PlayerTextures textures = profile.getTextures();

            // Decode base64 texture and create URL
            String decodedTexture = new String(java.util.Base64.getDecoder().decode(textureValue));
            String textureUrl = extractUrlFromJson(decodedTexture);

            if (textureUrl != null) {
                textures.setSkin(new URL(textureUrl));
                profile.setTextures(textures);
                skullMeta.setOwnerProfile(profile);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not set custom texture for navigation item: " + e.getMessage());
        }
    }

    /**
     * Extract URL from texture JSON
     */
    private String extractUrlFromJson(String json) {
        // Simple JSON parsing to extract URL
        if (json.contains("\"url\":\"")) {
            int start = json.indexOf("\"url\":\"") + 7;
            int end = json.indexOf("\"", start);
            if (end > start) {
                return json.substring(start, end);
            }
        }
        return null;
    }

    /**
     * Check if item is the exit map item
     */
    public boolean isExitMapItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(exitMapKey, PersistentDataType.BOOLEAN);
    }

    /**
     * Check if item is the lobby item
     */
    public boolean isLobbyItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(lobbyKey, PersistentDataType.BOOLEAN);
    }

    /**
     * Handle navigation item click
     */
    public void handleNavigationItemClick(Player player, ItemStack item) {
        if (isExitMapItem(item)) {
            // Execute /spawn command
            player.performCommand("spawn");

            // Send message
            String message = plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("map_exited");
            player.sendMessage(message);

            // Play sound
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("teleport"), 1.0f, 1.0f);

        } else if (isLobbyItem(item)) {
            // Send player to BungeeCord server "Lobby-1"
            sendPlayerToBungeeServer(player, "Lobby-1");

            // Send message
            String message = plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("lobby_teleported");
            player.sendMessage(message);

            // Play sound
            player.playSound(player.getLocation(), plugin.getConfigManager().getSound("teleport"), 1.0f, 1.0f);
        }
    }

    /**
     * Send player to a BungeeCord server
     */
    private void sendPlayerToBungeeServer(Player player, String serverName) {
        try {
            // Create BungeeCord plugin message
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream out = new java.io.DataOutputStream(b);

            out.writeUTF("Connect");
            out.writeUTF(serverName);

            // Send the plugin message
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());

            plugin.getLogger().info("Sending player " + player.getName() + " to server: " + serverName);

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to send player " + player.getName() + " to BungeeCord server " + serverName + ": " + e.getMessage());

            // Fallback: try /lobby command if BungeeCord message fails
            player.performCommand("lobby");
        }
    }
}