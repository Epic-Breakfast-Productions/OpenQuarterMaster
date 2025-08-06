# Open QuarterMaster Admin Guide

[Back](README.md)

## General, tl;dr

Bare minimum to get started is telling the app where to find Mongo;

The test keys were created using the following command:

```
openssl genrsa -out private_key.pem 4096
openssl rsa -pubout -in private_key.pem -out public_key.pem
# convert private key to pkcs8 format in order to import it from Java
openssl pkcs8 -topk8 -in private_key.pem -inform pem -out private_key_pkcs8.pem -outform pem -nocrypt
```
