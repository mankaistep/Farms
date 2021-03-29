package me.manaki.plugin.farms.listener;

import me.manaki.plugin.farms.Farms;
import me.manaki.plugin.farms.ItemStackManager;
import me.manaki.plugin.farms.Tasks;
import me.manaki.plugin.farms.config.Configs;
import me.manaki.plugin.farms.event.PlayerFarmHarvestEvent;
import me.manaki.plugin.farms.history.BLocation;
import me.manaki.plugin.farms.history.BlockHistory;
import me.manaki.plugin.farms.history.Histories;
import me.manaki.plugin.farms.tool.Tools;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
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
        if (Histories.inWaiting(b)) {
            e.setCancelled(true);
            return;
        }
        if (!Configs.getALlBlockTypes().contains(type.name())) return;

        // Check hook
        if (Farms.get().getHook() != null && !Farms.get().getHook().canExploit(b, p)) {
            p.sendMessage("§cKhông thể khai thác ở đây!");
            return;
        }

        // Remove drop
        e.setDropItems(false);

        // Save if world is claimed
        if (Configs.isWorldRespawn(p.getWorld().getName())) {
            Tasks.async(() -> {
                Histories.add(new BlockHistory(type, new BLocation(b.getWorld().getName(), b.getX(), b.getY(), b.getZ()), System.currentTimeMillis() + Configs.RESPAWN_SECONDS * 1000));
            });
        }


        // Check age
        if (b.getType() != Material.SUGAR_CANE && b.getBlockData() instanceof Ageable) {
            var ab = (Ageable) b.getBlockData();
            if (ab.getAge() < ab.getMaximumAge()) {
                p.sendMessage("§cChỉ có thể khai thác khi cây lớn tối đa");
                return;
            }
        }

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
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
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
            // Set and save
            Material m = Configs.getDefault(type);
            b.setType(m);

            // Drop
            ItemStack drop = null;
            if (rate(Configs.RARE_MATERIAL_CHANCE)) {
                drop = Configs.getRareMaterial(type);
            }
            else drop = Configs.getMaterial(type);

            String name = new ItemStackManager(drop).getName();
            Item i = p.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), drop);
            if (name != null) {
                i.setCustomName(name);
                i.setCustomNameVisible(true);
            }

            // Event
            Bukkit.getPluginManager().callEvent(new PlayerFarmHarvestEvent(p, m.name()));
        });

    }

    /*
    Disable harvest
     */
    @EventHandler
    public void onInteract(PlayerHarvestBlockEvent e) {
        Player p = e.getPlayer();
        Block b = e.getHarvestedBlock();
        Material type = b.getType();

        ItemStack is = p.getInventory().getItemInMainHand();
        String tid = Tools.read(is);
        if (Configs.getALlBlockTypes().contains(type.name()) && (tid == null || !Tools.isRightTool(tid, type))) {
            e.getItemsHarvested().clear();
            p.sendMessage("§cPhải dùng đúng công cụ để khai thác!");
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
