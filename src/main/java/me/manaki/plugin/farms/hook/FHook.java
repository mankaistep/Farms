package me.manaki.plugin.farms.hook;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface FHook {

    public boolean canExploit(Block b, Player p);

}
