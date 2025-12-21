package me.ghosttypes.reaper.util.services;

import meteordevelopment.meteorclient.utils.network.Http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceLoaderService {

    public static ArrayList<String> DEVELOPERS = new ArrayList<>();

    public static void init() {
        TL.cached.execute(() -> {
            String response = Http.get("https://raw.githubusercontent.com/MCDxAI/reaper-addon/main/assets/developer-list.json").sendString();
            if (response != null) {
                // Manual JSON parsing to avoid GSON dependency for a simple list
                String[] list = response.replace("[", "").replace("]", "").replace("\"", "").replace("\n", "").split(",");
                DEVELOPERS.clear();
                for (String s : list) {
                    if (!s.isBlank()) DEVELOPERS.add(s.trim());
                }
            }
        });
    }

    // Logo identifiers - using Identifier.of() for 1.21.11
    // Logo identifier
    public static final net.minecraft.util.Identifier LOGO = net.minecraft.util.Identifier.of("reaper", "icon.png");
}
