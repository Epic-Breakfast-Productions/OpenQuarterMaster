@feature
@feature-1
@feature-1.1
Feature: 1.1 - Installers

	This feature covers the creation of installers.

	@feature-1.1.1
	Scenario: Installers are made

	This tests that the installers can be built

		When the command to make the installers are made
		Then command returns successfully
		And the following installers were created:
			| deb |
#      | rpm |

	@feature-1.1.2
	Scenario Outline: Installers used

	This tests that the installers can be used to install the `oqm-captain` manager script.

		Given the command to make the installers are made
		And command returns successfully
		When the "<installerType>" installer is installed on "<os>"
		Then the installer completed successfully
		And the "oqm-captain" script is present

		Examples:
			| installerType | os     |
			| deb           | ubuntu |
#			| rpm           | fedora |
