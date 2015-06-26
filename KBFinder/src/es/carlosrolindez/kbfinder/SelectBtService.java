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
    
    private List<MessageDelayed> mListOut;
    
    private BluetoothSocket mSocket;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // initial state
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device
    public static final int STATE_DISCONNECTED = 3;  // now connected to a remote device
    
    public class MessageDelayed {
  
    	public static final int NO_QUESTION = 0;
    	public static final int QUESTION_ALL = 1;
    	public static final int RDS = 2;
    	public static final int BTID = 3;
    	public static final int FREQUENCY = 4;
    	
    	public String message;
    	public boolean delayed;
    	public int question;
    	
    	public MessageDelayed(String message, int question, boolean delayed) {
    		this.message = message;
    		this.delayed = delayed;
    		this.question = question;
    	}
    }
    
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
        mListOut = new ArrayList<MessageDelayed>();
        mActivity = activity;
    }
    
    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
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
        Log.d(TAG, "start");

        setState(STATE_CONNECTING);

            mConnectingThread = new ConnectingThread();
            mConnectingThread.start();
    }
    
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        mConnectingThread = null;

        mConnectedThreadInput = null;
        mConnectedThreadOutput = null;

        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
        
        setState(STATE_DISCONNECTED);
    }

    public  void write(String out,int question, boolean waitLonger) {
    	synchronized (mListOut) {
    		MessageDelayed message = new MessageDelayed(out,question,waitLonger);
    		mListOut.add(message);
    	}
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
            Log.d(TAG, "create ConnectedThreadInput");

            // Get the BluetoothSocket input and output streams
            try {
            	mmInStream = mSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
        }

        public void run() {
            Log.d(TAG, "BEGIN mConnectedThreadInput");
            byte[] buffer = new byte[255];
            int bytes;
            int question;
            setState(STATE_CONNECTED);

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
              		if (!mListOut.isEmpty()) {
              			synchronized (mListOut) {                    
              				question = mListOut.get(0).question;
              				if (mListOut.get(0).question!=MessageDelayed.NO_QUESTION)
              					mListOut.remove(0);
              			}
                   	}
              		else 
              			question = MessageDelayed.NO_QUESTION;

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(SelectBtHandler.MESSAGE_READ, bytes, question, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Exception during read", e);
                    mHandler.obtainMessage(SelectBtHandler.MESSAGE_READING_FAILURE).sendToTarget();
                    mConnectedThreadInput = null;
                    mActivity.disconnect();
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
        private boolean waitLonger;

        public ConnectedThreadOutput() {
            Log.d(TAG, "create ConnectedThreadOutput");
            
            paused = false;
            // Get the BluetoothSocket input and output streams
            try {
            	mmOutStream = mSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
        }

        public void run() {
            Log.d(TAG, "BEGIN mConnectedThreadOutput");
            byte[] buffer;
            

            while (true) {
           		if (!mListOut.isEmpty()) {
            		paused = true;
                    synchronized (mListOut) {
                    	waitLonger =  mListOut.get(0).delayed;
                        buffer = mListOut.get(0).message.getBytes();
                        if (mListOut.get(0).question==MessageDelayed.NO_QUESTION)
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
		                mConnectedThreadOutput = null;
		                mActivity.disconnect();
		                return;
		            }          			
           		}
            	if (paused) {
            	    try {  
            	    	if (waitLonger)
            	    		sleep(2500);
            	    	else
            	    		sleep(500);

            	    } catch (InterruptedException e) {
    	                Log.e(TAG, "Interrupted Exception during write", e);      	    	
            	    }
            		paused = false;
            	}
            }
        }
    }

}
