Service Operation Guide
==================================

This section goes over how the service generally operates, and main ideas behind it.

In General
----------

As stated, the External Item Search tool is a search aggregator from various sources. A single request
to this service results in many calls to downstream source providers:

.. mermaid::

	flowchart TD
		start((Search Call))
		callsStart@{ shape: diamond, label: "Make External Calls" }
		call1[External Service Call 1]
		call2[External Service Call 2]
		call3@{ shape: procs, label: "..."}
		callsEnd@{ shape: diamond, label: "Ext Calls Return" }
		finish((Call Result))

		start --> callsStart
		callsStart --> call1
		callsStart --> call2
		callsStart --> call3

		call1 --> callsEnd
		call2 --> callsEnd
		call3 --> callsEnd

		callsEnd -- Streams results --> finish

Individual calls are made in a parallel fashion, and results are processed as such, and individual
results are streamed back to the user as they come.

For example, calling ``/api/v1/search?lookupMethod=BARCODE&q=00888109010058`` results in:

.. code-block:: json

	[
		{
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
		}
	]

It should be noted that individual calls to external services are cached, in order to prevent the
waste of usage-based limits.

Terminology
-----------

Service / Provider
..................

The terms "service" and "provider" are used here interchangeably. A service / provider is an upstream
source of item data.

Source
..............

A "source" is mostly 1:1 with a "service". The reason this distinction exists is that sometimes
some services themselves are aggregators. This is used to be able to specify searching (or not
searching) for sources behind these aggregators.

Lookup Method
..............

There are a few general types of data that items are identified by. The lookup method is our way
to distinguish and search by these.

Example, ``TEXT`` is for a clear text search. ``BARCODE`` is searching for any standard barcode
(UPC, ISBN, etc).
