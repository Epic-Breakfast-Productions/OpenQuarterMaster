# Gotchas, FAQ, & Troubleshooting

[Back](README.md)

## Gotchas

These are common 'gotchas' that people can sometimes fall into when setting up or using the OQM system on a single host setup.

### Using uppercase letters in host/domain name

Trying to run OQM on a system with a hostname that has uppercase letters is known to cause issues. The issues arise in user token verification, as the URL with uppercase letters does not match the lowercase equivalent that browsers and other tools automatically reformat th URL into.

Please make sure your hostname is all lowercase letters: https://opensource.com/article/21/10/what-hostname

### Trying to run on VirtualBox

Unfortunately, the underlying virtualization engine used by VirtualBox does not include the AVX x86 extension required by Mongodb. This makes it impossible to run OQM on a host virtualized within Virtualbox. More information can be found on the [System Requirements](System%20Requirements.md) page.

### Getting going on smaller hardware, slower internet

While the minimum spec'ed hardware listed in our [System Requirements](System%20Requirements.md) page manages to run the OQM system quite well once its up, getting the system up and runnig can prove a heavy task.

In short, the initialization of every component is the heaviest aspect of running the system. It requires a decent internet connection at first run, and a fair bit of cpu. This can lead, on smaller machines, a slow start which in turn can lead to reported errors and failures to get started or installed due to timeouts.

Typically, in cases such as this, you can follow the "Issue: not all services came up after installing core components" entry in the troubleshooting section below.

## FAQ

### Can I use the host I installed on for anything else (other than OQM)?

Generally speaking, yes, with a couple of caveats!

For 'normal' usage (general desktop use; web browser, gui programs, etc), you will be fine. For hosting other services and running additional containers, we strongly recommend running on a different host.

Given the nature of Docker, allowing other users to run containers opens a large security can of worms. Additionally, the OQM system utilizes a range of [ports](ports.md) and expects to be the only system using host ports.

That being said, 'normal' use of the system should be fine, and a regular non-root/sudoer/wheel account should not be able to affect the OQM instance. In fact, a usecase of the system is to access the OQM system from the same box to simplify operations.

## Troubleshooting

If you are reading these steps, we assume you have already followed the quickstart guide, and followed the wizard in the station captain script to install the core components.

If these steps don't get you to where you need to go, feel free to reach out.

### Diagnosing: listing installed packages

`apt list oqm-*`

This will list out the available and installed OQM related packages.

### Diagnosing: see what's running

`sudo docker stats`

This will show you the containers that are running, refreshing to give you updates. From here it is easy to see if a/which container/service might be crashing.

`systemctl list-units --all oqm-*`

This lists the systemd services and their statuses. This can give you a more direct view of what is and isn't running.

### Diagnosing: view logs

`journalctl -u <oqm service>`

All logs from the services get sent to journalctl, so they are viewable from there. You can get the service names from `systemctl list-units --all oqm-*`.

### Issue: not all services came up after installing core components

You can try:

 - Just give it some time. The system should retry starting the services and might come up eventually
 - a restart/manual start of the failing components or even restarting the whole box (essentially the equivalent to giving the system a kick). This lets the system start fresh and often gets things moving.
   - `sudo systemctl restart <oqm service>` to restart a single service.
