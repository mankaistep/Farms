package me.manaki.plugin.farms.history;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.manaki.plugin.farms.Farms;
import me.manaki.plugin.farms.Tasks;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class Histories {

    private static final Set<BlockHistory> histories = Sets.newHashSet();
    private static FileConfiguration config;

    private static boolean change = false;

    public static boolean inWaiting(Block b) {
        for (BlockHistory h : histories) {
            if (h.getLocation().getWorld().equalsIgnoreCase(b.getWorld().getName())
                    && h.getLocation().toLocation().getBlockX() == b.getX()
                    && h.getLocation().toLocation().getBlockY() == b.getY()
                    && h.getLocation().toLocation().getBlockZ() == b.getZ()) return true;
        }
        return false;
    }

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

        synchronized (histories) {
            histories.clear();
            for (String s : config.getStringList("blocks")) {
                histories.add(BlockHistory.parse(s));
            }
        }
    }

    public static void checkAll() {
        synchronized (histories) {
            for (BlockHistory h : Lists.newArrayList(histories)) {
                if (h.getExpire() < System.currentTimeMillis()) {
                    Location l = h.getLocation().toLocation();
                    histories.remove(h);
                    Tasks.sync(() -> {
                        l.getBlock().setType(h.getType());
                    });
                    change = true;
                }
            }
            if (change) {
                write();
                change = false;
            }
        }
    }

    public static void add(BlockHistory h) {
        synchronized (histories) {
            histories.add(h);
        }
        change = true;
    }

    public static void write() {
        synchronized (histories) {
            config.set("blocks", histories.stream().map(BlockHistory::toString).collect(Collectors.toList()));
        }
        try {
            config.save(new File(Farms.get().getDataFolder(), "blocks.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
