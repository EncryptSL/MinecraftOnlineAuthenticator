package it.notreference.minecraftauth.protocol;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.notreference.minecraftauth.MinecraftOnlineAuthenticator;
import it.notreference.minecraftauth.auth.MinecraftEncryptionUtils;
import it.notreference.minecraftauth.events.MinecraftOnlineModeSetEvent;
import it.notreference.minecraftauth.events.MinecraftPremiumFailEvent;
import it.notreference.minecraftauth.protocol.hasjoined.HasjoinedAPI;
import org.bukkit.entity.Player;
import static com.comphenix.protocol.PacketType.Login.Client.START;
import static com.comphenix.protocol.PacketType.Login.Server.DISCONNECT;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.1
 * @destination Spigot
 *
 */

public class MinecraftOnlineValidator {

    private final MinecraftOnlineAuthenticator main;
    private final Player player;
    private final PacketEvent packetEvent;
    private final KeyPair keyPair;
    private final byte[] secret;

    public MinecraftOnlineValidator(MinecraftOnlineAuthenticator main, Player player, PacketEvent packetEvent, KeyPair keyPair, byte[] secret) {
        this.main = main;
        this.player = player;
        this.packetEvent = packetEvent;
        this.keyPair = keyPair;
        this.secret = Arrays.copyOf(secret, secret.length);
    }

    /**
     *
     * Returns the player name from IP.
     *
     * @param socketAddress
     * @return
     */
    public String getName(InetSocketAddress socketAddress) {
        InetAddress addr = socketAddress.getAddress();
       return main.getPlayerName(addr.getHostAddress());
    }

    /**
     *
     * Vaildates the connection.
     *
     */
    public void validate() {

        try {
            try {
                byte[] token = MinecraftOnlineAuthenticator.get().getIncomingConnections().getToken(getName(player.getAddress()));
            } catch(Exception exc) {
                kick("&cYour token couldn't be find from sessions list: The online mode request is invaild.");
                return;
            }
            byte[] token = MinecraftOnlineAuthenticator.get().getIncomingConnections().getToken(getName(player.getAddress()));
            verify(token);
        } finally {

            //
            synchronized(this) {
                packetEvent.setCancelled(true);
            }

        }

    }

    /**
     *
     * Verifes if the (packet) token equals to the stored token. (anti fake packet req)
     *
     * @param token
     * @param cipher
     * @return
     * @throws Exception
     */
    private boolean verifyToken(byte[] token, Cipher cipher) throws Exception {
        byte[] v = packetEvent.getPacket().getByteArrays().read(1);
        if (!Arrays.equals(token, MinecraftEncryptionUtils.decrypt(cipher, v))) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     * Sets the UUID to Premium.
     *
     * @param premiumUUID
     * @return
     */
    private boolean setPremium(UUID premiumUUID) {
            try {
                Object networkManager = manager();
                FieldUtils.writeField(networkManager, "spoofedUUID", premiumUUID, true);
                return true;
            } catch (Exception exc) {
            return false;
            }
    }

    /**
     *
     * Gets the Premium UUID of the specifed name.
     *
     * @param name
     * @return
     */
    private String getPremiumUUID(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            Scanner scanner = new Scanner(url.openStream());
            String line = scanner.nextLine();
            scanner.close();
            JsonObject obj = new Gson().fromJson(line, JsonObject.class);
            String sUUID = obj.get("id").getAsString();
            String uuid =
                    sUUID.substring(0, Math.min(sUUID.length(), 8))
                            + "-"
                            + sUUID.substring(8, Math.min(sUUID.length(), 12))
                            + "-"
                            + sUUID.substring(12, Math.min(sUUID.length(), 16))
                            + "-"
                            + sUUID.substring(16, Math.min(sUUID.length(), 20))
                            + "-"
                            + sUUID.substring(20, Math.min(sUUID.length(), 32));
            return uuid;
        } catch (Exception exc) {
            return "UNABLE_O_NO_PREMIUM";
        }
    }

    private boolean isPremium(String name) {

        if(getPremiumUUID(name) == null || getPremiumUUID(name).equalsIgnoreCase("UNABLE_O_NO_PREMIUM")) {
            return false;
        } else {
            return true;
        }

    }

