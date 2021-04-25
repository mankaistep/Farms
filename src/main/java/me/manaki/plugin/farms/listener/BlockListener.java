package me.manaki.plugin.farms.listener;

import me.manaki.plugin.farms.Farms;
import me.manaki.plugin.farms.ItemStackManager;
import me.manaki.plugin.farms.Tasks;
import me.manaki.plugin.farms.config.Configs;
import me.manaki.plugin.farms.event.PlayerFarmHarvestEvent;
import me.manaki.plugin.farms.history.BLocation;
import me.manaki.plugin.farms.history.BlockHistory;
import me.manaki.plugin.farms.history.Histories;
import me.manaki.plugin.farms.restrict.Restricts;
import me.manaki.plugin.farms.tool.Tools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class BlockListener implements Listener {

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent e) {
        // Check world
        Block b = e.getBlock();
        Location l = b.getLocation();

        if (!Configs.isWorld(l.getWorld().getName())) return;
        if (!Configs.isWorldRespawn(l.getWorld().getName())) return;

        e.setCancelled(true);
    }

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

        // Check mod break
        if (p.hasPermission("farms.admin") && p.getInventory().getItemInMainHand().getType() == Material.AIR) return;

        // Check restrict
        Block b = e.getBlock();
        Material type = b.getType();
        if (Configs.getFarmRestricts().containsKey(p.getWorld().getName())) {
            if (Restricts.isInCooldown(p, type.name())) {
                p.sendMessage("§cNơi này giới hạn số lượng tài nguyên có thể khai thác");
                p.sendMessage("§cKhai thác tiếp sau " + Restricts.getSecondRemain(p, type.name()) + " giây");
                e.setCancelled(true);
                return;
            }
            Restricts.add(p, type.name(), 1);
        }

        // Check block
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

        boolean success = true;

        // Remove drop
        e.setDropItems(false);

        // Save if world is claimed
        if (Configs.isWorldRespawn(p.getWorld().getName())) {
            Tasks.async(() -> {
                Histories.add(new BlockHistory(type, new BLocation(b.getWorld().getName(), b.getX(), b.getY(), b.getZ()), System.currentTimeMillis() + Configs.getRespawnTime(p.getWorld().getName()) * 1000L));
            });
        }


        // Check age
        if (b.getType() != Material.SUGAR_CANE && b.getBlockData() instanceof Ageable) {
            var ab = (Ageable) b.getBlockData();
            if (ab.getAge() < ab.getMaximumAge()) {
                p.sendMessage("§cChỉ có thể khai thác khi cây lớn tối đa");
                success = false;
            }
        }

        // Check item
        ItemStack is = p.getInventory().getItemInMainHand();
        String tid = Tools.read(is);
        if (is.getType() == Material.AIR || tid == null) {
            p.sendMessage("§cPhải dùng công cụ để có thể thu hoạch/khai thác khối này!");
            success = false;
        }

        // Check specific world
        if (Configs.isSpecificWorld(p.getWorld().getName())) {
            var tools = Configs.getTools(p.getWorld().getName());
            if (!tools.contains(tid)) {
                p.sendMessage("§cBởi một lý do nào đó, bạn không thể khai thác");
                return;
            }
        }

        // Check tool
        if (!Tools.isRightTool(tid, type)) {
            p.sendMessage("§cKhông đúng loại công cụ!");
            success = false;
        }

        // Durability
        int dur = Tools.getDur(is);
        if (tid != null && dur <= 0) {
            p.sendMessage("§cĐộ bền bằng 0, không thể khai thác");
            p.sendMessage("§cDùng Đá sửa chữa để sửa công cụ!");
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            success = false;
        }

        Material m = Configs.getDefault(type);
        if (m != Material.AIR) e.setCancelled(true);

        // Update durability
        if (tid != null) {
            Tasks.async(() -> {
                Tools.setDur(is, dur - 1);
                Tools.updateLore(tid, is);
                p.updateInventory();
            });
        }

        var canDrop = success;
        Tasks.sync(() -> {
            // Set and save
            b.setType(m);

            // Drop
            if (!canDrop) return;

            ItemStack drop = null;
            if (rate(Configs.RARE_MATERIAL_CHANCE)) {
                drop = Configs.getRareMaterial(type);
            }
            else drop = Configs.getMaterial(type);

            String name = new ItemStackManager(drop).getName();
            Item i = p.getWorld().dropItem(getDropLocation(p, b.getLocation()), drop);
            i.setVelocity(p.getLocation().subtract(i.getLocation()).toVector().normalize().multiply(0.1));
            if (name != null) {
                i.setCustomName(name);
                i.setCustomNameVisible(true);
            }

            // Event
            Bukkit.getPluginManager().callEvent(new PlayerFarmHarvestEvent(p, type.name()));
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

    public static Location getDropLocation(Player player, Location l) {
        var center = l.getBlock().getLocation().clone().add(0.5, 0.5, 0.5);
        if (player.getLocation().getBlockX() == center.getBlockX()) {
            var d = player.getLocation().getZ() - center.getZ();
            center.setZ(center.getZ() + (d / Math.abs(d)) * 0.7);
        }
        else {
            var d = player.getLocation().getX() - center.getX();
            center.setX(center.getX() + (d / Math.abs(d)) * 0.7);
        }

        return center;
    }

}
