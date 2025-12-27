# Reaper Addon Architecture Reference

This document describes the **original 1.19.4 source code** architecture located in `ai_reference/reaper-1.19.4/`. 

**NOTE:** This is for reference when understanding the original implementation - **DO NOT** copy these patterns directly into the new 1.21.11 implementation. Use modern Meteor addon patterns instead.

---

## Entry Point

`ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/Reaper.java` - Main addon class extending `MeteorAddon`
- Defines addon metadata, version, and folder structure
- Implements `onInitialize()` to load modules and services
- Implements `onRegisterCategories()` to register custom module categories

## Module System

### Module Loader: `ML.java` (Module Loader)
- Centralized module registration and loading
- Creates three custom categories: "Reaper", "Reaper Misc", "Windows"
- Load methods: `loadR()` (combat), `loadM()` (misc/chat/render), `loadW()` (windows), `loadH()` (HUD)
- Uses `addModules()` helper to register modules with Meteor's module system

**⚠️ DO NOT replicate this pattern** - Use standard Meteor module registration instead.

### Module Organization

```
modules/
├── chat/       - Chat-related modules (AutoEZ, PopCounter, etc.)
├── combat/     - PvP combat modules (AnchorGod, BedGod, Surround, etc.)
├── hud/        - HUD elements (CustomImage, Notifications, SpotifyHud)
├── misc/       - Miscellaneous utilities (RPC, PacketFly, etc.)
│   └── elytrabot/ - Complex pathfinding system with A* implementation
└── render/     - Rendering modules (ExternalHUD, HoleESP, etc.)
```

### Base Module Pattern

Most modules extend `ReaperModule` which provides common functionality (check `util/misc/ReaperModule.java`).

## Service System

### Service Loader: `SL.java` (Service Loader)
- Initializes background services at addon startup
- Thread pool manager: `TL.java` (Thread Loader)
- Services:
  - `GlobalManager` - Event subscription and global state
  - `ResourceLoaderService` - Asset downloading and management
  - `NotificationManager` - Notification system
  - `SpotifyService` - Spotify integration (currently disabled)
  - `WellbeingService` - (currently disabled)

## Utility Structure

```
util/
├── combat/     - Combat helpers (CityUtils)
├── misc/       - General utilities (MathUtil, MessageUtil, ModuleHelper)
├── network/    - Network utilities (MultipartUtility, PacketManager)
├── os/         - OS-specific operations (FileHelper, OSUtil)
├── player/     - Player interactions (Interactions, Stats)
├── render/     - Rendering helpers (ExternalWindow, Renderers)
├── services/   - Service implementations (see Service System above)
└── world/      - World interaction (BlockHelper, CombatHelper, RotationHelper)
```

### Key Utilities

- `BlockHelper` / `BlockBuilder` - Block placement abstractions
- `CombatHelper` / `DamageCalculator` - Combat calculations
- `RotationHelper` - Server-side rotation management
- `PlayerHelper` - Player state queries
- `ExternalWindow` - Native window rendering system

## Mixin System

Mixins are defined in `reaper.mixins.json`:
- `MinecraftClientMixin` - Core client hooks
- `Bootstrap` - Early initialization
- `meteor.FakePlayerMixin` - Meteor integration for fake players
- `meteor.MeteorBootstrap` - Meteor startup hooks
- `meteor.NotificationProxy` - Notification system integration

### Mixin Accessors

- `HeldItemRendererAccessor` - Access private fields in held item renderer
- `HeldItemRendererMixin` - Modify held item rendering behavior

## Event System

Custom events in `events/`:
- `DeathEvent` - Player death detection
- `InteractEvent` - Interaction tracking
- `UpdateHeldItemEvent` - Item switch detection
- `CancellablePlayerMoveEvent` - Movement control (originally used by ElytraBot - see DO_NOT_PORT.md)

## External Rendering (**NOT BEING PORTED**)

**See DO_NOT_PORT.md** - Replaced by meteor-client-webgui

The original addon included a sophisticated external window system:
- `ExternalWindow.java` - Base window implementation using native rendering
- `ExternalRenderers.java` - Rendering pipeline for external windows
- Modules: `ExternalHUD`, `ExternalFeed`, `ExternalNotifications`

This allowed rendering game information in separate OS windows outside Minecraft.

## Complex Subsystems (**NOT BEING PORTED**)

### ElytraBot (`modules/misc/elytrabot/`)

**See DO_NOT_PORT.md** - Already ported to meteor-rejects-v2

- Threaded pathfinding system with A* algorithm
- Multiple flight modes (ElytraFly, PacketFly)
- Custom movement events and utilities
- Timer and direction utilities for precise movement

## Important Implementation Notes

These patterns existed in the original code - understand them for reference, but implement using modern patterns:

- **Folder structure**: Original created `Reaper.FOLDER`, `Reaper.RECORDINGS`, `Reaper.ASSETS`, `Reaper.USER_ASSETS` on initialization
- **Thread management**: Used custom `TL.java` thread pool for async operations
- **Service system**: Custom `SL.java` service loader initialized background services
- **Discord RPC**: Had auto-enablement logic
- **Services**: Spotify and Wellbeing services were disabled in original
- **External windows**: Required native OS support for external rendering
- **Module loader**: Non-standard `ML.java` module loader - **DO NOT replicate this pattern**

## Asset Management

- `Reaper.FOLDER` - Main addon folder in Meteor directory
- `Reaper.RECORDINGS` - Recordings subfolder
- `Reaper.ASSETS` - Asset storage
- `Reaper.USER_ASSETS` - User-customizable assets

## Custom HUD Group

`Reaper.HUD_GROUP` - Dedicated HUD category for addon HUD elements

---

**Last Updated:** 2025-12-26
