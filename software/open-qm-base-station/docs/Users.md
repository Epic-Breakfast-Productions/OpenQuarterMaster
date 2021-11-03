# Users

This guide explains how users are authenticated in this system.

## Authentication

The endpoints on this server are protected by RBAC controls, utilizing JWT's.

If the config value `service.authMode` is set to `SELF`, then the service itself will provide those tokens and user
management.

If the config value is `EXTERNAL`, then the service assumes an external credential provider is at play and won't perform
user management.

### JWT

The jwt requires the following:

## User Roles

| Role | Description |
| ---- | :---------- |
| user | Given to everyone; required for any modification. |
| userAdmin | Required to look up or modify users beyond one's own. Given to the first created user. |
