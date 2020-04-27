package it.notreference.minecraftauth.auth.json;

import com.google.gson.JsonObject;
import com.sun.istack.internal.NotNull;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */
public class MinecraftJson {

    /**
     *
     * Makes minecraft.net auth json content.
     *
     * @param username
     * @param password
     * @return
     */
    public static String makeJson(@NotNull String username, @NotNull String password) {


        JsonObject obj1 = new JsonObject();
        obj1.addProperty("name", "Minecraft");
        obj1.addProperty("version", 1);
        JsonObject obj = new JsonObject();
        obj.add("agent", obj1);
        obj.addProperty("username", username);
        obj.addProperty("password", password);

        return obj.toString();

    }

}
