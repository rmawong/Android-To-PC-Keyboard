import time
import win32com.client

time.sleep(3) # allow user quickly switch to another window's text field

shell = win32com.client.Dispatch("WScript.Shell")

while True:
	shell.SendKeys("Hi Byron")
	time.sleep(1)
	shell.SendKeys("{Enter}")
	time.sleep(5)

