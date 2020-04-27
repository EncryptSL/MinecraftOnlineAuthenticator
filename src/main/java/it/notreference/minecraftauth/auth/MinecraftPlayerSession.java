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

public class MinecraftPlayerSession implements Session {

    private SessionStatus status;
    private boolean vaild;
    private final String playerName;
    private final String accessToken;
    private final String sessionUuid;

    public MinecraftPlayerSession(String playerName, String uuid, String token) {
        this.playerName = playerName;
        this.accessToken = token;
        this.sessionUuid = uuid;
        this.vaild = false;
        this.status = SessionStatus.SP_UNAUTHENTICATED;
    }

    public boolean isVaild() {
        return vaild;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Session setVaild(boolean x) {
    this.vaild = x;
    return this;
    }

    public Session setStatus(SessionStatus status) {
        this.status = status;
      return this;
    }

    public String getSessionUuid() {
        return sessionUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public boolean isVaildPremiumAuthenticated() {
        return status == SessionStatus.PREMIUM_AUTHENTICATED;
    }
}
