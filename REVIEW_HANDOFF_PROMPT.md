# OpenQuarterMaster 1.0 Release Review - Agent Handoff Prompt

## Instructions for Orchestrating Agent

You are tasked with orchestrating a comprehensive code review of the OpenQuarterMaster project to prepare it for a 1.0 release. You will distribute this work across multiple specialized subagents using the Task tool.

**Your Role**: You are the orchestrator. You should:
1. Launch subagents in parallel where possible (use single messages with multiple Task tool calls)
2. Track progress and collect findings
3. Prioritize P0 items first, then P1, etc.
4. Aggregate results and create actionable issue lists or fix PRs
5. Coordinate dependencies between tracks

---

## Project Context

**OpenQuarterMaster** is an open-source inventory management system with:
- **Backend**: Java 21 + Quarkus framework
- **Database**: MongoDB (with GridFS for files)
- **Auth**: Keycloak (OIDC/JWT)
- **Frontend**: Bootstrap 5 + jQuery + Qute templating
- **Messaging**: Kafka/Red Panda
- **Deployment**: Station Captain (Python) for single-host, Helm for Kubernetes

**Repository Structure**:
```
/home/user/OpenQuarterMaster/
├── software/
│   ├── core/
│   │   ├── oqm-core-api/          # Core REST API (Java/Quarkus)
│   │   └── oqm-core-base-station/ # Web UI (Java/Quarkus + Qute templates)
│   ├── plugins/                    # Plugin ecosystem
│   ├── libs/                       # Shared libraries
│   └── drivers/                    # DEPRECATED - hardware drivers
├── deployment/
│   ├── Single Host/
│   │   ├── Station-Captain/       # Python installer/manager
│   │   ├── Infrastructure/        # Docker service definitions
│   │   └── docs/                  # Deployment documentation
│   └── Kubernetes/
│       ├── helm/                  # Helm charts
│       └── yaml/                  # Raw K8s manifests
├── hardware/                      # Arduino MSS modules
└── .github/workflows/             # CI/CD pipelines
```

---

## Current State Summary

| Metric | Current | Target |
|--------|---------|--------|
| TODO/FIXME markers | 1,044 | <100 |
| API endpoint test coverage | ~22% | >70% |
| Service test coverage | ~63% | >80% |
| Disabled tests | 3+ | 0 |
| Hardcoded secrets in repo | 2+ | 0 |
| Documentation TODOs | 50+ | 0 |

---

## Review Tracks

Launch subagents for the following tracks. **Tracks 1-5 are P0 (Critical)** and should be started first.

---

### TRACK 1: Security Audit (P0 - CRITICAL)
**Launch 2-3 subagents in parallel**

```
SUBAGENT 1A - Secrets & Credentials Audit:
Search the entire codebase for hardcoded secrets, credentials, API keys, and passwords.

CRITICAL KNOWN ISSUES:
1. /home/user/OpenQuarterMaster/deployment/Kubernetes/yaml/openshift/secrets.yml:5
   - Contains Base64-encoded MongoDB password "password123"
   - ACTION: Document for removal, should use external secret management

2. /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/main/resources/application.yaml:174-179
   - Hardcoded test users with password "1!Letmein" in dev profile
   - ACTION: Verify these are dev-only and document security implications

Search patterns to use:
- Grep for: password, secret, apikey, api_key, token, credential, private_key
- Check all .yaml, .yml, .properties, .json config files
- Check environment variable defaults

Deliverable: List of all hardcoded secrets with file:line, severity, and recommended action.
```

```
SUBAGENT 1B - API Security Review:
Review API security configuration across all services.

KNOWN ISSUES:
1. CORS Configuration - /home/user/OpenQuarterMaster/software/drivers/open-qm-driver-server/src/main/resources/application.yaml:15
   - origins: "*" is too permissive
   - ACTION: Restrict to specific trusted domains

2. HTTPS Enforcement - /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/interfaces/RestInterface.java
   - Line 62: Only logs warning for non-HTTPS, should enforce in production

3. Rate Limiting - NOT IMPLEMENTED
   - Search for any rate limiting configuration
   - Recommend implementation approach

4. Security Headers - Check for presence of:
   - X-Content-Type-Options
   - X-Frame-Options
   - Content-Security-Policy
   - Strict-Transport-Security

Review files:
- All application.yaml/yml files
- RestInterface.java and similar base classes
- Traefik configuration in deployment/Single Host/Infrastructure/traefik/

Deliverable: Security configuration report with specific fixes needed.
```

