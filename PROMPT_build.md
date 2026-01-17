0a. Study `specs/investigation/*` to understand the Lombok SuperBuilder investigation requirements.
0b. Study @IMPLEMENTATION_PLAN.md to understand investigation findings and recommended fixes.
0c. Study @AGENTS.md to understand build/test commands for this project.

1. **Implement the recommended fix** for the Lombok @SuperBuilder compilation failure:
   - Follow the prioritized fix recommendations in @IMPLEMENTATION_PLAN.md
   - Start with the highest-priority, lowest-risk fix
   - Make minimal, focused changes

2. **Validate the fix**:
   - Run the 10-consecutive-build validation:
     ```bash
     cd software/core/oqm-core-api
     for i in {1..10}; do
         echo "=== Run $i ==="
         ./gradlew clean build -x test || { echo "FAILED on run $i"; exit 1; }
     done
     echo "SUCCESS: All 10 runs passed"
     ```
   - If any run fails, analyze the failure and adjust the fix
   - Document each attempt and its result

3. **If the first fix doesn't work**:
   - Document why it failed in @IMPLEMENTATION_PLAN.md
   - Move to the next recommended fix
   - Update findings based on what you learned

4. **When 10 consecutive builds pass**:
   - Update @IMPLEMENTATION_PLAN.md to mark the issue resolved
   - Document the successful fix and why it worked
   - `git add -A`
   - `git commit` with message: "Fix #1033: Resolve Lombok SuperBuilder compilation race condition"
   - `git push`

5. **Additional validation** (if time permits):
   - Run full test suite: `./gradlew build` (with tests)
   - Verify no regressions in other modules

IMPORTANT GUIDELINES:
- Prefer configuration changes over code changes when possible
- If code changes are needed, minimize modifications to the class hierarchy
- Document everything - future developers need to understand this fix
- Keep @IMPLEMENTATION_PLAN.md current with each attempt
- Update @AGENTS.md if you learn something new about the build process
