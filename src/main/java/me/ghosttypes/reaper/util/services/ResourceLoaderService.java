package me.ghosttypes.reaper.util.services;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.os.FileHelper;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ResourceLoaderService {

    // User lists
    public static String BETA_DB_URL = "https://pastebin.com/raw/2CyKb1Un";
    public static String USER_DB_URL = "https://pastebin.com/raw/rAevfDYC";

    public static ArrayList<String> DEVELOPERS = new ArrayList<>();
    public static ArrayList<String> BETA = new ArrayList<>();
    public static ArrayList<String> USER = new ArrayList<>();

    public static void initDB(ArrayList<String> db, String url) {
        TL.cached.execute(() -> {
            ArrayList<String> data = FileHelper.downloadList(url);
            if (data == null || data.isEmpty()) return;
            db.clear();
            db.addAll(data);
        });
    }

    public static void initUserDB() {
        // User database initialization - currently disabled
        // DEVELOPERS.addAll(List.of("GhostTypes", "EurekaEffect", "Kiriyaga", "Wide_Cat"));
        // initDB(BETA, BETA_DB_URL);
        // initDB(USER, USER_DB_URL);
    }

    // Logo identifiers - using Identifier.of() for 1.21.11
    public static final Identifier LOGO = Identifier.of("reaper", "cope_1");
    public static final Identifier LOGO_BEAMS = Identifier.of("reaper", "cope_2");
    public static final Identifier LOGO_COLORSPLASH = Identifier.of("reaper", "cope_3");
    public static final Identifier LOGO_GALAXY = Identifier.of("reaper", "cope_4");
    public static final Identifier LOGO_PURPLE = Identifier.of("reaper", "cope_5");
    public static final Identifier LOGO_RED = Identifier.of("reaper", "cope_6");

    public static ArrayList<Resource> serverResources = new ArrayList<>();

    public static void init() {
        initUserDB();
        for (Resource r : serverResources) {
            if (!r.isCached()) r.cache();
        }
        TL.cached.execute(() -> {
            while (mc.world == null) {
                try {
                    Thread.sleep(500);
                } catch (Exception ignored) {}
            }
            Reaper.log("Loading assets");
            serverResources.forEach(Resource::load);
        });
    }

    public static void bindAssetFromURL(Identifier asset, String url) {
        if (mc.world == null || asset == null || url == null) return;
        TL.cached.execute(() -> {
            try {
                var data = NativeImage.read(Http.get(url).sendInputStream());
                mc.getTextureManager().registerTexture(asset, new NativeImageBackedTexture(null, data));
            } catch (Exception ignored) {
            }
        });
    }

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
                        mc.getTextureManager().registerTexture(asset, new NativeImageBackedTexture(null, rsc));
                        is.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    public static class Resource {
        private final Identifier identifier;
        private final String url;
        private final String name;

        public Resource(Identifier identifier, String url, String name) {
            this.identifier = identifier;
            this.url = url;
            this.name = name;
        }

        public Identifier getIdentifier() { return this.identifier; }
        public String getUrl() { return this.url; }
        public String getName() { return this.name; }

        public String getFileName() { return this.name + ".png"; }
        public File getAsFile() { return new File(Reaper.ASSETS, this.getFileName()); }
        public boolean isCached() { return this.getAsFile().exists(); }

        public void cache() {
            TL.cached.execute(() -> {
                try {
                    File outFile = this.getAsFile();
                    if (!outFile.exists()) outFile.createNewFile();
                    InputStream is = Http.get(this.url).sendInputStream();
                    Reaper.log("Downloading asset " + this.name);
                    Reaper.log(outFile.getAbsolutePath());
                    // Using java.nio.Files instead of Apache Commons IO
                    Files.copy(is, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    is.close();
                } catch (Exception ignored) {
                    Reaper.log("Failed to download asset " + this.name);
                }
            });
        }

        public void load() {
            TL.cached.execute(() -> {
                File asset = this.getAsFile();
                if (asset == null || !asset.exists()) return;
                try {
                    InputStream is = new FileInputStream(asset);
                    var rsc = NativeImage.read(is);
                    mc.getTextureManager().registerTexture(this.getIdentifier(), new NativeImageBackedTexture(null, rsc));
                    Reaper.log("Loaded asset " + this.name);
                    is.close();
                } catch (Exception ignored) {
                    Reaper.log("Failed to load asset from cache " + this.name);
                }
            });
        }
    }
}
