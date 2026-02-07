#!/bin/bash
set -e

echo ">>> Updating package lists..."
apt-get update

# --- 1. Install Podman ---
echo ">>> Installing Podman..."
apt-get install -y podman

# --- 2. Install Quarkus CLI ---
echo ">>> Installing Quarkus CLI..."
# JBang needs a specific directory when running as root, or it might complain.
# We force it to install to /usr/local/bin for global access.
curl -Ls https://sh.jbang.dev | bash -s - trust add https://repo1.maven.org/maven2/io/quarkus/quarkus-cli/
curl -Ls https://sh.jbang.dev | bash -s - app install --fresh --force quarkus@quarkusio

# --- 3. Install 'act' ---
echo ">>> Installing act..."
curl -s https://raw.githubusercontent.com/nektos/act/master/install.sh | bash

# --- 4. Install Helm ---
echo ">>> Installing Helm..."
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# --- 6. Install Mandrel (GraalVM) ---
# SDKMAN does not like running as root. 
# We will use a direct install for GraalVM/Mandrel to avoid "user" issues.
# Or we can skip this for now to get the build stable.
# echo ">>> Skipping Mandrel for now to ensure stability."

echo ">>> Setup Complete!"