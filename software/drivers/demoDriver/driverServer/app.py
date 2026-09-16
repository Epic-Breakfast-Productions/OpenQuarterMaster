from flask import Flask
from flask import request
from colormap import rgb2hex
from flask_cors import CORS
import serial
import threading

mutex = threading.Lock()

app = Flask(__name__)
CORS(app)

port="/dev/ttyACM0"
returnChar='^'
returnSep='|'

serialPort = serial.Serial()
serialPort.baudRate = 115200
serialPort.port = port

serialPort.open()


@app.route('/postMessage', methods=['POST'])
def postMessage():
    requestData=request.get_json()
    
    if requestData is None:
    	print("Bad Request")
    	abort(400)
    print("Got request")
    
    message=requestData['message']
    
    print("Message: " + message)
    
    mutex.acquire()
    serialPort.write(message.encode("UTF-8"));
    serialPort.write(b'\n');
    mutex.release()
    return "OK";
    
@app.route('/getState', methods=['GET'])
def getState():
    print("Getting state from device.");
    serialPort.write(b'$S\n');
    
    latestResponseLine = None
    mutex.acquire()
    while serialPort.inWaiting() != 0 or latestResponseLine is None:
    	curRead = serialPort.readline().decode("UTF-8")
    	print("Got message from device: " + curRead)
    	if len(curRead) > 0 and curRead[0] == returnChar:
    		print("Got new response line")
    		latestResponseLine = curRead
    mutex.release()
    print("Final Message from device: " + latestResponseLine)
    if latestResponseLine is None:
    	print("ERRROR:: unable to read response.")
    	abort(500)
    if latestResponseLine[1] == 'E':
    	errMessage = latestResponseLine[2:]
    	print("ERROR:: Got error response from device: " + errMessage)
    	abort(500)
    
    responseArr = latestResponseLine.strip().split(returnSep);
    responseObj = {}
    
    responseObj['encoderVal'] = int(responseArr[1].strip());
    responseObj['encoderPressed'] = (responseArr[2].strip() == '1');
    responseObj['currentMessage'] = responseArr[3].strip();
    
    pixelList = responseArr[4:len(responseArr)-1];
    
    for i in range(len(pixelList)):
        rgbint = int(pixelList[i])
        Blue = rgbint % 256
        Green = rgbint // 256 % 256
        Red = rgbint // 256 // 256 % 256
        
        pixelList[i] = rgb2hex(Red, Green, Blue)
    
    responseObj['pixelColors'] = pixelList
    
    
    return responseObj;


@app.route("/")
def hello_world():
    print("Root hit.")
    return "<p>Hello, World!</p>"
