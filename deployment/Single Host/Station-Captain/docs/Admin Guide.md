# Administration Guide (Station captain OQM installation)

[Back](README.md)

## Important caveats:

 - Do NOT give non-sudo access to Docker to any user on the system. The setup currently relies on sudo-only access to Docker for security.
 - Read the [Gotchas, FAQ, & Troubleshooting](../../docs/tgf.md) guide to make sure things get setup and run smoothly.
 - 

## Administration Tools

### `oqm-captain`

This utility's job is to administer the oqm installation, and provides functionality to facilitate that.

Run `oqm-captain -h` for information about what it can do and how to do it.

### `oqm-config`

This utility is a dedicated configuration management tool that the installation depends on to operate.

Run `oqm-config -h` for information about what it can do and how to do it.