```
SUBAGENT 1C - Authentication & Authorization Review:
Review auth implementation for security issues.

FOCUS AREAS:
1. JWT token handling in /home/user/OpenQuarterMaster/software/libs/core-api-lib-quarkus/runtime/src/main/java/tech/ebp/oqm/lib/core/api/quarkus/runtime/sso/
2. Role definitions in /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/rest/auth/roles/Roles.java
3. @RolesAllowed usage consistency across all endpoints
4. Session management in Base Station
5. CSRF protection configuration

Deliverable: Auth security assessment with any gaps identified.
```

---

### TRACK 2: Core API Test Coverage (P0 - CRITICAL)
**Launch 3-4 subagents in parallel**

```
SUBAGENT 2A - Media Endpoint Tests (ZERO COVERAGE):
Create comprehensive tests for media endpoints.

Location: /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/interfaces/endpoints/media/

Files to test:
- ImageEndpoints.java
- FileAttachmentEndpoints.java

Test scenarios needed:
- Upload valid images (PNG, JPEG, BMP, GIF)
- Reject invalid MIME types
- File size limit enforcement
- Image resizing behavior
- GridFS storage verification
- Deletion and cleanup
- Error handling for corrupt files

Use existing test patterns from:
/home/user/OpenQuarterMaster/software/core/oqm-core-api/src/test/java/tech/ebp/oqm/core/api/

Deliverable: Complete test classes with >80% coverage for media endpoints.
```

```
SUBAGENT 2B - Transaction Endpoint Tests:
Create/complete tests for transaction and checkout endpoints.

KNOWN INCOMPLETE:
- /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/test/java/tech/ebp/oqm/core/api/service/mongo/AppliedTransactionServiceTest.java
  - Lines 1291-1302: 12+ TODO markers for transaction failure scenarios

Files to test:
- TransactionEndpoints in endpoints/inventory/
- ItemCheckoutService.java (currently UNTESTED)
- ItemCheckoutCrud endpoints

Deliverable: Complete transaction and checkout test coverage.
```

```
SUBAGENT 2C - Fix Disabled and Flaky Tests:
Re-enable and fix all disabled tests.

KNOWN DISABLED TESTS:
1. /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/test/java/tech/ebp/oqm/core/api/service/mongo/CustomUnitServiceTest.java:18
   - @Disabled - TODO reimplement entire test

2. /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/test/java/tech/ebp/oqm/core/api/service/mongo/file/MongoHistoriedFileServiceTest.java:31
   - @Disabled "Waiting on file transaction support"

3. /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/test/java/tech/ebp/oqm/core/api/service/schemaVersioning/upgraders/inventoryItem/bumpers/InvItemBumper2Test.java:23
   - @Disabled

KNOWN FLAKY:
- /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/test/java/tech/ebp/oqm/core/api/service/importExport/DataImportServiceTest.java:92
  - TODO fix flakiness in repeated test

Search for all @Disabled annotations and TODO markers in test files.

Deliverable: All tests enabled and passing, or documented why they should be removed.
```

```
SUBAGENT 2D - Remaining Endpoint Coverage:
Create tests for untested endpoints.

UNTESTED ENDPOINTS (verify and add tests):
- /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/interfaces/endpoints/interactingEntity/
- /home/user/OpenQuarterMaster/software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/interfaces/endpoints/identifiers/
- ItemListCrud
- ItemCategoriesCrud
- StoredEndpoints
- InBlockEndpoints

Target: Achieve >70% endpoint test coverage.

Deliverable: New test classes for all untested endpoints.
```

---

### TRACK 3: UI/E2E Test Completion (P0 - CRITICAL)
**Launch 2 subagents**

```
SUBAGENT 3A - Items and Storage UI Tests:
Complete all TODO markers in UI tests.

FILES WITH TODOS:
1. /home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/test/java/tech/ebp/oqm/core/baseStation/interfaces/ui/pages/ItemsUiTest.java
   - Lines 89-93: TODO types of item, images, files, categories
   - Lines 163-165: TODO edit, id copy button, transactions

2. /home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/test/java/tech/ebp/oqm/core/baseStation/interfaces/ui/pages/StorageBlockUiTest.java
   - Lines 86-99: Multiple TODOs including images, files, categories, edit, parent blocks, item viewing, printouts, bulk adding

Use Playwright test framework. Existing utilities in:
/home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/test/java/tech/ebp/oqm/core/baseStation/testResources/ui/

Deliverable: Complete UI test coverage for Items and Storage pages.
```

