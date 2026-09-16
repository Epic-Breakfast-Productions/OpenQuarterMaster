# Driver Server

Used for interacting with the Macropad

## Requirements

`pip install flask flask_cors colormap`
`sudo apt install python3-flask python3-colormap python3-serial`

The user who runs the app must have appropriate permissions:

`sudo usermod -a -G dialout $USER`

## Running

`flask run`

