package net.Ruben54213.Generator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class VoidWorldGenerator extends ChunkGenerator {

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Komplett leer - NICHTS generieren
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Komplett leer - KEINE Surface generieren
    }

    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Komplett leer - KEIN Bedrock generieren
    }

    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Komplett leer - KEINE Caves generieren
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false; // Explizit KEINE Noise-Generierung
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false; // Explizit KEINE Surface-Generierung
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return false; // Explizit KEIN Bedrock
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false; // Explizit KEINE Caves
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false; // Explizit KEINE Dekorationen
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false; // Explizit KEINE Mobs
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false; // Explizit KEINE Strukturen
    }

    // Hauptmethode für Chunk-Generierung - garantiert komplett leer
    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
        // Erstelle komplett leeren Chunk
        ChunkData chunkData = createChunkData(world);

        // Explizit alle Blöcke auf AIR setzen um 100% sicher zu gehen
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                    chunkData.setBlock(x, y, z, Material.AIR);
                }
            }
        }

        return chunkData;
    }
}