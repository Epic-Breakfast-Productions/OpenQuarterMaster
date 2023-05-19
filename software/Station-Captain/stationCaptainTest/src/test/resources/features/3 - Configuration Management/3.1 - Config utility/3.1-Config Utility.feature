@feature
@feature-3
@feature-3.1
Feature: 3.1 - Config Utility

	This feature covers the usage of the configuration utility `oqm-config`

	Background:
		Given the command to make the installers are made is successful
		And the installer is successfully installed on the os
		And test config files exist for testing config utility

	@feature-3.1.1
	Scenario: List all configs

	This tests that configs can be listed out

		When the "oqm-config -l" command is made on the running container
		Then command from the container returns successfully
		And the configurations are listed as output

	@feature-3.1.2
	Scenario Outline: Get a particular config value "<key>"

	This tests that configs can be grabbed individually

		When the "oqm-config -g <value>" command is made on the running container
		Then command from the container returns successfully
		And the configuration value "<expected>" was returned

		Examples:
			| value           | expected    |
			| test.testStr    | hello world |
			| test.testInt    | 100         |
			| test.testDouble | 1.5         |

	@feature-3.1.2
	@type-error
	Scenario Outline: Get a particular config value - bad config key "<key>"

	This tests that when given a bad config key, the error is handled gracefully

		When the "oqm-config -g <key>" command is made on the running container
		Then command from the container returns with 3 code
		And the config command outputs about the config key "<key>" not found

		Examples:
			| key                 |
			| foo.testStr         |
			| test.testFoo        |
			| test.testDouble.foo |

	@feature-3.1.3
	Scenario: Template Config Placeholder Replacement

	This tests that when provided a file, the output is that file with placeholders replaced with actual values

		When the "oqm-config -t /tmp/configTemplate.list" command is made on the running container
		Then command from the container returns successfully
		And the config template data returned has the placeholders filled

	@feature-3.1.3
	@type-error
	Scenario: Template Config Placeholder Replacement - File not found

	This tests that when provided a file, the output is that file with placeholders replaced with actual values

		When the "oqm-config -t /tmp/configTemplateFoo.list" command is made on the running container
		Then command from the container returns with 2 code
		And the config command outputs about the file not found

