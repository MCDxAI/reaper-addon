# Reaper Addon Port - Session Handoff Document

**Date:** 2025-12-20
**Session Type:** Autonomous Porting Session
**Status:** ✅ Successful - Backend Infrastructure Next

---

## 🎯 NEXT SESSION PRIORITY: BACKEND INFRASTRUCTURE

**CRITICAL:** The next session should focus on **backend infrastructure** before continuing with modules:

### Priority Order for Next Session:

1. **Services System** (ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/util/services/)
   - ✅ TL.java (Thread Loader) - ALREADY PORTED
   - ✅ NotificationManager.java - ALREADY PORTED
   - ✅ GlobalManager.java - STUB CREATED (needs full implementation)
   - ⏳ ResourceLoaderService.java - Asset downloading system
   - ⏳ SpotifyService.java - STUB CREATED (needs full implementation)
   - ⏳ SL.java (Service Loader) - Service initialization system

2. **Events System** (ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/events/)
   - ✅ InteractEvent.java - ALREADY PORTED
   - ⏳ DeathEvent.java - Player death detection
   - ⏳ UpdateHeldItemEvent.java - Item switch detection
   - ⏳ CancellablePlayerMoveEvent.java - Movement control (for ElytraBot)

3. **Mixins System** (ai_reference/reaper-1.19.4/src/main/resources/reaper.mixins.json)
   - ⏳ MinecraftClientMixin - Core client hooks
   - ⏳ Bootstrap - Early initialization
   - ⏳ HeldItemRendererAccessor/Mixin - Item rendering
   - ⏳ Meteor integration mixins (FakePlayerMixin, MeteorBootstrap, NotificationProxy)
   - **NOTE:** Mixins require careful target validation for 1.21.11

4. **Critical Utilities** (needed by many modules)
   - ⏳ Interactions.java - STUB EXISTS (needs full ~500 line implementation)
   - ⏳ RotationHelper.java - Server-side rotation management
   - ⏳ BlockHelper.java - Block placement abstractions (~600 lines)
   - ⏳ CombatHelper.java - Combat calculations
   - ⏳ DamageCalculator.java - Damage prediction

**Why Backend First:**
- Services provide infrastructure that modules depend on
- Events enable module functionality
- Mixins hook into game internals
- Without these, many modules cannot function properly

---

## 📊 Current Progress Summary

### ✅ Completed This Session

**Utilities Ported (13 files):**
- util/misc/MathUtil.java - Math utilities with 1.21.11 API updates
- util/misc/SystemTimer.java - Timer utility
- util/misc/Task.java - Single-execution task wrapper
- util/misc/AnglePos.java - Position with yaw/pitch
- util/os/OSUtil.java - OS detection and message boxes
- util/os/FileHelper.java - File operations
- util/misc/Formatter.java - Placeholder system, color conversions, emote system
- util/misc/MessageUtil.java - Chat message queue system
- util/world/PlayerHelper.java - Player query utilities
- util/misc/Sorter.java - String sorting
- util/misc/ScreenUtil.java - Screen type detection
- util/misc/ModuleHelper.java - Module management (stub)
- util/misc/ReaperModule.java - Base class for all Reaper modules

**Services Ported (3 files):**
- util/services/TL.java - Thread pool manager
- util/services/NotificationManager.java - Notification queue
- util/services/GlobalManager.java - Death tracking (stub)

**Stubs Created (2 files):**
- util/player/Stats.java - Combat statistics (stub - needs full impl)
- util/services/SpotifyService.java - Spotify integration (stub - needs full impl)

**Modules Ported (5 files):**
- modules/chat/NotificationSettings.java - User notification settings
- modules/chat/AutoLogin.java - Auto /login authentication
- modules/chat/Welcomer.java - Join/leave messages
- modules/misc/MultiTask.java - Eat while mining
- modules/misc/AutoRespawn.java - Auto respawn + rekit + excuses + highscore
- modules/misc/NoProne.java - Prevent prone position

**Events Ported (1 file):**
- events/InteractEvent.java - Custom interaction event

**Other Infrastructure (3 files):**
- modules/misc/ConfigTweaker.java - Settings group creator (utility, not a module)
- util/player/Interactions.java - Player interaction utilities (stub - needs full impl)
- Reaper.java - Updated with module registration and INVITE_LINK

