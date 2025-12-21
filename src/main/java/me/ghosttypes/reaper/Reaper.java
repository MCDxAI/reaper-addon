package me.ghosttypes.reaper;

import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reaper extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger(Reaper.class);
    public static final Category CATEGORY = new Category("Reaper");

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

        // Register chat modules
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.NotificationSettings());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.AutoLogin());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.Welcomer());

        // Register misc modules
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.MultiTask());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.AutoRespawn());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.NoProne());
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
