Running / Config Guide
======================

This section goes over how to run and configure the External Item Search service.

Running
-------

This service is distributed as a docker container, hosted on docker hub: https://hub.docker.com/r/ebprod/oqm-plugin-ext_item_search/tags

It has no runtime dependencies, and runs on its own.

Single Host Install
...................

To install on the standard single host deployment route, simply install the `oqm-plugin-ext+item+search` package.

Configuration
-------------

In General
..........

Given this is a `Quarkus <https://quarkus.io/>`_ based application, there is a fair bit of configuration
that is a carry-over from the framework. Notable and relevant examples:


.. list-table::
	:header-rows: 1

	*	- Config Key
		- Description
		- Values (Examples)
		- Default

	*	- .. code-block:: none

			quarkus.http.port
		- Sets the port that this service listens to.
		- .. code-block:: none

			8008
		- .. code-block:: none

			8008



Service / Provider
..................

Note:: URL configuration is noted here for completeness, but you should never need to touch this.

.. dropdown:: Barcode Lookup

	https://www.barcodelookup.com

	.. list-table::
		:header-rows: 1

		*	- Config Key
			- Description
			- Values (Examples)
			- Default

		*	- .. code-block:: none

				productLookup.providers.barcodelookup-com.enabled
			- If this provider is enabled or not. If api key is blank, overrides this to ``false``
			- .. code-block:: none

				true
			- .. code-block:: none

				true

		*	- .. code-block:: none

				productLookup.providers.barcodelookup-com.apiKey
			- The API key to use to authenticate with the service. If not specified, this service is disabled.
			- .. code-block:: none


			- .. code-block:: none



		*	- .. code-block:: none

				productLookup.providers.barcodelookup-com.url
			- The base URL of the website for API calls
			-
			- .. code-block:: none

				https://api.barcodelookup.com/

.. dropdown:: DataKick

	https://gtinsearch.org/

	.. list-table::
		:header-rows: 1

		*	- Config Key
			- Description
			- Values (Examples)
			- Default

		*	- .. code-block:: none

				productLookup.providers.datakick.enabled
			- If this provider is enabled or not.
			- .. code-block:: none

				true
			- .. code-block:: none

				true

		*	- .. code-block:: none

				productLookup.providers.datakick.url
			- The base URL of the website for API calls
			-
			- .. code-block:: none

				https://www.gtinsearch.org

.. dropdown:: Rebrickable

	https://rebrickable.com

	.. list-table::
		:header-rows: 1

		*	- Config Key
			- Description
			- Values (Examples)
			- Default

		*	- .. code-block:: none

				productLookup.providers.rebrickable.enabled
			- If this provider is enabled or not. If api key is blank, overrides this to ``false``
			- .. code-block:: none

				true
			- .. code-block:: none

				true

		*	- .. code-block:: none

				productLookup.providers.rebrickable.apiKey
			- The API key to use to authenticate with the service. If not specified, this service is disabled.
			- .. code-block:: none


			- .. code-block:: none



		*	- .. code-block:: none

				productLookup.providers.rebrickable.url
			- The base URL of the website for API calls
			-
			- .. code-block:: none

				https://rebrickable.com

.. dropdown:: UPC Item DB

	https://www.upcitemdb.com/

	.. list-table::
		:header-rows: 1

		*	- Config Key
			- Description
			- Values (Examples)
			- Default

		*	- .. code-block:: none

				productLookup.providers.upcitemdb.enabled
			- If this provider is enabled or not.
			- .. code-block:: none

				true
			- .. code-block:: none

				true

		*	- .. code-block:: none

				productLookup.providers.upcitemdb.apiKey
			- The API key to use to authenticate with the service. If api key is blank or unspecified, the trial endpoint is used.
			- .. code-block:: none


			- .. code-block:: none



		*	- .. code-block:: none

				productLookup.providers.upcitemdb.url
			- The base URL of the website for API calls
			-
			- .. code-block:: none

				https://api.upcitemdb.com

.. dropdown:: Open Food Facts

	https://world.openfoodfacts.org/

	.. list-table::
		:header-rows: 1

		*	- Config Key
			- Description
			- Values (Examples)
			- Default

		*	- .. code-block:: none

				productLookup.providers.openfoodfacts.enabled
			- If this provider is enabled or not.
			- .. code-block:: none

				true
			- .. code-block:: none

				true

		*	- .. code-block:: none

				productLookup.providers.openfoodfacts.url-barcode
			- The base URL of the website for API calls for barcodes
			-
			- .. code-block:: none

				https://world.openfoodfacts.net
		*	- .. code-block:: none

				productLookup.providers.openfoodfacts.url-search
			- The base URL of the website for API calls for text searches
			-
			- .. code-block:: none

				https://search.openfoodfacts.net
