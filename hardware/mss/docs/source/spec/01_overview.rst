Communication Spec Overview
#############################

Basic Concepts
==============

.. mermaid::

	---
	title: MSS Module Main Components
	---
	flowchart LR
		subgraph MSS Related
			direction TB
			module@{ shape: win-pane, label: "MSS Module"}
			controller[MSS Controller]
		end
		coreapi@{ shape: cyl, label: "OQM Core API"}
		user@{ shape: flag, label: "User"}
		other@{ shape: cloud, label: "Other Services"}

		user -- uses --> controller
		other -- uses --> controller

		controller -- controls --> module
		controller -- updates --> coreapi
		module -- reports to --> controller



The above is a diagram of the ecosystem as pertinent to the MSS Modules. This spec, however, is directly concerned with
just the MSS Module communication with the MSS Controller:

.. mermaid::

	---
	title:
	---
	flowchart LR
		module@{ shape: win-pane, label: "MSS Module"}
		controller[MSS Controller]

		controller <-- MSS Protocol --> module

Module Components
-----------------

Generally, the setup of a module is fairly straightforward. The two real components of a basic module are the microcontroller
and the storage units.

Typically, this would be an ESP32/Arduino style microcontroller, and a modified set of storage drawers(to add lights, etc).

Data Organization
-----------------

Block numbers
______________

Within a module, the individual blocks are identified by their number. This number is a count up to the number of blocks
supplied. For example, a module with 64 blocks would have block numbers starting from ``1`` to ``64``.


General Goals
=============

These are general goals for the communication spec to follow and help guide the design:

Functionality
-------------

These are the general functions that the spec should support

Lights
______

Lighting up and otherwise highlighting individual blocks.

Weights
_______

Taking weights of what is stored in individual storage blocks.

Passthrough of inventory events
_______________________________

Reporting on changes in inventory. Example, changes in weight or scanning out / in an item with a barcode, etc.

Locking / Unlocking of drawers
______________________________

If applicable, the spec should support the programmatic locking and unlocking of drawers.

Transmission Method Agnostic
----------------------------

There are a wide variety of over-the-wire/air transmission methods out there. This protocol is intended to sit on top
of the transmission protocol in order to be implementation agnostic, and be a flexible system.

Bidirectional
-------------

Messages / commands can be sent in either direction, either from the controller to the module, or vice versa.

"Dumbness" of Modules
---------------------

The modules themselves are generally not intended to hold or keep track of inventory themselves. They can report on the
state of sensors (such as weight), or provide an interface to help report inventory movement (scanners, etc), but all
logic of inventory management and what to do with that information is to be left to the controller and core inventory
system.
