Data Flow
#############################

This document outlines the behavior and flow of data in the protocol.

Overview
========

In general, the protocol works as such;

.. mermaid::

	sequenceDiagram
		participant Controller
		participant Module


		Controller->>+Module: Initialization (Get Info Command)
		Note right of Module: Always first step in communication
		Module-->>-Controller: Return OK, Module State

		loop Send Commands to Module
			Controller->>Module: Send command

			alt successful command
				Module -->>Controller: Return OK, optional data (if command prescribes)
			else request error
				Module -->>Controller: Return R_ERROR
			else module error
				Module -->> Controller: Return ERROR
			end
		end

		loop Send Reports to Controller
			Module ->> Controller: Send report
		end
		Note right of Module: No return for reports

Generally speaking, the protocol could be considered "rest inspired", where there is typically a command-response structure.
The process begins with the Controller making initial contact with the Module with a "Get Info" command (and receiving back
the module's information). From there, the Controller can send commands to the Module, and vice versa, the Module can send
state reports back to the Controller.

The rest of this document reviews the different commands and  individually.

Capabilities
------------

Not all modules are created equal, and therefore have different capabilities built in. A module declares what they are
capable of via the capabilities section of the Module Info they return. If a module receives a command to perform an action
it does not support, it returns a response of state ``UNSUPPORTED``.

Commands
========

A quick reminder that commands are sent from the controller to the module. These are the commands defined by the spec, with
expected results.

.. dropdown:: Get Module Info
	:icon: code

	This command is designed to prompt the return of information from the module pertinent to its operation. This
	includes (among other fields):

	 * Spec version it uses
	 * Serial id of the module, to uniquely identity it
	 * The number of blocks the system supports
	 * the capabilities of the module

	This command is intended as an initialization step for the controller to discover this information about the module.
	In some transmission modes, there might be additional data shared with the module to help facilitate communication.

	.. dropdown:: Request JSON
		:icon: arrow-up-right

		.. jsonschema:: ../_static/jsonSchemas/command/GetModuleInfoCommand.json
			:lift_title: false

	.. dropdown:: Response JSON
		:icon: arrow-down-left

		Returned is a ``CommandResponse`` object with ``ModuleInfo`` as the payload in the ``response`` field:

		.. jsonschema:: ../_static/jsonSchemas/command/CommandResponse.json
			:lift_title: false

		.. jsonschema:: ../_static/jsonSchemas/ModuleInfo.json
			:lift_title: false

.. dropdown:: Get Module State
	:icon: code

	This command is intended to get the current state of the module's blocks.

	.. dropdown:: Request JSON
		:icon: arrow-up-right

		.. jsonschema:: ../_static/jsonSchemas/command/GetModuleStateCommand.json
			:lift_title: false

	.. dropdown:: Response JSON
		:icon: arrow-down-left

		Returned is a ``CommandResponse`` object with ``ModuleState`` as the payload in the ``response`` field:

		.. jsonschema:: ../_static/jsonSchemas/command/CommandResponse.json
			:lift_title: false

		.. jsonschema:: ../_static/jsonSchemas/state/ModuleState.json
			:lift_title: false

		Further data on the ``ModuleState`` object fields:

		.. jsonschema:: ../_static/jsonSchemas/state/BlockState.json
			:lift_title: false

		.. jsonschema:: ../_static/jsonSchemas/state/BlockLightSetting.json
			:lift_title: false

		.. jsonschema:: ../_static/jsonSchemas/state/BlockWeightState.json
			:lift_title: false


.. dropdown:: Highlight Blocks
	:icon: code

	This command is intended to set certain blocks as highlighted. The command supports sending in blocks, in order to
	reduce the memory footprint of processing the entire request at once.

	.. mermaid::

		sequenceDiagram
			participant Controller
			participant Module


			Controller->>+Module: Highlight Blocks Command, carry = false
			Module-->>-Controller: Return OK, Module State

			loop Additional highlights
				Controller->>+Module: Highlight Blocks Command, carry = true
				Module-->>-Controller: Return OK, Module State
			end


	.. dropdown:: Request JSON
		:icon: arrow-up-right

		.. jsonschema:: ../_static/jsonSchemas/command/HighlightBlocksCommand.json
			:lift_title: false

	.. dropdown:: Response JSON
		:icon: arrow-down-left

		Returned is a ``CommandResponse`` object with no additional payload.

		.. jsonschema:: ../_static/jsonSchemas/command/CommandResponse.json
			:lift_title: false


.. dropdown:: Clear Highlight
	:icon: code

	This command is intended to clear any highlights currently active.

	.. dropdown:: Request JSON
		:icon: arrow-up-right

		.. jsonschema:: ../_static/jsonSchemas/command/ClearHighlightCommand.json
			:lift_title: false

	.. dropdown:: Response JSON
		:icon: arrow-down-left

		Returned is a ``CommandResponse`` object with no payload:

		.. jsonschema:: ../_static/jsonSchemas/command/CommandResponse.json
			:lift_title: false


.. dropdown:: Calibrate Weights
	:icon: code

	This command is intended to calibrate the weights held in the module. Intended to calibrate an empty module / block back to zero for accurate weight measurement.

	.. dropdown:: Request JSON
		:icon: arrow-up-right

		.. jsonschema:: ../_static/jsonSchemas/command/CalibrateWeightsCommand.json
			:lift_title: false

	.. dropdown:: Response JSON
		:icon: arrow-down-left

		Returned is a ``CommandResponse`` object with no payload:

		.. jsonschema:: ../_static/jsonSchemas/command/CommandResponse.json
			:lift_title: false


.. dropdown:: Pause Reports
	:icon: code

	This command is intended to tell the module to pause (and unpause) reporting of storage state. Intended for debugging
	or pausing potential mis-reads when adjusting, moving, cleaning, etc.

	.. dropdown:: Request JSON
		:icon: arrow-up-right

		.. jsonschema:: ../_static/jsonSchemas/command/PauseReportsCommand.json
			:lift_title: false

	.. dropdown:: Response JSON
		:icon: arrow-down-left

		Returned is a ``CommandResponse`` object with no payload:

		.. jsonschema:: ../_static/jsonSchemas/command/CommandResponse.json
			:lift_title: false


.. dropdown:: Lock Block(s)
	:icon: code

	This command is intended to tell the module to lock (and unlock) storage blocks.

	.. dropdown:: Request JSON
		:icon: arrow-up-right

		.. jsonschema:: ../_static/jsonSchemas/command/LockBlockCommand.json
			:lift_title: false

	.. dropdown:: Response JSON
		:icon: arrow-down-left

		Returned is a ``CommandResponse`` object with no payload:

		.. jsonschema:: ../_static/jsonSchemas/command/CommandResponse.json
			:lift_title: false


.. dropdown:: Notify User
	:icon: code

	This command is intended to tell the module display or notify the user of something.

	.. dropdown:: Request JSON
		:icon: arrow-up-right

		.. jsonschema:: ../_static/jsonSchemas/command/NotifyUserCommand.json
			:lift_title: false

	.. dropdown:: Response JSON
		:icon: arrow-down-left

		Returned is a ``CommandResponse`` object with no payload:

		.. jsonschema:: ../_static/jsonSchemas/command/CommandResponse.json
			:lift_title: false



State Reports
=============

State reports are reports on detected change in inventory. State reports are sent to the controller when:

 * There is a change in detected weight
 * The user has scanned something with the module, denoting it for removal or adding

They can be paused (and unpaused) by a `Pause Reports Command`.

The reports are to be sent over the medium as is.

The main data object that is sent:

.. jsonschema:: ../_static/jsonSchemas/report/InventoryEventReport.json
	:lift_title: false

Supporting objects:

.. jsonschema:: ../_static/jsonSchemas/report/ItemReport.json
	:lift_title: false

.. jsonschema:: ../_static/jsonSchemas/report/AmountItemReport.json
	:lift_title: false

.. jsonschema:: ../_static/jsonSchemas/report/UniqueItemReport.json
	:lift_title: false

.. jsonschema:: ../_static/jsonSchemas/report/BlockWeightReport.json
	:lift_title: false
