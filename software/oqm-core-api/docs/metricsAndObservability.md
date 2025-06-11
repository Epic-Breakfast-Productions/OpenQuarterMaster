# Metrics and Observability

The core services expose metrics using the Micrometer extension and
tracing information via OpenTelemetry.  When enabled, Prometheus can
scrape metrics from `/q/metrics` and trace data is forwarded to a Jaeger
or OTLP compatible backend.

## Enabling metrics

Metrics collection is active by default.  To disable it set
`quarkus.micrometer.export.prometheus.enabled=false` in the application
configuration.

## Enabling tracing

Configure the OpenTelemetry exporter with the endpoint of your tracing
backend, for example:

```properties
quarkus.otel.exporter.otlp.endpoint=http://localhost:4317
```

With Jaeger the legacy settings under `quarkus.jaeger.*` are also
supported.

Consult the [Quarkus OpenTelemetry guide](https://quarkus.io/guides/opentelemetry)
for additional options.
