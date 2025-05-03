@feature
Feature: 1.1 - Health

	This feature covers checking health of the running instance.

	@feature-1.1
	Scenario Outline: <serviceName> Health Check

	This tests that the <serviceName> is up and healthy.

		When the health check call to "<healthEndpoint>" at path "<servicePath>" is made
		Then the result of the healthcheck shows "<serviceName>" is running

		Examples:
			| serviceName  | servicePath        | healthEndpoint |
			| Core API     | /core/api          | /q/health      |
			| Base Station | /core/base-station | /q/health      |
			| Depot        | /core/depot        | /              |
			| Keycloak     | /infra/keycloak    | /              |

