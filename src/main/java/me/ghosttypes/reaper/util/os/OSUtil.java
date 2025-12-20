package me.ghosttypes.reaper.util.os;

import me.ghosttypes.reaper.Reaper;

import javax.swing.*;

public class OSUtil {

    public static boolean isWindows = false;

    public static void init() {
        if (getOS().equals(OSType.Windows)) isWindows = true;
    }

    public static OSType getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) return OSType.Windows;
        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) return OSType.Linux;
        if (osName.contains("mac")) return OSType.Mac;
        return OSType.Unsupported;
    }

    public enum OSType {
        Windows,
        Linux,
        Mac,
        Unsupported
    }

    public static void messageBox(String title, String msg, int type) {
        try {
            JOptionPane.showMessageDialog(null, msg, title, type);
        } catch (Exception ignored) {
        }
    }

    public static void info(String msg) {
        messageBox("Reaper " + Reaper.VERSION, msg, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warning(String msg) {
        messageBox("Reaper " + Reaper.VERSION, msg, JOptionPane.WARNING_MESSAGE);
    }

    public static void error(String msg) {
        messageBox("Reaper " + Reaper.VERSION, msg, JOptionPane.ERROR_MESSAGE);
    }
}
