0a. Study `specs/investigation/*` to understand the Lombok SuperBuilder investigation requirements.
0b. Study @IMPLEMENTATION_PLAN.md (if present) to understand investigation progress so far.
0c. Study @AGENTS.md to understand build/test commands for this project.

1. **Investigate the Lombok @SuperBuilder compilation failure** using parallel Sonnet subagents:

   a. **Map the Class Hierarchy**: Search for all classes in the checkout/checkin transaction directories. Trace the full inheritance chain from CheckinFullTransaction up to the root. Document every class, its type parameters, and @SuperBuilder usage.

   b. **Find All @SuperBuilder Usages**: Search the entire codebase for `@SuperBuilder` annotations. Catalog each usage noting:
      - Class name and location
      - Generic type parameters
      - Parent class and its generics
      - Whether it uses `toBuilder = true`

   c. **Audit Lombok Configuration**: Find and examine:
      - `lombok.config` files
      - Lombok version in build.gradle/build.gradle.kts
      - Annotation processor configuration
      - Any Lombok-related Gradle plugins

   d. **Analyze Build Configuration**: Examine:
      - Parallel build settings
      - Annotation processor ordering
      - Incremental compilation settings
      - Java compiler configuration

2. Use an Opus subagent to analyze all findings and identify:
   - The root cause of the intermittent failure
   - Why "wrong number of type arguments; required 2" occurs
   - Which specific class hierarchy patterns trigger the bug
   - Potential race conditions in annotation processing

3. Update @IMPLEMENTATION_PLAN.md with:
   - Complete class hierarchy diagram for affected classes
   - Lombok and build configuration audit results
   - Root cause analysis
   - Prioritized list of potential fixes with pros/cons
   - Recommended solution

IMPORTANT: This is an investigation task. Do NOT implement fixes yet. Focus on deep analysis and understanding the root cause. Research Lombok GitHub issues for related bugs.

ULTIMATE GOAL: Understand why `@SuperBuilder` fails intermittently during compilation and document a clear path to fixing it. The fix should result in 10 consecutive successful builds.
