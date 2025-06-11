# OpenQuarterMaster Documentation Update Plan

## Executive Summary

This document outlines a comprehensive plan to update the OpenQuarterMaster project documentation. The project is an inventory management system with hardware integration capabilities, plugin architecture, and multiple deployment options. The documentation has not kept pace with development, containing outdated information, incomplete sections, and references to deprecated components.

## Current State Analysis

### Technology Stack (Actual)
- **Java Version**: Java 21 (not Java 11 as referenced in some docs)
- **Framework**: Quarkus 3.21.4+ (oqm-core-api) and 3.22.1 (oqm-core-base-station)
- **Database**: MongoDB with Panache
- **Messaging**: Kafka (RedPanda implementation), not AMQ as some docs suggest
- **Container Runtime**: Red Hat UBI9 with OpenJDK 21
- **Authentication**: Keycloak with JWT

### Project Structure
- **Namespace**: `tech.ebp.oqm` (not `open-qm` as referenced)
- **Component Naming**: `oqm-*` prefix for services
- **Maven Group ID**: `com.ebp.openQuarterMaster`

## Priority 1: Critical Updates (Blocking Issues)

### 1.1 Main README.md Updates
- **Location**: `/OpenQuarterMaster/README.md`
- **Issues**:
  - References Java 11 in quick links (should be Java 21)
  - Uses old directory paths with spaces (`deployment/Single%20Host`)
  - Plugin status shows "Planned" for potentially implemented features
  - Architecture diagram may not reflect current state
- **Actions**:
  - Update Java version references to Java 21
  - Fix all directory paths (remove URL encoding)
  - Verify and update plugin implementation status
  - Update or remove architecture diagram
  - Add current version information

### 1.2 Component README Title Corrections
- **Location**: `/software/oqm-core-api/README.md`
- **Issue**: Title says "open-qm-base-station" instead of "oqm-core-api"
- **Action**: Correct the title and ensure content matches the component

### 1.3 Deployment Path Corrections
- **Locations**: Multiple README files
- **Issue**: References to `Single%20Host` with URL encoding
- **Action**: Update all paths to use actual directory names

### 1.4 Remove Non-Existent Component References
- **Issue**: Documentation references "core-depot" which doesn't exist
- **Action**: Remove all references to core-depot from documentation and deployment files

## Priority 2: Incomplete Documentation

### 2.1 API Documentation
- **Location**: `/software/oqm-core-api/docs/`
- **Issues**:
  - `Features.adoc` is empty template
  - `DataModel.md` is incomplete
  - No actual API endpoint documentation
- **Actions**:
  - Document all REST endpoints from `/interfaces/endpoints/`
  - Complete data model documentation
  - Add authentication/authorization details
  - Document plugin API integration

### 2.2 Deployment Documentation
- **Kubernetes** (`/deployment/Kubernetes/README.md`):
  - Add Helm chart usage instructions
  - Document YAML manifests purpose
  - Add example deployments
- **Docker Compose** (`/deployment/Compose/README.md`):
  - Remove "not implemented" notice if compose files exist
  - Add actual compose documentation
- **Single Host** (`/deployment/Single Host/`):
  - Add system requirements upfront
  - Document AVX CPU requirement
  - Add memory/CPU requirements

### 2.3 Build Documentation
- **Location**: `/software/oqm-core-api/docs/development/BuildingAndDeployment.adoc`
- **Issue**: "Building" section is empty
- **Actions**:
  - Add gradle build commands
  - Document native vs JVM builds
  - Add Docker build instructions
  - Include makeInstallers.sh usage

### 2.4 Base Station Documentation
- **Location**: `/software/oqm-core-base-station/README.md`
- **Issues**: Generic Quarkus boilerplate, no project-specific info
- **Actions**:
  - Add project description
  - Document UI features
  - Add development setup
  - Remove or complete TODOs

## Priority 3: Technology Updates

### 3.1 Java Version Updates
- **All Locations**: Update Java 11 references to Java 21
- **Docker**: Ensure Dockerfile documentation matches UBI9 OpenJDK 21 usage

### 3.2 Messaging System Clarification
- **Issue**: Inconsistent references to Kafka vs AMQ
- **Action**: Clarify that Kafka (RedPanda) is used throughout

### 3.3 Framework Version Documentation
- Add Quarkus version requirements
- Document version constraints (e.g., Quarkus 3.21.4 blocker)

## Priority 4: Consistency and Standards

### 4.1 Naming Convention Standardization
- **Issue**: Mix of `open-qm-*`, `oqm-*`, variations with spaces
- **Action**: Standardize on `oqm-*` naming throughout

### 4.2 Version Information
- Add version badges to README files
- Include compatibility matrix
- Document upgrade paths

### 4.3 Directory Structure Documentation
- Create clear project structure documentation
- Explain purpose of each major directory
- Add navigation aids between related docs

## Priority 5: New Documentation Needed

### 5.1 Getting Started Guide
- Quick start for developers
- Prerequisites and system requirements
- First deployment walkthrough

### 5.2 Plugin Development Guide
- Plugin architecture overview
- Creating new plugins
- Plugin API reference
- Example plugin walkthrough

### 5.3 Hardware Integration Guide
- MSS (Module Storage System) setup
- Arduino integration
- Driver development

### 5.4 Operations Guide
- Monitoring and observability (OpenTelemetry)
- Backup and restore procedures
- Performance tuning
- Troubleshooting guide

## Implementation Plan

### Phase 1: Critical Fixes (Week 1)
1. Fix all incorrect titles and paths
2. Update Java version references
3. Remove non-existent component references
4. Fix deployment directory references

### Phase 2: Core Documentation (Week 2-3)
1. Complete API documentation
2. Fill in empty documentation sections
3. Update deployment guides
4. Standardize naming conventions

### Phase 3: New Content (Week 4-5)
1. Create getting started guide
2. Write plugin development guide
3. Document hardware integration
4. Add operations guide

### Phase 4: Review and Polish (Week 6)
1. Technical review of all changes
2. Test all documented procedures
3. Update screenshots and diagrams
4. Final consistency check

## Success Metrics

- All documentation files have accurate, up-to-date information
- No references to deprecated components or versions
- Clear navigation between related documentation
- All code examples and commands work as documented
- New users can successfully deploy within 30 minutes
- Developers can create plugins following documentation

## Notes

- Consider adding a `DOCUMENTATION_STANDARDS.md` file
- Implement documentation testing in CI/CD
- Add "last updated" timestamps to docs
- Consider documentation versioning strategy
- Add contribution guidelines for documentation