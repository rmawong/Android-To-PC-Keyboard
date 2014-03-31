# file: rfcomm-server.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a server application that uses RFCOMM sockets
#
# $Id: rfcomm-server.py 518 2007-08-10 07:20:07Z albert $
import time
import win32com.client

from bluetooth import *

shell = win32com.client.Dispatch("WScript.Shell")

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "00001101-0000-1000-8000-00805F9B34FB"

advertise_service( 
    server_sock, 
    "SampleServer",
    service_id = uuid,
    service_classes = [ uuid, SERIAL_PORT_CLASS ],
    profiles = [ SERIAL_PORT_PROFILE ], 
)
                   
print("Waiting for connection on RFCOMM channel %d" % port)

client_sock, client_info = server_sock.accept()
print("Accepted connection from ", client_info)

try:
    while True:
        data = client_sock.recv(1024)
        if len(data) == 0: break
        shell.SendKeys(data)
	client_sock.send("Server received message!")
	
except IOError:
    pass

print("disconnected")

client_sock.close()
server_sock.close()
print("all done")
