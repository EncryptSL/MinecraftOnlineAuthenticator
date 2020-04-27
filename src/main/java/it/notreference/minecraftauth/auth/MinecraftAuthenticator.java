package it.notreference.minecraftauth.auth;

import it.notreference.minecraftauth.auth.exceptions.AuthException;
import it.notreference.minecraftauth.auth.exceptions.InvaildCredentialsException;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */

public interface MinecraftAuthenticator {

    /**
     *
     * Performs Minecraft.net login.
     *
     * @param username
     * @param password
     * @return
     */
    MinecraftPlayerSession performLogin(String username, String password) throws AuthException, InvaildCredentialsException;

}
