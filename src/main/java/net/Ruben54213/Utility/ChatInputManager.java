package net.Ruben54213.Utility;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatInputManager {

    private static final Set<UUID> playersWaitingForInput = new HashSet<>();

    public static void addPlayerWaitingForInput(UUID playerUUID) {
        playersWaitingForInput.add(playerUUID);
    }

    public static void removePlayerWaitingForInput(UUID playerUUID) {
        playersWaitingForInput.remove(playerUUID);
    }

    public static boolean isPlayerWaitingForInput(UUID playerUUID) {
        return playersWaitingForInput.contains(playerUUID);
    }

    /**
     * Get all players currently waiting for chat input
     * @return Set of player UUIDs waiting for input
     */
    public static Set<UUID> getPlayersWaitingForInput() {
        return new HashSet<>(playersWaitingForInput);
    }

    public static void clearAll() {
        playersWaitingForInput.clear();
    }
}