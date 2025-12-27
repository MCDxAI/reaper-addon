# Reaper Addon API Migration Reference: 1.19.4 → 1.21.11

This document contains the critical API breaking changes discovered during the port of Reaper from Minecraft 1.19.4 to 1.21.11.

**Status:** v0 COMPLETE ✅ (as of 2025-12-26)

---

## Gradle 9 Build Script Compatibility

### Source/Target Compatibility Assignment
```groovy
# Old (fails in Gradle 9):
sourceCompatibility = targetCompatibility = JavaVersion.VERSION_17

# New:
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
```

### archivesBaseName Deprecated
```groovy
# Old:
archivesBaseName = project.archives_base_name

# New:
base {
    archivesName = project.archives_base_name
}
```

### archivesBaseName Reference in Tasks
```groovy
# Old:
jar {
    from("LICENSE") {
        rename { "${it}_${project.archivesBaseName}" }
    }
}

# New:
jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}
```

### JUnit Testing Dependencies
Gradle 9.0+ requires explicit JUnit Platform launcher:

```toml
# gradle/libs.versions.toml
[libraries]
junitPlatformLauncher = { module = "org.junit.platform:junit-platform-launcher" }
```

```kotlin
// build.gradle.kts
dependencies {
    testRuntimeOnly(libs.junitPlatformLauncher)
}
```

### External Dependencies (OkHttp, Kotlin libraries)
Meteor Client 1.21.11 no longer automatically provides transitive dependencies. Must explicitly include:

```toml
[versions]
okhttp = "4.12.0"
okio = "3.6.0"
kotlin-stdlib = "1.9.10"

[libraries]
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okio = { module = "com.squareup.okio:okio", version.ref = "okio" }
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin-stdlib" }
```

```kotlin
dependencies {
    modImplementation(libs.okhttp)
    include(libs.okhttp)
    include(libs.okio)
    include(libs.kotlin.stdlib)
}
```

**Why:** Missing these causes `NoClassDefFoundError: kotlin/jvm/internal/Intrinsics` at runtime.

---

## Minecraft API Changes (1.21.10 → 1.21.11)

### DimensionType API Changes

**Old (1.19.4):**
```java
boolean bedWorks = world.getDimension().bedWorks();
boolean respawnAnchorWorks = world.getDimension().respawnAnchorWorks();
```

**New (1.21.11) - EnvironmentAttributes (UNRELIABLE CLIENT-SIDE):**
```java
import net.minecraft.world.attribute.EnvironmentAttributes;

// ⚠️ WARNING: EnvironmentAttributes is basically empty client-side and not reliable
// For bed spawning:
boolean canSetSpawn = world.getEnvironmentAttributes()
    .getAttributeValue(EnvironmentAttributes.BED_RULE_GAMEPLAY, blockPos)
    .canSetSpawn(world);

// For respawn anchors:
boolean respawnAnchorWorks = world.getEnvironmentAttributes()
    .getAttributeValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS_GAMEPLAY, blockPos);
```

**Recommended (1.21.11) - Direct Dimension Check:**
```java
import net.minecraft.world.World;

// Check if beds explode (Nether/End only)
if (mc.world != null && mc.world.getRegistryKey() == World.OVERWORLD) {
    // Beds DON'T explode in Overworld
}

// Check if anchors explode (everywhere except Nether)
if (mc.world != null && mc.world.getRegistryKey() == World.NETHER) {
    // Anchors DON'T explode in Nether
}
```

**Reason:** EnvironmentAttributes is unreliable on the client side. Use direct dimension registry key comparison instead.

### Camera API Changes

**Old:**
```java
Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
```

**New:**
```java
Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
```

### PlayerEntity Attack Cooldown

**Old:**
```java
mc.player.resetLastAttackedTicks();
```

**New:**
```java
mc.player.resetTicksSinceLastAttack();
```

### Renderer2D.render() Signature

**Old:**
```java
Renderer2D.COLOR.render(null);
```

**New:**
```java
Renderer2D.COLOR.render();  // No parameters
```

### HudRenderer Texture Rendering

**Old (1.19.4):**
```java
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;

GL.bindTexture(IMAGE);
Renderer2D.TEXTURE.begin();
Renderer2D.TEXTURE.texQuad(x, y, w, h, color);
Renderer2D.TEXTURE.render(null);
```

**New (1.21.11):**
```java
// Use HudRenderer's texture method directly
renderer.texture(x, y, w, h, IMAGE, color);
```

