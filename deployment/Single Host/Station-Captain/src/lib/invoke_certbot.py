from certbot import main as certbot_main
import argparse
import os

# This is a proof of concept script for Let's Encrypt

def create_directories(output_path):
    """Create the specified directories if they do not exist."""
    if not os.path.exists(output_path):
        os.makedirs(output_path)

def request_certificate(domain, email, mode, webroot_path=None, output_path=None, port=None, test_cert=False):

    create_directories(output_path)

    certbot_args = [
        "certonly",
        f"--{mode}",
        "-d", domain,
        "--email", email,
        "--agree-tos",
        "--non-interactive",
        "--expand",
        "--keep-until-expiring",
        "--rsa-key-size", "2048",
        "--cert-name", domain,
        "--config-dir", output_path,
        "--work-dir", output_path,
        "--logs-dir", output_path,
    ]

    if mode == "webroot":
        certbot_args.extend(["--webroot-path", webroot_path])
    elif mode == "standalone" and port:
        certbot_args.extend(["--standalone", "--http-01-port", str(port)])

    if test_cert:
        certbot_args.append("--test-cert")  # Add the flag to enable the staging environment

    try:
        certbot_main.main(certbot_args)
        print("Certificate obtained successfully.")
    except Exception as e:
        print(f"Error obtaining certificate: {e}")

def main():
    parser = argparse.ArgumentParser(description="Request SSL/TLS certificate using Certbot")
    parser.add_argument("--domain", required=True, help="Domain for which the certificate should be issued")
    parser.add_argument("--email", required=True, help="Email address for renewal and urgent notices")
    parser.add_argument("--mode", required=True, choices=["standalone", "webroot"], help="Validation mode (standalone or webroot)")
    parser.add_argument("--webroot-path", help="Webroot path for domain validation (required for webroot mode)")
    parser.add_argument("--output-path", required=True, help="Path to store the generated certificate files")
    parser.add_argument("--port", type=int, help="Port to use in standalone mode")
    parser.add_argument("--test-cert", action="store_true", help="Enable staging environment (request test certificate)")

    args = parser.parse_args()

    create_directories(args.output_path)

    request_certificate(args.domain, args.email, args.mode, args.webroot_path, args.output_path, args.port, args.test_cert)

if __name__ == "__main__":
    main()


# Request a test certificate in standalone mode
#python3 invoke_certbot.py --domain gw.adamjoline.com --email adamjoline@gmail.com --mode standalone --port 8444 --output-path /etc/oqm/certs --test-cert

# Request a test certificate in webroot mode
#python3 invoke_certbot.py --domain example.com --email your@example.com --mode webroot --webroot-path /var/www/html --output-path /etc/oqm/certs --test-cert

#Certbot version 1.21.0-1build1
