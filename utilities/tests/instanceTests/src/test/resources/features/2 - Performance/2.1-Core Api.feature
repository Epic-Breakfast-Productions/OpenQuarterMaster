@feature
Feature: 2.1 - Core API Performance

	This feature covers checking health of the running instance.

	Background:
		Given a "10s" buffer between tests has occurred

	@feature-1.2
	Scenario Outline: Core API performance test (<numClients> clients, <numStorageBlocks> blocks, <numItems> items)

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
			# Typical Home - Small shop situation
			| 1          | 5                | 5        | 5          |
			| 2          | 5                | 5        | 5          |
			| 2          | 100              | 5        | 5          |
			# Medium
			| 10         | 100              | 5        | 5          |
			| 50         | 100              | 5        | 5          |
			# Large
			| 100        | 100              | 5        | 5          |

		# TODO:: do over duration, not just # of requests