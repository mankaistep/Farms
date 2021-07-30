package me.manaki.plugin.farms.listener;

import com.google.common.collect.Sets;
import me.manaki.plugin.farms.Farms;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.Set;

public class BlockListener implements Listener {

    private final Set<Block> pendings;

    public BlockListener() {
        this.pendings = Sets.newHashSet();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        // Check world
        Player p = e.getPlayer();
        if (!Configs.isWorld(p.getWorld().getName())) return;

        // Check block
        Block b = e.getBlock();
        Material type = b.getType();

        // Type check
        if (!Configs.getALlBlockTypes().contains(type.name())) return;

        // Hook check
        if (Farms.get().getHook() != null && !Farms.get().getHook().canExploit(b, p)) {
            p.sendMessage("§cKhông thể khai thác ở đây!");
            return;
        }

        // Pending
        if (pendings.contains(b)) {
            e.setCancelled(true);
            return;
        }

        // History
        else if (Histories.inWaiting(b)) {
            e.setCancelled(true);
            return;
        }

        else if (Configs.getFarmRestricts().containsKey(p.getWorld().getName()) && !p.hasPermission("farms.admin") && Restricts.isInCooldown(p, type.name())) {
            p.sendMessage("§cĐợi " + Restricts.getSecondRemain(p, type.name()) + " giây để khai thác tiếp");
            e.setCancelled(true);
            return;
        }

        boolean success = true;

        // Check item
        ItemStack is = p.getInventory().getItemInMainHand();
        String tid = Tools.read(is);

        // Type check
        if (is.getType() == Material.AIR || tid == null) {
            if (!p.isSneaking()) {
                p.sendMessage("§cPhải dùng công cụ để có thể thu hoạch/khai thác khối này!");
                p.sendMessage("§c§lGiữ shift lúc phá để phá block (sẽ không rơi ra gì)");
                e.setCancelled(true);
                return;
            }
            else {
                p.sendMessage("§c§lGiữ shift lúc phá để phá block (sẽ không rơi ra gì)");
            }
            success = false;
        }

        // Check tool
        if (success && !Tools.isRightTool(tid, type)) {
            p.sendMessage("§cKhông đúng loại công cụ!");
            success = false;
        }

        // World check
        if (Configs.isSpecificWorld(p.getWorld().getName())) {
            var tools = Configs.getTools(p.getWorld().getName());
            if (!tools.contains(tid)) return;
        }

        // Durability
        int dur = Tools.getDur(is);
        if (success && dur <= 0) {
            p.sendMessage("§cĐộ bền bằng 0, không thể khai thác");;
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            success = false;
        }

        // Update durability
        if (success && is.getType() != Material.AIR && tid != null) {
            Tasks.sync(() -> {
                Tools.setDur(is, dur - 1);
                Tools.updateLore(tid, is);
                p.updateInventory();
            });
        }

        var canDrop = success;

        // Save if world is claimed
        if (Configs.isWorldRespawn(p.getWorld().getName())) {
            var time = Configs.getRespawnTime(p.getWorld().getName());
            Tasks.async(() -> {
                Histories.add(new BlockHistory(type, new BLocation(b.getWorld().getName(), b.getX(), b.getY(), b.getZ()), System.currentTimeMillis() + time * 1000L));
            });
        }

        // Drop
        if (!canDrop) return;

        Tasks.sync(() -> {
            if (e.isCancelled()) {
                b.breakNaturally();
                if (type.name().contains("ORE") || type == Material.STONE) {
                    Tasks.sync(() -> {
                        b.setType(Material.COBBLESTONE);
                    }, 10);
                }
            }
        });

        // Event
        Bukkit.getPluginManager().callEvent(new PlayerFarmHarvestEvent(p, type.name()));
        Restricts.add(p, type.name(), 1);
    }

    /*
    Convert log to wood
     */
    @EventHandler
    public void onPlayerPickup(ItemSpawnEvent e) {
        var drop = e.getEntity();
        var type = drop.getItemStack().getType();
        if (type.name().contains("LOG")) {
            var newtype = Material.valueOf(type.name().replace("_LOG", "_WOOD"));
            drop.getItemStack().setType(newtype);
        }
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

    public static Location getDropLocation(Player player, Block b) {
        var center = b.getLocation().add(0.5, 0.5, 0.5);
        var r = center.add(player.getLocation().add(0, 1, 0).subtract(center).toVector().normalize().multiply(1.5f).toLocation(player.getWorld()));
//        if (r.distanceSquared(center) > player.getLocation().distanceSquared(center)) {
//            return player.getLocation().add(player.getLocation().getDirection().multiply(0.2f)).add(0, 1, 0);
//        }
        return r;
    }

    private static double cal(double value) {
        return value / Math.abs(value);
    }

}