**Reason:** HudRenderer now provides simplified texture rendering API that handles binding and rendering internally.

### NetworkingBackend Introduction

**Old:**
```java
ClientConnection.connect(address, useEpoll, debugLogger);
new Bootstrap().group(ClientConnection.CLIENT_IO_GROUP.get())
```

**New:**
```java
import net.minecraft.network.NetworkingBackend;

ClientConnection.connect(address, NetworkingBackend.remote(useEpoll), debugLogger);
new Bootstrap().group(NetworkingBackend.remote(false).getEventLoopGroup())
```

### NativeImageBackedTexture Constructor

**Old (1.19.4):**
```java
NativeImage data = NativeImage.read(inputStream);
NativeImageBackedTexture texture = new NativeImageBackedTexture(data);
mc.getTextureManager().registerTexture(identifier, texture);
```

**New (1.21.11):**
```java
NativeImage data = NativeImage.read(inputStream);
NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> identifier.toString(), data);
mc.getTextureManager().registerTexture(identifier, texture);
```

**Reason:** Constructor now requires a `Supplier<String>` parameter for debugging/logging.

### MeteorToast Builder Pattern

**Old (1.19.4):**
```java
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.minecraft.item.Items;

MeteorToast toast = new MeteorToast(Items.TOTEM_OF_UNDYING, "Title", "Message", 2000);
mc.getToastManager().add(toast);
```

**New (1.21.11):**
```java
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.minecraft.item.Items;

MeteorToast toast = new MeteorToast.Builder("Title")
    .icon(Items.TOTEM_OF_UNDYING)
    .text("Message")
    .build();
mc.getToastManager().add(toast);
```

### Renderer2D.COLOR Quad Rendering in HUD

**Old (1.19.4):**
```java
import meteordevelopment.meteorclient.renderer.Renderer2D;

Renderer2D.COLOR.begin();
Renderer2D.COLOR.quad(x, y, width, height, color);
Renderer2D.COLOR.render(null);
```

**New (1.21.11):**
```java
// Use HudRenderer's quad method directly
renderer.quad(x, y, width, height, color);
```

---

## Minecraft API Changes (1.19.4 → 1.21.x)

### PlayerInventory Direct Field Access Removed

**Old (1.19.4):**
```java
mc.player.getInventory().selectedSlot = slot;
int prevSlot = mc.player.getInventory().selectedSlot;
```

**New (1.21.11):**
```java
InvUtils.swap(slot, false);  // Swap to slot
// For temporary swaps:
InvUtils.swap(slot, false);
// ... do work ...
InvUtils.swapBack();  // Returns to previous slot
```

**Reason:** `selectedSlot` field is now private. Use Meteor's InvUtils for slot management.

### ClientPlayerInteractionManagerAccessor Mixin Methods

**Old (1.19.4):**
```java
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;

float progress = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getBreakingProgress();
BlockPos pos = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getCurrentBreakingBlockPos();
```

**New (1.21.11):**
```java
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;

float progress = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).meteor$getBreakingProgress();
BlockPos pos = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).meteor$getCurrentBreakingBlockPos();
```

**Reason:** Meteor Client now prefixes all mixin accessor methods with `meteor$` to avoid conflicts.

### Entity Position API Changes

**Old (1.19.4):**
```java
Vec3d pos = entity.getPos();
Vec3d playerPos = mc.player.getPos();
```

**New (1.21.11):**
```java
Vec3d pos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
```

**Reason:** `Entity.getPos()` method was removed. Must construct Vec3d from individual coordinates.

### Fall Flying (Elytra) Detection

**Old (1.19.4):**
```java
if (mc.player.isFallFlying()) {
    // Player is gliding with elytra
}
```

**New (1.21.11):**
```java
if (mc.player.isGliding()) {
    // Player is gliding with elytra
}
```

**Reason:** Method renamed from `isFallFlying()` to `isGliding()`.

### Entity Step Height Management

**Old (1.19.4):**
```java
mc.player.setStepHeight(1.0f);  // Allow stepping up full blocks
mc.player.setStepHeight(0.6f);  // Reset to default
```

**New (1.21.11):**
```java
// Step height is now managed internally by the entity
// No direct setter available - entity handles it automatically
```

**Reason:** Step height is now controlled by entity state and cannot be directly modified.

### Player Input Movement Values

**Old (1.19.4):**
```java
double moveForward = mc.player.input.movementForward;
double moveStrafe = mc.player.input.movementSideways;
boolean jumping = mc.player.input.jumping;
```

