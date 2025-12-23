package me.ghosttypes.reaper.util.player;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Combat statistics tracking
 */
public class Stats {
    public static int kills = 0;
    public static int deaths = 0;
    public static int killStreak = 0;
    public static int highscore = 0;
    public static ArrayList<String> killfeed = new ArrayList<>();
    public static long startTime = System.currentTimeMillis();

    public static void reset() {
        kills = 0;
        deaths = 0;
        killStreak = 0;
        highscore = 0;
    }

    public static String getPlayTime() {
        return me.ghosttypes.reaper.util.misc.MathUtil.formatDuration(
            System.currentTimeMillis() - startTime, 
            "HH:mm:ss"
        );
    }

    public static String getKD() {
        if (deaths == 0) return String.valueOf(kills);
        double rawKD = (double) kills / deaths;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(rawKD);
    }

    public static void addKill(String kill) {
        kills++;
        killStreak++;
        if (killStreak > highscore) highscore = killStreak;
        killfeed.removeIf(k -> k.contains(kill));
        killfeed.add(kill);
        if (killfeed.size() > 10) killfeed.remove(0);
    }

    public static void addDeath() {
        deaths++;
        killStreak = 0;
    }
}
