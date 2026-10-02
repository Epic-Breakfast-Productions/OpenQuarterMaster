JWT Cheat Sheet
===============

We have this here to make it obvious where to get a JWT and how to use it with the system.

Getting a JWT
-------------

From Auth Provider (recommended)
++++++++++++++++++++++++++++++++

The recommended way to get a JWT for you to use in most cases (a script or other kind of automated tool) is to get one
directly from the auth provider. This usually makes for a more streamlined approach, and allows you to make a special account
for just the script.

Keycloak (as deployed by Single Node Host)
..........................................

To get a JWT from Keycloak, you need a couple things:

- The realm OQM is using
- Service Account Name
- Service Account Secret

To get the jwt, make the following REST call (example in bash/curl command):

.. code-block:: bash

    curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -u <client id>:<client secret> -d "grant_type=client_credentials" https://<host/ip of snh instance>/infra/keycloak/realms/oqm/protocol/openid-connect/token

This will get you a JWT to use. HOWEVER, note the expiration. it will be necessary to retrieve a new token before that expiration
comes around.

From Base Station UI (Testing)
++++++++++++++++++++++++++++++

You can also get a temporary JWT from the Base Station UI, for convenience. To access, click your user info dropdown, and
click "copy token".

Using the JWT
-------------

Example of the usage of the token:

.. code-block:: bash

	curl -X 'GET' 'https://<host/port>/core/api/api/v1/interacting-entity/self' -H 'accept: application/json' -H 'Authorization: Bearer <token>'
