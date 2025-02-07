# SNH Snapshot Backup Plugin

This plugin is made to allow you to backup snapshots to a different location via a variety of methods. More explicitly, it allows for automatic syncing of the snapshots taken by the normal SNH process.

> [!IMPORTANT]
> It is very highly recommended to have encryption of snapshots enabled when using this plugin.

## Behavior



## Backup Methods

### Local directory

> [!NOTE]
> Not yet implamented

Uses rsync(?) to sync the snapshots dir to another directory. Probably not terribly necessary, as if the backup location is available on the local machine, you could just set the snapshot location to that. But not impossible for edge cases to exist.

#### Configuration

TODO

### SSH

> [!NOTE]
> Not yet implemented

Uses ssh to synchronize files to a remote location.

#### Configuration

TODO


### FTP

> [!NOTE]
> Not yet implemented

#### Configuration

TODO

### Object Storage

> [!NOTE]
> Not yet implemented

Syncs backup files to an object storage/ S3 compatible server.

#### Configuration

TODO
