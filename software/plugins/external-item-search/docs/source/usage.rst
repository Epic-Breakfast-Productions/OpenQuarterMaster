Usage Guide
==================================

This section goes over how to use the External Item Search service.

Endpoints
---------

The service is fairly simple, and only has a few endpoints available.

These endpoints are documented at the running service's swagger endpoint at ``/q/swagger-ui/``

.. dropdown:: Get Search Methods
	:icon: code

	``GET`` ``/api/v1/info/methods``

	This endpoint gathers what search methods are currently available to use for searching.
	It also lists what services are available to do those searches.

	Example return data:

	.. code-block:: json

		{
			"BARCODE": [
				"BARCODE_LOOKUP",
				"DATAKICK",
				"UPC_ITEM_DB",
				"OPENFOODFACTS"
			],
			"TEXT": [
				"BARCODE_LOOKUP",
				"REBRICKABLE",
				"OPENFOODFACTS"
			],
			"PART_NUM": [
				"REBRICKABLE"
			],
			"SET_NUM": [
				"REBRICKABLE"
			]
		}

.. dropdown:: Get Search Providers
	:icon: code

	``GET`` ``/api/v1/info/providers``

	This endpoint gathers what providers (downstream services) we have available.

	Example return data:

	.. code-block:: json

		[
			{
				"id": "BARCODE_LOOKUP",
				"displayName": "BarcodeLookup.com",
				"description": "Comprehensive database of products, but a paid service. Can get a 2-week trial API key.",
				"cost": "Paid",
				"acceptsContributions": true,
				"homepage": "https://www.barcodelookup.com/",
				"enabled": true,
				"lookupMethods": [
					"BARCODE",
					"TEXT"
				],
				"lookupSources": [
					"BARCODE_LOOKUP"
				]
			},
			//...
		]


.. dropdown:: Search External Items
	:icon: code

	``GET`` ``/api/v1/search``

	This endpoint gathers what providers (downstream services) we have available.

	Example return data:

	.. code-block:: json

		[
			{ // successful responses will return with a "SUCCESS" type
				"service": "DATAKICK",
				"source": "DATAKICK",
				"method": "BARCODE",
				"name": "-  Blackberry peach",
				"unifiedName": "-  Blackberry peach",
				"description": "",
				"prices": {},
				"identifiers": {},
				"attributes": {
					"gtin14": "00888109010058",
					"size": "30ML",
					"ingredients": "s"
				},
				"images": [],
				"links": {},
				"type": "SUCCESS"
			},
			{ // services with no results will return with a "NO_RESULTS" type (omitted by default)
				"service": "UPC_ITEM_DB",
				"source": "UPC_ITEM_DB",
				"method": "BARCODE",
				"detail": "No results found.",
				"type": "NO_RESULTS"
			},
			{ // services that otherwise failed to be called will return with a "ERROR" type
				"service": "OPENFOODFACTS",
				"source": "OPENFOODFACTS",
				"method": "BARCODE",
				"detail": "Failed to connect.",
				"type": "ERROR"
			},
			//...
		]

Searching Specifics
-------------------

Searches take the following important GET parameters:

Search Query
............

``q`` (String)

This is the main search query. Required.

Lookup Method
.............

``lookupMethod``

One of the methods returned from the get methods endpoint, i.e, ``TEXT`` or ``BARCODE``

This is how to specify the method to use for search. Can specify multiple times to specify
multiple methods.

Omit to default to ``TEXT``.






