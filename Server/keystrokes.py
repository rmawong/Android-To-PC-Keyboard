import time
import win32com.client
import win32gui
import win32process

time.sleep(3) # allow user quickly switch to another window's text field

hwnd = win32gui.GetForegroundWindow()

_, pid = win32process.GetWindowThreadProcessId(hwnd)

shell = win32com.client.Dispatch("WScript.Shell")

while True:
	shell.SendKeys("Hi Byron")
	time.sleep(1)
	shell.SendKeys("{Enter}")
	time.sleep(5)
	