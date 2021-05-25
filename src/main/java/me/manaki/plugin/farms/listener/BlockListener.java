package me.manaki.plugin.farms.listener;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftAgeable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BlockListener implements Listener {

    private final Set<Block> pendings;

    private final int MAX_BREAK_COUNT = 20;
    private final Map<Block, Integer> cropBreakCounts;


    public BlockListener() {
        this.pendings = Sets.newHashSet();
        this.cropBreakCounts = Maps.newHashMap();
    }

    /*
    1. Disable minecraft durability
    2. Farms durability check
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

        // Pending
        if (pendings.contains(b)) {
            e.setCancelled(true);
            return;
        }

        // History
        if (Histories.inWaiting(b)) {
            e.setCancelled(true);
            return;
        }

        if (!Configs.getALlBlockTypes().contains(type.name())) return;
        if (Configs.getFarmRestricts().containsKey(p.getWorld().getName()) && !p.hasPermission("farms.admin") && Restricts.isInCooldown(p, type.name())) {
            p.sendMessage("§cĐợi " + Restricts.getSecondRemain(p, type.name()) + " giây để khai thác tiếp");
            e.setCancelled(true);
            return;
        }

        // Check hook
        if (Farms.get().getHook() != null && !Farms.get().getHook().canExploit(b, p)) {
            p.sendMessage("§cKhông thể khai thác ở đây!");
            return;
        }
        boolean success = true;

        // Remove drop
        e.setDropItems(false);

        // Check age
        if (b.getType() != Material.SUGAR_CANE && b.getType() != Material.CACTUS && b.getBlockData() instanceof Ageable) {
            var ab = (Ageable) b.getBlockData();
            if (ab.getAge() < ab.getMaximumAge()) {
//                p.sendMessage("§cChỉ có thể khai thác khi cây lớn tối đa");
                success = false;
            }
        }

        // Crop break count
        if (success && (b.getType() == Material.SUGAR_CANE || b.getType() == Material.CACTUS || b.getBlockData() instanceof Ageable)) {
            int count = cropBreakCounts.getOrDefault(b, 1);
            if (count != MAX_BREAK_COUNT) {
                int percent = count * 100 / MAX_BREAK_COUNT;
                p.sendActionBar("§e§lKhai thác " + Configs.getTrans(b.getType()) + ": §6§l" + percent + "%");
                if (b.getType() == Material.CACTUS) count += 5;
                else count++;
                cropBreakCounts.put(b, count);
                e.setCancelled(true);
                return;
            }
            p.sendActionBar("§e§lKhai thác thành công");
            cropBreakCounts.remove(b);
        }

        // Check item
        ItemStack is = p.getInventory().getItemInMainHand();
        String tid = Tools.read(is);
        if (success && (is.getType() == Material.AIR || tid == null)) {
            p.sendMessage("§cPhải dùng công cụ để có thể thu hoạch/khai thác khối này!");
            success = false;
        }

        // Check specific world
        if (Configs.isSpecificWorld(p.getWorld().getName())) {
            var tools = Configs.getTools(p.getWorld().getName());
            if (!tools.contains(tid)) return;
        }

        // Check tool
        if (success && !Tools.isRightTool(tid, type)) {
            p.sendMessage("§cKhông đúng loại công cụ!");
            success = false;
        }

        // Durability
        int dur = Tools.getDur(is);
        if (success && dur <= 0) {
            p.sendMessage("§cĐộ bền bằng 0, không thể khai thác");
            p.sendMessage("§cDùng Đá sửa chữa để sửa công cụ!");
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            success = false;
        }

        e.setCancelled(true);

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
        if (Configs.isWorldRespawn(p.getWorld().getName()) && Configs.isRespawn(b.getType())) {
            Tasks.async(() -> {
                Histories.add(new BlockHistory(type, new BLocation(b.getWorld().getName(), b.getX(), b.getY(), b.getZ()), System.currentTimeMillis() + Configs.RESPAWN_SECONDS * 1000));
            });
        }

        // Set and save
        if (b.getType() != Material.SUGAR_CANE && b.getType() != Material.CACTUS && b.getBlockData() instanceof Ageable) {
            pendings.add(b);
            Bukkit.getScheduler().runTask(Farms.get(), () -> {
                var ab = (Ageable) b.getBlockData();
                ab.setAge(1);
                b.setBlockData(ab);
                pendings.remove(b);
            });
        }
        else {
            Material m = Configs.getDefault(type);
            b.setType(m);
        }

        // Drop
        if (!canDrop) return;

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
        Bukkit.getPluginManager().callEvent(new PlayerFarmHarvestEvent(p, type.name()));
        Restricts.add(p, type.name(), 1);
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
