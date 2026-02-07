# Use the official image as a base
FROM mcr.microsoft.com/devcontainers/java:1-21-bullseye

# FIX: Remove the broken Yarn repository list that causes 'apt-get update' to fail
RUN rm -f /etc/apt/sources.list.d/yarn.list \
    && apt-get update

# (Optional) Ensure the vscode user has permissions if UID/GID mapping issues occur
RUN chown -R vscode:vscode /home/vscode