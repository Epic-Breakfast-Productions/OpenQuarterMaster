Interfaces
==========

.. admonition:: Definitions

   .. glossary::
      REST
         `REST (Representational State Transfer) <https://en.wikipedia.org/wiki/REST>`_ is the most widely used standard
         for communicating between processes on the open web.

      Messaging
         Messaging is another common pattern for asynchronous processing. This enables the
         `event driven messaging pattern <https://en.wikipedia.org/wiki/Event-driven_messaging>`_

REST
----

REST is the primary route through which the system is interacted with. All functionality goes through this interface.

.. _requirement-interfaces-messaging:

Messaging
---------

Messaging is a secondary interface through which the service operates, in order to provide asynchronous functionalities
to the system as a whole.
