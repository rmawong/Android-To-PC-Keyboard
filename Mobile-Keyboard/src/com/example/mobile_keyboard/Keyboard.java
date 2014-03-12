package com.example.mobile_keyboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Keyboard extends Activity {
	private static final int REQUEST_ENABLE_BT = 5; // System passes this back to onActivityResult() as the requestCode parameter
	protected static final int MESSAGE_READ = 8888; // constant for defining messages received

	public static final String NAME = "Keyboard";
	public static final UUID MY_UUID = UUID.fromString("88ad0f60-92f6-11e3-baa8-0800200c9a66"); // UUID identifies app on the device. Don't edit this.
	
	ConnectedThread keyboardThread;
	
	public Handler mHandler;
	Button mButton;
	EditText mEdit;
	
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private Menu mArrayAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);
        
        //set up vibrate
        final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        if(mBluetoothAdapter == null) {
        	//t.append("\n Bluetooth not supported");
        }
        else {
        	//t.append("\n Bluetooth supported");
        }
        
        if(!mBluetoothAdapter.isEnabled()) {
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        	System.out.println("REQUEST ENEABLE BT");
        }
        
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
        	for(BluetoothDevice device : pairedDevices) {
        		// Add the name and address to an array adapter to show in a ListView
        		//mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        	}
        }
        
        AcceptThread connectRequest = new AcceptThread();
        connectRequest.start();
        
//        mHandler = new Handler() {
//        	public void handleMessage(Message msg) {
//        		switch(msg.what) {
//	        		case MESSAGE_READ: {
//	        			byte[] readBuf = (byte[]) msg.obj;
//	        			String readMessage = new String(readBuf, 0, msg.arg1);
//	        		}
//        		}
//        	}
//        };
        
        
        
        mEdit = (EditText)findViewById(R.id.keystrokes);
        mButton = (Button)findViewById(R.id.button_log);

        mButton.setOnClickListener(
        		new View.OnClickListener() {
					
					@Override
					public void onClick(View v) 
					{
						byte[] bytes = mEdit.getText().toString().getBytes();
						Log.v("Keystrokes", mEdit.getText().toString());
						if(keyboardThread != null) {
							keyboardThread.write(bytes);
						}
						mEdit.setText(""); // clear text
					}
				});
    }


    private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;
    	
    	public AcceptThread() {
    		// Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                System.out.println("Listen using RFCOMM with service record");
            } catch (IOException e) { }
            
            mmServerSocket = tmp;
    	}
    	
    	public void run() {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        while (true) {
	            try {
	                socket = mmServerSocket.accept();
	                System.out.println("attempting to accept socket");
	            } catch (IOException e) {
	            	Log.e("Error accepting socket", "Socket");
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	            	Log.e("Socket != null", "Socket");
	                // Do work to manage the connection (in a separate thread)
	            	keyboardThread = new ConnectedThread(socket);
	            	keyboardThread.start();
	            	
	            	//This allows us to Toast on the MainActivity
	            	Keyboard.this.runOnUiThread(new Runnable() {

	                    @Override
	                    public void run() {
	                        Toast.makeText(Keyboard.this, "Connection Successful", Toast.LENGTH_SHORT).show();

	                    }
	                });
	            	
	            	try{
	                mmServerSocket.close();
	            	}catch(IOException e){
	            		Log.e("ERROR", "Socket did not close.");
	            	}
	                break;
	            }
	            else {
	            	System.out.println("Socket == null");
	            }
	        }
    	}
	        
	    public void cancel() {
	    	try {
	    		mmServerSocket.close();
	    	} catch (IOException e) { }
	    }
    } // end AcceptThread class
    
    
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
     
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
     
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
     
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
     
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
     
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
     
        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }
     
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    } // end ConnectedThread class
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.keyboard, menu);
        return true;
    }
    
}
