# OQM via Compose

This directory contains the resources needed to run the OQM system via Podman/Docker compose.

The scope at the moment is to provide a basic featureset, i.e, just the core features.

Important considerations:

 - This is a less secure setup. We don't provide configuration management, or SSL certs.
 - Remember to treat the `.env.example` as a TEMPLATE. There are default values for passwords that you should update.
 - This is a barebones setup; just the core utilities. No plugins or utilities included.

Use cases:

 - Local testing
 - Non-"prod" workloads where you don't need extras

For a more robust, and supported setup, please look  at [single host deployment](../Single%20Host/).
