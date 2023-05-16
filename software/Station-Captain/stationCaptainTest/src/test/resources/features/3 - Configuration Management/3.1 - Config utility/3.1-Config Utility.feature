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

