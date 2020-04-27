package it.notreference.minecraftauth.protocol;

import static com.comphenix.protocol.PacketType.Login.Client.ENCRYPTION_BEGIN;
import static com.comphenix.protocol.PacketType.Login.Client.START;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import it.notreference.minecraftauth.MinecraftOnlineAuthenticator;
import it.notreference.minecraftauth.auth.MinecraftEncryptionUtils;
import it.notreference.minecraftauth.events.MinecraftPlayerJoinEvent;
import org.bukkit.entity.Player;

import java.security.KeyPair;
import java.security.SecureRandom;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */

public class OnlineProtocolListener extends PacketAdapter  {

    private MinecraftOnlineAuthenticator main;
    private final SecureRandom secureRandom = new SecureRandom();
    private final KeyPair keyPair = MinecraftEncryptionUtils.generateKeyPair();

    public OnlineProtocolListener(MinecraftOnlineAuthenticator main) {
        super(params()
                .plugin(main)
                .types(START, ENCRYPTION_BEGIN)
                .optionAsync());
        this.main = main;
    }

    /**
     *
     * Registers this listener.
     *
     * @param main
     */
    public static void registerListener(MinecraftOnlineAuthenticator main) {

        ProtocolLibrary.getProtocolManager()
                .getAsynchronousManager()
                .registerAsyncHandler(new OnlineProtocolListener(main))
                .start(3);

    }


    @Override
    public void onPacketReceiving(PacketEvent packetEvent) {
        if (packetEvent.isCancelled())
            return;

        Player player = packetEvent.getPlayer();
        PacketType packetType = packetEvent.getPacketType();
        if (packetType == START) {
            onStart(packetEvent, player);
        } else {
            onOnlineEncryption(packetEvent, player);
        }
    }

    /**
     * Fired when a start request coming. (Not our packet sent for allow / start connection)
     *
     * @param packetEvent
     * @param player
     */
    private void onStart(PacketEvent packetEvent, Player player)  {
        PacketContainer p = packetEvent.getPacket();
        packetEvent.getAsyncMarker().incrementProcessingDelay();
        String userName = p.getGameProfiles().read(0).getName();

        MinecraftPlayerJoinEvent join = new MinecraftPlayerJoinEvent(new MinecraftOnlineRequest(secureRandom, packetEvent, player, keyPair.getPublic(), userName), player, userName);
        main.callEvent(join);

        if(join.isCancelled()) {
            try {
                join.getRequest().kickPlayer(join.getKickMsg());
            } catch(Exception exc) {

            }
        }
    }

    /**
     *
     * Fired when the MinecraftOnlineRequest is set to online mode. (ENCRYPTION_BEGIN PACKET).
     *
     * @param packetEvent
     * @param player
     */
    private void onOnlineEncryption(PacketEvent packetEvent, Player player) {
        packetEvent.getAsyncMarker().incrementProcessingDelay();
        byte[] secret = packetEvent.getPacket().getByteArrays().read(0);

        /*

        Vaildating Async: We need to call mojang API so we need to do this async. ^ (can take a while for high latency servers / connections)

         */
        main.getServer().getScheduler().runTaskAsynchronously(main, () -> {

            MinecraftOnlineValidator validator = new MinecraftOnlineValidator(main, player, packetEvent, keyPair, secret);
            validator.validate();

        });

    }

}
