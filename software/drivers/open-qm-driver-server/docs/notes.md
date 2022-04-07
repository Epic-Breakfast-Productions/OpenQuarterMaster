# Notes md

Making virtual serial ports:

`socat -d -d pty,raw,echo=0 pty,raw,echo=0`

Example output:

```
2022/04/06 16:40:40 socat[5521] N PTY is /dev/pts/3
2022/04/06 16:40:40 socat[5521] N PTY is /dev/pts/5
2022/04/06 16:40:40 socat[5521] N starting data transfer loop with FDs [5,5] and [7,7]
```

Will probably need to run this command in a thread, parse out the `dev`s created and use them for testing.

Looks like we can setup multiple w/o issue