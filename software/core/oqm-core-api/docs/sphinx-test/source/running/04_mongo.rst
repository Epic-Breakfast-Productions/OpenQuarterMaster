MongoDB Reference
#######################

This guide is an explainer to how MongoDB is leveraged by the Core API

Databases
=========

OQM manages its own databases in order to ensure proper separation of data in operation. It also leverages a "top level"
database to keep track of data common across all databases.

Top Level
---------

The top level database (as set by the ``QUARKUS_MONGODB_DATABASE`` configuration value), is home to several collections;

- Databases
- Interacting entities

These collections facilitate the operation of the system as a whole, and aren't specific to any individual inventory database.


Individual Inventory Databases
------------------------------

Individual Inventory Databases are created by the OQM core api service, and are named in the following format:

.. code-block:: none

    <topLevelDatabase>-<individual inventory database>

(where ``topLevelDatabase`` is the value supplied by ``QUARKUS_MONGODB_DATABASE``)

These databases hold the actual inventory data, and are distinct datasets from each other. This keeps a solid separation
of datasets, and allows optimizations for MongoDB to leverage.

.. note::

   The default behavior of the core API is to create a default database at startup, if no database exists. It is simply called ``default``.

Examples
--------

Given we have a value of ``oqm`` for ``QUARKUS_MONGODB_DATABASE``, and inventory databases of ``home``, and ``shop``,
we would have the following MongoDB databases:

.. code-block:: none

    oqm
    oqm-home
    oqm-shop

