JWT Reference
#######################

This guide is an explainer to how JWT's are used by the Core API, and what needs to be included.

Interacting Entity Overview
---------------------------

A Quick reminder of how the service handles entity data. Anytime an entity (user, another service, anything) interacts
with the system, that entity is added to the database of "Interacting Entities". This is simply a record of who has
used the system in order to have a convenient cache of user information to reference. This entity is identified by the
id given from the auth provider, and updated (as necessary) on new interactions.

JWT Structure / Required fields
-------------------------------

This here is an example JWT data section that outlines the required fields and how they are used.

.. code-block:: json

   {
     "exp": 1781320250, // (1)
     "iat": 1781318750, // (1)
     "auth_time": 1781318750, // (1)
     "jti": "onrtac:e9653807-bd3c-d223-ef91-c139d96293c2", // (1)
     "iss": "http://localhost:8100/realms/oqm", // (1)
     "aud": "account",
     "sub": "3defea0b-ebd8-48f1-94ac-2fa2e19acf79", // (2)
     "typ": "Bearer",
     "azp": "oqm-app",
     "sid": "yvF2M2dYwC42fuGMJJtPOooA",
     "acr": "1",
     "realm_access": {
       "roles": [
         "default-roles-oqm",
         "inventoryView",
         "offline_access",
         "itemCheckout",
         "inventoryEdit",
         "uma_authorization",
         "inventoryAdmin",
         "user"
       ]
     },
     "resource_access": {
       "account": {
         "roles": [
           "manage-account",
           "manage-account-links",
           "view-profile"
         ]
       }
     },
     "scope": "openid email microprofile-jwt profile",
     "upn": "snappawapa",
     "email_verified": false,
     "name": "Gregory Stewart", // (3)
     "groups": [ // (4)
       "default-roles-oqm",
       "inventoryView",
       "offline_access",
       "itemCheckout",
       "inventoryEdit",
       "uma_authorization",
       "inventoryAdmin",
       "user"
     ],
     "preferred_username": "snappawapa",// (5)
     "given_name": "Gregory",
     "family_name": "Stewart",
     "email": "contact@gjstewart.net" // (6)
   }

Annotations on above:

#. Default, pretty much required in any JWT
#. This ``sub`` (Subject) field is used to identify the entity / used as the ID of the entity from the external auth provider
#. Used as the name of the entity in the database records.
#. These groups are the actual fields that determine what the user is allowed to do. TODO:: link to a reference on the roles
#. Used as the entity's username in the database record
#. Used as the entity's email in the database record
