package it.notreference.minecraftauth.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.notreference.minecraftauth.auth.exceptions.AuthException;
import it.notreference.minecraftauth.auth.exceptions.InvaildCredentialsException;
import it.notreference.minecraftauth.auth.json.MinecraftJson;


/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */

public class MinecraftYggdrasil implements  MinecraftAuthenticator {

    private final String AUTH_URL = "https://authserver.mojang.com/authenticate";

    @Override
    public MinecraftPlayerSession performLogin(String username, String password) throws AuthException, InvaildCredentialsException {

        String jsonContent = MinecraftJson.makeJson(username, password);

        try {


            HttpRequest minecraftRequest = new HttpRequest()
                    .fromJson(jsonContent)
                    .doHttpPost(AUTH_URL);
            String risposta = minecraftRequest.getResponse();
            JsonObject json = new Gson().fromJson(risposta, JsonObject.class);

            String accessToken = json.get("accessToken").getAsString();

            JsonObject dataProfile = json.getAsJsonObject("selectedProfile");

            JsonObject data = dataProfile;
            String name = data.get("name").getAsString();
            String uuid = data.get("id").getAsString();

            return new MinecraftPlayerSession(name, uuid, accessToken);

        } catch(InvaildCredentialsException exc) {

            throw new InvaildCredentialsException("Invaild credentials for user " + username + "!");

        } catch (AuthException exc) {

            throw new AuthException("Unable to authenticate (user={user}): ".replace("{user}", username) + exc);

        }

    }

}
