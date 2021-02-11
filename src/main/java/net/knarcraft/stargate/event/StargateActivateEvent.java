package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StargateActivateEvent extends StargateEvent {

    private final Player player;
    private ArrayList<String> destinations;
    private String destination;

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public StargateActivateEvent(Portal portal, Player player, ArrayList<String> destinations, String destination) {
        super("StargatActivateEvent", portal);

        this.player = player;
        this.destinations = destinations;
        this.destination = destination;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<String> getDestinations() {
        return destinations;
    }

    public void setDestinations(ArrayList<String> destinations) {
        this.destinations = destinations;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

}
