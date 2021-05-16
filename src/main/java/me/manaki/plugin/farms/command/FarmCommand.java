package me.manaki.plugin.farms.command;

import me.manaki.plugin.farms.Farms;
import me.manaki.plugin.farms.config.Configs;
import me.manaki.plugin.farms.tool.Tools;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class FarmCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        try {

            if (args[0].equalsIgnoreCase("reload")) {
                Farms.get().reloadConfig();
                sender.sendMessage("§aAll done!");
            }

            else if (args[0].equalsIgnoreCase("getfixer")) {
                var player = (Player) sender;
                var is = Configs.getFixItem();
                player.getInventory().addItem(is);
            }

            else if (args[0].equalsIgnoreCase("gettool")) {
                var player = (Player) sender;
                var id = args[1];
                var is = Tools.buildTool(id);

                player.getInventory().addItem(is);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            sendHelp(sender);
        }

        return false;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage("§a/farms test");
        sender.sendMessage("§a/farms getfixer");
        sender.sendMessage("§a/farms gettool <id>");
    }
}