**Total:** 23 files ported, ~1300 lines of code

### 📈 Statistics

- **Commits:** 7 checkpoints
- **Functional Modules:** 5
- **Build Status:** ✅ Working (Gradle 9.2.0, Java 21, MC 1.21.11)
- **Lines Ported:** ~1300

---

## 🔧 Critical API Changes Encountered

### Minecraft API Changes (1.19.4 → 1.21.11)

| Old API (1.19.4) | New API (1.21.11) | Notes |
|------------------|-------------------|-------|
| `Text.of(msg)` | `Text.literal(msg)` | Text creation |
| `player.sendMessage(text)` | `player.sendMessage(text, false)` | Requires overlay boolean |
| `SharedConstants.getGameVersion().getName()` | `SharedConstants.getGameVersion().name()` | GameVersion method rename |
| `player.getEntityName()` | `player.getName().getString()` | Player name access |
| `entity.getEntityName()` | `entity.getName().getString()` | Entity name access |
| `profile.getName()` | `profile.name()` | GameProfile method |
| `player.isFallFlying()` | `player.isGliding()` | Elytra flying check |
| `inventory.getArmorStack(slot)` | `player.getEquippedStack(EquipmentSlot)` | Armor access |

### Common Patterns

**Color Conversion (Formatter.java):**
```java
// SettingColor -> MeteorColor
Color sToMC(SettingColor sc) {
    return new Color(sc.r, sc.g, sc.b, sc.a);
}

// java.awt.Color -> MeteorColor
Color cToMC(java.awt.Color c) {
    return new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
}
```

**Module Base Pattern (ReaperModule.java):**
```java
public abstract class ReaperModule extends Module {
    protected ReaperModule(Category category, String name, String description) {
        super(category, name, description);
    }

    // Routes module messages to notification system
    @Override
    public void info(String message, Object... args) {
        NotificationManager.addNotification(this.title, message);
    }
}
```

**Thread Pool Usage (TL.java):**
```java
public static ExecutorService cached = Executors.newCachedThreadPool();
public static ScheduledExecutorService schedueled = Executors.newScheduledThreadPool(1);
```

---

## 🗂️ Project Structure

### Current Source Layout

```
src/main/java/me/ghosttypes/reaper/
├── Reaper.java (main addon class - registers modules)
├── events/
│   └── InteractEvent.java
├── modules/
│   ├── chat/
│   │   ├── NotificationSettings.java
│   │   ├── AutoLogin.java
│   │   └── Welcomer.java
│   └── misc/
│       ├── AutoRespawn.java
│       ├── ConfigTweaker.java
│       ├── MultiTask.java
│       └── NoProne.java
└── util/
    ├── misc/
    │   ├── AnglePos.java
    │   ├── Formatter.java
    │   ├── MathUtil.java
    │   ├── MessageUtil.java
    │   ├── ModuleHelper.java (stub)
    │   ├── ReaperModule.java
    │   ├── ScreenUtil.java
    │   ├── Sorter.java
    │   ├── SystemTimer.java
    │   └── Task.java
    ├── os/
    │   ├── FileHelper.java
    │   └── OSUtil.java
    ├── player/
    │   ├── Interactions.java (stub - needs full impl)
    │   └── Stats.java (stub)
    ├── services/
    │   ├── GlobalManager.java (stub - needs full impl)
    │   ├── NotificationManager.java
    │   ├── SpotifyService.java (stub)
    │   └── TL.java
    └── world/
        └── PlayerHelper.java
```

### Reference Source (ai_reference/)

All original 1.19.4 code preserved in `ai_reference/reaper-1.19.4/`
Other reference addons available for API patterns.

---

## 🚀 Build System Details

### Gradle Configuration

- **Gradle Version:** 9.2.0
- **Java Version:** 21
- **Minecraft Version:** 1.21.11
- **Fabric Loader:** 0.18.2
- **Meteor Client:** 1.21.11-SNAPSHOT
- **Loom:** 1.14-SNAPSHOT

### Build Commands (via MCP Tools)

