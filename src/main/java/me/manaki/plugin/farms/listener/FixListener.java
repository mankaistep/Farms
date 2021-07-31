package me.manaki.plugin.farms.listener;

import me.manaki.plugin.farms.tool.Tools;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.meta.Damageable;

public class FixListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        var p = (Player) e.getWhoClicked();
        var current = e.getCurrentItem();
        var cursor = e.getCursor();

        if (cursor == null) return;
        if (current == null) return;
        if (!Tools.isFixItem(cursor)) return;

        e.setCancelled(true);

        if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.PLAYER) {
            p.sendMessage("§cChỉ có thể thao tác trong kho đồ của bạn");
            return;
        }

        var meta = current.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(0);
            current.setItemMeta(meta);
        }
        else {
            p.sendMessage("§cKhông thể sửa chữa đồ này");
            return;
        }

        cursor.setAmount(cursor.getAmount() - 1);

        p.sendMessage("§aSử dụng Đá sửa chữa thành công!");
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
    }

}
