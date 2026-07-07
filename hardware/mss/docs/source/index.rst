.. OQM MSS HArdware documentation master file, created by
   sphinx-quickstart on Mon Jul  6 23:13:09 2026.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

OQM MSS Hardware documentation
==============================

.. image::
	/_static/module_lit.png
	:align: center

.. jsonschema:: ./_static/jsonSchemas/ModuleInfo.json
	:lift_description: true


.. toctree::
   :maxdepth: 2
   :caption: Contents:

.. mermaid::

    sequenceDiagram
      participant Alice
      participant Bob
      Alice->John: Hello John, how are you?
      loop Healthcheck
          John->John: Fight against hypochondria
      end
      Note right of John: Rational thoughts <br/>prevail...
      John-->Alice: Great!
      John->Bob: How about you?
      Bob-->John: Jolly good!
