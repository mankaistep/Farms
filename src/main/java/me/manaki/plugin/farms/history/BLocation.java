package me.manaki.plugin.farms.history;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class BLocation {

    private String world;
    private double x;
    private double y;
    private double z;

    public BLocation(String world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String toString() {
        return this.world + ":" + this.x + ":" + this.y + ":" + this.z;
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(this.world), x, y, z);
    }

    public static BLocation parse(String s) {
        var world = s.split(":")[0];
        var x = Double.parseDouble(s.split(":")[1]);
        var y = Double.parseDouble(s.split(":")[2]);
        var z = Double.parseDouble(s.split(":")[3]);

        return new BLocation(world, x, y, z);
    }

}
