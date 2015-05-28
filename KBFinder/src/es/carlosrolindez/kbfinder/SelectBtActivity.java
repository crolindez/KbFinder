package es.carlosrolindez.kbfinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class SelectBtActivity extends Activity {
	
	public static final String LAUNCH_MAC = "Launcher MAC intent";
	private static SelectBtService service;
	private final SelectBtHandler  handler = new SelectBtHandler();
	
	private static int answerPending = 0;
	
	private static String message;

	private static final int NO_QUESTION = 0;
	private static final int QUESTION_ALL = 1;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectbt);
		Intent myIntent = getIntent();
		
		ImageButton button_all = (ImageButton)findViewById(R.id.all);
		button_all.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				askAll();

			}
		});

    	
	    String deviceMAC = myIntent.getStringExtra(SelectBtActivity.LAUNCH_MAC);	

		service = new SelectBtService(this, handler, deviceMAC);
		service.start();		
    }

	
	@Override
	protected void onDestroy() {
		service.stop();
		super.onDestroy();
	}
	
	public static void askAll() {
		service.write(("ALL ? \r").getBytes());
		answerPending = QUESTION_ALL;
    }
	

	private static String getStringFromMessage() {
		if (message.isEmpty()) return "";
		String arr[] = message.split(" ", 2);
		message = arr[1].trim(); 
		return arr[0];		
	}
	
	private static String getIdentifierFromMessage() {
		if (message.isEmpty()) return "";   // first "
		String arr[] = message.split("\"", 2);
		message = arr[1]; 
		if (message.isEmpty()) return "";	// second "
		arr = message.split("\"", 2);
		message = arr[1].trim(); 			// remove space after "
		return arr[0];		

	
	}
	
	public static void interpreter(String m) {

		message = m;

		switch (answerPending) {
			case QUESTION_ALL:
				String password = getStringFromMessage();						Log.e("Password",password);
				String identifier = getIdentifierFromMessage();					Log.e("identifier",identifier);
				String standByState = getStringFromMessage();					Log.e("standByState",standByState);
				String standByMasterSettings = getStringFromMessage();			Log.e("standByMasterSettings",standByMasterSettings);
				String standBySlaveSettings = getStringFromMessage();			Log.e("standBySlaveSettings",standBySlaveSettings);
				String autoPowerMaster = getStringFromMessage();				Log.e("autoPowerMaster",autoPowerMaster);
				String autoPowerSlave = getStringFromMessage();					Log.e("autoPowerSlave",autoPowerSlave);
				String autoPowerVolume = getStringFromMessage();				Log.e("autoPowerVolume",autoPowerVolume);
				String autoPowerFM = getStringFromMessage();					Log.e("autoPowerFM",autoPowerFM);
				String autoPowerEQ = getStringFromMessage();					Log.e("autoPowerEQ",autoPowerEQ);
				String channel = getStringFromMessage();						Log.e("channel",channel);
				String stationFM = getStringFromMessage();						Log.e("stationFM",stationFM);
				String infoRDS = getStringFromMessage();						Log.e("infoRDS",infoRDS);
				String tunerSensitivity = getStringFromMessage();				Log.e("tunerSensitivity",tunerSensitivity);
				String equalizationMode = getStringFromMessage();				Log.e("equalizationMode",equalizationMode);
				String volumeFM = getStringFromMessage();						Log.e("volumeFM",volumeFM);
				String keepFmOn = message;										Log.e("keepFmOn",keepFmOn);
	
				break;
			
		} 
		answerPending = NO_QUESTION;
		
	}
	


	public static class SelectBtHandler extends Handler {
		public final static int MESSAGE_STATE_CHANGE = 1;
		public final static int MESSAGE_CONNECTING_FAILURE = 2;
		public final static int MESSAGE_READ = 3;
		public final static int MESSAGE_READING_FAILURE = 4;
		public final static int MESSAGE_WRITE = 5;
		public final static int MESSAGE_WRITING_FAILURE = 6;

		
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case MESSAGE_STATE_CHANGE:
	                switch (msg.arg1) {
                    	case SelectBtService.STATE_CONNECTED:
                    		new Handler().postDelayed(new Runnable() {
                    		    @Override
                    		    public void run() {
                    		    	askAll();               
                    		    }
                    		}, 300);
                    		break;
	                    case SelectBtService.STATE_CONNECTING:
	                    case SelectBtService.STATE_DISCONNECTED:
	                    case SelectBtService.STATE_NONE:


	                }
	                break;
	            case MESSAGE_WRITE:
	                byte[] writeBuf = (byte[]) msg.obj;
	                String writeMessage = new String(writeBuf);
	                Log.e("message written",writeMessage+"-");
	                break;
	            case MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                Log.e("message received",readMessage);
	                interpreter(readMessage);
	                break;
	            case MESSAGE_CONNECTING_FAILURE:
	            case MESSAGE_READING_FAILURE:
	            case MESSAGE_WRITING_FAILURE:
	                break;
	        }
	    }
	}

}
