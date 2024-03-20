@feature
Feature: 2.1 - Core API Performance

	This feature covers checking health of the running instance.

	@feature-1.2
	Scenario Outline: Core API performance test

	This tests that the core api service is performant.

		Given the core api is on port 9001
		And we are using <numClients> clients
		And each client is creating <numStorageBlocks> storage blocks
		And each client is creating <numItems> items
		And each client is performing <numUpdates> updates to each object
		When the clients perform their actions
		Then all requests returned successfully

		Examples:
			| numClients | numStorageBlocks | numItems | numUpdates |
			| 1          | 5                | 5        | 5          |
			| 2          | 5                | 5        | 5          |
			| 100        | 100              | 5        | 5          |
