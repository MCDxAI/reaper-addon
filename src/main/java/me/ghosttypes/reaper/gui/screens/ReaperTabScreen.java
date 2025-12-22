package me.ghosttypes.reaper.gui.screens;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.systems.ReaperConfig;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.util.Util;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Main Reaper settings and info screen.
 * Displays notification settings, general configuration, and addon information.
 */
public class ReaperTabScreen extends WindowTabScreen {

    public ReaperTabScreen(GuiTheme theme, Tab tab) {
        super(theme, tab);
        window.padding = 6;
        window.spacing = 4;
        window.id = "reaper-settings";
    }

    @Override
    public void initWidgets() {
        ReaperConfig config = ReaperConfig.get();

        // Notifications Section
        add(theme.label("Notification Settings")).expandX();
        add(theme.horizontalSeparator()).expandX();
        add(theme.settings(config.settings)).expandX();

        add(theme.verticalSeparator()).expandX();

        // Info Section
        add(theme.label("Reaper Information")).expandX();
        add(theme.horizontalSeparator()).expandX();

        // Version
        WHorizontalList versionRow = add(theme.horizontalList()).expandX().widget();
        versionRow.add(theme.label("Version:"));
        versionRow.add(theme.label(Reaper.VERSION));

        // Author
        WHorizontalList authorRow = add(theme.horizontalList()).expandX().widget();
        authorRow.add(theme.label("Author:"));
        authorRow.add(theme.label("GhostTypes"));

        // GitHub Link Button
        WButton githubButton = add(theme.button("Open GitHub Repository")).expandX().widget();
        githubButton.action = () -> Util.getOperatingSystem().open("https://github.com/MCDxAI/reaper-addon");
    }
}
