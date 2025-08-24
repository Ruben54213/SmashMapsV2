package net.Ruben54213.Manager;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.bukkit.World;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MinIOManager {

    private final SmashMapsV2 plugin;
    private final MinioClient minioClient;
    private final String bucketName = "smashmaps";

    public MinIOManager(SmashMapsV2 plugin) {
        this.plugin = plugin;

        // MinIO Client konfigurieren
        this.minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("admin", "dfd!d3o4zgWHRSrgz23gred")
                .build();

        // Bucket erstellen falls nicht vorhanden
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                plugin.getLogger().info("MinIO bucket '" + bucketName + "' created successfully!");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to ensure MinIO bucket exists: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean saveMapToMinIO(SmashMap map) {
        try {
            // Erstelle temporäres ZIP-File
            File tempZipFile = createMapZipFile(map);
            if (tempZipFile == null) {
                return false;
            }

            // Upload ZIP zu MinIO
            String objectName = generateObjectName(map);
            uploadFileToMinIO(tempZipFile, objectName);

            // Upload maps.yml
            uploadMapsYmlToMinIO();

            // Temporäre Datei löschen
            tempZipFile.delete();

            plugin.getLogger().info("Map '" + map.getName() + "' successfully saved to MinIO!");
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save map to MinIO: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private File createMapZipFile(SmashMap map) {
        try {
            // Welt-Directory finden
            File worldDir = plugin.getWorldManager().getMapWorldDirectory(map);
            if (!worldDir.exists()) {
                plugin.getLogger().warning("World directory does not exist: " + worldDir.getPath());
                return null;
            }

            // Temporäres ZIP-File erstellen - nur mit ID
            File tempZipFile = new File(System.getProperty("java.io.tmpdir"),
                    "map_" + map.getId() + ".zip");

            // ZIP erstellen
            try (FileOutputStream fos = new FileOutputStream(tempZipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                zipDirectory(worldDir, worldDir.getName(), zos);
            }

            return tempZipFile;

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create ZIP file for map: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void zipDirectory(File directory, String basePath, ZipOutputStream zos) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                zipDirectory(file, basePath + "/" + file.getName(), zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(basePath + "/" + file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    private void uploadFileToMinIO(File file, String objectName) throws Exception {
        try (FileInputStream fileStream = new FileInputStream(file)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(fileStream, file.length(), -1)
                            .contentType("application/zip")
                            .build()
            );
        }
    }

    public void uploadMapsYmlToMinIO() throws Exception {
        File mapsYmlFile = new File(plugin.getDataFolder(), "maps.yml");
        if (!mapsYmlFile.exists()) {
            plugin.getLogger().warning("maps.yml file does not exist!");
            return;
        }

        // maps.yml direkt überschreiben, ohne Zeitstempel
        String objectName = "maps.yml";

        try (FileInputStream fileStream = new FileInputStream(mapsYmlFile)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(fileStream, mapsYmlFile.length(), -1)
                            .contentType("text/yaml")
                            .build()
            );
        }
    }

    private String generateObjectName(SmashMap map) {
        // Nur die ID verwenden, ohne Zeitstempel oder Name
        return "maps/" + map.getId() + ".zip";
    }
}