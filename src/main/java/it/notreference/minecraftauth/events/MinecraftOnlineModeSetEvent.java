package it.notreference.minecraftauth.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinecraftOnlineModeSetEvent extends Event {


    private final String playerName;
    private final String playerIP;

    private static HandlerList list = new HandlerList();

    public MinecraftOnlineModeSetEvent(String playerName, String playerIP) {
        this.playerName = playerName;
        this.playerIP = playerIP;
    }

    public String getPlayerIP() {
        return playerIP;
    }

    public String getPlayerName() {
        return playerName;
    }

    /**
     *
     * Returns a random HandlerList.
     *
     * @return
     */
    public static HandlerList getHandlerList() {
        return list;
    }

    /**
     *
     * Returns a random HandlerList.
     *
     * @return
     */
    @Override
    public HandlerList getHandlers() {
        return list;
    }


}
