package com.iris.shaders.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.io.FileInputStream;

public class IrisShadersDownloadClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("iris-shaders-download");
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Starting IRIS SHEDERS DOWNLOAD mod...");
        
        Path gameDir = FabricLoader.getInstance().getGameDir();
        File shaderpacksDir = new File(gameDir.toFile(), "shaderpacks");
        if (!shaderpacksDir.exists()) {
            shaderpacksDir.mkdirs();
        }
        
        File targetShader = new File(shaderpacksDir, "ComplementaryReimagined.zip");
        if (!targetShader.exists()) {
            downloadShader(targetShader);
        } else {
            LOGGER.info("Shader pack already exists. Skipping download.");
        }
        
        enableIris(gameDir.toFile());
    }
    
    private void downloadShader(File targetFile) {
        LOGGER.info("Downloading Complementary Reimagined shader...");
        try {
            // Fetch the latest version from Modrinth
            URL apiUrl = new URL("https://api.modrinth.com/v2/project/complementary-reimagined/version");
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "IrisShadersDownloadMod/1.0");
            
            InputStream is = conn.getInputStream();
            String response = new String(is.readAllBytes());
            is.close();
            
            // Hacky JSON parsing to find the first download URL
            String urlPrefix = "\"url\":\"";
            int urlStart = response.indexOf(urlPrefix);
            if (urlStart != -1) {
                int urlEnd = response.indexOf("\"", urlStart + urlPrefix.length());
                String downloadUrl = response.substring(urlStart + urlPrefix.length(), urlEnd);
                
                LOGGER.info("Found URL: " + downloadUrl);
                
                HttpURLConnection downloadConn = (HttpURLConnection) new URL(downloadUrl).openConnection();
                downloadConn.setRequestProperty("User-Agent", "IrisShadersDownloadMod/1.0");
                InputStream downloadIs = downloadConn.getInputStream();
                Files.copy(downloadIs, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                downloadIs.close();
                LOGGER.info("Successfully downloaded shader pack!");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to download shader pack", e);
        }
    }
    
    private void enableIris(File gameDir) {
        try {
            File irisProps = new File(gameDir, "config/iris.properties");
            if (!irisProps.getParentFile().exists()) {
                irisProps.getParentFile().mkdirs();
            }
            
            Properties props = new Properties();
            if (irisProps.exists()) {
                props.load(new FileInputStream(irisProps));
            }
            
            boolean changed = false;
            if (!"true".equals(props.getProperty("enableShaders"))) {
                props.setProperty("enableShaders", "true");
                changed = true;
            }
            if (!"ComplementaryReimagined.zip".equals(props.getProperty("shaderPack"))) {
                props.setProperty("shaderPack", "ComplementaryReimagined.zip");
                changed = true;
            }
            
            if (changed) {
                props.store(new FileOutputStream(irisProps), "Modified by IrisShadersDownload");
                LOGGER.info("Enabled shaders in iris.properties");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update iris.properties", e);
        }
    }
}
