#!/bin/bash

CONF_FILE="/etc/apache2/sites-available/000-default.conf"
CERT_FILE="/etc/oqm/certs/systemCert.crt"
KEY_FILE="/etc/oqm/certs/systemPrivateKey.pem"

if [ -f "$CERT_FILE" ] && [ -f "$KEY_FILE" ]; then
    echo "Determined have cert files, adjusting config file."
    cat <<EOF >> $CONF_FILE

<VirtualHost *:443>
    SSLEngine on
    SSLCertificateFile $CERT_FILE
    SSLCertificateKeyFile $KEY_FILE

    ServerAdmin webmaster@localhost
    DocumentRoot /var/www/html

    ErrorLog \${APACHE_LOG_DIR}/error.log
    CustomLog \${APACHE_LOG_DIR}/access.log combined
</VirtualHost>
EOF
#    a2enmod ssl
else
   echo "Running without SSL Config.";
fi

apache2-foreground
