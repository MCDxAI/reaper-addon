package me.ghosttypes.reaper.util.misc;

import me.ghosttypes.reaper.systems.ReaperConfig;
import me.ghosttypes.reaper.util.services.NotificationManager;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.Text;

public class ReaperModule extends Module {

    public ReaperModule(Category category, String name, String description) {
        super(category, name, description);
    }

    // Redirect info/warning/error to notifications (if enabled)

    @Override
    public void info(Text message) {
        ReaperConfig config = ReaperConfig.get();
        if (config != null && config.info.get()) NotificationManager.addNotification(message.getString());
        if (config != null && config.hide.get()) return;
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.sendMsg(title, message);
    }

    @Override
    public void warning(String message, Object... args) {
        ReaperConfig config = ReaperConfig.get();
        if (config != null && config.warning.get()) NotificationManager.addNotification(message);
        if (config != null && config.hide.get()) return;
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.warningPrefix(title, message, args);
    }

    @Override
    public void error(String message, Object... args) {
        ReaperConfig config = ReaperConfig.get();
        if (config != null && config.error.get()) NotificationManager.addNotification(message);
        if (config != null && config.hide.get()) return;
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.errorPrefix(title, message, args);
    }
}
