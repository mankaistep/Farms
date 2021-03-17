package me.manaki.plugin.farms.hook;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;

public class BSkyblockHook implements FHook{

    @Override
    public boolean canExploit(Block b, Player p) {
        Location l = b.getLocation();
        var im = BentoBox.getInstance().getIslandsManager();
        var pm = BentoBox.getInstance().getPlayersManager();

        var isl = im.getIslandAt(l);
        if (!isl.isPresent()) return true;

        var isp = im.getIsland(p.getWorld(), pm.getUser(p.getUniqueId()));
        return isl.get() == isp;
    }

}
