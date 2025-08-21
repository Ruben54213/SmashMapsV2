
package net.Ruben54213.Utility;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatInputManager {

    private static final Set<UUID> playersWaitingForInput = new HashSet<>();
    private static final Set<UUID> playersWaitingForSearchInput = new HashSet<>();

    public static void addPlayerWaitingForInput(UUID playerUUID) {
        playersWaitingForInput.add(playerUUID);
    }

    public static boolean isPlayerWaitingForInput(UUID playerUUID) {
        return playersWaitingForInput.contains(playerUUID);
    }

    public static void removePlayerWaitingForInput(UUID playerUUID) {
        playersWaitingForInput.remove(playerUUID);
    }

    /**
     * Get all players currently waiting for input
     * @return Set of player UUIDs waiting for input
     */
    public static Set<UUID> getPlayersWaitingForInput() {
        return new HashSet<>(playersWaitingForInput);
    }

    public static void addPlayerWaitingForSearchInput(UUID playerUUID) {
        playersWaitingForSearchInput.add(playerUUID);
    }

    public static boolean isPlayerWaitingForSearchInput(UUID playerUUID) {
        return playersWaitingForSearchInput.contains(playerUUID);
    }

    public static void removePlayerWaitingForSearchInput(UUID playerUUID) {
        playersWaitingForSearchInput.remove(playerUUID);
    }

    /**
     * Get all players currently waiting for search input
     * @return Set of player UUIDs waiting for search input
     */
    public static Set<UUID> getPlayersWaitingForSearchInput() {
        return new HashSet<>(playersWaitingForSearchInput);
    }
}