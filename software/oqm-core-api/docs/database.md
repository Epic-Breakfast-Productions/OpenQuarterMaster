# Database

This software component utilizes [Mongodb](https://www.mongodb.com/) as its storage backend. We chose this because:

1. It is very [scalable](https://www.mongodb.com/resources/basics/scaling)
2. Allows for very flexible data models, polymorphistic data
3. On the whole, speedy and easy to stand up

Using Mongodb, we store our object data and file data in one place.

## OQM Databases

As this utility is essentially a database on its own, we have the notion of "databases"; these can be intuitively conceptualized as distinct datasets all controlled by this service.

These are implemented by leveraging the flexibility of the Mongodb client, and by appending OQM database names to the end of the database name in configuration.

Additionally, the server uses the configured database as-is in order to store 'top-level' information that is relevant to all databases, such as users that have interacted with the system, entries to keep track of these custom databases, and [Custom Units](customUnits.md).

The system also ensures at least one database is present at all times, and creates a database named `default` on startup if no databases exist.

For example, with the configuration `quarkus.mongodb.database=openQuarterMaster`, and databases `default` and `home`, you will see the following databases and collections in MongoDB:

 - `openQuarterMaster` database
   - `InteractingEntity` collection
   - `OqmMongoDatabase` collection
   - ...
 - `openQuarterMaster-default` database
   - `StorageBlock` collection
   - ...
- `openQuarterMaster-home` database
  - `StorageBlock` collection
  - ...


## Configuration

For information on what configuration is available, see the Quarkus documentation on the topic: https://quarkus.io/guides/mongodb#configuration-reference

> [!NOTE]
> The only real item of note is that we leverage the `quarkus.mongodb.database` value to be the _prepend_ name for where we actually store inventory data. See above for more info.
