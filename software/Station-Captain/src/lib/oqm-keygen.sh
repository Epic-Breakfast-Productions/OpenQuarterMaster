#!/bin/bash

# Default values for optional parameters
path="/etc/oqm/certs"
domain=""
ip1=""
ip2=""
ip3=""
dns1=""
dns2=""
dns3=""

#Check for pre-existing certs
if [ -d "$path" ]; then
  echo
  echo "WARNING!!! THE DIRECTORY $path ALREADY EXISTS.THIS ACTION WILL DELETE ALL PREVIOUS CERTIFICATES AND RECREATE THE PKI TRUST CHAIN!"
  echo
  echo
  echo "DO YOU WANT TO DESTROY THE EXISTING CERTIFICATES AND RECREATE THEM? (y/n)"
  read answer
  if [ "$answer" == "y" ]; then
    echo "Removing directory $path..."
    rm -rf "$path"
    echo "Certificates at $path have been removed."
    mkdir -p ${path}
  else
    echo "Existing Certificates located at $path will not be removed."
  fi
else
  echo "No previous OQM SSL directory found."
  echo "Spawning new certs..."
  mkdir -p ${path}
fi

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  key="$1"
  case $key in
    -d|--domain)
      domain="$2"
      shift # past argument
      shift # past value
      ;;
    -i1|--ip1)
      ip1="$2"
      shift # past argument
      shift # past value
      ;;
    -i2|--ip2)
      ip2="$2"
      shift # past argument
      shift # past value
      ;;
    -i3|--ip3)
      ip3="$2"
      shift # past argument
      shift # past value
      ;;
    -d1|--dns1)
      dns1="$2"
      shift # past argument
      shift # past value
      ;;
    -d2|--dns2)
      dns2="$2"
      shift # past argument
      shift # past value
      ;;
    -d3|--dns3)
      dns3="$2"
      shift # past argument
      shift # past value
      ;;
    -p|--path)
      path="$2"
      shift # past argument
      shift # past value
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

# Check if domain and ip1 are provided, otherwise prompt the user for input
if [[ -z "$domain" || -z "$ip1" ]]; then
  # Prompt the user for domain name
  read -p "Enter domain name: " domain

  # Prompt the user for IP addresses
  read -p "Enter IP address 1: " ip1
  if [ -n "$ip1" ] && ! echo "$ip1" | grep -Eq '^([0-9]{1,3}\.){3}[0-9]{1,3}$'; then
    echo "Invalid IP address: $ip1"
    exit 1
  fi

  read -p "Enter IP address 2 (leave blank for none): " ip2
  if [ -n "$ip2" ] && ! echo "$ip2" | grep -Eq '^([0-9]{1,3}\.){3}[0-9]{1,3}$'; then
    echo "Invalid IP address: $ip2"
    exit 1
  fi

  read -p "Enter IP address 3 (leave blank for none): " ip3
  if [ -n "$ip3" ] && ! echo "$ip3" | grep -Eq '^([0-9]{1,3}\.){3}[0-9]{1,3}$'; then
    echo "Invalid IP address: $ip3"
    exit 1
  fi

  # Prompt the user for DNS names
  read -p "Enter DNS name 1: " dns1
  read -p "Enter DNS name 2 (leave blank for none): " dns2
  read -p "Enter DNS name 3 (leave blank for none): " dns3
fi

# Create root CA & Private key 
openssl req -x509 -sha256 -days 356 -nodes -newkey rsa:2048 -subj "/CN=${domain}/C=US/L=OQM" -keyout ${path}/rootCA.key -out ${path}/rootCA.crt 
chown root:root ${path}/rootCA.crt && sudo chmod 644 ${path}/rootCA.crt
chmod 640 ${path}/rootCA.key  && sudo chmod 640 ${path}/rootCA.key

openssl genrsa -out ${path}/${domain}.key 2048

# Create csr.conf with dynamic SAN's
csr_conf="[ req ]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[ dn ]
C = US
ST = PA
L = OQM
O = OQM
OU = OQM
CN = ${domain}

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]"
if [ -n "$dns1" ]; then
  csr_conf+="\nDNS.1 = ${dns1}"
fi

if [ -n "$dns2" ]; then
  csr_conf+="\nDNS.2 = ${dns2}"
fi

if [ -n "$dns3" ]; then
  csr_conf+="\nDNS.3 = ${dns3}"
fi

if [ -n "$ip1" ]; then
  csr_conf+="\nIP.1 = ${ip1}"
fi

if [ -n "$ip2" ]; then
  csr_conf+="\nIP.2 = ${ip2}"
fi

if [ -n "$ip3" ]; then
  csr_conf+="\nIP.3 = ${ip3}"
fi
csr_conf+="\n"

# Write csr.conf to file
echo -e "$csr_conf" > "${path}/csr.conf"

# create CSR request using private key
sudo openssl req -new -key ${path}/${domain}.key -out ${path}/${domain}.csr -config ${path}/csr.conf

# Create a external config file for the certificate
cert_conf="authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]"
if [ -n "$dns1" ]; then
  cert_conf+="\nDNS.1 = ${dns1}"
fi

if [ -n "$dns2" ]; then
  cert_conf+="\nDNS.2 = ${dns2}"
fi

if [ -n "$dns3" ]; then
  cert_conf+="\nDNS.3 = ${dns3}"
fi

if [ -n "$ip1" ]; then
  cert_conf+="\nIP.1 = ${ip1}"
fi

if [ -n "$ip2" ]; then
  cert_conf+="\nIP.2 = ${ip2}"
fi

if [ -n "$ip3" ]; then
  cert_conf+="\nIP.3 = ${ip3}"
fi
cert_conf+="\n"

# Write csr.conf to file
echo -e "$cert_conf" > "${path}/cert.conf"

# Create SSl with self signed CA

openssl x509 -req -in ${path}/${domain}.csr -CA ${path}/rootCA.crt -CAkey ${path}/rootCA.key -CAcreateserial -out ${path}/${domain}.crt -days 365 -sha256 -extfile ${path}/cert.conf