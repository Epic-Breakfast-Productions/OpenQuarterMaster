Development
#############################

This guide outlines how we do our development.

System Requirements & Setup
===========================

Individual projects have specific requirements, but generally speaking the following is a good baseline:

 - Some modern Linux Distro (We develop on Ubuntu, but Fedora or other comparable setup would work)
 - Java 21
 - Python 3.12+
 - Docker or Podman

OQM Development Workflow
===========================

In general, we adhere to the following guidelines to perform work on this repo:

Branching
---------

Branches:

- ``main``

  - our default branch
  - is the current source of truth, where releases are cut

- ``development``

  - where changes being staged for release go
  - final review into ``main``

- ``dev/[issue #]-[issue name]``

  - where work happens

.. mermaid::

	---
	title: Git Workflow
	---
	gitGraph
		commit
		commit
		branch development
		checkout development
		commit
		commit
		branch dev/issNum-some-issue
		checkout dev/issNum-some-issue
		commit
		commit
		checkout development
		merge dev/issNum-some-issue
		checkout main
		merge development
		commit
		commit

Github Project / Project Management
-----------------------------------

https://github.com/orgs/Epic-Breakfast-Productions/projects/8

All issues are to be assigned to the OQM github project for tracking purposes. We use the features of the project interface to track issues, assign them to team members, and manage workflows.

.. mermaid::

	---
	title: Issue Workflow
	---
	graph TD

		S1((GitHub Issues))
		S2((GitHub Discussions))

		I[Github Issue]
		P[OQM Github Project]
		R{Refined; info gathered, prioritized}
		M[Milestone]
		Ready[Issue Ready for Work]
		W[Work done]
		Review[Review]
		Merged[Merged]
		C[Closed]

		S1 --> I
		S2 -- Generates --> I
		I -- Added to --> P
		P --> R
		R --> P
		P -- Issue added to --> M
		M --> Ready
		Ready --> W
		W --> Review
		Review --> Merged
		Merged --> C



Versioning, Branching, Tagging
==============================

Versioning
----------

The software in this repository follows `Semantic Versioning <https://semver.org/>`_:

``{MAJOR}.{MINOR}.{PATCH}[-{LABEL}]``

.. epigraph::

	Given a version number MAJOR.MINOR.PATCH, increment the:

	 * MAJOR version when you make incompatible API changes,
	 * MINOR version when you add functionality in a backwards compatible manner, and
	 * PATCH version when you make backwards compatible bug fixes.

	Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.

The labels we typically use are below:

- ``SNAPSHOT``, to denote a particular version is a snapshot of the current state, not production.
- ``DEV``, to denote the particular version is in development; don't use 'for real'

Releases
--------

Each piece of software here will get it's own release tag scheme. Generally speaking, each will follow the following format: ``{TYPE}-{PROJECT}-{VERSION}``, where "type" refers to what kind of project it is.

Example for each project:

 - Base Station - ``core-base+station-1.0.0-DEV``
 - Mongo - ``infra-mongo-1.0.0-DEV``
 - Station Captain - ``manager-station+captain-1.0.0-DEV``

Installers
""""""""""

All installer/ package names need to follow the following format:

``oqm-{type}-{packageName}``

Installer name convention is to use the package name, plus the version and installer extension. All parts of the package name must be lower-case, and use `+` instead of spaces, with no other punctuation.

Examples:

- Station Captain: ``oqm-infra-station+captain``
- Station Captain installer (deb): ``oqm-infra-station+captain-1.0.0-DEV.deb``

