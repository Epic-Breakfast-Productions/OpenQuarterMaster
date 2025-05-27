# Getting Started with OpenQuarterMaster Development

Welcome to OpenQuarterMaster! This guide will help you get up and running with development quickly.

## Prerequisites

Before you begin, ensure you have the following installed:

### Required
- **Java 21** (OpenJDK or Oracle JDK)
  - Ubuntu/Debian: `sudo apt install openjdk-21-jdk`
  - macOS: `brew install openjdk@21`
  - Windows: Download from [Adoptium](https://adoptium.net/)
- **Docker** (for running infrastructure services)
  - [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/macOS)
  - [Docker Engine](https://docs.docker.com/engine/install/) (Linux)
- **Git** for version control

### Recommended
- **IntelliJ IDEA** or **VS Code** with Java extensions
- **Gradle 8.5+** (though the wrapper is included)
- **MongoDB Compass** for database inspection
- **Postman** or similar for API testing

## Quick Start (5 minutes)

### 1. Clone the Repository
```bash
git clone https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster.git
cd OpenQuarterMaster/OpenQuarterMaster
```

### 2. Start Infrastructure Services
```bash
# Start MongoDB and Kafka using Docker Compose
cd deployment/Single Host/Infrastructure
docker-compose up -d mongodb kafka
```

### 3. Run Core API
```bash
cd software/oqm-core-api
./gradlew quarkusDev
```
The API will be available at http://localhost:8080

### 4. Run Base Station UI (in a new terminal)
```bash
cd software/oqm-core-base-station
./gradlew quarkusDev
```
The UI will be available at http://localhost:8081

## Development Setup (Detailed)

### 1. Project Structure Overview

See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for detailed layout. Key directories:
- `software/oqm-core-api/` - REST API backend
- `software/oqm-core-base-station/` - Web UI
- `software/plugins/` - Extension plugins
- `deployment/` - Deployment configurations

### 2. IDE Setup

#### IntelliJ IDEA
1. Open the project root directory
2. Import as Gradle project
3. Set Project SDK to Java 21
4. Enable annotation processing for Lombok

#### VS Code
1. Install extensions:
   - Extension Pack for Java
   - Lombok Annotations Support
2. Open the project root directory
3. VS Code will auto-configure based on existing settings

### 3. Running Tests

```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# All tests
./gradlew check
```

### 4. Building

```bash
# Build JVM artifacts
./gradlew build

# Build native executable (requires GraalVM)
./gradlew build -Dquarkus.native.enabled=true

# Build Docker images
./gradlew build
docker build -f src/main/docker/Dockerfile.jvm -t oqm-core-api:latest .
```

## Common Development Tasks

### Adding a New REST Endpoint

1. Create a new resource class in `software/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/interfaces/endpoints/`
2. Add `@Path`, `@Produces`, and `@Consumes` annotations
3. Implement your endpoint methods with JAX-RS annotations
4. Add OpenAPI documentation annotations

Example:
```java
@Path("/api/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {
    @GET
    @Operation(summary = "Get all items")
    public List<Item> getAllItems() {
        // Implementation
    }
}
```

### Working with the Database

OQM uses MongoDB with Panache. Example entity:
```java
@MongoEntity(collection = "items")
public class Item extends PanacheMongoEntity {
    public String name;
    public String description;
    
    public static List<Item> findByName(String name) {
        return find("name", name).list();
    }
}
```

### Creating a Plugin

1. Copy `software/plugins/open-qm-plugin-demo/` as a template
2. Update `build.gradle` with your plugin details
3. Implement your plugin endpoints
4. Register your plugin with the core API

## Debugging

### Enable Debug Logging
Add to `application.yml`:
```yaml
quarkus:
  log:
    level: DEBUG
    category:
      "tech.ebp.oqm":
        level: DEBUG
```

### Remote Debugging
```bash
./gradlew quarkusDev -Ddebug=5005
```
Then attach your debugger to port 5005.

## Troubleshooting

### Common Issues

**Port Already in Use**
```bash
# Find process using port
lsof -i :8080
# or
netstat -tulnp | grep 8080
```

**MongoDB Connection Failed**
- Ensure MongoDB is running: `docker ps`
- Check connection string in `application.yml`
- Default: `mongodb://localhost:27017`

**Out of Memory During Build**
```bash
export GRADLE_OPTS="-Xmx4g"
./gradlew build
```

## Next Steps

- Read the [API Documentation](software/oqm-core-api/docs/README.md)
- Explore the [Base Station UI Guide](software/oqm-core-base-station/docs/README.md)
- Join our [Discord](https://discord.gg/cpcVh6SyNn) for help
- Check out [open issues](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/issues) to contribute

## Additional Resources

- [Quarkus Guides](https://quarkus.io/guides/)
- [MongoDB with Panache](https://quarkus.io/guides/mongodb-panache)
- [Building Native Executables](https://quarkus.io/guides/building-native-image)
- [OpenQuarterMaster Wiki](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/wiki)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on:
- Code style and standards
- Commit message format
- Pull request process
- Testing requirements

Happy coding! 🚀