package me.ghosttypes.reaper;

import me.ghosttypes.reaper.util.services.SL;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Reaper extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger(Reaper.class);
    public static final Category CATEGORY = new Category("Reaper");
    public static final HudGroup HUD_GROUP = new HudGroup("Reaper");

    // Folder structure
    public static final File FOLDER = new File(MeteorClient.FOLDER, "Reaper");
    public static final File RECORDINGS = new File(FOLDER, "recordings");
    public static final File ASSETS = new File(FOLDER, "assets");
    public static final File USER_ASSETS = new File(ASSETS, "user");

    public static final String MOD_ID = "reaper";
    public static final ModMetadata MOD_META;
    public static final String VERSION;
    public static final String INVITE_LINK = "https://discord.gg/RT5JFMZxvF";

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();
        String versionString = MOD_META.getVersion().getFriendlyString();
        if (versionString.equals("${version}")) versionString = "0.0.0-dev";
        VERSION = versionString;
    }

    @Override
    public void onInitialize() {
        LOG.info("Initializing Reaper {}", VERSION);

        // Create folder structure
        if (!FOLDER.exists()) FOLDER.mkdirs();
        if (!RECORDINGS.exists()) RECORDINGS.mkdirs();
        if (!ASSETS.exists()) ASSETS.mkdirs();
        if (!USER_ASSETS.exists()) USER_ASSETS.mkdirs();

        // Register chat modules
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.NotificationSettings());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.AutoLogin());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.Welcomer());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.ArmorAlert());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.PopCounter());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.AutoEZ());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.ChatTweaks());

        // Register misc modules
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.MultiTask());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.AutoRespawn());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.NoProne());

        // Load services
        SL.load();
    }

    public static void log(String message) {
        LOG.info(message);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "me.ghosttypes.reaper";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("MCDxAI", "reaper-addon");
    }
}