```
SUBAGENT 3B - Transaction UI Tests:
Complete transaction workflow UI tests.

FILE WITH TODOS:
/home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/test/java/tech/ebp/oqm/core/baseStation/interfaces/ui/pages/transactions/add/AddBulkTransactionUiTest.java
- Lines 166-176: 11 TODOs for various transaction scenarios

Also complete:
- AddUniqueSingleTransactionUiTest.java (2 TODOs)
- AddUniqueMultiTransactionUiTest.java (2 TODOs)
- AddAmtListTransactionUiTest.java (5 TODOs)

Deliverable: Complete transaction UI test coverage.
```

---

### TRACK 4: UX & Accessibility Fixes (P0 - CRITICAL)
**Launch 2 subagents**

```
SUBAGENT 4A - Form Validation Implementation:
Implement comprehensive client-side form validation.

CURRENT STATE:
- No validation framework detected
- Only 37 required attributes across all forms
- No visual feedback (is-valid/is-invalid Bootstrap classes)

LOCATION: /home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/main/resources/

Tasks:
1. Audit all form inputs in templates/tags/ and templates/webui/pages/
2. Add proper validation attributes
3. Implement client-side validation in META-INF/resources/res/js/
4. Add Bootstrap validation classes and error message display
5. Ensure validation on blur/change, not just submit

Key forms to fix:
- itemAddEditModal.html
- storageBlockAddEditModal.html
- All transaction forms

Deliverable: Comprehensive form validation with visual feedback.
```

```
SUBAGENT 4B - Accessibility Improvements:
Improve accessibility to WCAG 2.1 AA standards.

CURRENT STATE:
- Only 210 ARIA attributes across 61 files
- 14 instances of visually-hidden class
- Limited keyboard navigation

Tasks:
1. Add ARIA labels to all interactive elements
2. Add aria-describedby for form validation errors
3. Implement keyboard navigation for modals
4. Add skip-to-content links
5. Ensure focus management for dynamic content
6. Fix carousel visibility issue:
   /home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/main/resources/META-INF/resources/res/css/main.css
   - TODO comment about carousel controls not visible

LOCATION: /home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/main/resources/

Deliverable: Accessibility improvements with ARIA compliance.
```

---

### TRACK 5: Documentation Completion (P0 - CRITICAL)
**Launch 2 subagents**

```
SUBAGENT 5A - Configuration & Deployment Docs:
Complete critical deployment documentation.

FILES TO COMPLETE:

1. /home/user/OpenQuarterMaster/deployment/Single Host/docs/config.md
   CURRENT: Only 14 lines with 4 TODOs:
   - "TODO:: more practical examples"
   - "TODO:: go over how config is read"
   - "TODO:: go over secrets"
   - "TODO:: go over how config values/secrets are resolved"

   ACTION: Expand to comprehensive configuration reference

2. Create CHANGELOG.md at repository root
   - Document changes between versions
   - Follow Keep a Changelog format

3. Create UPGRADING.md or migration guide
   - Document upgrade paths
   - Breaking changes between versions

Deliverable: Complete configuration documentation and changelog.
```

```
SUBAGENT 5B - User Guide Completion:
Complete the Station Captain User Guide.

FILE: /home/user/OpenQuarterMaster/deployment/Single Host/Station-Captain/docs/User Guide.adoc

KNOWN TODOS (18+ sections):
- Version selection
- SSL/HTTPS cert verification
- Core installation/config options
- Plugin management
- User self-registration toggle
- Data clearing utilities
- Captain Settings section
- "Python UI (Planned/TODO)" - determine if removing or implementing

Also complete:
- /home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/main/resources/templates/webui/pages/help.html
  - Help documentation is incomplete

Deliverable: Complete User Guide with all TODO sections resolved.
```

---

### TRACK 6: Deprecated Code Cleanup (P1)
**Launch 1-2 subagents**

