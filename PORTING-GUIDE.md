# Reaper Addon Porting Guide: 1.19.4 → 1.21.11

This guide consolidates all known information for porting Reaper from Minecraft 1.19.4 (Meteor Client 0.5.3) to Minecraft 1.21.11.

**Challenge Level:** EXTREME - This is a massive version jump spanning multiple Minecraft releases with significant API changes.

## Porting Approach: Fresh Start

**CRITICAL: We are NOT patching the old code in place.**

The original 1.19.4 source has been moved to `ai_reference/reaper-1.19.4/` and preserved as a reference implementation. We are building a fresh 1.21.11 implementation from `meteor-addon-template`, porting modules and utilities as needed.

**Workflow:**
1. Start with `meteor-addon-template` as the base
2. Reference `ai_reference/reaper-1.19.4/` to understand original functionality
3. Reference `ai_reference/meteor-rejects-v2` and other 1.21.11 addons for current API patterns
4. Implement each module fresh using modern best practices
5. Port logic and algorithms, not code structure

**Why this approach:**
- Version gap is too large for incremental patching
- Original code used non-standard patterns (custom `ML.java` module loader)
- Starting fresh ensures clean, maintainable code following current best practices
- Original source is preserved and searchable just like other reference addons

## Phase 1: Build System Updates

### Required Version Updates

**Target Versions (1.21.11):**
```toml
# gradle/libs.versions.toml (SEE ai_reference/meteor-mcp-addon/gradle/libs.versions.toml for complete example)
minecraft = "1.21.11"
yarn-mappings = "1.21.11+build.3"
fabric-loader = "0.18.2"
loom = "1.14-SNAPSHOT"
meteor = "1.21.11-SNAPSHOT"
```

**IMPORTANT:** Don't create this from scratch - copy and adapt from `ai_reference/meteor-mcp-addon/gradle/libs.versions.toml`

**Current Versions (1.19.4):**
```properties
minecraft_version=1.19.4
yarn_mappings=1.19.4+build.2
loader_version=0.14.19
meteor_version=0.5.3
```

### Critical: Gradle 9.2.0+ Required

**Loom 1.14 requires Gradle 9.2.0 or higher.**

Update `gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.0-bin.zip
```

### Migrate to Modern Build Configuration

**Convert from gradle.properties to gradle/libs.versions.toml:**

The current project uses the old-style `build.gradle` with direct property references. Modern Meteor addons use `build.gradle.kts` with version catalogs.

**PRIMARY REFERENCE:** `ai_reference/meteor-mcp-addon` - This is your first-party addon with a perfect example of:
- Modern `build.gradle.kts` with Kotlin DSL
- Comprehensive `gradle/libs.versions.toml` version catalog
- All correct Gradle 9 patterns
- Complex dependency management with shading/includes
- Proper Java 21 configuration

**Also check:** `ai_reference/meteor-addon-template` for minimal structure.

### Gradle 9 Build Script Compatibility

**Issue 1: Source/Target Compatibility Assignment**
```groovy
# Old (will fail in Gradle 9):
sourceCompatibility = targetCompatibility = JavaVersion.VERSION_17

# New:
java {
    sourceCompatibility = JavaVersion.VERSION_21  // Note: Java 21 required for 1.21.11
    targetCompatibility = JavaVersion.VERSION_21
}
```

**Issue 2: archivesBaseName deprecated**
```groovy
# Old:
archivesBaseName = project.archives_base_name

# New:
base {
    archivesName = project.archives_base_name
}
```

**Issue 3: archivesBaseName reference in tasks**
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

**Issue 4: Protected exec() method**

If you have nested `exec()` calls in Exec tasks (unlikely in Reaper, but check external rendering tasks):
```kotlin
# Old (fails in Gradle 9):
register<Exec>("taskName") {
    doFirst {
        exec { ... }  // ❌ Protected method
    }
}

# New: Split into separate tasks with dependencies
register<Exec>("installTask") {
    onlyIf { !file("someDir").exists() }
}
register<Exec>("mainTask") {
    dependsOn("installTask")
}
```

### Update fabric.mod.json

```json
{
  "depends": {
    "java": ">=21",           // Update from >=17
    "minecraft": ">=1.21.11",  // Update from >=1.19.3
    "meteor-client": "*"
  }
}
```

### JUnit Testing Dependencies (if applicable)

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

If using any Kotlin-based libraries (OkHttp, etc.), you MUST explicitly include transitive dependencies:

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

**Why:** Meteor Client 1.21.11 no longer automatically provides these transitive dependencies. Missing them causes `NoClassDefFoundError: kotlin/jvm/internal/Intrinsics` at runtime.

