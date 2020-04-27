package it.notreference.minecraftauth;

import it.notreference.minecraftauth.auth.MinecraftEncryptionUtils;
import it.notreference.minecraftauth.auth.MojangURL;
import it.notreference.minecraftauth.protocol.MinecraftOnlineRequest;
import it.notreference.minecraftauth.protocol.OnlineProtocolListener;
import net.minecraft.server.v1_12_R1.MinecraftEncryption;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.spigot;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */
public class MinecraftOnlineAuthenticator extends JavaPlugin {

private static MinecraftOnlineAuthenticator online;
private List<String> premiumPlayers;
private HashMap<String, String> playerNames;
private String ver = "1.0";
private IncomingConnections incoming;
private YamlConfiguration config;

    /**
     *
     * Logs a message to all online staff players.
     *
     * @param msg
     */
    public void staffLog(String msg) {

        if(!getConfiguration().getBoolean("staff-log")) {
            return;
        }

        for(Player p: Bukkit.getServer().getOnlinePlayers()) {

            if(p.hasPermission("minecraftonlineauthenticator.use") || p.hasPermission("minecraftauthenticator.log") || p.isOp() || p.hasPermission("*")) {

                p.sendMessage("§6§lMinecraftOnlineAuthenticator §8» §eStaff-Log: §7" + msg);

            }

        }

    }

    /**
     *
     * Calls an event.
     *
     * @param e
     */
    public void callEvent(Event e) {

        this.getServer().getPluginManager().callEvent(e);

    }

    /**
     *
     * Prints an info message to console.
     *
     * @param e
     */
    public void info(String e) {

        Bukkit.getLogger().info("§6§lMinecraftOnlineAuthenticator §8» §aINFO:§7 " + e);

    }

    public void warn(String e) {

        Bukkit.getLogger().warning("§6§lMinecraftOnlineAuthenticator §8» §eWARNING:§7 " + e);

    }

    /**
     *
     * Prints an error message to console.
     *
     * @param e
     */
    public void error(String e) {

        Bukkit.getLogger().severe("§6§lMinecraftOnlineAuthenticator §8» §cERROR:§7 " + e);

    }

    /**
     *
     * Returns all stored player names
     *
     * @return
     */
    public HashMap<String, String> getPlayerNames() {

        return (HashMap<String, String>) playerNames.clone();

    }

    /**
     *
     * Puts a player to playerNames (can be find using the IP address)
     *
     * @param name
     * @param host
     */
    public void putPlayer(String name, String host) {
        if(playerNames.containsKey(name)) {
            playerNames.remove(name);
        }
        playerNames.put(host, name);
    }

    /**
     *
     * Removes a player to playerNames
     *
     * @param ip
     */
    public void removePlayer(String ip) {
        if(!playerNames.containsKey(ip)) {
            return;
        }
        playerNames.remove(ip);
    }

    /**
     * Returns the playerName of the associated ip.
     *
     * @param host
     * @return
     */
    public String getPlayerName(String host) {
        return playerNames.get(host);
    }

    /*

    ^^ I hope you will like my plugin.

     */
     @Override
    public void onEnable()  {

        info("Loading MinecraftOnlineAuthenticator " + ver + " by NotReference..");
        online = this;

       try {
           incoming = new IncomingConnections(this);
           premiumPlayers = new ArrayList<String>();
           playerNames = new HashMap<String, String>();
       } catch(Exception exc) {
           error("Unable to register classes and components. Disabling..");
           return;
       }

     if(!getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
         error("MinecraftOnlineAuthenticator Requires ProtocolLib. Please download it now from SpigotMC. Disabling..");
         return;
     }

     try {
         if (!getDataFolder().exists())
             getDataFolder().mkdir();

         File configFile = new File(getDataFolder(), "config.yml");
         if (!configFile.exists()) {
             saveDefaultConfig();
         }
     } catch(Exception exc) {
         error("Unable to make configuration dir / files. Disabling.. Please retry.");
         return;
     }

         try {

             File configFile = new File(getDataFolder(), "config.yml");
             config = YamlConfiguration.loadConfiguration(configFile);

         } catch(Exception exc) {
             error("Unable to load the configuration. Disabling.");
             return;
         }

         if(getServer().getOnlineMode()) {

             error("For use the plugin you need to turn the offline mode (or 2 request packets will be sent causing kicks).");
             error("Disabling..");
             return;

         }

         if(spigot().getConfig().getBoolean("settings.bungeecord")) {

             warn("This server is using bungeecord to true: Bungeecord doesn't allow online mode direct connections. (So you will be kicked if connect from bungeecord)");
             warn("This plugin is made for spigot servers without online mode. Please disable bungeecord.");
             warn("The plugin is not going to disable.");

         }

         try {
             getServer().getPluginManager().registerEvents(new MCBukkitListener(), this);
             getServer().getPluginCommand("minecraftonlineauthenticator").setExecutor(new Comandi());
             getServer().getPluginCommand("minecraftauth").setExecutor(new Comandi());
             getServer().getPluginCommand("mcauth").setExecutor(new Comandi());
             OnlineProtocolListener.registerListener(this);
         } catch(Exception exc) {
             error("Unable to register listeners and command handlers. Disabling..");
             return;
         }

         info("MinecraftOnlineAuthenticator " + ver + " by NotReference Enabled!");

     }

