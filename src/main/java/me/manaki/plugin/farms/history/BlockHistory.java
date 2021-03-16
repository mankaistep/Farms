package me.manaki.plugin.farms.history;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockHistory {

    private Material type;
    private BLocation location;
    private long expire;

    public BlockHistory(Material type, BLocation location, long expire) {
        this.type = type;
        this.location = location;
        this.expire = expire;
    }

    public Material getType() {
        return type;
    }

    public BLocation getLocation() {
        return location;
    }

    public long getExpire() {
        return expire;
    }

    @Override
    public String toString() {
        return this.type.name() + " " + this.location.toString() + " " + this.expire;
    }

    public static BlockHistory parse(String s) {
        String[] a = s.split(" ");
        Material type = Material.valueOf(a[0]);
        BLocation location = BLocation.parse(a[1]);
        long expire = Long.parseLong(a[2]);

        return new BlockHistory(type, location, expire);
    }
}
