package me.manaki.plugin.farms.history;

import com.google.common.collect.Lists;
import me.manaki.plugin.farms.Farms;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Histories {

    private static final List<BlockHistory> histories = Lists.newArrayList();
    private static FileConfiguration config;

    private static boolean change = false;

    public static void load() {
        File file = new File(Farms.get().getDataFolder(), "blocks.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        histories.clear();
        for (String s : config.getStringList("blocks")) {
            histories.add(BlockHistory.parse(s));
        }
    }

    public static void checkAll() {
        for (BlockHistory h : Lists.newArrayList(histories)) {
            if (h.getExpire() > System.currentTimeMillis()) {
                Location l = h.getLocation().toLocation();
                l.getBlock().setType(h.getType());
                histories.remove(h);
            }
        }
        if (change) {
            write();
            change = false;
        }
    }

    public static void add(BlockHistory h) {
        histories.add(h);
        change = true;
    }

    public static void write() {
        config.set("blocks", histories.stream().map(hb -> hb.toString()).collect(Collectors.toList()));
        try {
            config.save(new File(Farms.get().getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
