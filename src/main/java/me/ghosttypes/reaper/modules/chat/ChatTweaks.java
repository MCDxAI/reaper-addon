package me.ghosttypes.reaper.modules.chat;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.Formatter;
import me.ghosttypes.reaper.util.misc.ReaperModule;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class ChatTweaks extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> emotes = sgGeneral.add(new BoolSetting.Builder()
        .name("emotes")
        .description("Enables the Reaper emote system.")
        .defaultValue(false)
        .build());

    public final Setting<Boolean> customPrefix = sgGeneral.add(new BoolSetting.Builder()
        .name("custom-prefix")
        .description("Lets you set a custom prefix.")
        .defaultValue(false)
        .build());

    public final Setting<String> prefixText = sgGeneral.add(new StringSetting.Builder()
        .name("custom-prefix-text")
        .description("Override the [Reaper] prefix.")
        .defaultValue("Reaper")
        .visible(customPrefix::get)
        .build());

    public final Setting<Boolean> customPrefixColor = sgGeneral.add(new BoolSetting.Builder()
        .name("custom-prefix-color")
        .description("Lets you set a custom prefix color.")
        .defaultValue(false)
        .build());

    public final Setting<Boolean> chromaPrefix = sgGeneral.add(new BoolSetting.Builder()
        .name("chroma-prefix")
        .description("Use chroma/rainbow colors for prefix.")
        .defaultValue(false)
        .build());

    public final Setting<Double> chromaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("chroma-speed")
        .description("Speed of the chroma color.")
        .defaultValue(0.09)
        .min(0.01)
        .sliderMax(5)
        .decimalPlaces(2)
        .visible(chromaPrefix::get)
        .build());

    public final Setting<SettingColor> prefixColor = sgGeneral.add(new ColorSetting.Builder()
        .name("prefix-color")
        .description("Color of the prefix text.")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(customPrefixColor::get)
        .build());

    public final Setting<Boolean> themeBrackets = sgGeneral.add(new BoolSetting.Builder()
        .name("apply-to-brackets")
        .description("Apply the current prefix theme to the brackets.")
        .defaultValue(false)
        .build());

    public final Setting<Boolean> customBrackets = sgGeneral.add(new BoolSetting.Builder()
        .name("custom-brackets")
        .description("Set custom brackets.")
        .defaultValue(false)
        .build());

    public final Setting<String> leftBracket = sgGeneral.add(new StringSetting.Builder()
        .name("left-bracket")
        .description("Left bracket character.")
        .defaultValue("[")
        .visible(customBrackets::get)
        .build());

    public final Setting<String> rightBracket = sgGeneral.add(new StringSetting.Builder()
        .name("right-bracket")
        .description("Right bracket character.")
        .defaultValue("]")
        .visible(customBrackets::get)
        .build());

    private final RainbowColor prefixChroma = new RainbowColor();

    public ChatTweaks() {
        super(Reaper.CATEGORY, "chat-tweaks", "Various chat improvements.");
    }

    @Override
    public void onActivate() {
        ChatUtils.registerCustomPrefix("me.ghosttypes.reaper", this::getPrefix);
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;
        if (emotes.get()) message = Formatter.applyEmotes(message);
        event.message = message;
    }

    public Text getPrefix() {
        MutableText logo = Text.empty();
        MutableText prefix = Text.empty();
        String logoT = customPrefix.get() ? prefixText.get() : "Reaper";

        if (customPrefixColor.get() && !chromaPrefix.get()) {
            logo = logo.append(Text.literal(logoT).setStyle(logo.getStyle().withColor(TextColor.fromRgb(prefixColor.get().getPacked()))));
        } else if (chromaPrefix.get() && !customPrefixColor.get()) {
            prefixChroma.setSpeed(chromaSpeed.get() / 100);
            for (int i = 0; i < logoT.length(); i++) {
                logo = logo.append(Text.literal(String.valueOf(logoT.charAt(i)))
                    .setStyle(logo.getStyle().withColor(TextColor.fromRgb(prefixChroma.getNext().getPacked()))));
            }
        } else {
            logo = logo.append(logoT);
            logo = logo.setStyle(logo.getStyle().withFormatting(Formatting.RED));
        }

        String left = customBrackets.get() ? leftBracket.get() : "[";
        String right = customBrackets.get() ? rightBracket.get() + " " : "] ";

        if (themeBrackets.get()) {
            if (customPrefixColor.get() && !chromaPrefix.get()) {
                prefix = prefix.setStyle(prefix.getStyle().withColor(TextColor.fromRgb(prefixColor.get().getPacked())));
            } else if (chromaPrefix.get() && !customPrefixColor.get()) {
                prefixChroma.setSpeed(chromaSpeed.get() / 100);
                prefix = prefix.setStyle(prefix.getStyle().withColor(TextColor.fromRgb(prefixChroma.getNext().getPacked())));
            }
        } else {
            prefix = prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));
        }

        prefix = prefix.append(left);
        prefix = prefix.append(logo);
        prefix = prefix.append(right);

        return prefix;
    }
}