**CRITICAL:** Always use gradle-mcp-server MCP tools instead of direct bash commands:

```
# Load tool first
MCPSearch: select:mcp__gradle-mcp-server__gradle_build

# Then build
mcp__gradle-mcp-server__gradle_build(projectPath, skipTests=true)
```

**Build Verification:** ALWAYS verify build before committing/pushing.

---

## 📋 Known Issues & Patterns

### Issues Fixed This Session

1. **ConfigTweaker Instantiation Bug**
   - **Issue:** ConfigTweaker was being instantiated in Reaper.onInitialize() as if it were a module
   - **Fix:** Removed instantiation - ConfigTweaker is a utility class, not a module
   - **Commit:** e273071

### Important Patterns

1. **Module Registration Pattern:**
   ```java
   // In Reaper.java onInitialize()
   Modules.get().add(new ModuleName());
   ```

2. **Notification System:**
   - ReaperModule routes `info()` calls to NotificationManager
   - NotificationManager maintains a queue with auto-expiry
   - NotificationSettings module controls user preferences

3. **Thread Management:**
   - TL.cached for one-off async tasks
   - TL.schedueled for recurring scheduled tasks
   - MessageUtil uses TL for delayed messages

---

## 🔍 Reference Implementation Notes

### Deleted Features to Restore (ai_reference/reaper-deleted-features/)

**IMPORTANT:** These were lazily deleted instead of ported. Restore ALL of them:

**High Priority:**
1. AuraSync + AuraSyncService (79 lines) - RGB sync system
2. Stats module (149 lines) - Combat/client statistics display
3. Watermark (68 lines) - Reaper branding
4. TextItems (95 lines) - Item counter HUD
5. VisualBinds (82 lines) - Keybind display

**Medium Priority:**
6. ModuleSpoof (81 lines) - Fake module list
7. StreamerMode + StreamService (145 lines) - External screen for streaming

**Low Priority:**
8. DebugHud (107 lines) - Developer debug info
9. Greeting (17 lines) - Time-based greeting

### Module Categories (from original ML.java)

The original used a custom Module Loader (ML.java) with categories:
- "Reaper" (combat modules)
- "Reaper Misc" (misc modules)
- "Windows" (external window modules)

**Current:** We use `Reaper.CATEGORY` for all modules, registered in `Reaper.onRegisterCategories()`.

---

## 🎓 MCP Tools Usage

### CRITICAL: Always Use MCP Tools

**Code Search (code-search-mcp):**
```java
// Load tool
MCPSearch: select:mcp__code-search-mcp__search_text

// Search
mcp__code-search-mcp__search_text(path, pattern, limit)
mcp__code-search-mcp__search_files(path, pattern)
mcp__code-search-mcp__search_symbols(path, pattern)
```

**Gradle (gradle-mcp-server):**
```java
// Load tool
MCPSearch: select:mcp__gradle-mcp-server__gradle_build

// Build
mcp__gradle-mcp-server__gradle_build(projectPath, skipTests=true)
```

**Minecraft Development (minecraft-dev):**
```java
// Invoke skill first
Skill(skill="minecraft-fabric-dev")

// Then use tools
mcp__minecraft-dev__get_minecraft_source(version, className)
mcp__minecraft-dev__compare_versions(oldVersion, newVersion)
mcp__minecraft-dev__find_mapping(name, type)
```

---

## 📝 Commit Message Format

