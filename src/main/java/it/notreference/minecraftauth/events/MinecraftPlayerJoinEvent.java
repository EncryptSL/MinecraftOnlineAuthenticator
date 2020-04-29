package it.notreference.minecraftauth.events;

import it.notreference.minecraftauth.protocol.MinecraftOnlineRequest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */

public class MinecraftPlayerJoinEvent extends Event implements Cancellable {

    private final MinecraftOnlineRequest request;
    private final Player player;
    private boolean prevent = false;
    private List<String> javaPlugins;
    private final String name;
    private String kickMsg = "&cDisconnected due to event cancel.";
    private boolean cancel = false;

    public MinecraftPlayerJoinEvent(MinecraftOnlineRequest request, Player player, String name) {
        super(true);
        this.request = request;
        this.player = player;
        this.name = name;
        javaPlugins = new ArrayList<String>();
    }

    /**
     *
     * Sets the cancel message.
     *
     * @param kickMsg
     */
    public void setCancelMessage(String kickMsg) {
        this.kickMsg = kickMsg;
    }

    /**
     *
     * Returns the cancel message.
     *
     * @return
     */
    public String getKickMsg() {
        return kickMsg;
    }


    private static HandlerList list = new HandlerList();

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

    /**
     *
     * Return if cancelled.
     *
     * @return
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     *
     * Cancel the event.
     *
     * @param b
     */
    @Override
    public void setCancelled(boolean b) {
     cancel = b;
    }

    /**
     *
     * Returns the request packet.
     *
     * @return
     */
    public MinecraftOnlineRequest getRequest() {
        return request;
    }

    /**
     *
     * Sets the login request to online mode.
     *
     */
    public void setOnlineMode() throws Exception {
    request.setOnlineMode();
    }

    /**
     *
     * Prevents that this event can be handled by the main listener.
     *
     */
    public void preventMainEvents(JavaPlugin yourPlugin) {
        prevent = true;
        javaPlugins.add(yourPlugin.getName());
    }


    /**
     *
     * Returns if prevent.
     *
     * @return
     */
    public boolean isPrevent() {
    return  prevent;
    }

    /**
     *
     * Returns the plugin that cancelled the event.
     *
     * @return
     */
    public List<String> getPreventPluginName() {

        return javaPlugins;

    }

    /**
     *
     * Returns the player name.
     *
     * @return
     */
    public Player getPlayer() {
        return player;
    }

    /**
     *
     * Returns the player name. [At ENCRYPTION status the player name isn't avaliable with player.getName(); (when setOnlineMode())]
     *
     * @return
     */
    public String getPlayerName() {
        return name;
    }
}
