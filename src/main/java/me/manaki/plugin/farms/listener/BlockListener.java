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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

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

        // Save if world is claimed
        if (Configs.isWorldRespawn(p.getWorld().getName())) {
            var time = Configs.getRespawnTime(p.getWorld().getName());
            Tasks.async(() -> {
                Histories.add(new BlockHistory(type, new BLocation(b.getWorld().getName(), b.getX(), b.getY(), b.getZ()), System.currentTimeMillis() + time * 1000L));
            });
        }

        Tasks.sync(() -> {
            if (e.isCancelled()) {
                if (b.getType() == Material.REDSTONE_ORE) {
                    b.setType(Material.AIR);
                    b.getLocation().getWorld().dropItemNaturally(b.getLocation().clone().add(0.5, 0.5, 0.5), new ItemStack(Material.REDSTONE));
                }
                else if (b.getType() == Material.LAPIS_ORE) {
                    b.setType(Material.AIR);
                    b.getLocation().getWorld().dropItemNaturally(b.getLocation().clone().add(0.5, 0.5, 0.5), new ItemStack(Material.LAPIS_LAZULI));
                }
                else b.breakNaturally();

                // Subtract durability
                Tasks.sync(() -> {
                    var is = e.getPlayer().getInventory().getItemInMainHand();
                    if (is.getItemMeta() instanceof Damageable) {
                        var meta = (Damageable) is.getItemMeta();
                        if (meta.getDamage() >= is.getType().getMaxDurability()) {
                            p.getInventory().setItemInMainHand(null);
                            p.updateInventory();
                            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                        }
                        else {
                            meta.setDamage(meta.getDamage() + 1);
                            is.setItemMeta((ItemMeta) meta);
                        }
                    }
                });

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
        else if (type.name().equals("IRON_ORE")) {
            drop.getItemStack().setType(Material.IRON_INGOT);
        }
        else if (type.name().equals("GOLD_ORE")) {
            drop.getItemStack().setType(Material.GOLD_INGOT);
        }
    }

}
