package it.notreference.minecraftauth.protocol.exceptions;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */

public class OnlineModeInvaildStateException extends Exception {

    private final String motivo;

    public OnlineModeInvaildStateException(String motivo) {
        super(motivo);
        this.motivo = motivo;
    }

    public String getReason() {
        return motivo;
    }
}
