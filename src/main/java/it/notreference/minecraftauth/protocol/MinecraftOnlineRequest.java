package it.notreference.minecraftauth.protocol;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import it.notreference.minecraftauth.MinecraftOnlineAuthenticator;
import it.notreference.minecraftauth.auth.MinecraftEncryptionUtils;
import it.notreference.minecraftauth.events.MinecraftPremiumFailEvent;
import it.notreference.minecraftauth.protocol.exceptions.OnlineModeInvaildStateException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.comphenix.protocol.PacketType.Login.Client.START;
import static com.comphenix.protocol.PacketType.Login.Server.DISCONNECT;
import static com.comphenix.protocol.PacketType.Login.Server.ENCRYPTION_BEGIN;
import java.security.PublicKey;
import java.util.Random;
import java.util.UUID;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */

public class MinecraftOnlineRequest {

    private byte[] token;
    private final Random random;
    private final PacketEvent packetEvent;
    private final Player player;
    private boolean alreadyOnline;
    private boolean onlineModeCanSet = true;
    private final PublicKey key;
    private String playerName;

    public MinecraftOnlineRequest(Random random, PacketEvent packetEvent, Player player, PublicKey key, String playerName) {
        this.random = random;
        this.packetEvent = packetEvent;
        this.player = player;
        this.key = key;
        this.playerName = playerName;
        alreadyOnline = false;
    }

    /**
     *
     * Returns an empty string.
     *
     * @return
     */
    public String getServerHash() {
        return "";
    }

    /**    @deprecated   */
    @Deprecated
    public Player getPlayer() {
        return player;
    }

    /**
     *
     * Kicks the player with a message.
     *
     * @param messaggio
     * @throws Exception
     */
    public void kickPlayer(String messaggio) throws Exception {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer kick = new PacketContainer(DISCONNECT);
        kick.getChatComponents().write(0, WrappedChatComponent.fromText(messaggio.replace("&", "§")));
        try {
            protocolManager.sendServerPacket(player, kick);
        } finally {
            player.kickPlayer("Disconnect");
        }
    }

    /**
     *
     * Returns the packet event.
     *
     * @return
     */
    public PacketEvent getPacketEvent() {
        return packetEvent;
    }

    /**
     *
     * Sets the connection to online mode.
     *
     * @packet_sent ENCRYPTION_BEGIN
     * @require ProtocolLib
     * @throws Exception
     */
    public void setOnlineMode() throws Exception {

        if(!onlineModeCanSet) {

            throw new OnlineModeInvaildStateException("Could not set online mode at this state. (Packet cannot be sent).");

        }

        if(alreadyOnline) {
            return;
        }

        token = MinecraftEncryptionUtils.generateToken(random);
        MinecraftOnlineAuthenticator.get().getIncomingConnections().removeAll(MinecraftOnlineAuthenticator.get().getPlayerName(player.getAddress().getAddress().getHostAddress()));
        MinecraftOnlineAuthenticator.get().removePlayer(playerName);
        MinecraftOnlineAuthenticator.get().putPlayer(playerName, player.getAddress().getAddress().getHostAddress());
        MinecraftOnlineAuthenticator.get().getIncomingConnections().putToken(MinecraftOnlineAuthenticator.get().getPlayerName(player.getAddress().getAddress().getHostAddress()), token);

        PacketContainer onlineModeRequestPacket = new PacketContainer(ENCRYPTION_BEGIN);
        onlineModeRequestPacket.getStrings().write(0, "");
        onlineModeRequestPacket.getSpecificModifier(PublicKey.class).write(0, key);
        onlineModeRequestPacket.getByteArrays().write(0,  token);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, onlineModeRequestPacket);
        alreadyOnline = true;

    }

    /**
     *
     * Returns if the online mode can be set.
     *
     * @return
     */
    public boolean canSetOnlineMode() {
        return onlineModeCanSet;
    }

    /**
     *
     * Returns the cloned token.
     *
     * @return
     */
    public byte[] getToken() {
        return token.clone();
    }

    /**
     *
     * Allows  to start the cracked connection (local player cracked session).
     *
     */
    public void allowSPConnection() {

        if(alreadyOnline) {
            return;
        }

        if(!onlineModeCanSet) {
            return;
        }

        try {
            start(playerName);
        } catch(Exception exc) {
            MinecraftOnlineAuthenticator.get().error("Unable to make an SP Connection for player: " + playerName);
            onlineModeCanSet = false;
            try {
                kickPlayer("&cUnable to join: Could not make a cracked local session. Please rejoin.");
                onlineModeCanSet = false;
            } catch(Exception excc) {
                //il giocatore non potrà comunque entrare ma anzichè essere kickato otterà il messaggio Connessione scaduta.
            }
        }
    }

    /**
     *
     * Starts the session has cracked.
     *
     * @param playerName
     * @throws Exception
     */
    private void start(String playerName) throws Exception{
        PacketContainer start = new PacketContainer(START);
        WrappedGameProfile fakeProfile = new WrappedGameProfile(UUID.randomUUID(), playerName);
        start.getGameProfiles().write(0, fakeProfile);
        try {
            ProtocolLibrary.getProtocolManager().recieveClientPacket(player, start, false);
            onlineModeCanSet = false;
        } catch (Exception exc) {
            kickPlayer("&cUnable to join: Could not make a cracked local session. Please rejoin.");
            onlineModeCanSet = false;
        }
    }

}
