# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Reaper** is a Meteor Client addon being ported from Minecraft 1.19.4 to 1.21.11. This is a complex, multi-version jump that requires extensive API updates and refactoring.

**Original Source**: Minecraft 1.19.4 / Meteor Client 0.5.3 (preserved in `ai_reference/reaper-1.19.4/`)
**Target State**: Minecraft 1.21.11 / Latest Meteor Client (fresh implementation in `src/`)

## Porting Approach

**CRITICAL: We are starting from scratch, not patching old code.**

The original 1.19.4 source has been moved to `ai_reference/reaper-1.19.4/` as a reference implementation. We are building a fresh 1.21.11 implementation based on `meteor-addon-template`, porting modules and utilities as needed.

**Why this approach:**
- The version gap (1.19.4 → 1.21.11) is too large for incremental patching
- Old code used non-standard patterns (custom module loader, obscure organization)
- Starting fresh with modern best practices ensures clean, maintainable code
- Original source is preserved as searchable reference (just like other addons in `ai_reference/`)

## Critical Porting Context

This is one of the most challenging ports due to:
- Multiple Minecraft version changes (1.19.4 → 1.21.11)
- Significant Meteor Client API changes
- Fabric API evolution across versions
- Mixin compatibility updates required
- Non-standard patterns in original code

**READ PORTING-GUIDE.md FIRST** - Contains comprehensive porting instructions, known breaking changes, build system updates, and step-by-step migration strategy.

### AI Reference Directory