    private void verify(byte[] token) {

        main.info("Online Mode Verification process started. (" + getName(player.getAddress()) + ")");

        /*

        Initializing process..

         */

        /*

        Checking first of all if is premium.

         */
        if(!isPremium(getName(player.getAddress()))) {
            main.error(getName(player.getAddress()) + " is not premium. Kicked.");
            MinecraftPremiumFailEvent event = new MinecraftPremiumFailEvent(player, getName(player.getAddress()), "", "Unable to authenticate you with Minecraft.net: You aren't premium.");
            main.callEvent(event);
            kick("§cUnable to authenticate you with Minecraft.net: You aren't premium.");
        }

        Cipher c;
        SecretKey key;
        try {
            c = Cipher.getInstance(keyPair.getPrivate().getAlgorithm());
            c.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            key = MinecraftEncryptionUtils.decryptKey(c, secret);
        } catch (Exception exc) {
            main.error("An error has occurred.");
            MinecraftPremiumFailEvent event = new MinecraftPremiumFailEvent(player, getName(player.getAddress()), "", "Unable to authenticate you with Minecraft.net: Could not decrypt data.");
            main.callEvent(event);
            kick("&cUnable to authenticate you with Minecraft.net: Could not decrypt data.");
            return;
        }

        /*

        Verifing the token ^^

         */
        try {
            if (!encryptCon(key) || !verifyToken(token, c)) {
                main.error("Player sent data is invaild. Kicked! (" + getName(player.getAddress()) + ") (verifyToken = " + verifyToken(token, c) + ")");
                kick("&cUnable to authenticate you with Minecraft.net: Invaild token.");
                return;
            }
        } catch (Exception ex) {
            main.error("An error has occurred.");
            MinecraftPremiumFailEvent event = new MinecraftPremiumFailEvent(player, getName(player.getAddress()), "", "Unable to authenticate you with Minecraft.net: An error has occurred.");
            main.callEvent(event);
            kick("&cUnable to authenticate you with Minecraft.net: An error has occurred.");
            return;
        }
        /*

        Getting the server hash.

         */
        String vuoto = "";
        String playerUsername = getName(player.getAddress());
        String hash = MinecraftEncryptionUtils.getServerHash(vuoto, key, keyPair.getPublic());

        /*

        Finalizing..

         */
        try {

            InetAddress addr = ((InetSocketAddress) player.getAddress()).getAddress();
            HasjoinedAPI hasJoined = new HasjoinedAPI(main);
            HasjoinedAPI.Response hasjoinedResponse = hasJoined.newRequest(playerUsername, hash, addr);
            if(hasjoinedResponse.hasJoined()) {

                main.info("Player ({user}) [hasJoined = true] has been authenticated.".replace("{user}", getName(player.getAddress())));

              if(main.getConfiguration().getBoolean("setup-uuids")) {
               try {

                   String uuid = getPremiumUUID(getName(player.getAddress()));
                   setPremium(UUID.fromString(uuid));

               } catch(Exception exc) {
              main.error("Unable to set " + getName(player.getAddress()) + " 's uuid to Premium UUID.");
               }
              }
              main.addPremium(getName(player.getAddress()));
              start(getName(player.getAddress()));

            } else {
                main.error("Player ({user}) isn't logged in through Minecraft.net".replace("{user}", getName(player.getAddress())));
                MinecraftPremiumFailEvent event = new MinecraftPremiumFailEvent(player, getName(player.getAddress()), "", "Not authenticated with Minecraft.net: Your session seems to be invaild. Please restart the game and retry.");
                main.callEvent(event);
                kick("&cNot authenticated with Minecraft.net: Your session seems to be invaild. Please restart the game and retry.");
                return;
            }


        } catch(Exception exc) {
            main.error("Failed to login this player. (" + getName(player.getAddress()) + ")");
            MinecraftPremiumFailEvent event = new MinecraftPremiumFailEvent(player, getName(player.getAddress()), "", "Unable to authenticate you with Minecraft.net: Failed to contact Auth Servers.");
            main.callEvent(event);
            kick("&cUnable to authenticate you with Minecraft.net: Failed to contact Auth Servers.");
         return;
        }



    }

    private void start(String playerName) {


        try {

            main.callEvent(new MinecraftOnlineModeSetEvent(playerName, player.getAddress().getAddress().getHostAddress()));

        } catch(Exception exc) {

            try {

                main.callEvent(new MinecraftOnlineModeSetEvent(playerName, player.getAddress().getAddress().getHostAddress()));


            } catch(Exception exc2) {



            }

        }

        PacketContainer start = new PacketContainer(START);
        WrappedGameProfile fakeProfile = new WrappedGameProfile(UUID.randomUUID(), playerName);
        start.getGameProfiles().write(0, fakeProfile);
        try {
            ProtocolLibrary.getProtocolManager().recieveClientPacket(player, start, false);
        } catch (Exception exc) {
            MinecraftPremiumFailEvent event = new MinecraftPremiumFailEvent(player, getName(player.getAddress()), "", "Unable to authenticate you with Minecraft.net: Failed to start local session.");
            main.callEvent(event);
            kick("&cUnable to authenticate you with Minecraft.net: Failed to start local session.");
            return;
        }
        try {

          main.staffLog("§aSuccessfully applied online mode to player: " + getName(player.getAddress()) + " §2(isPremium & hasJoined = true)");

        } catch(Exception exc) {

        }

    }

    private boolean kick(String messaggio) {

        PacketContainer kick = new PacketContainer(DISCONNECT);
        kick.getChatComponents().write(0, WrappedChatComponent.fromText(messaggio.replace("&", "§")));
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, kick);
            player.kickPlayer("Disconnect");
        } catch (Exception exc) {
         return false;
        }

        try {

            if(MinecraftOnlineAuthenticator.get().getConfiguration().getBoolean("kick-log")) {
                MinecraftOnlineAuthenticator.get().staffLog("§f" + getName(player.getAddress()) + " §7 failed online mode setup. Kick Message:§f " + messaggio.replace("&", "§"));
            }

        } catch(Exception excc) {

        }
        return true;

    }

    private boolean encryptCon(SecretKey key) throws IllegalArgumentException {
        try {
            Object m = manager();
            Method method = FuzzyReflection.fromObject(m).getMethodByParameters("a", SecretKey.class);
            method.invoke(m, key);
            return true;
        } catch (Exception ex) {
            main.error("Unable to encrypt the connection.");
            MinecraftPremiumFailEvent event = new MinecraftPremiumFailEvent(player, getName(player.getAddress()), "", "Unable to authenticate you with Minecraft.net: Unable to encrypt data.");
            main.callEvent(event);
            kick("&cUnable to authenticate you with Minecraft.net: Unable to encrypt data.");
            return false;
        }
    }

    private Object manager() throws Exception {
        Object i = TemporaryPlayerFactory.getInjectorFromPlayer(player);
        Class<?> classe = Class.forName("com.comphenix.protocol.injector.netty.Injector");
        Object rawInjector = FuzzyReflection.getFieldValue(i, classe, true);
        return FieldUtils.readField(rawInjector, "networkManager", true);
    }



}