**New (1.21.11):**
```java
import net.minecraft.util.math.Vec2f;

// Movement input
Vec2f movement = mc.player.input.getMovementInput();
double moveForward = movement.y;  // Forward/backward
double moveStrafe = movement.x;   // Left/right

// Jumping state
boolean jumping = mc.player.input.playerInput.jump();
```

**Reason:** Input fields are now private. Use `getMovementInput()` for movement (returns Vec2f with x=strafe, y=forward) and `playerInput.jump()` for jump state.

### Armor Slot Access

**Old (1.19.4):**
```java
// Iterate armor slots
for (ItemStack armorPiece : mc.player.getArmorItems()) {
    // Process armor
}

// Direct access
ItemStack helmet = mc.player.getInventory().armor.get(3);
```

**New (1.21.11):**
```java
import net.minecraft.entity.EquipmentSlot;

// Access specific armor slots
ItemStack helmet = mc.player.getEquippedStack(EquipmentSlot.HEAD);
ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
ItemStack legs = mc.player.getEquippedStack(EquipmentSlot.LEGS);
ItemStack boots = mc.player.getEquippedStack(EquipmentSlot.FEET);

// Iterate armor slots
for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
    ItemStack armor = mc.player.getEquippedStack(slot);
    // Process armor
}
```

**Reason:** Direct armor list access removed. Use `getEquippedStack(EquipmentSlot)` instead.

### Player Name from GameProfile

**Old (1.19.4):**
```java
String name = player.getGameProfile().getName();
```

**New (1.21.11):**
```java
String name = player.getName().getString();
```

**Reason:** GameProfile API changed. Use `getName().getString()` on the entity directly.

### Entity Previous Position Tracking

**Old (1.19.4):**
```java
double prevY = mc.player.prevY;
```

**New (1.21.11):**
```java
double prevY = mc.player.lastY;
```

**Reason:** Field renamed from `prevY` to `lastY` (also applies to prevX, prevZ → lastX, lastZ).

### WorldRendererAccessor Mixin Methods

**Old (1.19.4):**
```java
import meteordevelopment.meteorclient.mixin.WorldRendererAccessor;

Map<Integer, BlockBreakingInfo> breakingInfos = ((WorldRendererAccessor) mc.worldRenderer).getBlockBreakingInfos();
```

**New (1.21.11):**
```java
import meteordevelopment.meteorclient.mixin.WorldRendererAccessor;

Map<Integer, BlockBreakingInfo> breakingInfos = ((WorldRendererAccessor) mc.worldRenderer).meteor$getBlockBreakingInfos();
```

**Reason:** Meteor Client now prefixes all mixin accessor methods with `meteor$`.

### Block State Replaceability Check

**Old (1.19.4):**
```java
boolean canPlace = BlockHelper.isReplacable(pos);  // Typo in original
```

**New (1.21.11):**
```java
boolean canPlace = BlockHelper.isReplaceable(pos);  // Fixed spelling
```

**Reason:** Fixed spelling from "Replacable" to "Replaceable" to match Minecraft API.

---

## Meteor Client API Changes

### Setting/Config System

**Old (1.19.4) - Module-based Settings:**
```java
// Settings stored in a module
public class NotificationSettings extends ReaperModule {
    public final Setting<Boolean> info = sgGeneral.add(...);
    public final Setting<Boolean> warning = sgGeneral.add(...);
}

// Accessed via:
NotificationSettings ns = Modules.get().get(NotificationSettings.class);
if (ns.info.get()) { ... }
```

**New (1.21.11) - System-based Settings:**
```java
// Settings stored in a custom System
public class ReaperConfig extends System<ReaperConfig> {
    private final SettingGroup sgNotifications = settings.createGroup("Notifications");

    public final Setting<Boolean> info = sgNotifications.add(...);
    public final Setting<Boolean> warning = sgNotifications.add(...);

    public static ReaperConfig get() {
        return Systems.get(ReaperConfig.class);
    }
}

// Accessed via:
ReaperConfig config = ReaperConfig.get();
if (config.info.get()) { ... }
```

**Reason:** Global addon settings should use Meteor's System architecture instead of dummy modules for better organization and persistence.

---

## Additional Resources

- **Official Fabric Documentation:** https://fabricmc.net/wiki/
- **Yarn Mappings:** https://fabricmc.net/versions.html
- **Meteor Client Discord:** For API questions
- **Minecraft Wiki:** For vanilla mechanic changes between versions

---

**Last Updated:** 2025-12-26 (v0 Complete)