**Pattern (from .claude/commands/push.md):**
```
<type>: <short summary>

<detailed description>

<API changes section if applicable>

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

**Types:** feat, fix, docs, refactor, chore

**IMPORTANT:**
- NO "Generated with Claude Code" message
- YES co-author attribution
- ALWAYS verify build before pushing
- Push frequently for checkpoints

---

## 🔮 Next Steps (Backend Infrastructure)

### Immediate Priorities (Next Session)

**Phase 1: Services (Critical Foundation)**
1. Port ResourceLoaderService.java
   - Asset downloading and management
   - File system setup
   - HTTP download utilities

2. Complete GlobalManager.java
   - Add tick event handler
   - Implement death entry cleanup
   - Full death tracking system

3. Complete SpotifyService.java
   - Media state tracking
   - Track/artist info
   - Integration with Formatter placeholders

4. Port SL.java (Service Loader)
   - Service initialization system
   - Replaces manual initialization
   - Coordinates all services

**Phase 2: Events (Enable Module Functionality)**
5. Port DeathEvent.java
   - Player death detection
   - Used by PopCounter, AutoEZ, etc.

6. Port UpdateHeldItemEvent.java
   - Item switch detection
   - Used by various combat modules

7. Port CancellablePlayerMoveEvent.java
   - Movement control
   - Critical for ElytraBot

**Phase 3: Mixins (Game Hooks)**
8. Set up reaper.mixins.json
9. Port MinecraftClientMixin
10. Port item rendering mixins
11. Port Meteor integration mixins

**Phase 4: Critical Utilities**
12. Complete Interactions.java (~500 lines)
13. Port RotationHelper.java
14. Port BlockHelper.java (~600 lines)
15. Port CombatHelper.java
16. Port DamageCalculator.java

### After Backend Complete: Resume Modules

**Simple Modules (Quick Wins):**
- Chat: ArmorAlert, ChatTweaks, PopCounter, BedAlerts, HoleAlert
- Misc: AntiAim, ChorusPredict, NoDesync, OldAnimations, StrictMove

**Complex Modules (Later):**
- Combat: 35+ modules (AnchorGod, BedGod, Surround, etc.)
- ElytraBot: Complex pathfinding system
- Render: External window system
- HUD: Custom elements

**Deleted Features (Restore After Backend):**
- AuraSync system
- Stats module
- Watermark
- TextItems, VisualBinds, etc.

---

## 💡 Tips for Next Session

1. **Start with minecraft-fabric-dev skill**
   ```
   Skill(skill="minecraft-fabric-dev")
   ```

2. **Use code-search-mcp for ALL searches**
   - Faster than grep/glob
   - Can search ai_reference in milliseconds
   - AST-aware

3. **Check meteor-client reference first**
   - Located in ai_reference/meteor-client
   - Shows current 1.21.11 API usage
   - Especially useful for events/mixins

4. **Validate mixin targets**
   - Use minecraft-dev tools
   - Compare 1.19.4 vs 1.21.11 class structure
   - Many classes moved/renamed

5. **Build frequently**
   - After each utility/service/event
   - Before committing
   - Catch API issues early

6. **Push checkpoints often**
   - After each completed component
   - After fixing build errors
   - Use descriptive commit messages

7. **Update Discord webhook**
   - Keep user informed of progress
   - Report blockers immediately
   - Celebrate milestones

---

## 🎯 Session Goals for Next Time

**Primary Goal:** Complete backend infrastructure (services, events, mixins)

**Success Criteria:**
- [ ] All services ported and functional
- [ ] Core events ported
- [ ] Essential mixins ported
- [ ] Critical utilities completed (Interactions, RotationHelper, BlockHelper)
- [ ] Build still passing
- [ ] Services integrate properly

**Stretch Goal:** Start porting simple modules that depend on the new backend

---

## 📚 Additional Resources

### Documentation
- See CLAUDE.md for full project context
- See PORTING-GUIDE.md for detailed API changes
- See INTERCONNECTIONS.md for AuraSync architecture

### Reference Implementations
- ai_reference/meteor-client - Current Meteor API
- ai_reference/meteor-rejects-v2 - Successfully ported to 1.21.11
- ai_reference/meteor-mcp-addon - Build system reference
- ai_reference/Trouser-Streak - Large addon examples

### Key Files to Review Before Starting
1. ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/util/services/
2. ai_reference/reaper-1.19.4/src/main/java/me/ghosttypes/reaper/events/
3. ai_reference/reaper-1.19.4/src/main/resources/reaper.mixins.json

---

## ✅ Pre-Session Checklist

Before starting next session:

- [ ] Read this entire document
- [ ] Review CLAUDE.md
- [ ] Invoke minecraft-fabric-dev skill
- [ ] Load code-search-mcp tools
- [ ] Load gradle-mcp-server tools
- [ ] Review current build status
- [ ] Check ai_reference for service implementations
- [ ] Set up TodoWrite tool with backend tasks

---

**End of Session Handoff - Good luck with the backend infrastructure! 🚀**