     public void onDisable() {

         int premiumPlayersLength = premiumPlayers.size();
        premiumPlayers.clear();
        getIncomingConnections().playerMap.clear();
        playerNames.clear();

        info("Removed " + premiumPlayersLength + " Premium connections from the list.");
        info("MinecraftOnlineAuthenticator " + ver + " by NotReference Disabled.");
        info("GoodBye.");

     }

    /**
     *
     * Returns the configuration.
     *
     * @return
     */
    public YamlConfiguration getConfiguration() {

        return config;

    }

    /**
     *
     * Removes a premium player to the list.
     *
     * @param name
     */
     public void removePremium(String name) {
         if(premiumPlayers.contains(name)) {
             premiumPlayers.remove(name);
         }
     }

    /**
     *
     * Returns if a player is online mode applied.
     *
     * @param name
     * @return
     */
     public boolean isPremiumConnected(String name) {
         return premiumPlayers.contains(name);
     }

    /**
     *
     * Returns premium players;
     *
     * @return
     */
    public List<String> getPremiumPlayers() {
         return new ArrayList<String>(premiumPlayers);
     }

    /**
     *
     * Adds a premium player to the list.
     *
     * @param name
     */
     public void addPremium(String name) {
         if(premiumPlayers.contains(name)) {
             return;
         }
        premiumPlayers.add(name);
     }

    /**
     *
     *
     * Returns the incoming connections.
     *
     * @return
     */
    public IncomingConnections getIncomingConnections() {

         return incoming;

     }


    /**
     *
     * Returns the plugin instance.
     *
     * @return
     */
     public static MinecraftOnlineAuthenticator get() {
         return online;
     }

    /**
     *
     * Returns the Unsafe Class.
     *
     * @return
     */
     public PacketUnsafe getUnsafe() {
         return new PacketUnsafe(this);
     }

    /**
     *
     * Returns a new MojangURLParser.
     *
     * @return
     */
     public MojangURLParser getUrlParser() {
         return new MojangURLParser(this);
     }

     public class MojangURLParser {

         private final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/{username}";
         private final String AUTH_URL = "https://authserver.mojang.com/authenticate";
         private final String HAS_JOINED_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username={username}&serverId={serverId}&ip={ip}";

         private MinecraftOnlineAuthenticator main;

         public MojangURLParser(MinecraftOnlineAuthenticator main) {
             this.main = main;
         }

         public String parseUrl(MojangURL url) {
             if(url == MojangURL.UUID_URL) {
                 return UUID_URL;
             }
             if(url == MojangURL.HAS_JOINED_URL) {
                 return HAS_JOINED_URL;
             }
             if(url == MojangURL.AUTH_URL) {
                 return AUTH_URL;
             }

             return "INVALID";

         }

     }

    /**
     *
     * NOTE:::
     * This call must be get using getIncomingConnections(),
     * because stored data can be lost. (if not using the method)
     *
     */
    public class IncomingConnections {

         private final MinecraftOnlineAuthenticator main;

         private final HashMap<String, byte[]> playerMap;

         public IncomingConnections(MinecraftOnlineAuthenticator main) {
             this.main = main;
             this.playerMap = new HashMap<String, byte[]>();
         }

         /**
          *
          * Returns the stored player token.
          *
          * @param playerName
          * @return
          */
         public byte[] getToken(String playerName) {

              return playerMap.get(playerName);

         }

         /**
          *
          * Puts a token if absent.
          *
          * @param playerName
          * @param token
          */
         public void putToken(String playerName, byte[] token) {
             if(playerMap.containsKey(playerName)) {

                 playerMap.remove(playerName);

             }
             playerMap.put(playerName, token);
         }

         /**
          *
          * Removes the incoming connections from playerName,
          *
          * @param playerName
          */
         public void removeAll(String playerName) {
             if(playerMap.containsKey(playerName))
                 playerMap.remove(playerName);
         }

     }

     public class PacketUnsafe {

         private MinecraftOnlineAuthenticator main;

         public PacketUnsafe(MinecraftOnlineAuthenticator main) {
             this.main = main;
         }

         /**
          *
          * NOTE: If you use this incorrectly, the player will be auto kicked.
          *
          * @param p
          * @deprecated
          */
         @Deprecated
         public void sendOnlineModePacketRequest(Player p) throws Exception {
             MinecraftOnlineRequest newRequest = new MinecraftOnlineRequest(new SecureRandom(), null, p, MinecraftEncryptionUtils.generateKeyPair().getPublic(), p.getName());
             newRequest.setOnlineMode();
         }

     }

}
