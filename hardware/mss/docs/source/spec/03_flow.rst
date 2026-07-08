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
it does not support, it returns a response of state "UNSUPPORTED".

Commands
========

A quick reminder that commands are sent from the controller to the module. These are the commands defined by the spec, with
expected results.

.. collapse:: Get Module Info

	This is a straightforward

State Reports
=============

