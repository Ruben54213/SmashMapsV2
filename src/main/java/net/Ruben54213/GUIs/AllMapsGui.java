package net.Ruben54213.GUIs;

import net.Ruben54213.SmashMapsV2;
import net.Ruben54213.Models.SmashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AllMapsGui {

    public enum FilterType {
        APPROVED, ALL, MY_MAPS
    }

    public enum SortType {
        NAME, AUTHOR, NEWEST
    }

    private final SmashMapsV2 plugin;
    private final Player player;
    private final Inventory inventory;
    private FilterType currentFilter = FilterType.ALL;
    private SortType currentSort = SortType.NAME;
    private String searchQuery = "";
    private int currentPage = 0;
    private final int MAPS_PER_PAGE = 28;

    public AllMapsGui(SmashMapsV2 plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        String title = plugin.getConfigManager().getAllMapsTitle();
        this.inventory = Bukkit.createInventory(null, plugin.getConfigManager().getAllMapsSize(), title);
        setupGui();
    }

    public AllMapsGui(SmashMapsV2 plugin, Player player, String searchQuery) {
        this.plugin = plugin;
        this.player = player;
        this.searchQuery = searchQuery;
        String title = plugin.getConfigManager().getAllMapsTitle() + " §8- §e" + searchQuery;
        this.inventory = Bukkit.createInventory(null, plugin.getConfigManager().getAllMapsSize(), title);
        setupGui();
    }

    private void setupGui() {
        fillWithGlass();
        addNavigationItems();
        displayMaps();
    }

    private void fillWithGlass() {
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glassPane);
        }
    }

    private void addNavigationItems() {
        if (currentPage > 0) {
            inventory.setItem(45, createLeftArrow());
        }

        inventory.setItem(48, createSearchBook());
        inventory.setItem(49, createFilterItem());
        inventory.setItem(50, createSortItem());

        List<SmashMap> filteredMaps = getFilteredMaps();
        int totalPages = (int) Math.ceil((double) filteredMaps.size() / MAPS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            inventory.setItem(53, createRightArrow());
        }
    }

    private ItemStack createLeftArrow() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l§nVorherige Seite");
            setSkullTexture(meta, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==");
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "all_maps_nav"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    "prev_page");
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private ItemStack createRightArrow() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l§nNächste Seite");
            setSkullTexture(meta, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "all_maps_nav"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    "next_page");
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private void setSkullTexture(SkullMeta meta, String texture) {
        try {
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");

            Object gameProfile = gameProfileClass.getDeclaredConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), null);

            Object property = propertyClass.getDeclaredConstructor(String.class, String.class)
                    .newInstance("textures", texture);

            Method getProperties = gameProfileClass.getDeclaredMethod("getProperties");
            Object properties = getProperties.invoke(gameProfile);
            Method put = properties.getClass().getDeclaredMethod("put", Object.class, Object.class);
            put.invoke(properties, "textures", property);

            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, gameProfile);
        } catch (Exception e) {
            OfflinePlayer fallback = Bukkit.getOfflinePlayer("MHF_ArrowLeft");
            meta.setOwningPlayer(fallback);
        }
    }

    private ItemStack createSearchBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lSuche");
            meta.setLore(Arrays.asList(
                    "§7Klicke um nach einer Map zu suchen!",
                    searchQuery.isEmpty() ? "§8Keine aktuelle Suche" : "§7Aktuelle Suche: §e" + searchQuery
            ));
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "all_maps_nav"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    "search");
            book.setItemMeta(meta);
        }
        return book;
    }

    private ItemStack createFilterItem() {
        ItemStack hopper = new ItemStack(Material.HOPPER);
        ItemMeta meta = hopper.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lFilter");
            meta.setLore(Arrays.asList(
                    "§7Aktueller Filter: §e" + getFilterName(),
                    "§7Klicke um Filter zu ändern!"
            ));
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "all_maps_nav"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    "filter");
            hopper.setItemMeta(meta);
        }
        return hopper;
    }

    private ItemStack createSortItem() {
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = netherStar.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lSortierung");
            meta.setLore(Arrays.asList(
                    "§7Aktuelle Sortierung: §e" + getSortName(),
                    "§7Klicke um Sortierung zu ändern!"
            ));
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "all_maps_nav"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    "sort");
            netherStar.setItemMeta(meta);
        }
        return netherStar;
    }

    private String getFilterName() {
        switch (currentFilter) {
            case APPROVED: return "Approved";
            case MY_MAPS: return "Meine Karten";
            default: return "Alle";
        }
    }

    private String getSortName() {
        switch (currentSort) {
            case AUTHOR: return "Author";
            case NEWEST: return "Neueste";
            default: return "Name";
        }
    }

    private void displayMaps() {
        List<SmashMap> filteredMaps = getFilteredMaps();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        int startIndex = currentPage * MAPS_PER_PAGE;
        int endIndex = Math.min(startIndex + MAPS_PER_PAGE, filteredMaps.size());

        int[] mapSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < mapSlots.length; i++) {
            SmashMap map = filteredMaps.get(i);
            ItemStack mapItem = createMapItem(map, dateFormat);
            inventory.setItem(mapSlots[slotIndex], mapItem);
            slotIndex++;
        }
    }

    private List<SmashMap> getFilteredMaps() {
        List<SmashMap> maps = new ArrayList<>();

        switch (currentFilter) {
            case APPROVED:
                maps = plugin.getMapManager().getApprovedMaps();
                break;
            case MY_MAPS:
                maps = new ArrayList<>(plugin.getMapManager().getPlayerMaps(player.getUniqueId()));
                break;
            case ALL:
            default:
                maps = plugin.getMapManager().getAllMaps();
                break;
        }

        if (!searchQuery.isEmpty()) {
            maps = maps.stream()
                    .filter(map -> {
                        // Search in both the original name and display name (stripped of color codes for search)
                        String mapName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', map.getName())).toLowerCase();
                        String displayName = map.getIconDisplayName() != null ?
                                ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', map.getIconDisplayName())).toLowerCase() :
                                mapName;
                        String query = searchQuery.toLowerCase();
                        return mapName.contains(query) || displayName.contains(query);
                    })
                    .collect(Collectors.toList());
        }

        switch (currentSort) {
            case NAME:
                maps.sort((m1, m2) -> {
                    String name1 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', m1.getName()));
                    String name2 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', m2.getName()));
                    return String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
                });
                break;
            case AUTHOR:
                maps.sort((m1, m2) -> {
                    String author1 = Bukkit.getOfflinePlayer(m1.getOwnerUUID()).getName();
                    String author2 = Bukkit.getOfflinePlayer(m2.getOwnerUUID()).getName();
                    if (author1 == null) author1 = "Unknown";
                    if (author2 == null) author2 = "Unknown";
                    return String.CASE_INSENSITIVE_ORDER.compare(author1, author2);
                });
                break;
            case NEWEST:
                maps.sort((m1, m2) -> Long.compare(m2.getCreationTime(), m1.getCreationTime()));
                break;
        }

        return maps;
    }

    private ItemStack createMapItem(SmashMap map, SimpleDateFormat dateFormat) {
        ItemStack item = new ItemStack(map.getIconMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Get display name with color support - no italic formatting
            String displayName;
            if (map.getIconDisplayName() != null && !map.getIconDisplayName().isEmpty()) {
                displayName = ChatColor.translateAlternateColorCodes('&', map.getIconDisplayName());
            } else {
                displayName = ChatColor.translateAlternateColorCodes('&', map.getName());
            }

            String ownerName = Bukkit.getOfflinePlayer(map.getOwnerUUID()).getName();
            if (ownerName == null) ownerName = "Unknown";

            // Set the display name with status indicator - reset formatting to prevent italic
            meta.setDisplayName("§r" + displayName + " §8(§c✘§8) §7(#" + String.format("%04d", map.getId()) + "/0)");

            String formattedDate = dateFormat.format(new Date(map.getCreationTime()));
            meta.setLore(Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Typ: &aSmash"),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Ersteller: &e" + ownerName),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Status: " + (map.isApproved() ? "&aApproved" : "&cNicht approved")),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Spiele: &e?"),
                    ChatColor.translateAlternateColorCodes('&', "&8→ &7Aktualisiert: &e" + formattedDate),
                    "",
                    ChatColor.translateAlternateColorCodes('&', "&6Klicke zum Teleportieren!")
            ));

            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "map_id"),
                    org.bukkit.persistence.PersistentDataType.INTEGER,
                    map.getId());

            item.setItemMeta(meta);
        }

        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void nextPage() {
        List<SmashMap> filteredMaps = getFilteredMaps();
        int totalPages = (int) Math.ceil((double) filteredMaps.size() / MAPS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            setupGui();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            setupGui();
        }
    }

    public void cycleFilter() {
        switch (currentFilter) {
            case ALL:
                currentFilter = FilterType.APPROVED;
                break;
            case APPROVED:
                currentFilter = FilterType.MY_MAPS;
                break;
            case MY_MAPS:
                currentFilter = FilterType.ALL;
                break;
        }
        currentPage = 0;
        setupGui();
    }

    public void cycleSort() {
        switch (currentSort) {
            case NAME:
                currentSort = SortType.AUTHOR;
                break;
            case AUTHOR:
                currentSort = SortType.NEWEST;
                break;
            case NEWEST:
                currentSort = SortType.NAME;
                break;
        }
        currentPage = 0;
        setupGui();
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
        this.currentPage = 0;
        setupGui();
    }

    public FilterType getCurrentFilter() {
        return currentFilter;
    }

    public SortType getCurrentSort() {
        return currentSort;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}