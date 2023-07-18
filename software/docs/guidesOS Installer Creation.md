# Creating a customized Installer Image

## Setps

### Install Cubic

### Create Release

#### Base OS

Use the latest Ubuntu LTS version as the base.

#### Setup Disk Info

 - Version: `OQM-<version number>`
 - Release: `OQM on <name>`
 - Release URL: `<url of release on Github>`

#### Setup Virtual environment

 1. Update the system: `sudo apt update && sudo apt upgrade`
 2. Install `curl`: `apt install curl`
 3. Move files into system
   1. OQM Station Captain Installer
   2. Image files
 4. Install OQM Captian:
   1. Download latest station captain release: `curl -ks https://api.github.com/repos/Epic-Breakfast-Productions/OpenQuarterMaster/releases/latest | grep "browser_download_url.*.deb" | cut -d : -f 2,3 | tr -d \" | xargs wget --no-check-certificate`
   2. Install: `apt install -f ./<deb file just downloaded>`
 5. Set gnome settings:
   1. `gsettings set org.gnome.desktop.background picture-uri file:///usr/share/backgrounds/muo_wallpaper.jpg`
 6.  



## Tips and tricks

 - To find settings values, use `dconf watch /` while tweaking settings to find where they are set

## Resorces

 - https://github.com/PJ-Singh-001/Cubic
 - https://www.makeuseof.com/create-custom-ubuntu-iso-cubic/
 - https://itsfoss.com/create-custom-linux-mint-iso/
