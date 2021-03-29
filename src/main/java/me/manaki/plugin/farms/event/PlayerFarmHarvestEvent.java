package me.manaki.plugin.farms.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerFarmHarvestEvent extends PlayerEvent {

    private String id;

    public PlayerFarmHarvestEvent(Player player, String id) {
        super(player);
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    /*
     *  Required
     */

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }

}
