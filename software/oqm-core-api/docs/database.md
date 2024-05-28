# Database

This software component utilizes [Mongodb](https://www.mongodb.com/) as its storage backend. We chose this because:

1. It is very [scalable](https://www.mongodb.com/resources/basics/scaling)
2. Allows for very flexible data models, polymorphistic data
3. On the whole, speedy and easy to stand up

Using Mongodb, we store our object data and file data in one place.

<!-- TODO:: uncomment when true
## OQM Databases

As this utility is essentially a database on its own, we have the notion of "databases"; these can be intuitively conceptualized as distinct datasets all controlled by this service.

These are implemented by leveraging the flexibility of the Mongodb client, and by appending OQM database names to the end of the database name in configuration.

-->

## Configuration

For information on what configuration is available, see the Quarkus documentation on the topic: https://quarkus.io/guides/mongodb#configuration-reference

<!-- TODO:: uncomment when true
> [!NOTE]
> The only real item of note is that we leverage the `quarkus.mongodb.database` value to be the _prepend_ name for where we actually store data. See above for more info.
-->


