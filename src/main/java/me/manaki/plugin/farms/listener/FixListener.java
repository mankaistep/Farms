package me.manaki.plugin.farms.listener;

import me.manaki.plugin.farms.config.Configs;
import me.manaki.plugin.farms.tool.Tools;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class FixListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        var p = (Player) e.getWhoClicked();
        var current = e.getCurrentItem();
        var cursor = e.getCursor();

        if (!Tools.isFixItem(cursor)) return;
        var tid = Tools.read(current);
        if (tid == null) return;
        e.setCancelled(true);

        var t = Configs.getTool(tid);
        var newdur = Math.min(t.getDurability(), Configs.FIX_BONUS + Tools.getDur(current));
        Tools.setDur(current, newdur);
        Tools.updateLore(tid, current);

        cursor.setType(Material.AIR);

        p.updateInventory();
    }

}
