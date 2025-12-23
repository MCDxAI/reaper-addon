package me.ghosttypes.reaper;

import me.ghosttypes.reaper.gui.tabs.ReaperTab;
import me.ghosttypes.reaper.modules.hud.*;
import me.ghosttypes.reaper.systems.ReaperConfig;
import me.ghosttypes.reaper.util.services.ServiceLoader;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
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
    public static final File USER_ASSETS = new File(FOLDER, "user_assets");
    public static final File RECORDINGS = new File(FOLDER, "recordings");

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
        if (!USER_ASSETS.exists()) USER_ASSETS.mkdirs();
        if (!RECORDINGS.exists()) RECORDINGS.mkdirs();

        // Register Reaper systems
        Systems.add(new ReaperConfig());

        // Register Reaper GUI tab
        Tabs.add(new ReaperTab());

        // Register chat modules
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.AutoLogin());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.Welcomer());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.ArmorAlert());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.PopCounter());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.AutoEZ());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.ChatTweaks());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.BedAlerts());
        Modules.get().add(new me.ghosttypes.reaper.modules.chat.HoleAlert());

        // Register combat modules
        Modules.get().add(new me.ghosttypes.reaper.modules.combat.AntiSurround());
        Modules.get().add(new me.ghosttypes.reaper.modules.combat.SmartHoleFill());
        Modules.get().add(new me.ghosttypes.reaper.modules.combat.ReaperLongJump());
        Modules.get().add(new me.ghosttypes.reaper.modules.combat.AnchorGod());
        Modules.get().add(new me.ghosttypes.reaper.modules.combat.BedGod());
        Modules.get().add(new me.ghosttypes.reaper.modules.combat.ReaperSurround());

        // Register misc modules
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.MultiTask());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.AutoRespawn());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.NoProne());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.NoDesync());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.ChorusPredict());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.AntiAim());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.OldAnimations());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.StrictMove());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.RPC());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.OneTap());
        Modules.get().add(new me.ghosttypes.reaper.modules.misc.WideScaffold());

        // Register render modules
        Modules.get().add(new me.ghosttypes.reaper.modules.render.ReaperHoleESP());


        // Register HUD elements
        Hud.get().register(AuraSync.INFO);
        Hud.get().register(Stats.INFO);
        Hud.get().register(Watermark.INFO);
        Hud.get().register(TextItems.INFO);
        Hud.get().register(VisualBinds.INFO);
        Hud.get().register(ModuleSpoof.INFO);
        Hud.get().register(DebugHud.INFO);
        Hud.get().register(Greeting.INFO);
        Hud.get().register(CustomImage.INFO);
        Hud.get().register(Killfeed.INFO);
        Hud.get().register(Notifications.INFO);

        // Load services
        ServiceLoader.load();
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
