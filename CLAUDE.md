# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Reaper** is a Meteor Client addon for Minecraft 1.21.11.

**Status:** v0 COMPLETE ✅ (as of 2025-12-26)
- **39 modules ported** from 1.19.4 (9 chat, 12 misc, 9 combat, 8 HUD, 1 render)
- **10 modules excluded** (documented in DO_NOT_PORT.md - replaced by other projects, unfinished, or unused)
- **All utilities and services ported**
- **Build system fully updated** (Gradle 9.2.0, Java 21, Minecraft 1.21.11)

### Key Documentation

- **PORTING-GUIDE.md** - API breaking changes reference (1.19.4 → 1.21.11)
- **DO_NOT_PORT.md** - Features intentionally excluded from the port
- **ARCHITECTURE.md** - Original 1.19.4 codebase structure reference
- **README.md** - User-facing project documentation

## AI Reference Directory

The `ai_reference/` folder contains critical reference implementations:

- **reaper-1.19.4** - **ORIGINAL SOURCE CODE** - Complete 1.19.4 implementation for understanding original functionality (see ARCHITECTURE.md for structure)
- **reaper-deleted-features** - **FULLY REVIEWED** - All features reviewed; 9 restored, 2 excluded (see DO_NOT_PORT.md). Can be safely deleted.
- **meteor-mcp-addon** - **FIRST-PARTY REFERENCE** - **USE FOR BUILD CONFIGURATION** - Perfect example of modern build.gradle.kts, gradle/libs.versions.toml, Gradle 9 patterns
- **meteor-rejects-v2** - **FIRST-PARTY REFERENCE** - Successfully ported to 1.21.11, contains ElytraBot (already ported, see DO_NOT_PORT.md)
- **meteor-client** - Official Meteor Client source (current API)
- **Trouser-Streak** - Large addon (66 modules, updated to 1.21.11)
- **meteor-addon-template** - Official template with current best practices
- **meteor-villager-roller** - Clean, focused example addon
- **Numby-hack** - 22 modules with diverse implementations
- **Nora-Tweaks** - Multi-version support example

**IMPORTANT**: The `ai_reference/` directory is in `.gitignore` but you MUST still read and search it for reference implementations.

**meteor-mcp-addon is the authoritative build configuration reference** - perfect examples of modern Gradle 9 patterns, Kotlin DSL, version catalogs, and complex dependency management.

### When to Search ai_reference/

- **Understanding original Reaper functionality** - check reaper-1.19.4 (see ARCHITECTURE.md)
- **Updating build configuration** - check meteor-mcp-addon's build.gradle.kts and gradle/libs.versions.toml
- **Migrating to Gradle 9** - meteor-mcp-addon shows all correct patterns
- **Finding module examples** - check meteor-rejects-v2, Trouser-Streak, or other addons for 1.21.11 patterns
- **Understanding Meteor Client APIs** - check meteor-client source
- **Checking fabric.mod.json structure** - check meteor-addon-template

## Code Search Tools

**CRITICAL: Use code-search-mcp MCP server tools for ALL code searches.**

This project has the `code-search-mcp` MCP server enabled, which provides significantly more powerful search capabilities than traditional Grep/Glob tools.

### Available Tools

**Stack Detection:**
- `mcp__code-search-mcp__detect_stacks` - Automatically detect project technology stacks

**File Search:**
- `mcp__code-search-mcp__search_files` - Fast file search with pattern matching

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
1. Use code-search-mcp tools instead of Grep/Glob/find when searching code
2. Search across both main source and `ai_reference/` directories

**Example workflow:**
```
# Instead of: Grep for "ElytraBot"
# Do: Use mcp__code-search-mcp__search_symbols to search for "ElytraBot" class

# Instead of: Glob for "*.gradle"
# Do: Use mcp__code-search-mcp__search_files with extension filter

# Instead of: Grep for method usage patterns
# Do: Use mcp__code-search-mcp__search_text or search_symbols
```

### Performance Benefits

- Indexed search is 10-100x faster than grep on large codebases
- Can search across 1000+ files (including ai_reference/) in milliseconds
- AST-aware searching eliminates false positives from comments/strings

## Minecraft Fabric Development Toolkit

**CRITICAL: Always invoke the `minecraft-fabric-dev` skill when starting development/coding work.**

This project has the `minecraft-fabric-dev` skill available, which provides a comprehensive toolkit specifically designed for Fabric Minecraft mod development and porting.

### The minecraft-fabric-dev Skill

**When to invoke:**
- At the start of any coding/development session
- When you encounter Minecraft API changes or compatibility issues
- When working with mixins, mappings, or Fabric-specific APIs
- When you need to understand Minecraft source code

**How to invoke:**
Use the Skill tool: `Skill(skill="minecraft-fabric-dev")`

**What it provides:**
- Comprehensive guidance for Fabric mod development
- Integration with minecraft-dev MCP server tools
- Mixin validation and analysis
- Mapping lookup and remapping utilities
- Access to Minecraft decompiled source code

### minecraft-dev MCP Tools Integration

The `minecraft-fabric-dev` skill works with the `minecraft-dev` MCP server tools:

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

## Build System

### Target Dependencies (1.21.11)

```toml
# gradle/libs.versions.toml (reference: meteor-mcp-addon)
minecraft = "1.21.11"
yarn-mappings = "1.21.11+build.3"
fabric-loader = "0.18.2"
loom = "1.14-SNAPSHOT"
meteor = "1.21.11-SNAPSHOT"
java = "21"
```

### Gradle Tasks

**CRITICAL: Use gradle-mcp-server MCP tools instead of terminal commands for Gradle operations.**

This project has the `gradle-mcp-server` MCP server enabled. You MUST use these tools instead of running `./gradlew` commands directly.

#### Available Gradle Tools

**Project Information:**
- `mcp__gradle-mcp-server__gradle_version` - Get Gradle and JVM version info
- `mcp__gradle-mcp-server__gradle_project_info` - Get project structure, plugins, configurations
- `mcp__gradle-mcp-server__gradle_subprojects` - List all subprojects
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

**Examples:**
```
# Instead of: ./gradlew build
# Do: Use mcp__gradle-mcp-server__gradle_build

# Instead of: ./gradlew clean
# Do: Use mcp__gradle-mcp-server__gradle_execute with task "clean"

# Instead of: ./gradlew runClient
# Do: Use mcp__gradle-mcp-server__gradle_execute with task "runClient"

# Instead of: ./gradlew tasks --all
# Do: Use mcp__gradle-mcp-server__gradle_list_tasks

# Instead of: ./gradlew dependencies
# Do: Use mcp__gradle-mcp-server__gradle_dependencies
```

#### Benefits

- Structured output that's easier to parse programmatically
- Better error handling and reporting
- Consistent interface across different Gradle versions
- No need to worry about shell escaping or path issues

### Maven Repositories

```groovy
maven { url = "https://maven.meteordev.org/releases" }
maven { url = "https://maven.meteordev.org/snapshots" }
```

---

**Last Updated:** 2025-12-26 (v0 Complete)
