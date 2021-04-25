package me.manaki.plugin.farms.restrict;

import com.google.common.collect.Maps;
import org.bukkit.entity.Player;

import java.util.Map;

public class Restricts {

    private static final Map<String, RestrictData> restricts = Maps.newHashMap();

    public static boolean isInCooldown(Player player, String block) {
        return restricts.containsKey(player.getName()) && restricts.get(player.getName()).isInCooldown(block);
    }

    public static void add(Player player, String block, int amount) {
        var rd = restricts.getOrDefault(player.getName(), new RestrictData(player.getName()));
        rd.add(block, amount);
        restricts.put(player.getName(), rd);
    }

    public static int getSecondRemain(Player player, String block) {
        return restricts.get(player.getName()).getCooldownRemain(block);
    }

}
