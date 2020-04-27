package it.notreference.minecraftauth.auth;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */

public interface Session {

    /**
     *
     * Returns if the session has been validated.
     *
     * @return
     */
    boolean isVaild();

    /**
     *
     * Returns the session status.
     *
     * @return
     */
    SessionStatus getStatus();

    /**
     *
     * Sets the validation status.
     *
     * @param x
     */
    Session setVaild(boolean x);

    /**
     *
     * Sets a new SessionStatus.
     *
     * @param status
     */
     Session setStatus(SessionStatus status);

    /**
     *
     *
     * Returns the UUID.
     *
     * @return
     */
    String getSessionUuid();

    /**
     *
     * Returns the player name.
     *
     * @return
     */
    String getPlayerName();

    /**
     *
     * Returns the access token.
     *
     * @return
     */
    String getAccessToken();

    /**
     *
     * Returns if the player is authenticated through Minecraft.net
     *
     * @return
     */
    boolean isVaildPremiumAuthenticated();

}