```
SUBAGENT 6A - Remove Deprecated Subsystems:
Clean up deprecated code marked for removal.

DEPRECATED ITEMS:

1. Driver Subsystem - ENTIRE DIRECTORY:
   /home/user/OpenQuarterMaster/software/drivers/
   - README states: "THESE PARTICULAR PROJECTS ARE DEAD"
   - Includes: open-qm-driver-server, demoDriver

2. open-qm-core Library:
   /home/user/OpenQuarterMaster/software/libs/open-qm-core/
   - README states: "DEPRECATED/DEAD... scheduled for removal"

Tasks:
1. Verify no active code depends on these
2. Remove from build configurations
3. Remove from CI/CD workflows (.github/workflows/driverServer.yml, driverLib.yml)
4. Update any documentation references

Deliverable: Clean removal of deprecated code with no broken dependencies.
```

---

### TRACK 7: CI/CD Pipeline Fixes (P1)
**Launch 1 subagent**

```
SUBAGENT 7A - Enable Container-Based Tests:
Fix disabled CI/CD features.

KNOWN ISSUES:

1. /home/user/OpenQuarterMaster/.github/workflows/core-api.yml:45
   containerBased: [ false ] # TODO:: enable true

2. /home/user/OpenQuarterMaster/.github/workflows/core-baseStation.yml:44
   containerBased: [ false ] # TODO:: enable true

3. Station Captain workflow partially commented out

Tasks:
1. Enable container-based integration tests
2. Ensure all tests pass in CI
3. Add coverage reporting/enforcement
4. Document any blockers preventing enablement

Review all workflows in .github/workflows/

Deliverable: Fully functional CI/CD with container tests enabled.
```

---

### TRACK 8: Kubernetes/Helm Improvements (P1)
**Launch 1-2 subagents**

```
SUBAGENT 8A - Helm Chart Production Readiness:
Expand Helm chart for production use.

CURRENT STATE:
/home/user/OpenQuarterMaster/deployment/Kubernetes/helm/oqm-core/values.yaml
- Only 9 lines total
- Missing critical production configurations

ADD TO values.yaml:
- Resource limits and requests
- Replica counts and autoscaling
- Storage class configuration
- Ingress configuration
- Image pull policies
- Database credentials (external secrets reference)
- Environment-specific overrides
- Health check configuration

Also fix hardcoded versions:
- /home/user/OpenQuarterMaster/deployment/Kubernetes/argocd/standard/core-depot-deployment.yaml:19
- /home/user/OpenQuarterMaster/deployment/Kubernetes/yaml/openshift/deployments.yml (uses mongo:latest)

Complete Helm README TODOs.

Deliverable: Production-ready Helm chart with comprehensive values.yaml.
```

---

### TRACK 9: Station Captain Python Review (P2)
**Launch 1-2 subagents**

```
SUBAGENT 9A - Station Captain Code Quality:
Address TODO markers and improve code quality.

50+ TODO markers across Python files:

HIGH PRIORITY:
- /home/user/OpenQuarterMaster/deployment/Single Host/Station-Captain/src/lib/ConfigManager.py (12 TODOs)
- /home/user/OpenQuarterMaster/deployment/Single Host/Station-Captain/src/lib/UserInteraction.py (11 TODOs)
- /home/user/OpenQuarterMaster/deployment/Single Host/Station-Captain/src/lib/PackageManagement.py (8 TODOs)
- /home/user/OpenQuarterMaster/deployment/Single Host/Station-Captain/src/lib/CertsUtils.py (5 TODOs - ACME support #998)

Also:
- Complete unit tests in unitTests/
- /home/user/OpenQuarterMaster/deployment/Single Host/Station-Captain/unitTests/ - "TODO:: finish tests"

Deliverable: Resolved TODOs and improved test coverage for Station Captain.
```

---

### TRACK 10: Plugin Ecosystem Review (P2)
**Launch 1 subagent**

```
SUBAGENT 10A - Plugin Status Assessment:
Assess all plugins for 1.0 readiness.

PLUGINS TO REVIEW:
/home/user/OpenQuarterMaster/software/plugins/

ACTIVE (verify complete):
- external-item-search/
- alert-messenger/
- alt-formats/
- image-search/
- mss-controller/ (has TODOs)
- cloud-context/

PLACEHOLDER (determine fate):
- mapper/ (minimal)
- miffie-llm/ (minimal)

Tasks:
1. Verify each active plugin builds and tests pass
2. Document incomplete plugins
3. Recommend: complete, defer, or remove placeholder plugins
4. Update /home/user/OpenQuarterMaster/software/plugins/docs/WritingPlugins.md (currently minimal)

Deliverable: Plugin readiness report with recommendations.
```

