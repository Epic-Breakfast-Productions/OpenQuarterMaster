# Kiosk Mode Setup

Assuming coming from a relatively clean OS, and the base station is installed.

## Raspberry Pi

Assuming coming from a clean `raspbian-lite` install.

Sources:
 - https://reelyactive.github.io/diy/pi-kiosk/

 1. Install the X Window System (X11):
    1. `sudo apt-get install --no-install-recommends xserver-xorg`
    2. `sudo apt-get install --no-install-recommends xinit`
    3. `sudo apt-get install --no-install-recommends x11-xserver-utils`
 2. Install Chromium & kiosk dependencies
    1. `sudo apt-get install chromium-browser`
    2. `sudo apt-get install matchbox-window-manager xautomation unclutter`
    3. (optionally, for color emojis) `sudo apt-get install fonts-noto-color-emoji`
 3. Create the kiosk startup script (`~/kiosk.sh`):
    ```bash
    #!/bin/sh
    xset -dpms     # disable DPMS (Energy Star) features.
    xset s off     # disable screen saver
    xset s noblank # don't blank the video device
    matchbox-window-manager -use_titlebar no &
    unclutter &    # hide X mouse cursor unless mouse activated
    chromium-browser --display=:0 --kiosk --incognito --window-position=0,0 http://localhost
    ```
    (Ensure script is executable with following command: `chmod 755 ~/kiosk.sh`)
 4. Add the kiosk script to `.bashrc`:
    
    Add the following:
    `xinit /home/pi/kiosk -- vt$(fgconsole)`
 5. Misc setup in `raspi-config`:
    - dssd




## Ubuntu

TODO

## Fedora

TODO