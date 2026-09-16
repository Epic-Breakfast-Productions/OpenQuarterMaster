Entities, Auth, and Roles
============================================

.. admonition:: Definitions

   .. glossary::
      Auth
         The process of authenticating users and verifying credentials and permissions.

      Interacting Entity
         Any entity (user, service, plugin, etc) that is interacting with the system.

      JWT
         A `Json Web Token (JWT) <https://www.jwt.io/introduction>`_ is a method for conveying information securely between entities, often
         used to convey authorization after authentication with an external provider.

JWT Based Auth
--------------

Entities are to authenticate themselves to the system with JWTs.
We consume JWT's to provide flexibility in auth provider, and the service is agnostic to any specific
JWT provider.

.. TODO include required fields

Roles
-----

The service uses roles in order to gatekeep if a particular interacting entity is allowed to access a given
resource.

.. TODO list roles

Keep track of entities interacting with the system
--------------------------------------------------

All entities that interact with the system are to be registered in the system, and available for reference in other areas.

Registration
^^^^^^^^^^^^

The first time an interacting entity interacts with the system, it is registered as an entity.

Updates
^^^^^^^

When an entity interacts in subsequent requests, the information in the given JWT is checked against
the current values stored in the database. If information differs, it is updated.

Allow entities to retrieve their own information
------------------------------------------------

Entities are allowed to retrieve their own data held on them.

Allow information to be retrieved on other entities
---------------------------------------------------

Others' full data
^^^^^^^^^^^^^^^^^

Others' Reference
^^^^^^^^^^^^^^^^^

Entities should be able to retrieve a reference object for any other entity, containing a bare minimum of data about the other entity.
Used to make sure UI's can properly display basic entity information in context.






