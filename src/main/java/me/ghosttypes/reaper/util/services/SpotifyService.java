package me.ghosttypes.reaper.util.services;

/**
 * Stub class for Spotify service functionality
 * TODO: Full implementation to be ported later
 */
public class SpotifyService {
    public static String currentTrack = null;
    public static String currentArtist = null;

    public static boolean hasMedia() {
        return currentTrack != null && !currentTrack.isBlank();
    }
}
