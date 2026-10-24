Databases
=========

.. admonition:: Definitions

	.. glossary::

		Database
			A distinct, logical set of inventory data. Similar to the "database" concept in SQL.

Support Many Databases
----------------------

The service will support many individual databases.

Database Info
-------------

Databases have the following metadata to facilitate their definition and use:

 - Name - Used to uniquely identify the database
 - ID - Additionally used to uniquely identify the database
 - Display name - The name to display to users
 - Description - A quick description of the database.

Database Creation
-----------------

Administrators will be allowed to create databases as needed.

Database Deletion
-----------------

Administrators will be allowed to delete databases as needed.

Initial Database
----------------

At startup, if no databases yet exist, a database named ``default`` will be created.
