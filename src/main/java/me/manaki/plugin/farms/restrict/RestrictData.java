package me.manaki.plugin.farms.restrict;

import com.google.common.collect.Maps;
import me.manaki.plugin.farms.config.Configs;

import java.util.List;
import java.util.Map;

public class RestrictData {

    private final String player;
    private final Map<String, Integer> amounts;
    private final Map<String, Long> cooldowns;

    public RestrictData(String player) {
        this.player = player;
        this.amounts = Maps.newHashMap();
        this.cooldowns = Maps.newHashMap();
    }

    public String getPlayer() {
        return player;
    }

    public Map<String, Integer> getAmounts() {
        return amounts;
    }

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    public boolean isInCooldown(String block) {
        return cooldowns.containsKey(block) && cooldowns.get(block) > System.currentTimeMillis();
    }

    public int getCooldownRemain(String block) {
        if (!isInCooldown(block)) return 0;
        return Long.valueOf((cooldowns.get(block) - System.currentTimeMillis()) / 1000).intValue() + 1;
    }

    public void add(String block, int amount) {
        if (isInCooldown(block)) return;
        int after = amounts.getOrDefault(block, 0) + amount;
        amounts.put(block, after);
        for (Map.Entry<String, List<Restrict>> e : Configs.getFarmRestricts().entrySet()) {
            var l = e.getValue();
            for (Restrict r : l) {
                if (r.getBlockType().equalsIgnoreCase(block)) {
                    int ar = r.getAmount();
                    if (after >= ar) {
                        cooldowns.put(block, System.currentTimeMillis() + r.getSeconds() * 1000L);
                        this.amounts.remove(block);
                    }
                }
            }
        }
    }

}
