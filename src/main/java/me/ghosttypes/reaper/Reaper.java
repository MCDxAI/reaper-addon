package me.ghosttypes.reaper;

import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reaper extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger(Reaper.class);
    public static final Category CATEGORY = new Category("Reaper");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Reaper Addon");

        // Register custom module category
        Modules.registerCategory(CATEGORY);

        // TODO: Register modules here as they are ported
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
