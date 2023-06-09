#Generates root certificate that can be used to issue and sign x509 certs.

function certs_generate_root() {
  # Default values for optional parameters
  local path="$SHARED_CONFIG_DIR/certs"
  local name=""

  # Create the directory if it doesn't exist
  if [ ! -d "$path" ]; then
    mkdir -p "$path"
  fi

    # Parse command line arguments
  while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
      -d|--domain)
        name="$2"
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

  # Check if domain is provided, otherwise prompt the user for input
  if [[ -z "$name" ]]; then
    # Prompt the user for domain name
    read -p "Enter the name of the Root CA, You can use an organization name here (e.g ACME): " name
  fi

  # Check for pre-existing certs
  if [ -f "$path/$name-CA.crt" ] && [ -f "$path/$name-CA.key" ]; then
    echo "WARNING!!! EXISTING KEY AND CERTIFICATE ALREADY EXIST. DO YOU WANT TO REMOVE EXISTING ROOT CA AND RECREATE IT? (y/n)"
    read answer
    if [ "$answer" == "y" ]; then
      echo "Creating a backup of existing certificates..."
      tar -czvf "$path/$name-CA-archived-$(date +%Y-%m-%d_%H-%M-%S).tar.gz" "$path/$name-CA.crt" "$path/$name-CA.key"
      echo "Removing files $path/$name-CA.crt and $path/$name-CA.key..."
      rm -f "$path/$name-CA.crt" "$path/$namw-CA.key"
      echo "Certificates successfully removed."
      echo "Creating Root Certificate..."

    else
      echo "Certificates located at $path/$name-CA.crt and $path/$name-CA.key were not removed."
      return
    fi
  fi

  # Create root CA & Private key
  echo "Creating Root CA and dependencies for $name.."
  (
    openssl req -x509 -sha256 -days 2920 -nodes -newkey rsa:2048 \
      -subj "/CN=${name}/C=US/L=OQM" \
      -keyout ${path}/$name-CA.key \
      -out ${path}/$name-CA.crt \
      2> >(while read line; do echo -ne "."; done)
  ) > /dev/null
 
  echo "New Root CA and private key successfully created."
  echo ""
  echo "NOTICE: Install the $name CA in your browser as a trusted root to avoid certificate mismatch errors!"
  
  chown root:root "${path}/$name-CA.crt" && chmod 644 "${path}/$name-CA.crt"
  chmod 640 "${path}/$name-CA.key" && chown root:root "${path}/$name-CA.key" && chmod 640 "${path}/$name-CA.key"
}




#Generates and x509 and all dependiencies and signs with root cert create in generate_root funtion.
function certs_generate_cert() {
  # Default values for optional parameters
  #local SHARED_CONFIG_DIR='/etc/oqm'  #For Testing
  local path="$SHARED_CONFIG_DIR/certs"
  local ip1=""
  local ip2=""
  local ip3=""
  local dns1=""
  local dns2=""
  local dns3=""
  local name=""

  cert_files=($path/*.crt)
cert_files_count=${#cert_files[@]}

  if [[ $cert_files_count -eq 0 ]]; then
    echo "No existing CA found in $path"
    read -p "Continue creating a self-signed certificate without a CA? (y/n): " confirm
    if [[ "$confirm" == "y" ]]; then
      if [[ -z "$name" ]]; then
        read -p "Enter the FQDN or IP address to be used on the certificate: " name
      fi
      (
      openssl req -x509 -sha256 -days 2920 -nodes -newkey rsa:2048 \
      -subj "/CN=${name}/C=US/L=OQM" \
      -keyout ${path}/$name.key \
      -out ${path}/$name.crt \
      2> >(while read line; do echo -ne "."; done)
      ) > /dev/null
    else
      echo "Aborted."
      return
    fi
  else
    if [[ $cert_files_count -eq 1 ]]; then
      cert_file=${cert_files[0]}
      echo "Using the existing signing CA located at: $cert_file"
      domain=$(openssl x509 -noout -subject -in $cert_file | awk -F' ' '{print $3}' | tr -d ',')
    else
      echo "Multiple CA certificates found in $path:"
      select cert_file in "${cert_files[@]}"; do
        if [[ -n "$cert_file" ]]; then
          echo "Using the selected signing CA located at: $cert_file"
          domain=$(openssl x509 -noout -subject -in $cert_file | awk -F' ' '{print $3}' | tr -d ',')
          break
        fi
      done
    fi
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

  # Check if common name (aka domain) is provided, otherwise prompt the user for input
  if [[ -z "$domain" ]]; then
    # Prompt the user for domain name
    read -p "Enter the webserver's DNS common name or IP address: " domain
  fi

  # Prompt the user for alternative names and IP addresses
  read -p "Do you want to add alternative names or IP addresses to the certificate? (y/n): " add_san

  if [[ $add_san == "y" ]]; then
      read -p "Enter IP address 1: " ip1
      if [ ! -z "$ip1" ] && ! echo "$ip1" | grep -Eq '^([0-9]{1,3}\.){3}[0-9]{1,3}$'; then
          echo "Invalid IP address: $ip1"
          exit 1
      fi

      read -p "Enter IP address 2 (leave blank for none): " ip2
      if [ ! -z "$ip2" ] && ! echo "$ip2" | grep -Eq '^([0-9]{1,3}\.){3}[0-9]{1,3}$'; then
          echo "Invalid IP address: $ip2"
          exit 1
      fi

      read -p "Enter IP address 3 (leave blank for none): " ip3
      if [ ! -z "$ip3" ] && ! echo "$ip3" | grep -Eq '^([0-9]{1,3}\.){3}[0-9]{1,3}$'; then
          echo "Invalid IP address: $ip3"
          exit 1
      fi
          # Prompt the user for Alternate DNS names
      read -p "Enter DNS name 1 (leave blank for none): " dns1
      read -p "Enter DNS name 2 (leave blank for none): " dns2
      read -p "Enter DNS name 3 (leave blank for none): " dns3
  fi

  # Create csr.conf with dynamic SAN's
  csr_conf="[ req ]
default_bits = 2048
prompt = no
default_md = sha256
distinguished_name = dn"

  if [[ $add_san == "y" ]]; then
    csr_conf+="\nreq_extensions = req_ext\n\n[ req_ext ]\nsubjectAltName = @alt_names\n\n[ alt_names ]"

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
  fi

csr_conf+="\n\n[dn]
C = US
ST = PA
L = OQM
O = OQM
OU = OQM
CN = ${domain}"

  # Write csr.conf to file
  echo -e "$csr_conf" > "${path}/csr.conf"

  # create CSR & private key
  openssl genrsa -out ${path}/${domain}.key 2048
  openssl req -new -key ${path}/${domain}.key -out ${path}/${domain}.csr -config ${path}/csr.conf

  # Create a external config file for the certificate
  cert_conf="authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment"
  
  if [[ $add_san == "y" ]]; then
    cert_conf+="\nsubjectAltName = @alt_names\n\n[ alt_names ]"

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
  fi

  # Write csr.conf to file
  echo -e "$cert_conf" > "${path}/cert.conf"

  # Generate x509 cert and sign with existin CA
  openssl x509 -req -in ${path}/${domain}.csr -CA ${path}/$name.crt -CAkey ${path}/$name.key \
    -CAcreateserial -out ${path}/${domain}.crt \
    -days 365 -sha256 -extfile ${path}/cert.conf \
    2> >(while read line; do echo -ne "."; done)
  echo "Successfully created X509 cert for $domain and signed with Certifcate authority"

}