## Phase 2: API Breaking Changes (1.21.10 → 1.21.11)

These are changes between 1.21.10 and 1.21.11. There will be MANY MORE changes from 1.19.4 → 1.21.11 that aren't documented here yet.

### 1. DimensionType API → EnvironmentAttributes

**Old API (used in 1.19.4 and earlier 1.21.x):**
```java
boolean bedWorks = world.getDimension().bedWorks();
boolean respawnAnchorWorks = world.getDimension().respawnAnchorWorks();
```

**New API (1.21.11):**
```java
import net.minecraft.world.attribute.EnvironmentAttributes;

// For bed spawning:
boolean canSetSpawn = world.getEnvironmentAttributes()
    .getAttributeValue(EnvironmentAttributes.BED_RULE_GAMEPLAY, blockPos)
    .canSetSpawn(world);

// For respawn anchors:
boolean respawnAnchorWorks = world.getEnvironmentAttributes()
    .getAttributeValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS_GAMEPLAY, blockPos);
```

**Affected Modules:** Any module checking dimension properties for beds, respawn anchors, or dimension-specific behavior.

### 2. Camera API Changes

**Old:**
```java
Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
```

**New:**
```java
Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
```

**Affected Modules:** Rendering modules, ESP modules.

### 3. PlayerEntity Attack Cooldown

**Old:**
```java
mc.player.resetLastAttackedTicks();
```

**New:**
```java
mc.player.resetTicksSinceLastAttack();
```

**Affected Modules:** Combat modules (QuickMend, AnchorGod, BedGod, etc.).

### 4. Renderer2D.render() Signature

**Old:**
```java
Renderer2D.COLOR.render(null);
```

**New:**
```java
Renderer2D.COLOR.render();  // No parameters
```

**Affected Modules:** HUD elements, rendering modules.

### 4a. HudRenderer Texture Rendering

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

**Affected Modules:** CustomImage, Watermark, and HUD modules rendering textures.

### 5. GuiTheme.module() Signature

**Old:**
```java
public abstract WWidget module(Module module);
```

**New:**
```java
public WWidget module(Module module) {
    return module(module, module.title);
}

public abstract WWidget module(Module module, String title);
```

**Impact:** If you have custom GUI themes or module rendering, update method signatures and add `title` parameter handling.

### 6. NetworkingBackend Introduction

Minecraft 1.21.11 introduced `NetworkingBackend` to abstract network I/O.

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

**Affected Modules:** PacketFly or any module using direct network connections.

### 7. NativeImageBackedTexture Constructor

**Old (1.19.4):**
```java
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

NativeImage data = NativeImage.read(inputStream);
NativeImageBackedTexture texture = new NativeImageBackedTexture(data);
mc.getTextureManager().registerTexture(identifier, texture);
```

**New (1.21.11):**
```java
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

NativeImage data = NativeImage.read(inputStream);
NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> identifier.toString(), data);
mc.getTextureManager().registerTexture(identifier, texture);
```

**Reason:** Constructor now requires a `Supplier<String>` parameter (typically a lambda returning the identifier string) for debugging/logging purposes.

**Affected Modules:** CustomImage, ResourceLoaderService, and any code loading custom textures.

### 8. MeteorToast Builder Pattern

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

**Reason:** MeteorToast now uses Builder pattern for construction instead of direct constructor.

**Affected Modules:** Notifications HUD, PopCounter, ArmorAlert, and any module showing toast notifications.

### 9. Renderer2D.COLOR Quad Rendering in HUD

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

**Reason:** HudRenderer provides simplified quad rendering that handles begin/render internally.

**Affected Modules:** Notifications HUD and HUD modules rendering colored rectangles.

## Phase 3: Additional Breaking Changes (1.19.4 → 1.21.x)

These are changes that occurred across the major version jump. **Documented from actual porting experience.**

### 1. PlayerInventory Direct Field Access Removed

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

**Affected Modules:** AntiSurround, QuickMend, and any module that manually swaps hotbar slots.

### 2. ClientPlayerInteractionManagerAccessor Mixin Methods

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

**Affected Modules:** AntiSurround, SpeedMine, AutoMine, and any module tracking block breaking.

### 3. Entity Position API Changes

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

**Affected Modules:** AntiSurround, TargetStrafe, and any module using entity positions.

### 4. Fall Flying (Elytra) Detection

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

**Reason:** Method renamed from `isFallFlying()` to `isGliding()` for clarity.

**Affected Modules:** ReaperLongJump, ElytraFly, and any module checking elytra state.

