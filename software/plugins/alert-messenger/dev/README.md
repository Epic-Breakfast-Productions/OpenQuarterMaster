# Dev Files

These files are just used for development purposes.

To regenerate keys:

```
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout devTest-cert-key.pem -out devTest-cert-cert.pem -subj "/C=US/ST=TestState/L=TestTown/O=OQM/CN=localhost"
```

Remember to update the realm cert when you upgrade the cert
