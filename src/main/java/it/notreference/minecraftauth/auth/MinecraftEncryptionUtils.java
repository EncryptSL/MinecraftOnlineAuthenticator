package it.notreference.minecraftauth.auth;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Random;

/**
 *
 * MinecraftOnlineAuthenticator by NotReference
 *
 * @author NotReference
 * @version 1.0
 * @destination Spigot
 *
 */



public class MinecraftEncryptionUtils {

    /**
     *
     * Decrypts data.
     *
     * @param cipher
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(Cipher cipher, byte[] data) throws Exception {
        return cipher.doFinal(data);
    }


    /**
     *
     * Returns the server hash.
     *
     * @param sessionId
     * @param sharedSecret
     * @param publicKey
     * @return
     */
    public static String getServerHash(String sessionId, Key sharedSecret, PublicKey publicKey) {
        try {
            byte[] serverHash = getServerHashBytes(sessionId, sharedSecret, publicKey);
            return (new BigInteger(serverHash)).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERRORE";
        }
    }

    /**
     *
     * Generates a random token.
     *
     * @param random
     * @return
     */
    public static byte[] generateToken(Random random) {
        byte[] token = new byte[4];
        random.nextBytes(token);
        return token;
    }


    /**
     *
     * Decrypts the key.
     *
     * @param cipher
     * @param key
     * @return
     * @throw Exception
     */
    public static SecretKey decryptKey(Cipher cipher, byte[] key) throws Exception {
        return new SecretKeySpec(decrypt(cipher, key), "AES");
    }


    /**
     *
     * Returns the server hash
     *
     * @param sessionId
     * @param secretKey
     * @param publicKey
     * @return
     * @throws Exception
     */
    private static byte[] getServerHashBytes(String sessionId, Key secretKey, PublicKey publicKey) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(sessionId.getBytes(StandardCharsets.ISO_8859_1));
        digest.update(secretKey.getEncoded());
        digest.update(publicKey.getEncoded());
        return digest.digest();
    }

    /**
     *
     * Generates a keyPair.
     *
     * @return
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1_024);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception exc) {
            return null;
        }
    }

}
