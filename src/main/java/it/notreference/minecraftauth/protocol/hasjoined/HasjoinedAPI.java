package it.notreference.minecraftauth.protocol.hasjoined;

import it.notreference.minecraftauth.MinecraftOnlineAuthenticator;
import it.notreference.minecraftauth.auth.MojangURL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */

public class HasjoinedAPI {

    private final MinecraftOnlineAuthenticator main;
    //https://sessionserver.mojang.com/session/minecraft/hasJoined?username={username}&serverId={serverId}&ip={ip}
    private final String url;

    public HasjoinedAPI(MinecraftOnlineAuthenticator main) {
        this.main = main;
        this.url = main.getUrlParser().parseUrl(MojangURL.HAS_JOINED_URL);
    }

    public HasjoinedAPI.Response newRequest(String playerName, String serverHash, InetAddress addr) throws Exception{
        String ip = URLEncoder.encode(addr.getHostAddress(), StandardCharsets.UTF_8.name());
        String hasJoinedURL =  url.replace("{username}", playerName).replace("{serverId}", serverHash).replace("{ip}", ip);
        URL uri = new URL(hasJoinedURL);
        URLConnection connection = uri.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("User-Agent", "MinecraftOnlineAuthenticatorAPI");
        if (((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_OK) {
            return new HasjoinedAPI.Response(true);
        } else {
            return new HasjoinedAPI.Response(false);
        }

    }

    public static class Response {

        private final boolean joined;

        public Response(boolean joined) {
            this.joined = joined;
        }

        public boolean hasJoined() {
            return joined;
        }

    }

}
