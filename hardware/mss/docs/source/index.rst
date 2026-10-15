.. OQM MSS HArdware documentation master file, created by
   sphinx-quickstart on Mon Jul  6 23:13:09 2026.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

OQM MSS Hardware Documentation
==============================

.. image::
	/_static/module_lit.png
	:align: center

.. toctree::
   :maxdepth: 2
   :caption: Contents:

   Build & Usage Guide <guide/index>
   Hardware <hardware/index>
   Firmware <firmware/index>
   Communication Spec <spec/index>


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
