package me.ghosttypes.reaper.util;

import me.ghosttypes.reaper.Reaper;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * A ResourcePack wrapper that intercepts icon loading requests and serves custom Reaper icons.
 * All other resource requests are delegated to the wrapped ResourcePack.
 */
public class ReaperIconResourcePack implements ResourcePack {
    private final ResourcePack wrapped;

    public ReaperIconResourcePack(ResourcePack wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    @Nullable
    public InputSupplier<InputStream> openRoot(String... segments) {
        // Check if this is a request for an icon file
        if (segments.length >= 2 && segments[0].equals("icons")) {
            String fileName = segments[segments.length - 1];

            // Map to our custom Reaper icons
            if (fileName.equals("icon_16x16.png")) {
                return () -> Reaper.class.getResourceAsStream("/assets/reaper/icon_16x16.png");
            } else if (fileName.equals("icon_32x32.png")) {
                return () -> Reaper.class.getResourceAsStream("/assets/reaper/icon_32x32.png");
            }
            // For other icon sizes, return null to fall back to wrapped pack
        }

        // Delegate all other requests to the wrapped pack
        return wrapped.openRoot(segments);
    }

    @Override
    @Nullable
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        return wrapped.open(type, id);
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
        wrapped.findResources(type, namespace, prefix, consumer);
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return wrapped.getNamespaces(type);
    }

    @Override
    @Nullable
    public <T> T parseMetadata(ResourceMetadataSerializer<T> metadataSerializer) throws IOException {
        return wrapped.parseMetadata(metadataSerializer);
    }

    @Override
    public ResourcePackInfo getInfo() {
        return wrapped.getInfo();
    }

    @Override
    public void close() {
        wrapped.close();
    }
}