---

### TRACK 11: Incomplete UI Features (P2)
**Launch 1 subagent**

```
SUBAGENT 11A - Complete or Remove Incomplete Features:
Address incomplete user-facing features.

KNOWN INCOMPLETE:

1. Item Lists feature - COMMENTED OUT:
   /home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/main/resources/templates/tags/nav/mainWebPageTemplate.html
   - Lines 104-114 commented out
   - Decide: complete or remove

2. User profile features:
   /home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/main/resources/templates/webui/pages/you.html
   - TODO: add checked out items
   - TODO: add item history search
   - TODO: add storage history search

3. Storage capacity management:
   - storage.html line 137 - not implemented

4. Help documentation:
   /home/user/OpenQuarterMaster/software/core/oqm-core-base-station/src/main/resources/templates/webui/pages/help.html
   - Multiple TODO sections

Deliverable: All features either completed or cleanly removed with UI updated.
```

---

### TRACK 12: Infrastructure & Hardware (P3)
**Launch 1 subagent if time permits**

```
SUBAGENT 12A - Infrastructure Service Cleanup:
Address infrastructure TODOs.

KNOWN ISSUES:
1. /home/user/OpenQuarterMaster/deployment/Single Host/metrics/grafana/oqm-infra-grafana.service
   - "TODO:: figure out better username/password situation"
   - "TODO:: figure out how to configure"

2. /home/user/OpenQuarterMaster/deployment/Single Host/metrics/jaeger/oqm-infra-jaeger.service
   - "TODO:: hook up prometheus"
   - "TODO:: figure out how to configure"

3. Version pinning - several services use "latest" tag

4. Hardware (lower priority):
   - /home/user/OpenQuarterMaster/hardware/mss/ - 15+ TODOs
   - /home/user/OpenQuarterMaster/hardware/speed-scanner/ - TODO #1011, incomplete

Deliverable: Infrastructure improvements and documented hardware status.
```

---

## Orchestration Strategy

### Phase 1 - Launch P0 Tracks (Parallel)
Launch all P0 subagents simultaneously:
- Track 1: 3 security subagents
- Track 2: 4 test coverage subagents
- Track 3: 2 UI test subagents
- Track 4: 2 UX/accessibility subagents
- Track 5: 2 documentation subagents

Total: 13 subagents in parallel

### Phase 2 - Launch P1 Tracks
After P0 subagents report back, launch:
- Track 6: 1-2 cleanup subagents
- Track 7: 1 CI/CD subagent
- Track 8: 1-2 Kubernetes subagents

### Phase 3 - Launch P2/P3 Tracks
After P1 completes:
- Track 9: 1-2 Station Captain subagents
- Track 10: 1 plugin subagent
- Track 11: 1 UI feature subagent
- Track 12: 1 infrastructure subagent

### Aggregation
As subagents complete, aggregate findings into:
1. Critical issues requiring immediate fixes
2. GitHub issues to create for tracking
3. Pull requests with fixes
4. Updated documentation

---

## Success Criteria

Before declaring 1.0 ready:
- [ ] Zero hardcoded secrets in repository
- [ ] CORS properly restricted
- [ ] >70% endpoint test coverage
- [ ] All disabled tests fixed or removed
- [ ] All P0 UI tests complete
- [ ] Form validation implemented
- [ ] Basic accessibility compliance
- [ ] Configuration documentation complete
- [ ] User Guide complete
- [ ] Deprecated code removed
- [ ] Container-based CI tests enabled
- [ ] All P0/P1 TODO markers resolved

---

## Notes for Subagents

When launching subagents, include these instructions:
1. Read relevant files before making changes
2. Follow existing code patterns and style
3. Run tests locally if possible (gradle test, npm test, pytest)
4. Provide specific file:line references in findings
5. For code changes, create atomic commits with clear messages
6. Document any blockers or dependencies discovered
7. Report back with: completed items, remaining items, blockers, recommendations

Good luck with the review!