The `ai_reference/` folder contains critical reference implementations:
- **reaper-1.19.4** - **ORIGINAL SOURCE CODE** - The complete original Reaper addon from Minecraft 1.19.4 - use this to understand original functionality, logic, and feature implementations when porting modules
- **meteor-mcp-addon** - **FIRST-PARTY REFERENCE** - **USE THIS FOR BUILD CONFIGURATION** - Perfect example of modern build.gradle.kts, gradle/libs.versions.toml, Gradle 9 patterns, complex dependency management
- **meteor-rejects-v2** - **FIRST-PARTY REFERENCE** - Successfully ported to 1.21.11, contains ElytraBot implementation (likely same/similar code as this addon's ElytraBot)
- **meteor-client** - Official Meteor Client source (current API)
- **Trouser-Streak** - Large addon (66 modules, 511 stars, updated to 1.21.11)
- **meteor-addon-template** - Official template with current best practices
- **meteor-villager-roller** - Clean, focused example addon
- **Numby-hack** - 22 modules with diverse feature implementations
- **Nora-Tweaks** - Recently updated with multi-version support

**IMPORTANT**: The `ai_reference/` directory is in `.gitignore` but you MUST still read and search it whenever you need reference implementations. It provides the fastest and most accurate way to understand current API usage patterns, gradle configurations, and implementation approaches for 1.21.11.

**meteor-rejects-v2 is especially valuable** because it was previously ported from an older version to 1.21.11 and contains the same ElytraBot code that exists in this addon. Use it as the primary reference for understanding how existing Reaper code should be updated.

**meteor-mcp-addon is the authoritative build configuration reference** - it has perfect examples of modern Gradle 9 patterns, Kotlin DSL, version catalogs, and complex dependency management with shading.

Always search `ai_reference/` when:
- **Understanding original Reaper functionality** - check reaper-1.19.4 to see how modules worked originally
- **Updating build configuration** - check meteor-mcp-addon's build.gradle.kts and gradle/libs.versions.toml
- **Migrating to Gradle 9** - meteor-mcp-addon shows all correct patterns
- **Porting modules** - check reaper-1.19.4 for original logic, then meteor-rejects-v2 or other addons for 1.21.11 patterns
- Updating dependencies or gradle configuration
- Migrating modules to new Meteor Client APIs
- Understanding how mixins should be structured for 1.21.11
- Finding examples of specific module types or features
- Checking fabric.mod.json structure for current versions
- **Porting ElytraBot** - compare reaper-1.19.4's implementation with meteor-rejects-v2's ported version

## Code Search Tools

**CRITICAL: Use code-search-mcp MCP server tools for ALL code searches.**

This project has the `code-search-mcp` MCP server enabled, which provides significantly more powerful search capabilities than traditional Grep/Glob tools. You MUST proactively use these tools instead of basic terminal commands.

### Available Tools (Always load via MCPSearch first)

**Stack Detection:**
- `mcp__code-search-mcp__detect_stacks` - Automatically detect project technology stacks
  - Use at project start to understand the codebase structure
  - Helps identify build systems, languages, frameworks

**File Search:**
- `mcp__code-search-mcp__search_files` - Fast file search with pattern matching
  - Supports wildcards, extensions, directory filters
  - Much faster than find/ls for large codebases
  - Example: Find all Java files, search by name patterns

**Code Search:**
- `mcp__code-search-mcp__search_text` - Search file contents with advanced filtering
- `mcp__code-search-mcp__search_symbols` - Find classes, methods, functions, interfaces
- `mcp__code-search-mcp__search_ast_pattern` - AST-based pattern matching
- `mcp__code-search-mcp__search_ast_rule` - Complex AST rule searching

**Indexing:**
- `mcp__code-search-mcp__refresh_index` - Rebuild search index for a directory
- `mcp__code-search-mcp__cache_stats` - Check index cache status

**Dependency Analysis:**
- `mcp__code-search-mcp__analyze_dependencies` - Map out code dependencies

### Usage Pattern

**ALWAYS:**
1. Load tools via `MCPSearch` with `select:mcp__code-search-mcp__<tool_name>` first
2. Use code-search-mcp tools instead of Grep/Glob/find when searching code
3. Search across both main source and `ai_reference/` directories

**Example workflow:**
```
# Instead of: Grep for "ElytraBot"
# Do: Load mcp__code-search-mcp__search_symbols, then search for "ElytraBot" class

# Instead of: Glob for "*.gradle"
# Do: Load mcp__code-search-mcp__search_files with extension filter

# Instead of: Grep for method usage patterns
# Do: Load mcp__code-search-mcp__search_text or search_symbols
```

### When to Use Each Tool

- **detect_stacks**: First action in a new project/directory
- **search_files**: Finding files by name/extension (replaces Glob)
- **search_text**: Finding code patterns in file contents (replaces Grep)
- **search_symbols**: Finding class/method/function definitions
- **search_ast_pattern**: Complex code structure searches (e.g., all methods with specific annotations)
- **analyze_dependencies**: Understanding module relationships before refactoring

### Performance Benefits

- Indexed search is 10-100x faster than grep on large codebases
- Can search across 1000+ files (including ai_reference/) in milliseconds
- AST-aware searching eliminates false positives from comments/strings

## Minecraft Fabric Development Toolkit

**CRITICAL: Always invoke the `minecraft-fabric-dev` skill when starting development/coding work.**

This project has the `minecraft-fabric-dev` skill available, which provides a comprehensive toolkit specifically designed for Fabric Minecraft mod development and porting. You MUST invoke this skill at the beginning of any development session.

### The minecraft-fabric-dev Skill

**When to invoke:**
- At the start of any coding/development session
- Before porting modules from 1.19.4 to 1.21.11
- When you encounter Minecraft API changes or compatibility issues
- When working with mixins, mappings, or Fabric-specific APIs
- When you need to understand Minecraft source code

**How to invoke:**
Use the Skill tool: `Skill(skill="minecraft-fabric-dev")`

**What it provides:**
- Comprehensive guidance for Fabric mod development
- Porting assistance from other mod loaders (Forge, NeoForge)
- Integration with minecraft-dev MCP server tools
- Mixin validation and analysis
- Mapping lookup and remapping utilities
- Access to Minecraft decompiled source code

### minecraft-dev MCP Tools Integration

The `minecraft-fabric-dev` skill works hand-in-hand with the `minecraft-dev` MCP server tools. These provide programmatic access to Minecraft internals:

**Decompilation & Source Access:**
- `mcp__minecraft-dev__get_minecraft_source` - Get decompiled Minecraft source code
- `mcp__minecraft-dev__decompile_minecraft_version` - Decompile specific MC version
- `mcp__minecraft-dev__list_minecraft_versions` - List available versions
- `mcp__minecraft-dev__search_minecraft_code` - Search Minecraft source code

**Version Comparison:**
- `mcp__minecraft-dev__compare_versions` - Compare API changes between versions
- `mcp__minecraft-dev__compare_versions_detailed` - Detailed version diff

**Mappings & Remapping:**
- `mcp__minecraft-dev__find_mapping` - Look up class/method/field mappings
- `mcp__minecraft-dev__remap_mod_jar` - Remap mod JARs to different mappings
- `mcp__minecraft-dev__get_registry_data` - Get game registry information

**Mixin & Access Widener Support:**
- `mcp__minecraft-dev__analyze_mixin` - Validate and analyze mixin code
- `mcp__minecraft-dev__validate_access_widener` - Check access widener syntax

**Indexing & Search:**
- `mcp__minecraft-dev__index_minecraft_version` - Build search index for MC version
- `mcp__minecraft-dev__search_indexed` - Search indexed Minecraft code
- `mcp__minecraft-dev__get_documentation` - Get Minecraft API documentation
- `mcp__minecraft-dev__search_documentation` - Search documentation

**Mod Analysis:**
- `mcp__minecraft-dev__analyze_mod_jar` - Analyze existing mod structure
- `mcp__minecraft-dev__decompile_mod_jar` - Decompile mod JARs
- `mcp__minecraft-dev__search_mod_code` - Search mod source code
- `mcp__minecraft-dev__index_mod` - Index mod for searching
- `mcp__minecraft-dev__search_mod_indexed` - Search indexed mod code

### Workflow

**Standard development workflow:**
1. **Start session**: Invoke `minecraft-fabric-dev` skill
2. **Load MCP tools**: Use MCPSearch to load needed minecraft-dev tools
3. **Research**: Use search/decompile tools to understand Minecraft APIs
4. **Compare versions**: Use compare_versions to see what changed between 1.19.4 and 1.21.11
5. **Code**: Make changes to your mod
6. **Validate**: Use analyze_mixin to verify mixin code
7. **Build**: Use gradle-mcp-server tools to build and test

### Example Use Cases

**Porting a module from 1.19.4 to 1.21.11:**
1. Invoke `minecraft-fabric-dev` skill for porting guidance
2. Use `compare_versions` to see API changes
3. Use `find_mapping` to locate renamed classes/methods
4. Use `search_minecraft_code` to find new API usage patterns
5. Use `analyze_mixin` to validate mixin targets still exist

**Understanding Minecraft internals:**
1. Invoke `minecraft-fabric-dev` skill
2. Use `get_minecraft_source` to view decompiled code
3. Use `search_minecraft_code` to find specific implementations
4. Use `get_registry_data` for game data structures

**Checking reference implementations:**
1. Use `analyze_mod_jar` on ai_reference mods
2. Use `search_mod_code` to find specific patterns
3. Compare with your implementation

## Build System

### Target Dependencies (1.21.11)

When setting up the new implementation, use these target versions:

```toml
# gradle/libs.versions.toml (reference: meteor-mcp-addon)
minecraft = "1.21.11"
yarn-mappings = "1.21.11+build.3"
fabric-loader = "0.18.2"
loom = "1.14-SNAPSHOT"
meteor = "1.21.11-SNAPSHOT"
java = "21"
```

### Original Dependencies (1.19.4 - Reference Only)

The original source in `ai_reference/reaper-1.19.4/` used:

```properties
minecraft_version=1.19.4
yarn_mappings=1.19.4+build.2
loader_version=0.14.19
meteor_version=0.5.3
java=17
loom=0.12-SNAPSHOT
```

### Gradle Tasks

**CRITICAL: Use gradle-mcp-server MCP tools instead of terminal commands for Gradle operations.**

This project has the `gradle-mcp-server` MCP server enabled. You MUST use these tools instead of running `./gradlew` commands directly via Bash.

#### Available Gradle Tools (Always load via MCPSearch first)

**Project Information:**
- `mcp__gradle-mcp-server__gradle_version` - Get Gradle and JVM version info
- `mcp__gradle-mcp-server__gradle_project_info` - Get project structure, plugins, configurations
- `mcp__gradle-mcp-server__gradle_subprojects` - List all subprojects in multi-project builds
- `mcp__gradle-mcp-server__gradle_check_wrapper` - Verify Gradle wrapper status

**Task Management:**
- `mcp__gradle-mcp-server__gradle_list_tasks` - List all available Gradle tasks
- `mcp__gradle-mcp-server__gradle_execute` - Execute any Gradle task with arguments

**Common Operations:**
- `mcp__gradle-mcp-server__gradle_build` - Build the project (replaces `./gradlew build`)
- `mcp__gradle-mcp-server__gradle_test` - Run tests (replaces `./gradlew test`)
- `mcp__gradle-mcp-server__gradle_dependencies` - Show dependency tree

**Maintenance:**
- `mcp__gradle-mcp-server__gradle_stop_daemon` - Stop Gradle daemon
- `mcp__gradle-mcp-server__gradle_clear_cache` - Clear Gradle caches

#### Usage Pattern

**ALWAYS:**
1. Load tools via `MCPSearch` with `select:mcp__gradle-mcp-server__<tool_name>` first
2. Use gradle-mcp-server tools instead of `./gradlew` commands
3. Use `gradle_execute` for custom tasks not covered by dedicated tools

**Examples:**
```
# Instead of: ./gradlew build
# Do: Load and call mcp__gradle-mcp-server__gradle_build

# Instead of: ./gradlew clean
# Do: Load gradle_execute, then execute task "clean"

# Instead of: ./gradlew runClient
# Do: Load gradle_execute, then execute task "runClient"

# Instead of: ./gradlew tasks --all
# Do: Load and call mcp__gradle-mcp-server__gradle_list_tasks

# Instead of: ./gradlew dependencies
# Do: Load and call mcp__gradle-mcp-server__gradle_dependencies
```

#### Benefits

- Structured output that's easier to parse programmatically
- Better error handling and reporting
- Consistent interface across different Gradle versions
- No need to worry about shell escaping or path issues

**Reference commands (for documentation only):**
```bash
# Build the addon
./gradlew build

# Clean build artifacts
./gradlew clean

# Run Minecraft with the addon (development)
./gradlew runClient
```

### Maven Repositories

```groovy
maven { url = "https://maven.meteordev.org/releases" }
maven { url = "https://maven.meteordev.org/snapshots" }
```

## Original Code Architecture (Reference Only)

**NOTE:** This section describes the original 1.19.4 source code located in `ai_reference/reaper-1.19.4/`. This is for reference when porting - **DO NOT** copy these patterns directly into the new implementation. Use modern Meteor addon patterns instead.

### Entry Point

`ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/Reaper.java` - Main addon class extending `MeteorAddon`
- Defines addon metadata, version, and folder structure
- Implements `onInitialize()` to load modules and services
- Implements `onRegisterCategories()` to register custom module categories

### Module System

**Module Loader**: `ML.java` (Module Loader)
- Centralized module registration and loading
- Creates three custom categories: "Reaper", "Reaper Misc", "Windows"
- Load methods: `loadR()` (combat), `loadM()` (misc/chat/render), `loadW()` (windows), `loadH()` (HUD)
- Uses `addModules()` helper to register modules with Meteor's module system

**Module Organization**:
```
modules/
├── chat/       - Chat-related modules (AutoEZ, PopCounter, etc.)
├── combat/     - PvP combat modules (AnchorGod, BedGod, Surround, etc.)
├── hud/        - HUD elements (CustomImage, Notifications, SpotifyHud)
├── misc/       - Miscellaneous utilities (RPC, PacketFly, etc.)
│   └── elytrabot/ - Complex pathfinding system with A* implementation
└── render/     - Rendering modules (ExternalHUD, HoleESP, etc.)
```

**Base Module Pattern**: Most modules extend `ReaperModule` which likely provides common functionality (check `util/misc/ReaperModule.java`).

### Service System

**Service Loader**: `SL.java` (Service Loader)
- Initializes background services at addon startup
- Thread pool manager: `TL.java` (Thread Loader)
- Services:
  - `GlobalManager` - Event subscription and global state
  - `ResourceLoaderService` - Asset downloading and management
  - `NotificationManager` - Notification system
  - `SpotifyService` - Spotify integration (currently disabled)
  - `WellbeingService` - (currently disabled)

### Utility Structure

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

**Key Utilities**:
- `BlockHelper` / `BlockBuilder` - Block placement abstractions
- `CombatHelper` / `DamageCalculator` - Combat calculations
- `RotationHelper` - Server-side rotation management
- `PlayerHelper` - Player state queries
- `ExternalWindow` - Native window rendering system

### Mixin System

Mixins are defined in `reaper.mixins.json`:
- `MinecraftClientMixin` - Core client hooks
- `Bootstrap` - Early initialization
- `meteor.FakePlayerMixin` - Meteor integration for fake players
- `meteor.MeteorBootstrap` - Meteor startup hooks
- `meteor.NotificationProxy` - Notification system integration

**Mixin Accessors**:
- `HeldItemRendererAccessor` - Access private fields in held item renderer
- `HeldItemRendererMixin` - Modify held item rendering behavior

### Event System

Custom events in `events/`:
- `DeathEvent` - Player death detection
- `InteractEvent` - Interaction tracking
- `UpdateHeldItemEvent` - Item switch detection
- `CancellablePlayerMoveEvent` - Movement control (used by ElytraBot)

### External Rendering

The addon includes a sophisticated external window system:
- `ExternalWindow.java` - Base window implementation using native rendering
- `ExternalRenderers.java` - Rendering pipeline for external windows
- Modules: `ExternalHUD`, `ExternalFeed`, `ExternalNotifications`

This allows rendering game information in separate OS windows outside Minecraft.

### Complex Subsystems

**ElytraBot** (`modules/misc/elytrabot/`):
- Threaded pathfinding system with A* algorithm
- Multiple flight modes (ElytraFly, PacketFly)
- Custom movement events and utilities
- Timer and direction utilities for precise movement
- **NOTE**: The same/similar ElytraBot code exists in `ai_reference/meteor-rejects-v2` already ported to 1.21.11 - use as direct reference

## Porting Strategy

**SEE PORTING-GUIDE.md** for comprehensive step-by-step instructions, known breaking changes, and detailed migration information.

Quick overview when porting modules from 1.19.4 to 1.21.11:

1. **Read PORTING-GUIDE.md** - Contains all known breaking changes and detailed instructions
2. **Start with ai_reference**: Always check how similar modules are implemented in the reference addons
3. **Gradle first**: Update build system to Gradle 9.2.0+ and modern version catalog (check `meteor-addon-template`)
4. **Fabric mod metadata**: Update `fabric.mod.json` to current schema (Java 21+, Minecraft 1.21.11)
5. **Mixins**: Verify all mixin targets still exist in 1.21.11 (class/method names may have changed)
6. **API migration**: Meteor Client API has changed significantly - use `meteor-client` source and reference addons to find new patterns
7. **Imports**: Many Minecraft classes have moved or been renamed across versions
8. **Test incrementally**: Port modules in categories (chat → misc → combat → render)

### Known Breaking Changes (See PORTING-GUIDE.md for complete list)

**Critical Gradle 9 Changes:**
- Source/target compatibility must use `java {}` block
- `archivesBaseName` → `base.archivesName`
- JUnit Platform launcher required for tests
- Kotlin/OkHttp dependencies must be explicitly included

**Major API Changes:**
- DimensionType API → EnvironmentAttributes system
- Camera.getPos() → getCameraPos()
- PlayerEntity attack cooldown method renamed
- NetworkingBackend introduced for network I/O
- Renderer2D.render() signature changed
- Packet class restructuring
- Entity tracking changes
- Block placement mechanics updates
- Rendering system overhauls
- Setting/Config system changes in Meteor Client
- Event system updates

## File Structure Notes

**Asset Management**:
- `Reaper.FOLDER` - Main addon folder in Meteor directory
- `Reaper.RECORDINGS` - Recordings subfolder
- `Reaper.ASSETS` - Asset storage
- `Reaper.USER_ASSETS` - User-customizable assets

**Custom HUD Group**: `Reaper.HUD_GROUP` - Dedicated HUD category for addon HUD elements

## Development Workflow

**Fresh Implementation Workflow:**

1. **Start with meteor-addon-template** - Copy as base for modern structure
2. **Port module by module** - Check `ai_reference/reaper-1.19.4/` for original logic
3. **Reference modern patterns** - Use meteor-rejects-v2 and other 1.21.11 addons for API usage
4. **Build frequently** - Use gradle-mcp-server tools to build and test
5. **Test in-game** - Verify each module works before moving to the next
6. **Iterate** - Discover → Understand → Implement → Test → Repeat

**Build Commands (via gradle-mcp-server MCP tools):**
- Build: Use `mcp__gradle-mcp-server__gradle_build`
- Run client: Use `mcp__gradle-mcp-server__gradle_execute` with task "runClient"
- Clean: Use `mcp__gradle-mcp-server__gradle_execute` with task "clean"

## Important Notes from Original Implementation

These patterns existed in the original code - understand them for reference, but implement using modern patterns:

- **Folder structure**: Original created `Reaper.FOLDER`, `Reaper.RECORDINGS`, `Reaper.ASSETS`, `Reaper.USER_ASSETS` on initialization
- **Thread management**: Used custom `TL.java` thread pool for async operations
- **Service system**: Custom `SL.java` service loader initialized background services
- **Discord RPC**: Had auto-enablement logic
- **Services**: Spotify and Wellbeing services were disabled in original
- **External windows**: Required native OS support for external rendering
- **Module loader**: Non-standard `ML.java` module loader - **DO NOT replicate this pattern**
