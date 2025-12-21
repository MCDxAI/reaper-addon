# DO NOT PORT

This file tracks features, utilities, and code from the original Reaper 1.19.4 implementation that will **NOT** be ported to the 1.21.11 version.

---

## Utilities

### `MultipartUtility.java`
- **Location (1.19.4)**: `src/main/java/me/ghosttypes/reaper/util/network/MultipartUtility.java`
- **Purpose**: Third-party utility class for sending multipart HTTP POST requests to upload files to a web server
- **Reason for exclusion**: 
  - The `FileHelper.uploadFile()` method that uses this utility is never called anywhere in the codebase
  - Appears to be unused/leftover code from a planned or removed feature
  - No file upload functionality is needed in the 1.21.11 port
- **Dependencies**: Used by `FileHelper.uploadFile()` (also unused)

---

## Services

### `SpotifyService.java`
- **Location (1.19.4)**: `src/main/java/me/ghosttypes/reaper/util/services/SpotifyService.java`
- **Purpose**: Integrates with Spotify to display currently playing track and artist information
- **Reason for exclusion**: 
  - **Replaced by [meteor-mcp-addon](https://github.com/MCDxAI/meteor-mcp-addon)** - a separate project that enables Meteor Client to connect to MCP servers, providing cross-platform Spotify integration and much more
  - Windows-only implementation - uses Windows CMD commands to query Spotify process
  - Already commented out in initialization (`SL.java` has `//SpotifyService.init();`)
  - Limited cross-platform support compared to MCP-based approach
- **Used by**: 
  - `Formatter.java` - for `{songtitle}` and `{artist}` placeholders
  - `ExternalHUD.java` - displays Spotify info in external window (also excluded)
  - `SpotifyHud.java` - in-game HUD module for Spotify
  - `RPC.java` - Discord Rich Presence integration
- **Dependencies**: `TL.java` (scheduled executor service), `OSUtil.java`
- **Note**: The meteor-mcp-addon project provides superior cross-platform functionality for external integrations like Spotify, making this Windows-specific implementation obsolete.

---

## Modules

### External Rendering Modules
These modules render game information in separate Java AWT windows outside the Minecraft client. **Replaced by [meteor-client-webgui](https://github.com/MCDxAI/meteor-client-webgui)** - a browser-based UI with bidirectional communication that eliminates the need for Java GUI/AWT hacks.

#### `ExternalFeed.java`
- **Location (1.19.4)**: `src/main/java/me/ghosttypes/reaper/modules/render/ExternalFeed.java`
- **Purpose**: Renders a kill feed in an external window outside the Minecraft client
- **Reason for exclusion**: 
  - **Replaced by meteor-client-webgui** - provides superior browser-based rendering
  - Minimal implementation (stub/placeholder in 1.19.4)
- **Dependencies**: `ExternalRenderers.java`

#### `ExternalHUD.java`
- **Location (1.19.4)**: `src/main/java/me/ghosttypes/reaper/modules/render/ExternalHUD.java`
- **Purpose**: Renders a comprehensive HUD in an external window with coordinates, biome, ping, TPS, active modules, player list, and Spotify integration
- **Reason for exclusion**: 
  - **Replaced by meteor-client-webgui** - provides superior browser-based rendering with bidirectional communication
  - Depends on `SpotifyService` (also excluded)
- **Dependencies**: `ExternalRenderers.java`, `SpotifyService.java`, `NotificationManager.java`

#### `ExternalNotifications.java`
- **Location (1.19.4)**: `src/main/java/me/ghosttypes/reaper/modules/render/ExternalNotifications.java`
- **Purpose**: Renders notifications in an external window outside the Minecraft client with chroma color support
- **Reason for exclusion**: 
  - **Replaced by meteor-client-webgui** - provides superior browser-based rendering
  - In-game notifications are more practical for most users
- **Dependencies**: `ExternalRenderers.java`, `NotificationManager.java`

### Already Ported Elsewhere

#### `elytrabot` Package
- **Location (1.19.4)**: `src/main/java/me/ghosttypes/reaper/modules/misc/elytrabot/`
- **Purpose**: Automated elytra flight bot with pathfinding, featuring threaded execution, A* pathfinding, multiple flight modes (ElytraFly, PacketFly), and Baritone integration
- **Reason for exclusion**: 
  - **Already ported to [meteor-rejects-v2](https://github.com/MCDxAI/meteor-rejects-v2)** - a 1.21.11 addon containing this and other features
  - Avoids code duplication across addons
- **Package contents**:
  - `ElytraBotThreaded.java` - Main module (935 lines)
  - `events/CancellablePlayerMoveEvent.java` - Custom event
  - `utils/` - AStar pathfinding, DirectionUtil, ElytraFly, PacketFly, MiscUtil, TimerUtil

---

### Unfinished/Incomplete Modules

#### `GhostCA.java`
- **Location (1.19.4)**: `src/main/java/me/ghosttypes/reaper/modules/combat/GhostCA.java`
- **Purpose**: Crystal Aura module (intended for automated crystal PvP combat)
- **Reason for exclusion**: 
  - Completely unfinished - only contains settings definitions with no implementation
  - No event handlers or actual combat logic implemented
  - Only has constructor and field declarations
  - Would require complete implementation from scratch
- **Dependencies**: None (standalone stub)

---

## Deleted Features - Already Restored

These features were deleted in commit `72bcc034246f49fa7db15574a671088327654fb4` (July 24, 2022) but have been successfully restored in the 1.21.11 port, **except** for the following:

### `StreamerMode.java`
- **Location (1.19.4 deleted)**: `src/main/java/me/ghosttypes/reaper/modules/misc/StreamerMode.java`
- **Purpose**: Move sensitive info to external screen for streaming
- **Reason for exclusion**: 
  - **Replaced by [meteor-client-webgui](https://github.com/MCDxAI/meteor-client-webgui)** - a browser-based UI with bidirectional communication
  - Depended on `StreamService` (also excluded)
  - Original relied on Bootstrap mixin which modified headless value - caused bugs and instability
  - WebGUI approach is cleaner and more maintainable
- **Dependencies**: `StreamService.java`, `Bootstrap` mixin
- **Note**: All other deleted features (AuraSync, Stats, Watermark, TextItems, VisualBinds, ModuleSpoof, DebugHud, Greeting, AuraSyncService) have been successfully restored.

### `StreamService.java`
- **Location (1.19.4 deleted)**: `src/main/java/me/ghosttypes/reaper/util/services/StreamService.java`
- **Purpose**: Backend service for external window rendering
- **Reason for exclusion**: 
  - **Replaced by [meteor-client-webgui](https://github.com/MCDxAI/meteor-client-webgui)** - provides superior browser-based rendering
  - Used by `StreamerMode` (also excluded)
  - External window system replaced by modern WebGUI approach
- **Dependencies**: `ExternalWindow.java`, `ExternalRenderers.java`

---

## Notes
- This file should be updated as we discover more code that doesn't need to be ported
- Always verify that code is truly unused before adding it here
- **All deleted features directory contents have been reviewed and either restored or documented here**
