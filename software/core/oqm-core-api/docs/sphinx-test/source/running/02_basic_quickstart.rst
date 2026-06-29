Basic Quickstart
################

Probably the best way to convey a barebones quickstart is to examine a compose file. The example below is an excerpt from
our `Compose Deployment Method <https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/deployment/Compose>`_:

.. code-block:: yaml

   # This compose file stands up a basic instance of Open QuarterMaster.
   # More information available here: https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/deployment/Compose/README.md
   #
   version: '3.8'

   # Define the internal network
   networks:
     oqm_internal:
       driver: bridge

   services:
     mongo:
       image: docker.io/mongo:${MONGO_IMAGE_TAG}
       container_name: oqm-infra-mongo
       restart: ${SYS_RESTART}
       networks:
         - oqm_internal
       volumes:
         # Mounts the container's internal data directory to a named volume
         - ${SYS_DATA_DIR}/mongo:/data/db
       environment:
         MONGO_INITDB_ROOT_USERNAME: ${MONGO_ROOT_USERNAME}
         MONGO_INITDB_ROOT_PASSWORD: ${MONGO_ROOT_PASSWORD}
         # This will create the database immediately upon startup
         MONGO_INITDB_DATABASE: ${MONGO_DATABASE}
       healthcheck:
         test: [ "CMD", "mongosh", "--quiet", "--eval", "db.runCommand({ping: 1}).ok" ]
         interval: 10s
         timeout: 10s
         retries: 5
         start_period: 20s

     postgres:
       # ... postgres is for keycloak
     keycloak:
       # ...

     core-api:
       image: docker.io/ebprod/oqm-core-api:${CORE_API_PORT_IMAGE_TAG}
       container_name: oqm-core-api
       restart: ${SYS_RESTART}
       depends_on:
         mongo:
           condition: service_healthy
         keycloak:
           condition: service_healthy
       networks:
         - oqm_internal
       ports:
         - "${CORE_API_PORT}:80"
       environment:
         "smallrye.jwt.verify.key.location": http://oqm-infra-keycloak:8080/realms/oqm/protocol/openid-connect/certs
         QUARKUS_MONGODB_CONNECTION-STRING: mongodb://${MONGO_ROOT_USERNAME}:${MONGO_ROOT_PASSWORD}@oqm-infra-mongo:27017
         QUARKUS_MONGODB_DATABASE: ${MONGO_DATABASE}
         "mp.messaging.outgoing.events-outgoing.enabled": "false"

As you can tell, the actual configuration of the ``core-api`` container is actually pretty straight forward. Simply point
it to a MongoDB database, and how to validate its certs, and you are off to the races.
