@feature
Feature: 1.1 - Installers

	This feature covers the installation of OQM on a single host.


	@feature-1.1.1
	Scenario: Installation

	This tests that the install can occur without a hitch

		Given OQM is not currently installed
		And the host is setup for install
		When the command to install OQM is made
		Then OQM is running
