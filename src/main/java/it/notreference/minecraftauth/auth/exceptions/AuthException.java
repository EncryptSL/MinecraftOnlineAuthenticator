package it.notreference.minecraftauth.auth.exceptions;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */
public class AuthException extends Exception {

    private String r;

    public AuthException(String motivo) {
        super(motivo);
        r = motivo;
    }


    public String getReason() {
        return r;
    }

}
