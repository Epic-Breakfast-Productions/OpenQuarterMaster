# Infrastructure Service Files

Service files for non-installer services, not made by this project.

Installed by:

```
sudo cp <file>.service /etc/systemd/system/

sudo systemctl daemon-reload

... (service can now be managed by usual systemctl commands)
```

## External host ports, when forwarded

TODO:: move to these

| Port  | Infra Service    | Purpose                           | Forwarded |
|-------|------------------|-----------------------------------|-----------|
| 27017 | Mongo            | Mongodb connection port           | Optional  |
| 8090  | Jaeger           | Jaeger UI                         | Optional  |
| 8091  | Jaeger           | Jaeger collector                  | Optional  |
| 8092  | Jaeger           | OTLP gRPC port                    | Optional  |
| 8095  | Prometheus       | Prometheus UI                     | Optional  |
| 8100  | OpenTelemetry    | OpenTelemetry?                    | Optional  |
| 8105  | ActiveMQ Artemis | Artemis UI                        | Optional  |
| 8106  | ActiveMQ Artemis | Artemis broker port (Qpid JMS)    | Optional  |
| 8107  | ActiveMQ Artemis | Artemis broker port (Artemis JMS) | Optional  |
| 8110  | Grafana          | Grafana UI port                   | Optional  |
| 8115  | Keycloak         | Keycloak port                     | Yes       |
| 8120  | Postgres         | Postgres port                     | Optional  |

## TODO's

- Document how service username/passwords are to be handled
