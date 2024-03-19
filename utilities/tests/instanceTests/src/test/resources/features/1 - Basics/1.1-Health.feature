@feature
Feature: 1.1 - Health

	This feature covers checking health of the running instance.

	@feature-1.1.1
	Scenario Outline: <serviceName> Health Check

	This tests that the <serviceName> is up and healthy.

		When the health check call to "<healthEndpoint>" on port <servicePort> is made
		Then the result of the healthcheck shows "<serviceName>" is running

		Examples:
			| serviceName  | servicePort | healthEndpoint |
			| Core API     | 9001        | /q/health      |
			| Base Station | 9006        | /q/health      |
			| Depot        | 443         | /              |
			| Keycloak     | 8115        | /              |

