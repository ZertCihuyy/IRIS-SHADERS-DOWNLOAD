package com.iris.shaders.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.io.FileInputStream;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.Minecraft;

public class IrisShadersDownloadClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("iris-shaders-download");
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Starting IRIS SHEDERS DOWNLOAD mod...");
        
        // Register the search and download command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("downloadshader")
                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(context -> {
                        String shaderName = StringArgumentType.getString(context, "name");
                        context.getSource().sendFeedback(Component.literal("§e[IrisDownloader] Searching Modrinth for: " + shaderName));
                        
                        new Thread(() -> {
                            searchAndDownloadShader(shaderName, context.getSource());
                        }).start();
                        
                        return 1;
                    })));
        });
        
        // Add a button to the Iris Shaders Menu
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen.getClass().getSimpleName().equals("ShaderPackScreen")) {
                Screens.getButtons(screen).add(Button.builder(Component.literal("Search Modrinth"), button -> {
                    client.setScreen(new ChatScreen("/downloadshader "));
                }).bounds(10, 10, 120, 20).build());
            }
        });
        
        // Default behavior (download Complementary if no shaders exist)
        Path gameDir = FabricLoader.getInstance().getGameDir();
        File shaderpacksDir = new File(gameDir.toFile(), "shaderpacks");
        if (!shaderpacksDir.exists()) {
            shaderpacksDir.mkdirs();
        }
        
        File targetShader = new File(shaderpacksDir, "ComplementaryReimagined.zip");
        if (!targetShader.exists()) {
            new Thread(() -> {
                try {
                    downloadProjectBySlug("complementary-reimagined", null);
                } catch (Exception e) {
                    LOGGER.error("Failed to download default shader", e);
                }
            }).start();
        }
    }
    
    private void searchAndDownloadShader(String query, FabricClientCommandSource source) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String encodedFacets = URLEncoder.encode("[[\"project_type:shader\"]]", "UTF-8");
            URL searchUrl = new URL("https://api.modrinth.com/v2/search?query=" + encodedQuery + "&facets=" + encodedFacets + "&limit=1");
            
            HttpURLConnection conn = (HttpURLConnection) searchUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "IrisShadersDownloadMod/1.0");
            
            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();
            
            JsonArray hits = response.getAsJsonArray("hits");
            if (hits.size() == 0) {
                source.sendFeedback(Component.literal("§c[IrisDownloader] No shaders found matching: " + query));
                return;
            }
            
            JsonObject firstHit = hits.get(0).getAsJsonObject();
            String slug = firstHit.get("slug").getAsString();
            String title = firstHit.get("title").getAsString();
            
            source.sendFeedback(Component.literal("§a[IrisDownloader] Found: " + title + " (" + slug + "). Downloading..."));
            
            downloadProjectBySlug(slug, source);
            
        } catch (Exception e) {
            LOGGER.error("Failed to search Modrinth", e);
            if (source != null) {
                source.sendFeedback(Component.literal("§c[IrisDownloader] Error occurred while searching. See log."));
            }
        }
    }
    
    private void downloadProjectBySlug(String slug, FabricClientCommandSource source) throws Exception {
        URL apiUrl = new URL("https://api.modrinth.com/v2/project/" + slug + "/version");
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "IrisShadersDownloadMod/1.0");
        
        InputStreamReader reader = new InputStreamReader(conn.getInputStream());
        JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();
        reader.close();
        
        if (versions.size() == 0) {
            if (source != null) source.sendFeedback(Component.literal("§c[IrisDownloader] No files available for this shader."));
            return;
        }
        
        JsonObject latestVersion = versions.get(0).getAsJsonObject();
        JsonArray files = latestVersion.getAsJsonArray("files");
        if (files.size() == 0) return;
        
        JsonObject primaryFile = files.get(0).getAsJsonObject();
        for (JsonElement fileElem : files) {
            JsonObject f = fileElem.getAsJsonObject();
            if (f.has("primary") && f.get("primary").getAsBoolean()) {
                primaryFile = f;
                break;
            }
        }
        
        String downloadUrl = primaryFile.get("url").getAsString();
        String filename = primaryFile.get("filename").getAsString();
        
        Path gameDir = FabricLoader.getInstance().getGameDir();
        File shaderpacksDir = new File(gameDir.toFile(), "shaderpacks");
        if (!shaderpacksDir.exists()) shaderpacksDir.mkdirs();
        
        File targetFile = new File(shaderpacksDir, filename);
        
        HttpURLConnection downloadConn = (HttpURLConnection) new URL(downloadUrl).openConnection();
        downloadConn.setRequestProperty("User-Agent", "IrisShadersDownloadMod/1.0");
        InputStream downloadIs = downloadConn.getInputStream();
        Files.copy(downloadIs, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        downloadIs.close();
        
        if (source != null) {
            source.sendFeedback(Component.literal("§a[IrisDownloader] Downloaded " + filename + " successfully!"));
            source.sendFeedback(Component.literal("§e[IrisDownloader] Applying shader to config..."));
        }
        LOGGER.info("Successfully downloaded shader pack: " + filename);
        
        enableIris(gameDir.toFile(), filename);
        
        if (source != null) {
            source.sendFeedback(Component.literal("§a[IrisDownloader] Shader applied! Press F3+R or open Video Settings to see changes."));
        }
    }
    
    private void enableIris(File gameDir, String packName) {
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
            if (!packName.equals(props.getProperty("shaderPack"))) {
                props.setProperty("shaderPack", packName);
                changed = true;
            }
            
            if (changed) {
                props.store(new FileOutputStream(irisProps), "Modified by IrisShadersDownload");
                LOGGER.info("Enabled shader " + packName + " in iris.properties");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update iris.properties", e);
        }
    }
}
