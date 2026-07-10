The System
#############################

Open QuarterMaster is a modular system, and has a few different components. Listed below are the different components and links to their individual documentation.

Software
========

.. mermaid::

	---
	title: Overall System Diagram
	---
	graph TD

		user((User))

		subgraph Infrastructure
			I-Mongo[(MongoDB)]
			I-Postgres[(Postgres)]
			I-Keycloak[Keycloak]
			I-Kafka[(Kafka)]
			I-Plugin[(Plugin DB)]

			I-Keycloak --> I-Postgres
		end

		subgraph Core Components
			C-API[Core API]
			C-BS[\Base Station/]

			C-BS -->|Reads/Writes to| C-API
		end

		subgraph Plugins
			P-a[\Plugin/]
		end


		user -->|Auth using| I-Keycloak
		user --> C-BS
		user --> P-a

		C-API --> I-Mongo
		C-API -->|Writes Messages to| I-Kafka
		C-API -->|Auth using| I-Keycloak

		C-BS -->|Auth using| I-Keycloak

		P-a --> C-API
		P-a -->|Auth using| I-Keycloak
		P-a -->|Reads messages from| I-Kafka
		P-a --> I-Plugin

		subgraph Key
			key-ui[\UI/]
			key-service[Service]
			key-database[(Database)]
		end

The above diagram describes how the system is designed, no matter the deployment method.

At the center of the system are the core components. These make up the base functionalities of the system, and
where plugins come in to extend that functionality. The Core API handles the bulk of this; all
basic inventory handling is done through this service. The Base Station is our main
front-end that users interact with. It is simply a wrapper of the functionalities provided by the Core API.

It is also important to note you can run just the Core API, if your system just needs an IMS backend. The rest of the OQM
system is intended to bring in a full end-user functional ecosystem, but is by no means required should you just need the core
functionality / interface.

The Infrastructure section here is to denote which technologies we are building on top of. For example, the Core API
directly relies on MongoDB as it's database backend, as well as writing messages to the Kafka broker. Generally, we
prefer using `Keycloak <https://www.keycloak.org/>`_ for our authentication needs, but this could be any OIDC auth provider.

Plugins operate separately from the core components, and add additional functionalities on top of the basic inventory
management ones. One example is providing an api to search for items from external sources (``external-item-search``).
Plugins can both tie into existing infrastructure components, such as Kafka and Keycloak, or they can operate on their
own with their own database.

Core Components
---------------

These are the components that make up the core functionalities of the system. Any OQM system is expected to include these.

* `Core API <https://docs.openquartermaster.com/components/software/core/api/>`_ - The Core service in charge of handling all inventory management tasks. Can be run as the IMS component in any software ecosystem.
* `Characteristics <https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/software/core/oqm-core-characteristics>`_ - Service for disseminating characteristics about a system, including who runs it, custom logos, etc.
* `Base Station <https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/software/core/oqm-core-base-station>`_ - The main frontend of the OQM system. Intended as a close to 1:1 feature parity for the core api backend.

Plugins
-------

Plugins are how we achieve our modular system. They add onto the core functionalities and enhance the experience with tailored experiences and tools.

Listed below are plugins we consider "released" and ready to use:

* `External Item Search <https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/software/plugins/external-item-search>`_ - A tool to get information about items from external sources.
* `Storagotchi <https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/software/plugins/storagotchi>`_ - A fun virtual pet to encourage inventory management.
