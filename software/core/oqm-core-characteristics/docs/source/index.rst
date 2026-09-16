.. OQM Core API documentation master file, created by
   sphinx-quickstart on Fri May 22 23:49:45 2026.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

#######################################
OQM Core Characteristics Server
#######################################

`Back to Overall Docs <https://docs.openquartermaster.com>`_

This project is the server providing characteristics functionalities for the Open QuarterMaster ecosystem.

Characteristics we define as customizations for the system to make it your own. Such as:

 - logos
 - banners
 - company information

Documentation table of contents:
--------------------------------

.. toctree::
	:maxdepth: 2
	:caption: Contents:

	running/index
	usage/index


Characteristics Server Design
-----------------------------

The server was designed to e simple and low-overhead as possible. Given the simple usecase, and desire for a small overall footprint,
we went with an extremely basic Python-based solution leveraging `Fastapi <https://fastapi.tiangolo.com/>`_.

This allows for a functional setup while providing a memory footprint of less than 25MiB.
