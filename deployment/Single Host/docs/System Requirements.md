# OQM Single Host Deployment System Requirements

This is a set of requirements for the entire system, not necessarily just the station captain.

This system is regularly tested on Ubuntu 22.04.

## Software Requirements

- A Modern Linux OS
    - At the moment we only support Debian-based systems (`apt`), we plan on eventually also supporting Fedora/RHEL (`dnf`) systems. (Tested on Ubuntu 22.04, Raspbian)
    - Tested and verified:
      - Ubuntu 22.04
      - Lubuntu 22.04
      - Raspbian (current version as of Dec 2023)

## Hardware Requirements:

- 4(quad) cores
  - As in, 4 threads total. Two cores with two threads each works.
- 8gb of RAM
- CPU Requirements
    - One of the following architectures:
        - 64-bit `x64` ISA, intel or AMD
            - Must include [AVX](https://en.wikipedia.org/wiki/Advanced_Vector_Extensions). Known supported:
                - Any AMD
                - Any modern Intel i3, i5, i7, i9
                - Intel N95, N100
        - 64-bit ARM v8

Note:: it is possible certain plugins, or a large number of plugins, will require more resources or have different hardware requirements.

### Proven SBC's and other 'specialty' hardware:

- ARM:
    - [Raspberry Pi 5B](https://www.raspberrypi.com/products/raspberry-pi-5/), 8Gb memory recommended. 4Gb would probably be fine for testing purposes only, but any less is probably not going to be enough for a good experience.