### 5. Entity Step Height Management

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

**Affected Modules:** ReaperLongJump, Step, and movement modules that modified step height.

### 6. Player Input Movement Values

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

**Affected Modules:** ReaperLongJump, TargetStrafe, Speed, and movement-based modules.

### 7. Armor Slot Access

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

**Affected Modules:** SelfTrapPlus, AutoTotem, ArmorNotifier, and armor-related modules.

### 8. Player Name from GameProfile

**Old (1.19.4):**
```java
String name = player.getGameProfile().getName();
```

**New (1.21.11):**
```java
String name = player.getName().getString();
```

**Reason:** GameProfile API changed. Use `getName().getString()` on the entity directly.

**Affected Modules:** AntiSurround, PopCounter, and modules displaying player names.

### 9. Entity Previous Position Tracking

**Old (1.19.4):**
```java
double prevY = mc.player.prevY;
```

**New (1.21.11):**
```java
double prevY = mc.player.lastY;
```

**Reason:** Field renamed from `prevY` to `lastY` (also applies to prevX, prevZ → lastX, lastZ).

**Affected Modules:** ReaperSurround, NoFall, and modules tracking position changes.

### 10. WorldRendererAccessor Mixin Methods

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

**Affected Modules:** ReaperSurround, AutoCity, and modules tracking block breaking.

### 11. Block State Replaceability Check

**Old (1.19.4):**
```java
boolean canPlace = BlockHelper.isReplacable(pos);  // Typo in original
```

**New (1.21.11):**
```java
boolean canPlace = BlockHelper.isReplaceable(pos);  // Fixed spelling
```

**Reason:** Fixed spelling from "Replacable" to "Replaceable" to match Minecraft API.

**Affected Modules:** ReaperSurround and placement modules using this utility.

### Packet System Restructuring

Many packet classes have been moved, renamed, or restructured. Check `ai_reference/meteor-client` and `ai_reference/meteor-rejects-v2` for current packet usage.

### Block Placement Mechanics

Block placement validation and mechanics have changed significantly. Reference modern implementations in:
- `ai_reference/Trouser-Streak` (has complex placement modules)
- `ai_reference/meteor-rejects-v2`

### Entity Tracking Changes

Entity tracking and player list management has evolved. Check Meteor Client's current patterns.

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

**Affected Code:** NotificationProxy mixin, any code accessing global addon settings.

### Event System

Meteor's event system may have changes. Verify:
- Event registration patterns
- Event priorities
- Custom events (Reaper has several: `DeathEvent`, `InteractEvent`, etc.)

## Phase 4: Mixin Compatibility

**Current Mixins in reaper.mixins.json (1.21.11):**
- `MinecraftClientMixin` - Core client hooks
- `HeldItemRendererAccessor` - Access held item animation fields
- `HeldItemRendererMixin` - Fires UpdateHeldItemEvent for OldAnimations
- `NotificationProxy` - Intercepts ChatUtils to route messages to notifications

**Removed/Not Ported:**
- `Bootstrap` - Not needed in fresh implementation
- `meteor.FakePlayerMixin` - Check if still needed
- `meteor.MeteorBootstrap` - Not needed (was disabled in original)

**Actions Required:**
1. Verify each mixin target class still exists with same name
2. Verify mixin target methods still exist (may have been renamed/remapped)
3. Check method signatures haven't changed
4. Update Mixin annotations if needed
5. Test that mixins apply correctly (check game logs for mixin errors)

**Reference:** Check `ai_reference/meteor-client` and other reference addons for current mixin patterns.

## Phase 5: Module-by-Module Porting Strategy

### Recommended Order

**0. Start with Fresh Template (CRITICAL):**
- Copy `ai_reference/meteor-addon-template` as the base
- Update to modern `build.gradle.kts` (reference: `ai_reference/meteor-mcp-addon/build.gradle.kts`)
- Create `gradle/libs.versions.toml` (reference: `ai_reference/meteor-mcp-addon/gradle/libs.versions.toml`)
- Update Gradle wrapper to 9.2.0
- Update all version numbers for 1.21.11
- **Get a successful build** before writing any addon code

**1. Basic Infrastructure:**
- Implement `Reaper.java` (main addon class) using modern patterns
- Register custom categories (reference: `ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/Reaper.java` for category names)
- **DO NOT** port `ML.java` - use standard Meteor module registration instead
- Set up folder structure (reference original `Reaper.FOLDER`, `Reaper.ASSETS`, etc.)
- Create basic `fabric.mod.json` and `reaper.mixins.json`

