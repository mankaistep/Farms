package me.manaki.plugin.farms;

import me.manaki.plugin.farms.command.FarmCommand;
import me.manaki.plugin.farms.config.Configs;
import me.manaki.plugin.farms.history.Histories;
import me.manaki.plugin.farms.hook.BSkyblockHook;
import me.manaki.plugin.farms.hook.FHook;
import me.manaki.plugin.farms.listener.BlockListener;
import me.manaki.plugin.farms.listener.FixListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Farms extends JavaPlugin {

    private FileConfiguration config;
    private FHook hook;

    @Override
    public void onEnable() {
        // Reload config
        this.reloadConfig();

        // Listener
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new FixListener(), this);

        // Command
        this.getCommand("farms").setExecutor(new FarmCommand());

        // Task check
        Tasks.async(Histories::checkAll, 0, 5);

        // Check hook
        if (Bukkit.getPluginManager().isPluginEnabled("BentoBox")) {
            this.hook = new BSkyblockHook();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Histories.checkAll();
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
        Configs.reload(config);
        Histories.load();
    }

    public static Farms get() {
        return JavaPlugin.getPlugin(Farms.class);
    }

    public FHook getHook() {
        return this.hook;
    }
}
