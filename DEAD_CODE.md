# Dead Code Tracking

This document tracks unused code, legacy infrastructure, and dead code paths that should be cleaned up in a future refactoring pass.

## Format
Each entry includes:
- **Description**: What the code does/was intended for
- **Status**: Why it's unused
- **Files & Lines**: Exact locations for bulk cleanup
- **Related Code**: Any dependent code that should also be removed

---

## Unused Directory Constants

### `RECORDINGS` Constant
**Description**: Static constant for a recordings subdirectory  
**Status**: Created on initialization but never used by any modules or utilities  
**Reason**: Likely legacy from 1.19.4 features that were not ported  

**Files & Lines**:
- `src/main/java/me/ghosttypes/reaper/Reaper.java:30` - Constant definition
- `src/main/java/me/ghosttypes/reaper/Reaper.java:52` - Directory creation check

**Cleanup Actions**:
1. Remove constant declaration at line 30
2. Remove directory creation check at line 52

---

### `ASSETS` Constant
**Description**: Static constant for an assets directory  
**Status**: Only used to create `USER_ASSETS` subdirectory, but neither are used by any code  
**Reason**: Likely legacy from 1.19.4 features that were not ported  

**Files & Lines**:
- `src/main/java/me/ghosttypes/reaper/Reaper.java:31` - Constant definition
- `src/main/java/me/ghosttypes/reaper/Reaper.java:32` - Referenced by `USER_ASSETS` definition
- `src/main/java/me/ghosttypes/reaper/Reaper.java:53` - Directory creation check
- `src/main/java/me/ghosttypes/reaper/Reaper.java:54` - `USER_ASSETS` directory creation check (dependent)

**Cleanup Actions**:
1. Remove `ASSETS` constant declaration at line 31
2. Remove `USER_ASSETS` constant declaration at line 32 (dependent on `ASSETS`)
3. Remove directory creation checks at lines 53-54

**Note**: `USER_ASSETS` is also unused and should be removed along with `ASSETS`

---

## Unused OS Detection Utility

### `OSUtil.java` - OS Detection and Message Boxes
**Description**: Utility class for OS detection and displaying JOptionPane message boxes  
**Status**: `init()` is called during service loading but results are never used anywhere  
**Reason**: Was a dependency for `GhostCA.java` (listed in `DO_NOT_PORT.md`). No other ported features use this utility.

**Class Location**:
- `src/main/java/me/ghosttypes/reaper/util/os/OSUtil.java` - Entire file (49 lines)

**Called From**:
- `src/main/java/me/ghosttypes/reaper/util/services/SL.java:6` - Import statement
- `src/main/java/me/ghosttypes/reaper/util/services/SL.java:16` - `OSUtil.init()` call

**Unused Members**:
- `isWindows` static field - Set by `init()` but never read
- `getOS()` - Only called internally by `init()`
- `messageBox()` - Never called
- `info()` - Never called
- `warning()` - Never called
- `error()` - Never called

**Cleanup Actions**:
1. Remove `OSUtil.init()` call from `SL.java:16`
2. Remove import from `SL.java:6`
3. Delete entire file `src/main/java/me/ghosttypes/reaper/util/os/OSUtil.java`
4. Delete parent directory `src/main/java/me/ghosttypes/reaper/util/os/` if empty after deletion

---

### `FileHelper.java` - File Operations and HTTP Downloads
**Description**: Utility class for file operations, JAR launching, and HTTP file/list downloads  
**Status**: Completely unused - no imports or method calls found anywhere in codebase  
**Reason**: Mentioned in `DO_NOT_PORT.md` as a dependency for unused `uploadFile()` method. No ported features use this utility.

**Class Location**:
- `src/main/java/me/ghosttypes/reaper/util/os/FileHelper.java` - Entire file (57 lines)

**Unused Members**:
- `getFormattedPath()` - Formats file path with quotes
- `start()` - Starts a Java JAR file as process
- `downloadList()` - Downloads text list from URL using Meteor's Http client
- `downloadFile()` - Downloads file from URL to local file

**Cleanup Actions**:
1. Delete entire file `src/main/java/me/ghosttypes/reaper/util/os/FileHelper.java`
2. Delete parent directory `src/main/java/me/ghosttypes/reaper/util/os/` if empty after OSUtil and FileHelper deletion

**Note**: This file can be deleted together with `OSUtil.java` as they're in the same directory.

---

## Cleanup Priority
- [ ] **Low Priority**: Unused directory constants - No functional impact, just cleanup for cleaner codebase

---

## Notes
- Before removing any code listed here, verify with a fresh search that usage hasn't been added
- Update this document when performing bulk cleanup operations
- Cross-reference with `DO_NOT_PORT.md` to ensure removed features aren't accidentally being cleaned up
