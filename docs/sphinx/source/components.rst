.. Open QuarterMaster documentation master file, created by
   sphinx-quickstart on Sun May 24 11:57:35 2026.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Components
#############################

Open QuarterMaster is a modular system, and has a few different components. Listed below are the different components and links to their individual documentation.

Core Components
===============

These are the components that make up the core functionalities of the system. Any OQM system is expected to include these.

* `Core API <https://docs.openquartermaster.com/components/software/core/api/>`_ - The Core service in charge of handling all inventory management tasks
* `Characteristics <https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/software/core/oqm-core-characteristics>`_ - Service for disseminating characteristics about a system, including who runs it, custom logos, etc.
* `Base Station <https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/software/core/oqm-core-base-station>`_ - The main frontend of the OQM system. Intended as a close to 1:1 feature parity for the core api backend.

Plugins
=======

Plugins are how we achieve our modular system. They add onto the core functionalities and enhance the experience with tailored experiences and tools.

* `External Item Search <https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/software/plugins/external-item-search>`_ - A tool to get information about items from external sources.
* `Storagotchi <https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/software/plugins/storagotchi>`_ - A fun virtual pet to encourage inventory management.
