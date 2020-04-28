package it.notreference.minecraftauth;

import it.notreference.minecraftauth.events.MinecraftPlayerJoinEvent;
import it.notreference.minecraftauth.protocol.MinecraftOnlineRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

public class MCBukkitListener implements Listener {

    @EventHandler
    public void onJoin(MinecraftPlayerJoinEvent event) {

        if(event.isCancelled()) {
            return;
        }

       if(MinecraftOnlineAuthenticator.get().getServer().getPluginManager().isPluginEnabled("PremiumLoginSpigot")) {

           return;

       }

        if(MinecraftOnlineAuthenticator.get().getConfiguration().getBoolean("force-online-mode")) {
            MinecraftOnlineRequest requestPacket = event.getRequest();
            try {
                String playerName = event.getPlayerName();
                Bukkit.getLogger().info("§6§lMinecraftOnlineAuthenticator  §8» §aOnline mode set to true for player: " + playerName);
                staffLog("Setting up online mode for player §f" + playerName + "§7..");
                requestPacket.setOnlineMode();
                return;
            } catch(Exception exc) {
                Bukkit.getLogger().severe("§6§lMinecraftOnlineAuthenticator  §8» §cUnable to set online mode for player " + event.getPlayerName());
            }
        }

        if(MinecraftOnlineAuthenticator.get().getConfiguration().getBoolean("auto-protect-op-players")) {
            if(event.getPlayer().isOp() || event.getPlayer().hasPermission("*")) {
                MinecraftOnlineRequest requestPacket = event.getRequest();
                try {
                    String playerName = event.getPlayerName();
                    Bukkit.getLogger().info("§6§lMinecraftOnlineAuthenticator  §8» §e" + playerName + " is a an OP player (config require op protection)! Setting up online mode..");
                    Bukkit.getLogger().info("§6§lMinecraftOnlineAuthenticator  §8» §aOnline mode set to true for player: " + playerName);
                    staffLog("A OP player joined the server! Applying online mode as set in configuration.. §a(" + playerName + ")");
                    requestPacket.setOnlineMode();
                    return;
                } catch(Exception exc) {
                    Bukkit.getLogger().severe("§6§lMinecraftOnlineAuthenticator  §8» §cUnable to set online mode for player " + event.getPlayerName());
                }
            }
        }

        if(MinecraftOnlineAuthenticator.get().getConfiguration().getStringList("protected-players") != null) {

            List<String> n = new ArrayList<String>();
            List<String> protetti = MinecraftOnlineAuthenticator.get().getConfiguration().getStringList("protected-players");
            for(String nomi: protetti) {
                n.add(nomi.toLowerCase());
            }
            if(n.contains(event.getPlayerName().toLowerCase())) {
                MinecraftOnlineRequest requestPacket = event.getRequest();
                try {
                    String playerName = event.getPlayerName();
                    Bukkit.getLogger().info("§6§lMinecraftOnlineAuthenticator  §8» §e" + playerName + " is a protected player! Setting up online mode..");
                    Bukkit.getLogger().info("§6§lMinecraftOnlineAuthenticator  §8» §aOnline mode set to true for player: " + playerName);
                    staffLog("A protected player joined the server! Applying online mode as set in configuration.. §a(" + playerName + ")");
                    requestPacket.setOnlineMode();
                    return;
                } catch(Exception exc) {
                    Bukkit.getLogger().severe("§6§lMinecraftOnlineAuthenticator  §8» §cUnable to set online mode for player " + event.getPlayerName());
                }
            }

        }

        /*

        Allowing player to connect.

         */
        event.getRequest().allowSPConnection();
        if(MinecraftOnlineAuthenticator.get().getConfiguration().getBoolean("log-sp-connections-too")) {

            staffLog("§7Allowed to start a SP Cracked Session for user:§f " + event.getPlayerName());

        }

    }

    /*
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        MinecraftOnlineAuthenticator.get().removePlayer(event.getPlayer().getAddress().getAddress().getHostAddress());
        //debug per testare
        Bukkit.getLogger().info("[MinecraftOnlineAuthenticator] Player IP has been put.");
        MinecraftOnlineAuthenticator.get().putPlayer(event.getPlayer().getName(), event.getPlayer().getAddress().getAddress().getHostAddress());
    }
    */

    public void staffLog(String msg) {

        if(!MinecraftOnlineAuthenticator.get().getConfiguration().getBoolean("staff-log")) {
            return;
        }

        for(Player p: Bukkit.getServer().getOnlinePlayers()) {

            if(p.hasPermission("minecraftonlineauthenticator.use") || p.hasPermission("minecraftauthenticator.log") || p.isOp() || p.hasPermission("*")) {

                p.sendMessage("§6§lMinecraftOnlineAuthenticator §8» §eStaff-Log: §7" + msg);

            }

        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(MinecraftOnlineAuthenticator.get().isPremiumConnected(event.getPlayer().getName())) {

            staffLog("A premium player (with online mode enabled) has left from the server. §a(" + event.getPlayer().getName() + ")");

        }
        MinecraftOnlineAuthenticator.get().removePremium(event.getPlayer().getName());
        MinecraftOnlineAuthenticator.get().getIncomingConnections().removeAll(MinecraftOnlineAuthenticator.get().getPlayerName(event.getPlayer().getAddress().getAddress().getHostAddress()));
        MinecraftOnlineAuthenticator.get().removePlayer(event.getPlayer().getAddress().getAddress().getHostAddress());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        boolean log1 = false;
        if(MinecraftOnlineAuthenticator.get().isPremiumConnected(event.getPlayer().getName())) {
            log1 = true;
        }
        if(!log1) {
            staffLog("A cracked (or not authenticated) player has been kicked from the server for the reason:§f " + event.getReason() + " §b(Player: " + event.getPlayer().getName() + ")");
        }
        MinecraftOnlineAuthenticator.get().removePremium(event.getPlayer().getName());
        MinecraftOnlineAuthenticator.get().getIncomingConnections().removeAll(MinecraftOnlineAuthenticator.get().getPlayerName(event.getPlayer().getAddress().getAddress().getHostAddress()));
        MinecraftOnlineAuthenticator.get().removePlayer(event.getPlayer().getAddress().getAddress().getHostAddress());
        if(event.getReason().toLowerCase().contains("failed to ") && event.getReason().toLowerCase().contains("invaild session")) {
            event.setReason("§cUnable to authenticate you with Minecraft.net: Invaild session.");
        }
        if(log1) {
            if(MinecraftOnlineAuthenticator.get().isPremiumConnected(event.getPlayer().getName())) {

                staffLog("A premium player (with online mode enabled) has been kicked from the server, §a(" + event.getPlayer().getName() + ") §7for the reason:§f " + event.getReason());

            }
        }
    }

}
