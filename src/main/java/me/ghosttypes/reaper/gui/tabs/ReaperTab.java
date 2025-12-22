package me.ghosttypes.reaper.gui.tabs;

import me.ghosttypes.reaper.gui.screens.ReaperTabScreen;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import net.minecraft.client.gui.screen.Screen;

/**
 * Reaper tab registration for Meteor Client GUI.
 * Creates the "Reaper" tab that displays global Reaper settings and info.
 */
public class ReaperTab extends Tab {

    public ReaperTab() {
        super("Reaper");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new ReaperTabScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof ReaperTabScreen;
    }
}
