package me.manaki.plugin.farms.listener;

import me.manaki.plugin.farms.Tasks;
import me.manaki.plugin.farms.config.Configs;
import me.manaki.plugin.farms.tool.Tool;
import me.manaki.plugin.farms.tool.Tools;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class BlockListener implements Listener {

    /*
    1. Disable minecraft durability
    2. Farms durablity check
    3. Farm block check
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        // Check world
        Player p = e.getPlayer();
        if (!Configs.isWorld(p.getWorld().getName())) return;

        // Check block
        Block b = e.getBlock();
        Material type = b.getType();
        if (Configs.getALlBlockTypes().contains(type.name())) e.setCancelled(true);
        else return;

        // Check item
        ItemStack is = p.getInventory().getItemInMainHand();
        String tid = Tools.read(is);
        if (is.getType() == Material.AIR || tid == null) {
            p.sendMessage("§cPhải dùng công cụ để có thể thu hoạch/khai thác khối này!");
            return;
        }

        // Check tool
        if (!Tools.isRightTool(tid, type)) {
            p.sendMessage("§cKhông đúng loại công cụ!");
            return;
        }

        // Durability
        int dur = Tools.getDur(is);
        if (dur <= 0) {
            p.sendMessage("§cĐộ bền bằng 0, không thể khai thác");
            p.sendMessage("§cDùng Đá sửa chữa để sửa công cụ!");
            return;
        }

        e.setCancelled(true);

        // Update durability
        Tasks.async(() -> {
            Tools.setDur(is, dur - 1);
            Tools.updateLore(tid, is);
            p.updateInventory();
        });

        Tasks.sync(() -> {

            // Destroy packet
            if (Configs.isDestroyPacket(type)) {
                p.sendBlockChange(b.getLocation(), Bukkit.createBlockData(Material.AIR));
                System.out.println("Bum");
            }
            else {
                // Default
                Material m = Configs.getDefault(type);
                b.setType(m);
                System.out.println(m.name() + " ?");
            }

            // Drop
            ItemStack drop = null;
            if (rate(Configs.RARE_MATERIAL_CHANCE)) {
                drop = Configs.getRareMaterial(type);
            }
            else drop = Configs.getMaterial(type);

            String name = drop.getItemMeta().hasDisplayName() ? drop.getItemMeta().getDisplayName() : null;
            Item i = p.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), drop);
            if (name != null) {
                i.setCustomName(name);
                i.setCustomNameVisible(true);
            }
        });

    }

    /*
    Disable special interact with configed blocks
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();
        if (b == null) return;
        Material type = b.getType();

        ItemStack is = p.getInventory().getItemInMainHand();
        String tid = Tools.read(is);
        if (Configs.getALlBlockTypes().contains(type.name()) && (tid == null || !Tools.isRightTool(tid, type))) {
            e.setCancelled(true);
            p.sendMessage("§cDùng đúng công cụ để khai thác!");
        }
    }

    public static boolean rate(double chance) {
        if (chance >= 100)
            return true;
        double rate = chance * 100;
        int random = new Random().nextInt(10000);
        return random < rate;
    }

}
