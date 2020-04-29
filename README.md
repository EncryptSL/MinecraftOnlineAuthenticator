# MinecraftOnlineAuthenticator
Allows spigot offline mode servers to make online Minecraft.net authentication (so allow online mode for premium players)

## Dependencies
* [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

## Works With
* [PremiumLogin](https://www.spigotmc.org/resources/premiumlogin.76336/)

## Commands
* /minecraftauth fakepacket (player) - This will send a fake OnlineModeRequest [EncryptionBegin] Packet. (player crash)

* /minecraftauth authenticate (username) (password) - This will provide the access token and the profile data of the specifed account (if correct credentials).

* /minecraftauth getpremiumlist - Shows the currently premium players logged in using MCOnlineAuthenticatorOnlineMode.

* /minecraftauth getuuid (player) - Shows the player currently UUID.

* /minecraftauth protect (player) - Protects/Unprotects a player. (Protection  = Blocks the player to join without the verified online mode)"

* /minecraftauth reload - Reloads the configuration.


## Features

* Allows Premium Players to perform online mode connections and switch to premium UUID.

* Skin auto apply.

* Allows Premium Players to being protected from cracked players:

   Example:
   If Pippo is premium and is protected, he can join the server only if logged in
   trough the premium launcher. Else will be kicked.
   
* No BungeeCord! This plugin if a plugin for spigot without bungeecord only. (If you have bungeecord is only useful when maintenance / new server)    

* Staff Log (Can be set to false in the configuration)

* Async Events & Verification Task - No lag. All verify events are async. So, don't worry about server lag.

* Security. This plugin prevents other plugin from sending fake packets and spoof a fake verify token. It only accept server plugin api call / join call. 

(It generates a random token for the player and store it into a list, if the received verify token in the packet is not = to the stored token, the player will be kicked)

### API
Our api events are very simple to use.
Current API Version: b1.0

#### Example Code to use the api:
```java

import it.notreference.minecraftauth.events.MinecraftPlayerJoinEvent;
import it.notreference.minecraftauth.protocol.MinecraftOnlineRequest;

@EventHandler
public void onJoin(MinecraftPlayerJoinEvent event) {

MinecraftOnlineRequest req = event.getRequest();
//Note: When you do setOnlineMode() player name can't be found in player.getName(), you need to use event.getPlayerName();

if(event.getPlayerName().equalsIgnoreCase("...")) {

req.setOnlineMode();

}

}

@EventHandler
public void onFail(MinecraftPremiumFailEvent event) {

YourPlugin.getLogger().error(event.getPlayerName() + " failed to login in online-mode (invaild session?) Kick Message: " + event.getKickReason());

}

//or

public Session authenticate(String user, String password) throws AuthException, InvaildCredentialsException {

MinecraftAuthenticator auth = new MinecraftYggdrasil();
return auth.PerformLogin(user, password);

}

//or

public boolean isLoggedInOnlineMode(String playerName) {

return MinecraftOnlineAuthenticator.get().isPremiumConnected(playerName);

}
```

