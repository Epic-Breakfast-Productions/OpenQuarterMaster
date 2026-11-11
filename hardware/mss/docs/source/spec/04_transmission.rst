Transmission-Specific Protocol Addendum
########################################

The base spec only covers the actual data flow protocol of a module, but some transmission protocols require an additional
layer of protocol in order to function. We define these here.


Serial
======

Serial (over USB) is the simplest transmission method, and does not have any additional context to cover it.


REST
==============

This transmission method should be considered as EXPERIMENTAL for the time being.

Endpoint
--------

The REST endpoint the module is to present is `/api/command` to receive commands.

HTTPS
-----

The use of HTTPS is highly encouraged from both sides when implementing this. The rest of the security scheme pivots on
the usage of https.

Auth
----

TODO:: determine if use this, or go full mutual authentication

The protocol requires mutual authentication in the form of a preshared key.

This key is given to both the module and the controller (documented in the respective locations), and is presented from
either communication direction (either in command calls from the controller, or in reports from the module) in a standard
`Basic Authentication <https://en.wikipedia.org/wiki/Basic_access_authentication>`_ REST header. The module's serial id
is the username, and the preshared key is the password.

All calls made in either direction are to share this header.

Example,

With:

 - Serial ID of ``11223344``
 - Preshared key of ``password``

Then the value of the header would be:

``Authorization: Basic MTEyMjMzNDQ6cGFzc3dvcmQ=``

(given the username/password portion is base64 encoded)


Report URL
------------

The controller must supply a callback URL for the module to report to. This is to be provided via a header in command requests;

``OQM-MSS-REPORT-URL``

This is the full URL of where to send reports to, not just a base uri.

Every time the module receives a command, it is to update the held report url (as necessary).

Bluetooth
=========

(this is a planned feature in the future)
