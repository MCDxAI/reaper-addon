---
trigger: always_on
---

Never use grep or manual terminal commands to search for / locate anything in the workspace

Always use the highly optimized `code-search` tool

1. detect_stacks
Detect technology stacks in a directory
2. search_symbols
Search for code symbols (classes, functions, methods, etc.)
3. search_text
Search for text/code patterns using ripgrep
4. search_files
Search for files by name, pattern, or extension
5. refresh_index
Rebuild the symbol index for a directory
6. cache_stats
Get cache statistics for indexed directories
7. clear_cache
Clear cached indices
8. analyze_dependencies
Analyze project dependencies from manifest files (package.json, Cargo.toml, pom.xml, etc.)
9. search_ast_pattern
Search code using AST pattern matching with metavariables ($VAR for capture, $$VAR for single anonymous, $$$VAR for multiple)
10. search_ast_rule
Search code using complex AST rules with relational (inside, has, precedes, follows) and composite (all, any, not) operators
11. check_ast_grep
Check if ast-grep is available and get version information