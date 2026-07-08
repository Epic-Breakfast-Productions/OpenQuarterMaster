Transmission-Specific Protocol Addendum
########################################

The base spec only covers the actual data flow protocol of a module, but some transmission protocols require an additional
layer of protocol in order to function. We define these here.


Serial
======

Serial (over USB) is the simplest transmission method, and does not have any additional context to cover it.


REST
==============

Callback URL
------------

The controller must supply a callback URL for the module to report to.

Auth
----

The protocol requires mutual authentication in the form of preshared keys.

TODO:: work this out


Bluetooth
=========

(this is a planned feature in the future)
