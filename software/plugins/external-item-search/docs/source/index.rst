.. External Item Search documentation master file, created by
   sphinx-quickstart on Mon Aug 24 14:56:39 2026.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

External Item Search Documentation
==================================

`Back to Overall Docs <https://docs.openquartermaster.com>`_

The External Item Search tool is intended to glean item information from many sources in order to unify the search
interface, and the schema of the results.

This tool is intended to be used as a plugin for the OQM ecosystem, but there is no reason that it couldn't be used
outside of this context.

Supported item providers (sources of our searches) (* = requires your own API key / auth):

 - \* `Barcode Lookup <https://www.barcodelookup.com/>`_
 - `DataKick <https://gtinsearch.org/>`_
 - \* `UPC Item DB <https://www.upcitemdb.com/>`_ (can require key, uses trial if no key)
 - `Open Food Facts <https://world.openfoodfacts.org/>`_
 - \* `Rebrickable <https://rebrickable.com/>`_

.. toctree::
	:maxdepth: 3
	:caption: Guides:

	general
	usage
	running
