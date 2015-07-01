package es.carlosrolindez.kbfinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import es.carlosrolindez.kbfinder.SelectBtActivity.SelectBtHandler;

public class SelectBtService {

    private static final String TAG = "SelectBtService";
    
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    


    // Member fields
    private final SelectBtHandler mHandler;
    private final BluetoothDevice mDevice;

    private ConnectingThread mConnectingThread;
    private ConnectedThreadInput mConnectedThreadInput;
    private ConnectedThreadOutput mConnectedThreadOutput;

    private int mState;
    private boolean blocked;
    
    private List<String> mListOut;
    
    private BluetoothSocket mSocket;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // initial state
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device
    public static final int STATE_DISCONNECTED = 3;  // now connected to a remote device
    public static final int STATE_RECONNECTING = 4;  // re-connecting to a remote device
    

    

    
    public interface DisconnectActivity {
    	public void disconnect();
    }
    
    private DisconnectActivity mActivity;
   
    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public SelectBtService(DisconnectActivity activity,SelectBtHandler handler,String deviceMAC) {
        mState = STATE_NONE;
        mHandler = handler;
        mDevice = KBdevice.deviceInArray(A2dpService.deviceList, deviceMAC);
        mSocket = null;
        mListOut = new ArrayList<String>();
        mActivity = activity;
        blocked = false;
    }
    
    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.e(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(SelectBtHandler.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    
    /**
     * Start the service.
     */
    public synchronized void start() {
        Log.e(TAG, "start");
        setState(STATE_CONNECTING);

        mConnectingThread = new ConnectingThread();
        mConnectingThread.start();

    }

    
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.e(TAG, "stop");
        	
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
        mConnectingThread= null;


 
        mConnectedThreadInput = null;
        if (mConnectedThreadOutput != null)
        	mConnectedThreadOutput.closeThread = true;      
        mConnectedThreadOutput = null;
        setState(STATE_DISCONNECTED);
    }

    public  void write(String out) {
    	Log.e(TAG,"write");
    	synchronized (mListOut) {
    		mListOut.add(out);
    	}
    }
    
    public void block() {
    	blocked = true;
    }
    
    public void unblock() {
    	blocked = false;
    }
    
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectingThread extends Thread {

        public ConnectingThread() {

            try {
            	mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
        }

        public void run() {
            Log.d(TAG, "BEGIN mConnectingThread");
            setName("ConnectingThread");

            // Make a connection to the BluetoothSocket
            try {
                mSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                
                mHandler.obtainMessage(SelectBtHandler.MESSAGE_CONNECTING_FAILURE).sendToTarget();
                mConnectingThread = null;
                mActivity.disconnect();
                return;
            }

            mConnectingThread = null;

            // Start the thread to manage the connection and perform transmissions
            mConnectedThreadInput = new ConnectedThreadInput();
            mConnectedThreadOutput = new ConnectedThreadOutput();
            mConnectedThreadInput.start();
            mConnectedThreadOutput.start();//           setState(STATE_CONNECTED);

        }
    }
    
    
    
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThreadInput extends Thread {
        private InputStream mmInStream;
      
        
        public ConnectedThreadInput() {
            Log.e(TAG, "create ConnectedThreadInput");

            // Get the BluetoothSocket input and output streams
            try {
            	mmInStream = mSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
        }

        public void run() {
            Log.e(TAG, "BEGIN mConnectedThreadInput");
            byte[] buffer = new byte[255];
            int bytes;
            setState(STATE_CONNECTED);

            // Keep listening to the InputStream while connected
            while (true) {
                try {
	                    // Read from the InputStream
	                    bytes = mmInStream.read(buffer);
	                    mHandler.obtainMessage(SelectBtHandler.MESSAGE_READ, bytes, -1, buffer).sendToTarget();              			
                } catch (IOException e) {
                    Log.e(TAG, "Exception during read", e);
                    mHandler.obtainMessage(SelectBtHandler.MESSAGE_READING_FAILURE).sendToTarget();
                    mConnectedThreadInput = null;
                    
  //                  mActivity.disconnect();
                    return;
                }
            }
        }
    }
    
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */	
    private class ConnectedThreadOutput extends Thread {
        private OutputStream mmOutStream;
        private boolean paused;
        public boolean closeThread;

        public ConnectedThreadOutput() {
            Log.e(TAG, "create ConnectedThreadOutput");
            
            paused = false;
            closeThread = false;
            // Get the BluetoothSocket input and output streams
            try {
            	mmOutStream = mSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
        }

        public void run() {
            Log.e(TAG, "BEGIN mConnectedThreadOutput");
            byte[] buffer;
    
    	    try {  
    	    		sleep(500);
    	    } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted Exception during warmup", e);      	    	
    	    }
    		
            while (true) {
            	
            	if (closeThread) {
	                Log.e(TAG, "write thread ordered to close");
            		return;
            	}

            	if (!blocked) {
	            	if (!mListOut.isEmpty()) {
	            		paused = true;
	                    synchronized (mListOut) {
	                        buffer = mListOut.get(0).getBytes();
	                        	mListOut.remove(0);
	            		}
	                    try {       	
			                mmOutStream.write(buffer);
			                // Share the sent message back to the UI Activity
			                mHandler.obtainMessage(SelectBtHandler.MESSAGE_WRITE, -1, -1, buffer)
			                        .sendToTarget();
			            } catch (IOException e) {
			                Log.e(TAG, "Exception during write", e);
			                mHandler.obtainMessage(SelectBtHandler.MESSAGE_WRITING_FAILURE).sendToTarget();
			                return;
			            }          
	           		}
	
	                if (paused) {
	            	    try {  
	            	    	sleep(500);
	            	    } catch (InterruptedException e) {
	    	                Log.e(TAG, "Interrupted Exception during write", e);      	    	
	            	    }
	            		paused = false;
	 //           		blocked = false;
	            	}
            	}
            }
        }
        
 
    }

}
