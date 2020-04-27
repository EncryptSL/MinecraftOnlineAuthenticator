package it.notreference.minecraftauth;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
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

public class ConfigurationManager {

   private final MinecraftOnlineAuthenticator main;

    public ConfigurationManager(MinecraftOnlineAuthenticator main) {

        this.main = main;
    }

    /**
     * Loads a config if exists.
     *
     * @param file
     * @return
     */
    public YamlConfiguration loadConfig(File file) {
        try {
            return YamlConfiguration.loadConfiguration(file);
        } catch(Exception exc) {
            throw new RuntimeException("Unable to load a configuration with specified file.");
        }
    }

    /**
     * Copies file data to another.
     *
     * @param stream
     * @param toFile
     * @throws IOException
     */
    public boolean copyFileData(InputStream stream, File toFile) throws IOException {

        if(!toFile.exists())
            toFile.createNewFile();

        try {

         Files.copy(stream, toFile.toPath());
         return true;

        } catch(Exception exc) {

            return false;

        }


    }

    /**
     *
     * Encodes a string in base64
     *
     * @param stringa
     * @return
     */
    public String base64_encode(String stringa) {

        return Base64.getEncoder().encodeToString(stringa.getBytes());

    }

    /**
     *
     * Copies the config file to a temp file.
     *
     * @param config
     * @return
     */
    public File copyConfigFileToTemp(File config) {

        try(InputStream inputStream = main.getResource(config.getName())) {

            byte[] randomNameBytes = new byte[6];
            Random random = new Random();
            random.nextBytes(randomNameBytes);
            String name = new String(randomNameBytes);
            String randomName = base64_encode(name);
            File tempFile = new File(main.getDataFolder(), randomName + ".yml");

            copyFileData(inputStream, tempFile);
            return tempFile;

        } catch(Exception exc) {
            return null;
        }

    }


}
