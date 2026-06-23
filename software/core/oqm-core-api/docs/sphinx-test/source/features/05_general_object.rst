General Object Requirements
===========================

These requirements go over general requirements for all held data objects.

Relations
--------------

Objects in this database have relations, linking some objects to others.

Reference Removal before deletion
+++++++++++++++++++++++++++++++++

In order to keep the database consistent, we must enforce not having dangling references to since deleted objects.

Before deleting an object, the following conditions must be met:

- All references TO the object must be removed

OR

- All referencing objects must me deleted along with the object

Data Changes
------------

History
+++++++

Generally speaking, all objects are to have an associated history for each individual. This history is intended as additive only,
as in immutable as it is created.

Messaging
+++++++++

All individual data changes are to be sent out in the form of a message sent via the :ref:`requirement-interfaces-messaging` interface.
This is not necessarily the whole change, just a notification of the change, along with relevant information.
