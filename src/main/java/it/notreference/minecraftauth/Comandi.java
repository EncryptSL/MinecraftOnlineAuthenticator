package it.notreference.minecraftauth;

import it.notreference.minecraftauth.auth.MinecraftAuthenticator;
import it.notreference.minecraftauth.auth.MinecraftYggdrasil;
import it.notreference.minecraftauth.auth.Session;
import it.notreference.minecraftauth.auth.exceptions.AuthException;
import it.notreference.minecraftauth.auth.exceptions.InvaildCredentialsException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

public class Comandi implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {

      if(cmd.getName().equalsIgnoreCase("minecraftonlineauthenticator") || cmd.getName().equalsIgnoreCase("minecraftauth") || cmd.getName().equalsIgnoreCase("mcauth")) {

          commandSender.sendMessage("§7This server is using §aMinecraftOnlineAuthenticator §7by §eNotReference§7.");
          if(!(commandSender instanceof Player)) {
              CommandSender s = commandSender;
              if(args.length < 1) {
                  commandSender.sendMessage("§aAvaliable commands for CONSOLE:");
                  s.sendMessage("§bMinecraftOnlineAuthenticator 1.0 by §3NotReference§b. §3§l(CONSOLE Cmds)");
                  s.sendMessage("§7/minecraftauth fakepacket (player) - This will send a fake OnlineModeRequest [EncryptionBegin] Packet. (player crash)");
                  s.sendMessage("§7/minecraftauth authenticate (username) (password) - This will provide the access token of the specifed account (if correct credentials).");
                  s.sendMessage("§7/minecraftauth getpremiumlist - Shows the currently premium players logged in using MCOnlineAuthenticatorOnlineMode.");
                  s.sendMessage("§7/minecraftauth getuuid (player) - Shows the player currently UUID.");
                  s.sendMessage("§7/minecraftauth protect (player) - Protects/Unprotects a player. (Protection  = Blocks the player to join without the verified online mode)");
                  s.sendMessage("§7/minecraftauth reload - Reloads the configuration.");
                  return false;
              } else {
                  String comando = args[0];
                  if (comando.equalsIgnoreCase("reload")) {

                      MinecraftOnlineAuthenticator.get().reloadConfig();
                      commandSender.sendMessage("§aDone.");
                      return false;

                  }

                  if(comando.equalsIgnoreCase("fakePacket")) {

                      if(args.length != 2) {

                          s.sendMessage("§bMmh, §7did you mean §e/minecraftauth fakepacket (player) §7?");
                          return false;

                      } else {

                          Player target = Bukkit.getServer().getPlayer(args[1]);
                          if(target == null) {
                              s.sendMessage("§bMmh, §7this player isn't online.");
                              return false;
                          }
                          try {
                              MinecraftOnlineAuthenticator.get().getUnsafe().sendOnlineModePacketRequest(target);
                              s.sendMessage("§aPacket sent! The player should be crashed.");
                              return false;
                          } catch(Exception exc) {
                              s.sendMessage("§cUnable to send the fake packet.");
                              return false;
                          }
                      }
                  } //comando fakepacket

                  if(comando.equalsIgnoreCase("authenticate")) {

                      if (args.length != 3) {

                          s.sendMessage("§bMmh, §7did you mean §e/minecraftauth authenticate (username) (password) §7?");
                          return false;


                      } else {

                          s.sendMessage("§b[Async] Authenticating.. Please wait..");
                          Bukkit.getServer().getScheduler().runTaskAsynchronously(MinecraftOnlineAuthenticator.get(), () -> {
                              MinecraftAuthenticator yggdrasil = new MinecraftYggdrasil();
                              try {
                                  Session session = yggdrasil.performLogin(args[1], args[2]);
                                  s.sendMessage("§a[200] Successfully logged in as " + session.getPlayerName());
                                  s.sendMessage("§a[200] UUID: " + session.getSessionUuid());
                                  s.sendMessage("§a[200] AccessToken: " + session.getAccessToken());
                                  return;
                              } catch(InvaildCredentialsException exception) {

                                  s.sendMessage("§c[403] Invaild username or password. Please retry.");
                                  return;

                              } catch(AuthException exception) {

                                  s.sendMessage("§c[?] Unable to authenticate: An error has occurred.");
                                  return;

                              }
                          });

                          return false;

                      }
                  } //comando authenticate


                  if(comando.equalsIgnoreCase("getpremiumlist")) {

                      s.sendMessage("§b[Async] Please wait.. Loading data..");

                      Bukkit.getServer().getScheduler().runTaskAsynchronously(MinecraftOnlineAuthenticator.get(), () -> {

                          s.sendMessage("§7Premium players logged with Minecraft.net: §e" + MinecraftOnlineAuthenticator.get().getPremiumPlayers());

                      });

                      return false;
                  } //fine premiumlist

                  if(comando.equalsIgnoreCase("protect")) {

                      if(args.length != 2) {

                          s.sendMessage("§bMmh, §7did you mean §e/minecraftauth protect (player) §7?");
                          return false;
                      } else {

                          String target = args[1];

                          if(allLower(MinecraftOnlineAuthenticator.get().getConfiguration().getStringList("protected-players")).contains(target.toLowerCase())) {

                              List<String> players = MinecraftOnlineAuthenticator.get().getConfiguration().getStringList("protected-players");
                              players.remove(target);

                              MinecraftOnlineAuthenticator.get().getConfiguration().set("protected-players", players);
                              MinecraftOnlineAuthenticator.get().saveConfig();
                              MinecraftOnlineAuthenticator.get().reloadConfig();

                              s.sendMessage("§eSuccess! The player " + target + " isn't protected anymore.");
                              return false;

                          } else {


                              List<String> players = MinecraftOnlineAuthenticator.get().getConfiguration().getStringList("protected-players");
                              players.add(target);

                              MinecraftOnlineAuthenticator.get().getConfiguration().set("protected-players", players);
                              MinecraftOnlineAuthenticator.get().saveConfig();
                              MinecraftOnlineAuthenticator.get().reloadConfig();

                              s.sendMessage("§aSuccess! The player " + target + " is now protected!");
                              return false;



                          }
                      }
                  }//fine protect

                  if(comando.equalsIgnoreCase("getuuid")) {

                      if (args.length != 2) {

                          s.sendMessage("§bMmh, §7did you mean §e/minecraftauth getuuid (player) §7?");
                          return false;

                      } else {

                          Player target = Bukkit.getServer().getPlayer(args[1]);
                          if (target == null) {

                              s.sendMessage("§bMmh, §7this player seems to be offline.");
                              return false;

                          }

                          s.sendMessage("§e" + args[1] + "§7's UUID is: " + target.getUniqueId());
                          return false;

                      }
                  } //fine getuuid

                  s.sendMessage("§bMinecraftOnlineAuthenticator 1.0 by §3NotReference§b. §3§l(CONSOLE Cmds)");
                  s.sendMessage("§7/minecraftauth fakepacket (player) - This will send a fake OnlineModeRequest [EncryptionBegin] Packet. (player crash)");
                  s.sendMessage("§7/minecraftauth authenticate (username) (password) - This will provide the access token of the specifed account (if correct credentials).");
                  s.sendMessage("§7/minecraftauth getpremiumlist - Shows the currently premium players logged in using MCOnlineAuthenticatorOnlineMode.");
                  s.sendMessage("§7/minecraftauth getuuid (player) - Shows the player currently UUID.");
                  s.sendMessage("§7/minecraftauth protect (player) - Protects/Unprotects a player. (Protection  = Blocks the player to join without the verified online mode)");
                  s.sendMessage("§7/minecraftauth reload - Reloads the configuration.");
                  return false;

              } //fine args else

          } //fine di se (instanceof player)

          else {

              Player p = (Player) commandSender;
              if(!p.hasPermission("minecraftonlineauthenticator.use")) {
                  p.sendMessage("§cYou do not have permission to perform this command.");
                  return false;
              }

              if(args.length < 1) {
                  p.sendMessage("§bMinecraftOnlineAuthenticator 1.0 by §3NotReference§b.");
                  p.sendMessage("§7/minecraftauth fakepacket (player) - This will send a fake OnlineModeRequest [EncryptionBegin] Packet. (player crash)");
                  p.sendMessage("§7/minecraftauth authenticate (username) (password) - This will provide the access token of the specifed account (if correct credentials).");
                  p.sendMessage("§7/minecraftauth getpremiumlist - Shows the currently premium players logged in using MCOnlineAuthenticatorOnlineMode.");
                  p.sendMessage("§7/minecraftauth getuuid (player) - Shows the player currently UUID.");
                  p.sendMessage("§7/minecraftauth protect (player) - Protects/Unprotects a player. (Protection  = Blocks the player to join without the verified online mode)");
                  p.sendMessage("§7/minecraftauth reload - Reloads the configuration.");
                  return false;
              }

              String comando = args[0];
              if(comando.equalsIgnoreCase("reload")) {

                  MinecraftOnlineAuthenticator.get().reloadConfig();
                  p.sendMessage("§aDone.");
                  return false;

              }
              if(comando.equalsIgnoreCase("fakePacket")) {

                  if(args.length != 2) {

                      p.sendMessage("§bMmh, §7did you mean §e/minecraftauth fakepacket (player) §7?");
                      return false;

                  } else {

                      Player target = Bukkit.getServer().getPlayer(args[1]);
                      if(target == null) {
                          p.sendMessage("§bMmh, §7this player isn't online.");
                          return false;
                      }
                      try {
                          MinecraftOnlineAuthenticator.get().getUnsafe().sendOnlineModePacketRequest(target);
                           p.sendMessage("§aPacket sent! The player should be crashed.");
                           return false;
                      } catch(Exception exc) {
                          p.sendMessage("§cUnable to send the fake packet.");
                          return false;
                      }
                  }
              }

              if(comando.equalsIgnoreCase("authenticate")) {

                  if (args.length != 3) {

                      p.sendMessage("§bMmh, §7did you mean §e/minecraftauth authenticate (username) (password) §7?");
                      return false;


                  } else {

                   p.sendMessage("§b[Async] Authenticating.. Please wait..");
                   Bukkit.getServer().getScheduler().runTaskAsynchronously(MinecraftOnlineAuthenticator.get(), () -> {
                       MinecraftAuthenticator yggdrasil = new MinecraftYggdrasil();
                       try {
                          Session session = yggdrasil.performLogin(args[1], args[2]);
                          p.sendMessage("§a[200] Successfully logged in as " + session.getPlayerName());
                          p.sendMessage("§a[200] UUID: " + session.getSessionUuid());
                          p.sendMessage("§a[200] AccessToken: " + session.getAccessToken());
                          return;
                       } catch(InvaildCredentialsException exception) {

                           p.sendMessage("§c[403] Invaild username or password. Please retry.");
                           return;

                       } catch(AuthException exception) {

                           p.sendMessage("§c[?] Unable to authenticate: An error has occurred.");
                           return;

                       }
                   });

                 return false;

                  }
              }

              if(comando.equalsIgnoreCase("getpremiumlist")) {

                  p.sendMessage("§b[Async] Please wait.. Loading data..");

                  Bukkit.getServer().getScheduler().runTaskAsynchronously(MinecraftOnlineAuthenticator.get(), () -> {

                      p.sendMessage("§7Premium players logged with Minecraft.net: §e" + MinecraftOnlineAuthenticator.get().getPremiumPlayers());

                  });

                  return false;
              }

              if(comando.equalsIgnoreCase("getuuid")) {

                  if(args.length != 2) {

                      p.sendMessage("§bMmh, §7did you mean §e/minecraftauth getuuid (player) §7?");
                      return false;

                  } else {

                      Player target =  Bukkit.getServer().getPlayer(args[1]);
                      if(target == null) {

                          p.sendMessage("§bMmh, §7this player seems to be offline.");
                          return false;

                      }

                      p.sendMessage("§e" + args[1] + "§7's UUID is: " + target.getUniqueId());
                      return false;

                  }


              }

              if(comando.equalsIgnoreCase("protect")) {

                  if(args.length != 2) {

                      p.sendMessage("§bMmh, §7did you mean §e/minecraftauth protect (player) §7?");
                      return false;
                  } else {

                      String target = args[1];

                      if(allLower(MinecraftOnlineAuthenticator.get().getConfiguration().getStringList("protected-players")).contains(target.toLowerCase())) {

                          List<String> players = MinecraftOnlineAuthenticator.get().getConfiguration().getStringList("protected-players");
                          players.remove(target);

                          MinecraftOnlineAuthenticator.get().getConfiguration().set("protected-players", players);
                          MinecraftOnlineAuthenticator.get().saveConfig();
                          MinecraftOnlineAuthenticator.get().reloadConfig();

                          p.sendMessage("§eSuccess! The player " + target + " isn't protected anymore.");
                          return false;

                      } else {


                              List<String> players = MinecraftOnlineAuthenticator.get().getConfiguration().getStringList("protected-players");
                              players.add(target);

                              MinecraftOnlineAuthenticator.get().getConfiguration().set("protected-players", players);
                              MinecraftOnlineAuthenticator.get().saveConfig();
                              MinecraftOnlineAuthenticator.get().reloadConfig();

                          p.sendMessage("§aSuccess! The player " + target + " is now protected!");
                          return false;



                      }
                  }
              }


              p.sendMessage("§bMinecraftOnlineAuthenticator 1.0 by §3NotReference§b.");
              p.sendMessage("§7/minecraftauth fakepacket (player) - This will send a fake OnlineModeRequest [EncryptionBegin] Packet. (player crash)");
              p.sendMessage("§7/minecraftauth authenticate (username) (password) - This will provide the access token of the specifed account (if correct credentials).");
              p.sendMessage("§7/minecraftauth getpremiumlist - Shows the currently premium players logged in using MCOnlineAuthenticatorOnlineMode.");
              p.sendMessage("§7/minecraftauth getuuid (player) - Shows the player currently UUID.");
              p.sendMessage("§7/minecraftauth protect (player) - Protects/Unprotects a player. (Protection  = Blocks the player to join without the verified online mode)");
              return false;



          }

      }

        return false;


    }

    public List<String> allLower(List<String> listaPrincipale) {

        List<String> temp = new ArrayList<String>();

        for(String s: listaPrincipale) {
            temp.add(s.toLowerCase());
        }

        return temp;
    }

}
