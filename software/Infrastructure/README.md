# Infrastructure Service Files

Service files for non-installer services, not made by this project.

Installed by:

```
sudo cp <file>.service /etc/systemd/system/

sudo systemctl daemon-reload

... (service can now be managed by usual systemctl commands)
```

## Host Ports used by the various infra pieces

| Port  | Infra Service    | Purpose                           |
|-------|------------------|-----------------------------------|
| 27017 | Mongo            | Mongodb connection port           |
| 8090  | Jaeger           | Jaeger UI                         |
| 8091  | Jaeger           | Jaeger collector                  |
| 8092  | Jaeger           | Jaeger collector 2?               |
| 8093  | Prometheus       | Prometheus UI                     |
| 8094  | OpenTelemetry    | Prometheus UI                     |
| 8095  | ActiveMQ Artemis | Artemis UI                        |
| 8096  | Jaeger           | OTLP gRPC port                    |
| 5672  | ActiveMQ Artemis | Artemis broker port (Qpid JMS)    |
| 61616 | ActiveMQ Artemis | Artemis broker port (Artemis JMS) |
| 8110  | Grafana          | Grafana UI port                   |
| 8115  | Keycloak         | Keycloak port                     |

## NEW, proposed Host Ports used by the various infra pieces

TODO:: move to these

| Port  | Infra Service    | Purpose                           |
|-------|------------------|-----------------------------------|
| 27017 | Mongo            | Mongodb connection port           |
| 8090  | Jaeger           | Jaeger UI                         |
| 8091  | Jaeger           | Jaeger collector                  |
| 8092  | Jaeger           | OTLP gRPC port                    |
| 8095  | Prometheus       | Prometheus UI                     |
| 8100  | OpenTelemetry    | OpenTelemetry?                    |
| 8105  | ActiveMQ Artemis | Artemis UI                        |
| 8106  | ActiveMQ Artemis | Artemis broker port (Qpid JMS)    |
| 8107  | ActiveMQ Artemis | Artemis broker port (Artemis JMS) |
| 8110  | Grafana          | Grafana UI port                   |
| 8115  | Keycloak         | Keycloak port                     |
| 8120  | Postgres         | Postgres port                     |

## TODO's

- Figure out how service username/passwords are to be handled
