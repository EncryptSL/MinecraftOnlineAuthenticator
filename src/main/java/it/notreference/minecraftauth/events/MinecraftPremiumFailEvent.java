package it.notreference.minecraftauth.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */
public class MinecraftPremiumFailEvent extends Event {

    private final Player p;
    private final String pName;
    private final String serverHash;
    private final String reason;

    /**
     *
     *
     *
     * @param p
     * @param pName
     * @param serverHash
     * @param reason
     */
    public MinecraftPremiumFailEvent(Player p, String pName, String serverHash, String reason) {
        this.p = p;
        this.pName = pName;
        this.serverHash = serverHash;
        this.reason = reason;
    }

    /**
     *
     * Returns the player.
     *
     * @return
     */
    public Player getPlayer() {
        return p;
    }

    /**
     *
     * Returns the playerName.
     *
     * @return
     */
    public String getPlayerName() {
        return pName;
    }


    /**
     *
     * Returns the serverHash (empty).
     *
     * @return
     */
    public String getServerHash() {
        return serverHash;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }

    /**
     *
     * Returns the kick fail reason.
     *
     * @return
     */
    public String getKickReason() {
        return reason;
    }
}