**2. Utilities (Port as Needed):**
- Start with utilities actually used by modules you're porting
- Reference `ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/util/` for original implementations
- Implement using current Minecraft/Meteor APIs
- Common utilities likely needed:
  - `util/world/` - BlockHelper, CombatHelper, PlayerHelper, RotationHelper
  - `util/combat/` - CityUtils, DamageCalculator
  - `util/misc/` - MathUtil, MessageUtil
  - `util/render/` - Rendering helpers

**3. Simple Modules (Chat):**
- AutoLogin
- Welcomer
- ChatTweaks
- AutoEZ
- PopCounter

**4. Misc Modules:**
- AutoRespawn
- MultiTask
- RPC
- ConfigTweaker

**5. Rendering Modules:**
- HUD elements first (simpler)
- ESP modules
- External rendering (most complex - may need significant rework)

**6. Combat Modules (Most Complex):**
- ReaperSurround
- SmartHoleFill
- AnchorGod
- BedGod
- QuickMend
- etc.

**7. ElytraBot (Most Complex Subsystem):**
- **USE ai_reference/meteor-rejects-v2 AS PRIMARY REFERENCE**
- Compare original implementation in `ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/modules/misc/elytrabot/`
- With ported version in meteor-rejects-v2
- This subsystem is identical/very similar to the one already ported
- Adapt patterns from meteor-rejects-v2's working implementation

### For Each Module

**Fresh Implementation Process:**

