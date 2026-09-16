Configuration Reference
#######################

Below is the configuration reference to configure your Core API instance.

All values are optional, unless otherwise marked by: *Required*

Core API Specific Configuration
===============================

.. list-table::
   :header-rows: 1

   * - Config Key
     - Description
     - Values (Examples)
     - Default
   * - .. code-block:: none

          service.ops.currency
     - Sets the currency to be used by the system. Use any value that can be passed to
       `Currency.getInstance() <https://docs.oracle.com/javase/8/docs/api/java/util/Currency.html#getInstance-java.lang.String->`_
     - .. code-block:: none

          USD
       .. code-block:: none

          GBP
     - .. code-block:: none

                 USD
   * - .. code-block:: none

          service.item.expiryCheck.cron
     - Sets the interval at which expiry checks happen. In a cron format. `Docs <https://quarkus.io/guides/scheduler-reference#cron>`_
     -  .. code-block:: none

           0 0 * ? * * # Every Hour
     -  .. code-block:: none

           0 0 * ? * * # Every Hour
   * - .. code-block:: none

          service.tempDir
     - The directory to be used for temporary file creation and storage. Used for file uploads, and export bundle creation.
     -  .. code-block:: none

           /tmp
     -  .. code-block:: none

           /tmp/oqm-core-api
   * - .. code-block:: none

          service.image.resizing.enabled
     - If the service is to resize images when they are uploaded (images for attached images, not images uploaded as generic files)
     -  .. code-block:: none

           true
        .. code-block:: none

           false
     -  .. code-block:: none

           true
   * - .. code-block:: none

          service.image.resizing.height
     - The height to resize images to, in number of pixels.
     -  .. code-block:: none

           500
     -  .. code-block:: none

           750
   * - .. code-block:: none

          service.image.resizing.width
     - The width to resize images to, in number of pixels.
     -  .. code-block:: none

           500
     -  .. code-block:: none

           ${service.image.resizing.height}
   * - .. code-block:: none

          service.image.resizing.savedType
     - The image format to use to save the resulting images as.
     -  Either:

        .. code-block:: none

           jpg

        or:

        .. code-block:: none

           png
     -  .. code-block:: none

           jpg

MongoDB Configuration
=====================

For Mongo configuration, we leverage the settings as presented by Quarkus. For convenience, we have the most important configuration below, but
further options can be found on the `Quarkus MongoDB Configuration <https://quarkus.io/guides/mongodb#configuration-reference>`_ documentation.

Please see :doc:`04_mongo` for more information on how MongoDB is leveraged and used by the Core API.

.. list-table::
   :header-rows: 1

   * - Config Key
     - Description
     - Values (Examples)
     - Default
   * - .. code-block:: none

          QUARKUS_MONGODB_CONNECTION-STRING

       *Required*
     - The connection string to actually connect to Mongo
     - .. code-block:: none

           mongodb://${user}:${pass}@${host}:${port}
     - None, must supply this value.
   * - .. code-block:: none

          QUARKUS_MONGODB_DATABASE
     - The database to use for OQM. Also the prefix to use for specific datasets.
     - .. code-block:: none

           openQuarterMaster
     - .. code-block:: none

          openQuarterMaster

Kafka Configuration
=====================

For Kafka configuration, we leverage the settings as presented by Quarkus. For convenience, we have the most important configuration below, but
further options can be found on the `Quarkus Kafka Configuration <https://quarkus.io/guides/kafka#kafka-configuration>`_ documentation.

.. list-table::
   :header-rows: 1

   * - Config Key
     - Description
     - Values (Examples)
     - Default
   * - .. code-block:: none

          mp.messaging.outgoing.events-outgoing.enabled

       *Required*
     - Whether or not to enable the messaging functionality.
     - Either:

       .. code-block:: none

           true

       or:

       .. code-block:: none

                  false
     - None, must supply this value.
   * - .. code-block:: none

          mp.messaging.outgoing.events-outgoing.bootstrap.servers

       *Required*, if kafka enabled
     - The kafka bootstrap server(s) to connect to.
     - .. code-block:: none

           OUTSIDE://{infra.kafka.host}:{infra.kafka.port}

     - None, must supply this value (if kafka enabled).

JWT Configuration
=====================

For JWT configuration, we leverage the settings as presented by Quarkus. For convenience, we have the most important configuration below, but
further options can be found on the `Quarkus JWT Configuration <https://quarkus.io/guides/security-jwt#configuration-reference>`_ documentation.

Please see :doc:`05_jwt` for more information on how MongoDB is leveraged and used by the Core API.

.. list-table::
   :header-rows: 1

   * - Config Key
     - Description
     - Values (Examples)
     - Default
   * - .. code-block:: none

          smallrye.jwt.verify.key.location

       *Required*

       Or, some configuration that provides the certs for JWT verification.
     - The location of where to retrieve certs to verify JWT's. Config property allows for a specified external or internal
       location of the public key. The value can be a relative path or a URL.
     - .. code-block:: none

          http://oqm-infra-keycloak:8080/realms/oqm/protocol/openid-connect/certs

       .. code-block:: none

          /path/to/cert.pub
     - None, must supply this value.

Metrics/OpenTelemetry Configuration
====================================

For OpenTelemetry configuration, we leverage the settings as presented by Quarkus. For convenience, we have the most important configuration below, but
further options can be found on the `Quarkus OpenTelemetry Configuration <https://quarkus.io/guides/opentelemetry#configuration-reference>`_ documentation.

.. list-table::
   :header-rows: 1

   * - Config Key
     - Description
     - Values (Examples)
     - Default
   * - .. code-block:: none

          quarkus.otel.exporter.otlp.endpoint

     - Where to export OpenTelemetry metrics to.
     - .. code-block:: none

          http://#{host}:4317
     - None.
