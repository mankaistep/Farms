package me.manaki.plugin.farms.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.manaki.plugin.farms.tool.Tool;
import me.manaki.plugin.farms.tool.Tools;
import me.manaki.plugin.shops.storage.ItemStorage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Configs {

    public static double RARE_MATERIAL_CHANCE = 0;
    public static String FIX_ITEM;
    public static int FIX_BONUS;
    public static int RESPAWN_SECONDS;

    private static List<String> ALLOW_WORLDS = Lists.newArrayList();
    private static List<String> RESPAWN_WORLDS = Lists.newArrayList();
    private static List<String> RESPAWNS = Lists.newArrayList();

    private final static Map<String, String> TRANS = Maps.newHashMap();
    private final static Map<String, String> MATERIALS = Maps.newHashMap();
    private final static Map<String, String> DEFAULTS = Maps.newHashMap();
    private final static Map<String, Tool> TOOLS = Maps.newHashMap();
    private final static Map<String, String> RARE_MATERIALS = Maps.newHashMap();


    public static void reload(FileConfiguration config) {
        RARE_MATERIAL_CHANCE = config.getDouble("rare-material.chance");
        FIX_ITEM = config.getString("fix.item");
        FIX_BONUS = config.getInt("fix.bonus");
        RESPAWN_SECONDS = config.getInt("respawn-seconds");

        // Destroy packet
        RESPAWNS = config.getStringList("respawns");

        // Worlds
        ALLOW_WORLDS = config.getStringList("allow-worlds");

        // Worlds
        RESPAWN_WORLDS = config.getStringList("respawn-worlds");

        // Trans
        TRANS.clear();
        for (String id : config.getConfigurationSection("trans").getKeys(false)) {
            TRANS.put(id.toUpperCase(), config.getString("trans." + id));
        }

        // Materials
        MATERIALS.clear();
        for (String id : config.getConfigurationSection("material").getKeys(false)) {
            MATERIALS.put(id.toUpperCase(), config.getString("material." + id));
        }

        // Rare Materials
        RARE_MATERIALS.clear();
        for (String id : config.getConfigurationSection("rare-material.item").getKeys(false)) {
            RARE_MATERIALS.put(id.toUpperCase(), config.getString("rare-material.item." + id));
        }

        // Defaults
        DEFAULTS.clear();
        for (String id : config.getConfigurationSection("default").getKeys(false)) {
            DEFAULTS.put(id.toUpperCase(), config.getString("default." + id).toUpperCase());
        }

        // Tools
        TOOLS.clear();
        for (String id : config.getConfigurationSection("tool").getKeys(false)) {
            String texture = config.getString("tool." + id + ".texture");
            Material textureType = Material.valueOf(texture.split(" ")[0].toUpperCase());
            int textureData = Integer.parseInt(texture.split(" ")[1].toUpperCase());
            int durability = config.getInt("tool." + id + ".durability");
            List<String> blocks = config.getStringList("tool." + id + ".blocks");
            String name = config.getString("tool." + id + ".name");
            if (name != null) name = name.replace("&", "§");
            TOOLS.put(id, new Tool(id, name, textureType, textureData, durability, blocks));
        }
    }

    public static String getTrans(Material m) {
        if (TRANS.containsKey(m.name())) return TRANS.get(m.name());
        return m.name();
    }

    public static ItemStack getMaterial(Material m) {
        var id = MATERIALS.getOrDefault(m.name(), m.name());
        return ItemStorage.get(id);
    }

    public static ItemStack getRareMaterial(Material m) {
        var id = RARE_MATERIALS.getOrDefault(m.name(), m.name());
        return ItemStorage.get(id);
    }

    public static Material getDefault(Material m) {
        return Material.valueOf(DEFAULTS.getOrDefault(m.name(), Material.AIR.name()));
    }

    public static Tool getTool(String id) {
        return TOOLS.getOrDefault(id, null);
    }

    public static ItemStack getFixItem() {
        ItemStack is = ItemStorage.get(FIX_ITEM);
        ItemMeta meta = is.getItemMeta();
        meta.getPersistentDataContainer().set(Tools.getFixerKey(), PersistentDataType.STRING, "fixer");
        is.setItemMeta(meta);

        return is;
    }

    public static Set<String> getALlBlockTypes() {
        Set<String> set = Sets.newHashSet();
        for (Tool tool : TOOLS.values()) {
            set.addAll(tool.getBlocks());
        }
        return set;
    }

    public static Map<String, Tool> getTools() {
        return TOOLS;
    }

    public static boolean isWorld(String w) {
        return ALLOW_WORLDS.contains(w);
    }

//    public static boolean isRespawn(Material m) {
//        return RESPAWNS.contains(m.name());
//    }

    public static boolean isWorldRespawn(String w) {
        return RESPAWN_WORLDS.contains(w);
    }

}
