package me.ghosttypes.reaper.util.services;

import me.ghosttypes.reaper.Reaper;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ResourceLoaderService {

    public static ArrayList<String> DEVELOPERS = new ArrayList<>();

    public static void init() {
        TL.cached.execute(() -> {
            String response = Http.get("https://raw.githubusercontent.com/MCDxAI/reaper-addon/main/assets/developer-list.json").sendString();
            if (response != null) {
                // Manual JSON parsing to avoid GSON dependency for a simple list
                String[] list = response.replace("[", "").replace("]", "").replace("\"", "").replace("\n", "").split(",");
                DEVELOPERS.clear();
                for (String s : list) {
                    if (!s.isBlank()) DEVELOPERS.add(s.trim());
                }
            }
        });
    }

    // Logo identifiers - using Identifier.of() for 1.21.11
    // Logo identifier
    public static final net.minecraft.util.Identifier LOGO = net.minecraft.util.Identifier.of("reaper", "icon.png");

    /**
     * Bind a texture from a URL to an Identifier.
     * Downloads the image from the URL and registers it with Minecraft's TextureManager.
     * Runs asynchronously in the thread pool.
     *
     * @param asset The Identifier to bind the texture to
     * @param url The URL to download the image from
     */
    public static void bindAssetFromURL(Identifier asset, String url) {
        if (mc.world == null || asset == null || url == null) return;
        TL.cached.execute(() -> {
            try {
                var data = NativeImage.read(Http.get(url).sendInputStream());
                mc.getTextureManager().registerTexture(asset, new NativeImageBackedTexture(() -> asset.toString(), data));
            } catch (Exception ignored) {
                // Silently fail if download or texture registration fails
            }
        });
    }

    /**
     * Bind a texture from a file to an Identifier.
     * Searches for the file in the USER_ASSETS folder and registers it with Minecraft's TextureManager.
     * Supports both exact filename match and filename with .png extension.
     * Runs asynchronously in the thread pool.
     *
     * @param asset The Identifier to bind the texture to
     * @param fileName The filename to search for (with or without .png extension)
     */
    public static void bindAssetFromFile(Identifier asset, String fileName) {
        if (mc.world == null || asset == null || fileName == null) return;
        if (!Reaper.USER_ASSETS.exists()) return;
        TL.cached.execute(() -> {
            File[] userAssets = Reaper.USER_ASSETS.listFiles();
            if (userAssets == null || userAssets.length < 1) return;
            for (File f : userAssets) {
                String fn = f.getName();
                if (fn.equalsIgnoreCase(fileName) || fn.equalsIgnoreCase(fileName + ".png")) {
                    try {
                        InputStream is = new FileInputStream(f);
                        var rsc = NativeImage.read(is);
                        mc.getTextureManager().registerTexture(asset, new NativeImageBackedTexture(() -> asset.toString(), rsc));
                        is.close();
                    } catch (Exception ignored) {
                        // Silently fail if file read or texture registration fails
                    }
                }
            }
        });
    }
}
