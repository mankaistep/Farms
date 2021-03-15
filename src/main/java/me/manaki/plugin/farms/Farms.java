package me.manaki.plugin.farms;

import me.manaki.plugin.farms.command.FarmCommand;
import me.manaki.plugin.farms.config.Configs;
import me.manaki.plugin.farms.listener.BlockListener;
import me.manaki.plugin.farms.listener.FixListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Farms extends JavaPlugin {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Reload config
        this.reloadConfig();

        // Listener
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new FixListener(), this);

        // Command
        this.getCommand("farms").setExecutor(new FarmCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
        Configs.reload(config);
    }

    public static Farms get() {
        return JavaPlugin.getPlugin(Farms.class);
    }
}