1. **Read original module** in `ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/modules/`
2. **Understand the functionality** - What does it do? What settings does it have? What are the key features?
3. **Search ai_reference/** for similar 1.21.11 modules:
   - Check meteor-rejects-v2 first (especially for combat/misc modules)
   - Check Trouser-Streak for complex combat modules
   - Check meteor-client for base patterns
4. **Create new module file** in your fresh `src/` directory
5. **Implement using modern patterns:**
   - Use current Meteor module base classes
   - Use current setting types and patterns
   - Use current event system
   - Use current Minecraft APIs
6. **Port the logic, not the code structure:**
   - Understand the algorithm/behavior
   - Rewrite using current APIs
   - Don't copy-paste old code
7. **Test build** after each module
8. **Test in-game** when possible
9. **Iterate** - Fix errors, refine functionality

## Phase 6: Build and Test

### Build Commands

```bash
# Clean build
./gradlew clean build

# Just build
./gradlew build

# Run in development
./gradlew runClient
```

### Build Output

JAR location: `build/libs/reaper-[version].jar`

### Testing Checklist

After successful build:

- [ ] Addon loads without errors
- [ ] All modules appear in Meteor GUI
- [ ] Categories are correctly created
- [ ] HUD elements render correctly
- [ ] Services initialize properly
- [ ] External windows work (if applicable)
- [ ] Test combat modules functionality
- [ ] Test ElytraBot pathfinding
- [ ] Test notification system
- [ ] No crashes in common scenarios

## Reference Priority

When you need to look something up during the fresh implementation:

**1. STARTING POINT: meteor-addon-template** (`ai_reference/meteor-addon-template`)
- **Use this as your base** - Copy this to start your fresh implementation
- Clean, minimal structure following current best practices
- Correct modern patterns for addon structure
- Template for `fabric.mod.json`, mixins, and main addon class

**2. BUILD CONFIGURATION: meteor-mcp-addon** (`ai_reference/meteor-mcp-addon`)
- **YOUR first-party addon with perfect build setup**
- **USE THIS as primary reference for:**
  - `build.gradle.kts` with Kotlin DSL
  - `gradle/libs.versions.toml` version catalog
  - Gradle 9 compatibility patterns
  - Complex dependency management (shading, includes)
  - Java 21 configuration
  - JUnit Platform launcher setup
- Contains all correct versions for 1.21.11
- Shows how to handle Kotlin/OkHttp dependencies properly

**3. ORIGINAL FUNCTIONALITY: reaper-1.19.4** (`ai_reference/reaper-1.19.4`)
- **THE ORIGINAL SOURCE CODE** - Complete 1.19.4 implementation
- **Use this to understand WHAT to port:**
  - Module functionality and features
  - Setting configurations
  - Algorithms and logic
  - Utility implementations
  - Event system usage
- **DO NOT copy the code structure** - Non-standard patterns (ML.java, etc.)
- **DO reference the logic** - Understand behavior, then reimplement using modern patterns

**4. CODE PORTING REFERENCE: meteor-rejects-v2** (`ai_reference/meteor-rejects-v2`)
- YOUR first-party addon already ported to 1.21.11
- Contains ElytraBot (same/similar code as Reaper)
- **USE THIS to understand HOW to port:**
  - Compare with reaper-1.19.4's ElytraBot to see the migration
  - See which APIs replaced old ones
  - Understand current event patterns
  - See how modules are structured in 1.21.11

**5. meteor-client** (`ai_reference/meteor-client`)
- Official Meteor Client source
- Authoritative API reference
- Current patterns and best practices

**6. Trouser-Streak** (`ai_reference/Trouser-Streak`)
- Large, complex addon (66 modules)
- Many combat/grief modules similar to Reaper's
- Recently maintained
- Good for finding alternative implementation patterns

**7. Other reference addons** (Numby-hack, Nora-Tweaks, meteor-villager-roller)
- Diverse examples
- Different implementation patterns

## Known Challenges

### 1. Version Gap (1.19.4 → 1.21.11)
This is a MASSIVE jump. Expect many undocumented breaking changes. Plan to discover and fix issues incrementally.

### 2. External Rendering System
Reaper's `ExternalWindow` system is complex and may have platform-specific issues with newer Minecraft versions.

### 3. Services System
The custom service loader (`SL.java`) and thread pool (`TL.java`) may need updates for newer Minecraft threading models.

### 4. Mixin Compatibility
With such a large version gap, expect mixin targets to have changed significantly. Some mixins may need complete rewrites.

### 5. ElytraBot Complexity
The threaded pathfinding system with A* is complex. **Good news:** It's already ported in meteor-rejects-v2, so you can reference the working implementation directly.

## Expected Timeline

**Conservative Estimate:**
- Build system updates: 1-2 hours
- Infrastructure porting: 2-4 hours
- Simple modules: 4-8 hours
- Complex modules: 8-16 hours
- Combat modules: 8-16 hours
- ElytraBot: 4-8 hours (with meteor-rejects-v2 reference)
- Testing and bug fixes: 8+ hours

**Total:** 35-54+ hours of work

**Reality:** Likely longer due to undocumented breaking changes and unexpected issues.

## Progress Tracking

**Last Updated:** 2025-12-20

### Completed ✅

- [x] **Build system updated** - Gradle 9.2.0, Kotlin DSL, version catalog
- [x] **Core infrastructure ported** - ReaperModule, TL, NotificationManager
- [x] **Basic utilities ported** - MathUtil, SystemTimer, Task, AnglePos, OSUtil, FileHelper
- [x] **First module registered** - NotificationSettings

### In Progress 🚧

- [ ] **Remaining utilities** - MessageUtil, Formatter, PlayerHelper, RotationHelper, BlockHelper, CombatHelper
- [ ] **Chat modules** - AutoLogin, Welcomer, ChatTweaks, AutoEZ, PopCounter, etc.
- [ ] **Misc modules** - AutoRespawn, MultiTask, RPC, ConfigTweaker, etc.

### Pending ⏳

- [ ] **HUD modules** - CustomImage, Notifications, Killfeed, SpotifyHud
- [ ] **Render modules** - ExternalHUD, ExternalFeed, ReaperHoleESP
- [ ] **Combat modules** - ReaperSurround, SmartHoleFill, AnchorGod, BedGod, QuickMend, etc.
- [ ] **ElytraBot** - Threaded pathfinding subsystem
- [ ] **Services** - ResourceLoaderService, GlobalManager, MessageUtil service
- [ ] **External rendering** - ExternalWindow system
- [ ] **Deleted features restoration** - AuraSync, Stats, Watermark, TextItems, VisualBinds, etc.
- [ ] **In-game testing** - Full functionality verification

### Statistics

- **Files Ported:** 60+
- **Lines of Code:** ~6100+
- **Modules Registered:** 38 (9 chat, 12 misc, 7 combat, 11 HUD)
- **Services:** 6 (TL, SL, NotificationManager, GlobalManager, ResourceLoaderService, AuraSyncService)
- **Build Status:** ✅ Working
- **Last Updated:** 2025-12-22

## Additional Resources

- **Official Fabric Documentation:** https://fabricmc.net/wiki/
- **Yarn Mappings:** https://fabricmc.net/versions.html
- **Meteor Client Discord:** For API questions
- **Minecraft Wiki:** For vanilla mechanic changes between versions

---

**Last Updated:** 2025-12-20
**Source Documents:**
- METEOR-UPDATE-GUIDE.md (1.21.10 → 1.21.11 changes)
- GRADLE-9-EXEC-COMPATIBILITY.md (Gradle 9 compatibility)
- METEOR-REJECTS-V2-UPDATE.md (meteor-rejects-v2 port log)
