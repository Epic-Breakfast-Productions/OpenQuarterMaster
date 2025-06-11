# SNH Configuration

[Back](README.md)

Configuration on a single node host is facilitated by the `oqm-config` utility.

Use `oqm-config -h` to get started.

Example usage to read a value:

```bash
oqm-config -g 'infra.mongodb.host'
```

Configuration files are stored under `/etc/oqm/serviceConfig/`.  User
changes are merged with defaults at startup.  Secret values can also be
stored in this directory and referenced by key; the utility resolves the
final values when templates are rendered.
