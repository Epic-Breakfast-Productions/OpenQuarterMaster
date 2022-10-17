# Infrastructure Service Files

Service files for non-installer services, not made by this project.

Installed by:

```
suco cp <file>.service /etc/systemd/system/

sudo systemctl daemon-reload

... (service can now be managed by usual systemctl commands)
```

## Host Ports used by the various infra pieces

| Port  | Infra Service | Purpose                 |
|-------|---------------|-------------------------|
| 27017 | Mongo         | Mongodb connection port |
| 8090  | Jaeger        | Jaeger UI               |
| 8091  | Jaeger        | Jaeger collector        |
| 8092  | Jaeger        | Jaeger collector 2?     |
| 8093  | Prometheus    | Prometheus UI           |
| 8094  | OpenTelemetry | Prometheus UI           |

