# Station Captain Documentation

[Back](../)

Documentation pages:

 - [Quickstart Guide](Quickstart%20Guide.md)
 - [User Guide](User%20Guide.adoc)
 - [Admin Guide](Admin%20Guide.md)

See also:

 - [Setting up Kiosk Mode](../../docs/guides/Kiosk%20Mode.md)

## Scratch notes for Let's Encrypt

 - https://www.digitalocean.com/community/tutorials/how-to-use-certbot-standalone-mode-to-retrieve-let-s-encrypt-ssl-certificates-on-ubuntu-16-04

Steps: 

 1. Stop Base station
 2. Run: `sudo certbot certonly --standalone --preferred-challenges http -d demo.openquartermaster.com`
    - Certs will be placed in: `/etc/letsencrypt/live/<domain>/`
 4. Move certs to relevant locations
