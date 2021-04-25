package me.manaki.plugin.farms.tool;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.manaki.plugin.farms.Farms;
import me.manaki.plugin.farms.config.Configs;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;

public class Tools {

    private static final String DUR_LINE = "§aĐộ bền: §f";

    public static String read(ItemStack is) {
        if (is == null || is.getType() == Material.AIR) return null;
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return null;

        if (meta.getPersistentDataContainer().has(getToolKey(), PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(getToolKey(), PersistentDataType.STRING);
        }

        return null;
    }

    public static boolean isFixItem(ItemStack is) {
        if (is == null || is.getType() == Material.AIR) return false;
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(getFixerKey(), PersistentDataType.STRING);
    }

    public static int getDur(ItemStack is) {
        String id = read(is);
        assert id != null : "[Farms] NULL item can't has durability stat!";

        ItemMeta meta = is.getItemMeta();
        if (meta == null) return 0;

        if (meta.getPersistentDataContainer().has(getDurKey(), PersistentDataType.INTEGER)) {
            return meta.getPersistentDataContainer().get(getDurKey(), PersistentDataType.INTEGER);
        }

        return -1;
    }

    public static void setDur(ItemStack is, int dur) {
        String id = read(is);
        assert id != null : "[Farms] NULL item can't has durability stat!";

        ItemMeta meta = is.getItemMeta();
        meta.getPersistentDataContainer().set(getDurKey(), PersistentDataType.INTEGER, dur);
        is.setItemMeta(meta);

    }

    public static boolean isRightTool(String id, Material m) {
        if (Configs.getTool(id) == null) return false;
        return Configs.getTool(id).getBlocks().contains(m.name());
    }

    public static ItemStack buildTool(String id) {
        Tool t = Configs.getTool(id);
        assert t != null;

        ItemStack is = new ItemStack(t.getTextureType());

        ItemMeta meta = is.getItemMeta();
        if (t.getName() != null) meta.setDisplayName(t.getName());
        meta.setCustomModelData(t.getTextureData());
        meta.getPersistentDataContainer().set(getToolKey(), PersistentDataType.STRING, id);
        is.setItemMeta(meta);

        setDur(is, t.getDurability());
        updateLore(id, is);

        return is;
    }

    public static void updateLore(String id, ItemStack is) {
        Tool t = Configs.getTool(id);
        assert t != null;

        ItemMeta meta = is.getItemMeta();
        List<String> lore = Lists.newArrayList();
        lore.add(DUR_LINE + getDur(is) + "/" + t.getDurability());
        lore.add("§aKhai thác được: ");
        Set<String> set = Sets.newLinkedHashSet();

        for (String type : t.getBlocks()) {
            set.add("§f  " + Configs.getTrans(Material.valueOf(type.toUpperCase())));
        }
        lore.addAll(set);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        is.setItemMeta(meta);
    }

    public static NamespacedKey getToolKey() {
        return new NamespacedKey(Farms.get(), "tool");
    }

    public static NamespacedKey getDurKey() {
        return new NamespacedKey(Farms.get(), "dur");
    }

    public static NamespacedKey getFixerKey() {
        return new NamespacedKey(Farms.get(), "fixer");
    }

}
