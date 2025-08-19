package net.Ruben54213.Models;

import org.bukkit.Material;
import java.util.UUID;

public class SmashMap {

    private final int id;
    private final UUID ownerUUID;
    private final String name;
    private final String worldName;
    private final long creationTime;
    private Material iconMaterial;
    private String iconDisplayName;

    public SmashMap(int id, UUID ownerUUID, String name, String worldName) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.name = name;
        this.worldName = worldName;
        this.creationTime = System.currentTimeMillis();
        this.iconMaterial = Material.GRASS_BLOCK; // Default icon
        this.iconDisplayName = null;
    }

    public SmashMap(int id, UUID ownerUUID, String name, String worldName, Material iconMaterial, String iconDisplayName) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.name = name;
        this.worldName = worldName;
        this.creationTime = System.currentTimeMillis();
        this.iconMaterial = iconMaterial != null ? iconMaterial : Material.GRASS_BLOCK;
        this.iconDisplayName = iconDisplayName;
    }

    public int getId() {
        return id;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public void setIconMaterial(Material iconMaterial) {
        this.iconMaterial = iconMaterial;
    }

    public String getIconDisplayName() {
        return iconDisplayName;
    }

    public void setIconDisplayName(String iconDisplayName) {
        this.iconDisplayName = iconDisplayName;
    }

    @Override
    public String toString() {
        return "SmashMap{" +
                "id=" + id +
                ", ownerUUID=" + ownerUUID +
                ", name='" + name + '\'' +
                ", worldName='" + worldName + '\'' +
                ", creationTime=" + creationTime +
                ", iconMaterial=" + iconMaterial +
                ", iconDisplayName='" + iconDisplayName + '\'' +
                '}';
    }
}