package net.Ruben54213.Manager;

import net.Ruben54213.Models.SmashMap;

import java.util.*;

public class IconSelectionManager {

    private static final Map<UUID, SmashMap> playersWaitingForIcon = new HashMap<>();

    public static void addPlayerWaitingForIcon(UUID playerUUID, SmashMap map) {
        playersWaitingForIcon.put(playerUUID, map);
    }

    public static void removePlayerWaitingForIcon(UUID playerUUID) {
        playersWaitingForIcon.remove(playerUUID);
    }

    public static boolean isPlayerWaitingForIcon(UUID playerUUID) {
        return playersWaitingForIcon.containsKey(playerUUID);
    }

    public static SmashMap getPlayerWaitingMap(UUID playerUUID) {
        return playersWaitingForIcon.get(playerUUID);
    }

    /**
     * Get all players currently waiting for icon selection
     * @return Set of player UUIDs waiting for icon selection
     */
    public static Set<UUID> getPlayersWaitingForIcon() {
        return new HashSet<>(playersWaitingForIcon.keySet());
    }

    public static void clearAll() {
        playersWaitingForIcon.clear();
    }
}