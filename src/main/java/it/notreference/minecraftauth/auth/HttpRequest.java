package it.notreference.minecraftauth.auth;

import com.sun.istack.internal.NotNull;
import it.notreference.minecraftauth.auth.exceptions.AuthException;
import it.notreference.minecraftauth.auth.exceptions.InvaildCredentialsException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */
public class HttpRequest {

    private String responseStr;
    private String json;


    /**
     *
     * Returns the auth url.
     *
     * @return
     */
    public String getAuthUrl() {
        return "https://authserver.mojang.com/authenticate";
    }

    /**
     *
     * Returns the received response.
     *
     * @return
     */
    public String getResponse() {
        return responseStr;
    }

    /**
     *
     * Sets the json data.
     *
     * @param jsonData
     * @return
     */
    public HttpRequest fromJson(@NotNull String jsonData) {
        json = jsonData;
        return this;
    }

    /**
     *
     * Makes http post with json data.
     *
     * @param urlAutenticazione
     * @return
     * @throws AuthException
     * @throws InvaildCredentialsException
     */
    public HttpRequest doHttpPost(@NotNull String urlAutenticazione) throws AuthException, InvaildCredentialsException {

        if(json == null) {

            throw new RuntimeException("The request must be in a json format.");


        } else {

            try {
                byte[] contentBytes = json.getBytes("UTF-8");

                URL uri = new URL(urlAutenticazione);
                URLConnection connection = uri.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty("Accept-Charset", "UTF-8");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Length", Integer.toString(contentBytes.length));
                //connection.setRequestProperty("User-Agent", "MinecraftOnlineAuthenticatorAPI");

                OutputStream requestStream = connection.getOutputStream();
                requestStream.write(contentBytes, 0, contentBytes.length);
                requestStream.close();

                String response = "An error has occurred.";
                BufferedReader responseStream;
                if (((HttpURLConnection) connection).getResponseCode() == 200) {
                    responseStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                } else {
                    responseStream = new BufferedReader(new InputStreamReader(((HttpURLConnection) connection).getErrorStream(), "UTF-8"));
                }

                response = responseStream.readLine();
                responseStream.close();
                this.responseStr = response;

                if (((HttpURLConnection) connection).getResponseCode() == 403) {
                    throw new InvaildCredentialsException("Invaild credentials, retry.");
                }
            } catch(Exception exc) {
                throw new AuthException("Unable to login:  " + exc.getMessage());
            }


        }
        return this;
    }

}
