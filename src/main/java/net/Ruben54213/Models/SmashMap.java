
package net.Ruben54213.Models;

import org.bukkit.Material;

import java.util.UUID;

public class SmashMap {
    private final int id;
    private final UUID ownerUUID;
    private String name;
    private final String worldName;
    private final long creationTime;

    // Icon settings
    private Material iconMaterial;
    private String iconDisplayName;

    // Approved status
    private boolean approved;

    public SmashMap(int id, UUID ownerUUID, String name, String worldName) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.name = name;
        this.worldName = worldName;
        this.creationTime = System.currentTimeMillis();
        this.iconMaterial = Material.GRASS_BLOCK; // Default
        this.iconDisplayName = null;
        this.approved = false; // Default not approved
    }

    public SmashMap(int id, UUID ownerUUID, String name, String worldName, Material iconMaterial, String iconDisplayName) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.name = name;
        this.worldName = worldName;
        this.creationTime = System.currentTimeMillis();
        this.iconMaterial = iconMaterial;
        this.iconDisplayName = iconDisplayName;
        this.approved = false; // Default not approved
    }

    public SmashMap(int id, UUID ownerUUID, String name, String worldName, Material iconMaterial, String iconDisplayName, boolean approved) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.name = name;
        this.worldName = worldName;
        this.creationTime = System.currentTimeMillis();
        this.iconMaterial = iconMaterial;
        this.iconDisplayName = iconDisplayName;
        this.approved = approved;
    }

    // Getters
    public int getId() { return id; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getName() { return name; }
    public String getWorldName() { return worldName; }
    public long getCreationTime() { return creationTime; }
    public Material getIconMaterial() { return iconMaterial; }
    public String getIconDisplayName() { return iconDisplayName; }
    public boolean isApproved() { return approved; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setIconMaterial(Material iconMaterial) { this.iconMaterial = iconMaterial; }
    public void setIconDisplayName(String iconDisplayName) { this.iconDisplayName = iconDisplayName; }
    public void setApproved(boolean approved) { this.approved = approved; }
}