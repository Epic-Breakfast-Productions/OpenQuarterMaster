# OQM Single Host Deployment System Requirements

[Back](README.md)

This is a set of requirements for the entire core system, not necessarily including any plugins.

This system is regularly tested on Ubuntu 24.04.

In addition to these requirements, please look through the "Gotchas" section on the [Gotchas, FAQ, & Troubleshooting](tgf.md) page to ensure a quick and easy setup.

## Software Requirements

- A Modern Linux OS
    - At the moment we only support Debian-based systems (`apt`), we plan on eventually also supporting Fedora/RHEL (`dnf`) systems.
    - Tested and verified:
      - Ubuntu 22.04
      - Lubuntu 22.04
      - Ubuntu 24.04
      - Raspbian (current version as of Dec 2024)

## Hardware Requirements:

- 4gb of RAM absolute testing minimum, 8gb recommended for real usage minimum. Based on:
  - Minimum memory usage is around 2GiB, with up to 2.5GiB at startup
  - The above stats are for minimum usage, no real traffic. Need some headroom for rest of OS and under load.
- CPU Requirements
    - 2 cores on headless server, 4 cores on a host with a desktop
    - One of the following architectures:
        - 64-bit `x64` ISA, intel or AMD
            - Must include [AVX](https://en.wikipedia.org/wiki/Advanced_Vector_Extensions). Known supported:
                - Any AMD
                - Any modern Intel i3, i5, i7, i9
                - Intel N95, N100, N300
        - 64-bit ARM v8
- Virtualization
  - The system typically runs well in a virtualized environment, but ensure that environment meets the above spec, and haven't seen issues in cloud instances.
  - It has been seen that __VirtualBox cannot run OQM as it does not provide the AVX extension__
  - Tested on:
    - KVM/LibVirt
    - Hyper-V

> [!NOTE]
> it is possible certain plugins, or a large number of plugins, will require more resources or have different hardware requirements.

> [!NOTE]
> If running on your own hardware, we strongly recommend that system be run on a UPS/ Battery Backup in order to prevent power outages and dips from affecting the system.

### Proven SBC's and other 'specialty' hardware:

- ARM:
    - [Raspberry Pi 5B](https://www.raspberrypi.com/products/raspberry-pi-5/)
        - 8Gb+ memory recommended. 4Gb would probably be fine for testing and minimal purposes only, but any less than 8Gb is probably not going to be enough for a good experience.
