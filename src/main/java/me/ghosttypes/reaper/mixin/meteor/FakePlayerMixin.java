package me.ghosttypes.reaper.mixin.meteor;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.AnglePos;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.player.FakePlayer;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Enhances Meteor Client's FakePlayer module with recording and playback capabilities.
 * Allows users to record their movements and replay them on fake players.
 *
 * @author GhostTypes
 */
@Mixin(FakePlayer.class)
public class FakePlayerMixin {
    // Constants
    @Unique
    private static final String FILE_EXTENSION = ".rec";
    @Unique
    private static final String TIMESTAMP_FORMAT = "MM.dd.HH.mm.ss";
    @Unique
    private static final String CSV_DELIMITER = ",";
    @Unique
    private static final int EXPECTED_CSV_FIELDS = 5;
    @Unique
    private static final int RECORDING_NAME_MIN_WIDTH = 400;
    @Unique
    private static final int HEAD_ROTATION_INTERPOLATION_STEPS = 3;

    @Shadow(remap = false)
    @Final
    private SettingGroup sgGeneral;

    @Shadow(remap = false)
    @Final
    public Setting<String> name;

    @Unique
    private Setting<Boolean> loopSetting = null;
    @Unique
    private boolean isRecording = false;
    @Unique
    private boolean isPlaying = false;
    @Unique
    private final List<AnglePos> currentRecording = new ArrayList<>();
    @Unique
    private final List<AnglePos> originalRecording = new ArrayList<>();
    @Unique
    private WTextBox recordingNameInput;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onInit(CallbackInfo ci) {
        loopSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("loop")
            .description("Whether to loop the recorded movement after playing.")
            .defaultValue(true)
            .build());
    }

    @Inject(method = "getWidget", at = @At("RETURN"), cancellable = true, remap = false)
    private void onGetWidget(GuiTheme theme, CallbackInfoReturnable<WWidget> info) {
        WHorizontalList buttonRow = theme.horizontalList();
        WVerticalList layout = theme.verticalList();

        WButton startButton = buttonRow.add(theme.button("Start Recording")).widget();
        WButton stopButton = buttonRow.add(theme.button("Stop Recording")).widget();
        WButton playButton = buttonRow.add(theme.button("Play Recording")).widget();
        WButton importButton = buttonRow.add(theme.button("Import")).widget();
        WButton exportButton = buttonRow.add(theme.button("Export")).widget();

        playButton.action = this::onPlayRecording;
        startButton.action = this::onStartRecording;
        stopButton.action = this::onStopRecording;
        importButton.action = this::onImportRecording;
        exportButton.action = this::onExportRecording;

        layout.add(info.getReturnValue());
        layout.add(theme.horizontalSeparator()).expandX();
        layout.add(buttonRow);

        WHorizontalList inputRow = theme.horizontalList();
        recordingNameInput = inputRow.add(theme.textBox("Recording Name"))
            .minWidth(RECORDING_NAME_MIN_WIDTH)
            .expandX()
            .widget();
        layout.add(inputRow);

        info.setReturnValue(layout);
    }

    @Unique
    private void onStartRecording() {
        currentRecording.clear();
        originalRecording.clear();
        isRecording = true;
        isPlaying = false;
        ChatUtils.info("Recording started...");
    }

    @Unique
    private void onStopRecording() {
        // Stop playback if it's running
        if (isPlaying) {
            isPlaying = false;
            // Restore currentRecording from backup so it's not partially consumed
            currentRecording.clear();
            currentRecording.addAll(originalRecording);
            ChatUtils.info("Playback stopped. (Recording preserved: " + originalRecording.size() + ")");
            return;
        }

        // Stop recording and backup
        if (isRecording) {
            isRecording = false;
            originalRecording.clear();
            originalRecording.addAll(currentRecording);
            ChatUtils.info("Recording stopped. (Size: " + currentRecording.size() + ")");
            return;
        }

        // Neither recording nor playing
        ChatUtils.warning("Nothing to stop.");
    }

    @Unique
    private void onPlayRecording() {
        if (currentRecording.isEmpty() && originalRecording.isEmpty()) {
            ChatUtils.error("No recording to play! Record something first.");
            return;
        }
        if (currentRecording.isEmpty() && !originalRecording.isEmpty()) {
            currentRecording.addAll(originalRecording);
        }
        isRecording = false;
        isPlaying = true;
        ChatUtils.info("Playing recording...");
    }

    @Unique
    private void onImportRecording() {
        String recordingName = recordingNameInput.get();
        if (recordingName == null || recordingName.trim().isEmpty()) {
            ChatUtils.error("Please enter a recording name!");
            return;
        }

        ensureRecordingsDirectoryExists();

        File recordingFile = new File(Reaper.RECORDINGS, recordingName + FILE_EXTENSION);
        if (!recordingFile.exists()) {
            ChatUtils.error("Recording not found: " + recordingName);
            return;
        }

        importRecordingFromFile(recordingFile);
    }

    @Unique
    private void onExportRecording() {
        if (currentRecording.isEmpty()) {
            ChatUtils.error("No recording to export!");
            return;
        }
        exportRecordingToFile(currentRecording);
    }

    @Unique
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (isRecording && mc.player != null) {
            Vec3d pos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            currentRecording.add(new AnglePos(pos, mc.player.getYaw(), mc.player.getPitch()));
        }

        if (isPlaying) {
            if (!currentRecording.isEmpty()) {
                AnglePos angles = currentRecording.remove(0);
                FakePlayerManager.forEach(entity -> {
                    entity.updateTrackedPositionAndAngles(angles.getPos(), angles.getYaw(), angles.getPitch());
                    entity.updateTrackedHeadRotation(angles.getYaw(), HEAD_ROTATION_INTERPOLATION_STEPS);
                });
            } else {
                if (!originalRecording.isEmpty() && loopSetting.get()) {
                    currentRecording.addAll(originalRecording);
                } else {
                    isPlaying = false;
                }
            }
        }
    }

    @Unique
    private void ensureRecordingsDirectoryExists() {
        if (!Reaper.RECORDINGS.exists()) {
            if (!Reaper.RECORDINGS.mkdirs()) {
                Reaper.log("Failed to create recordings directory: " + Reaper.RECORDINGS.getPath());
            }
        }
    }

    @Unique
    private void importRecordingFromFile(File file) {
        currentRecording.clear();
        int lineNumber = 0;
        int skippedLines = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(CSV_DELIMITER);
                if (data.length != EXPECTED_CSV_FIELDS) {
                    Reaper.log("Skipping malformed line " + lineNumber + ": expected " + EXPECTED_CSV_FIELDS + " fields, got " + data.length);
                    skippedLines++;
                    continue;
                }

                try {
                    float yaw = Float.parseFloat(data[0]);
                    float pitch = Float.parseFloat(data[1]);
                    double x = Double.parseDouble(data[2]);
                    double y = Double.parseDouble(data[3]);
                    double z = Double.parseDouble(data[4]);
                    currentRecording.add(new AnglePos(new Vec3d(x, y, z), yaw, pitch));
                } catch (NumberFormatException e) {
                    Reaper.log("Skipping line " + lineNumber + " with invalid number format: " + line);
                    skippedLines++;
                }
            }

            originalRecording.clear();
            originalRecording.addAll(currentRecording);

            if (skippedLines > 0) {
                ChatUtils.warning("Imported " + file.getName() + " (Size: " + currentRecording.size() + ", Skipped: " + skippedLines + " malformed lines)");
            } else {
                ChatUtils.info("Imported " + file.getName() + " (Size: " + currentRecording.size() + ")");
            }
        } catch (IOException e) {
            Reaper.log("Error reading recording file: " + file.getName());
            e.printStackTrace();
            ChatUtils.error("Error reading recording: " + file.getName());
            currentRecording.clear();
        }
    }

    @Unique
    private void exportRecordingToFile(List<AnglePos> recording) {
        ensureRecordingsDirectoryExists();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));
        File outputFile = new File(Reaper.RECORDINGS, timestamp + FILE_EXTENSION);

        // Handle same-second collision by appending counter
        int counter = 1;
        while (outputFile.exists()) {
            outputFile = new File(Reaper.RECORDINGS, timestamp + "_" + counter + FILE_EXTENSION);
            counter++;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (AnglePos anglePos : recording) {
                Vec3d pos = anglePos.getPos();
                writer.write(anglePos.getYaw() + CSV_DELIMITER +
                            anglePos.getPitch() + CSV_DELIMITER +
                            pos.x + CSV_DELIMITER +
                            pos.y + CSV_DELIMITER +
                            pos.z);
                writer.newLine();
            }

            ChatUtils.info("Exported recording: " + outputFile.getName() + " (Size: " + recording.size() + ")");
        } catch (IOException e) {
            Reaper.log("Error exporting recording: " + outputFile.getName());
            e.printStackTrace();
            ChatUtils.error("Error exporting recording: " + outputFile.getName());
        }
    }
}
