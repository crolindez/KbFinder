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
	

	private static String getString(String message) {
		if (message.isEmpty()) return "";
		String arr[] = message.split(" ", 2);
		message = arr[1]; 
		return arr[0];		
	}
	public static void interpreter(String message) {
		String header = getString(message);

		switch (answerPending) {
		case QUESTION_ALL:
			String password = getString(message);
			String identifier = getString(message);
			String standByState = getString(message);
			String standByMasterSettings = getString(message);
			String standBySlaveSettings = getString(message);
			String autoPowerMaster = getString(message);
			String autoPowerSlave = getString(message);
			String autoPowerVolume = getString(message);
			String autoPowerFM = getString(message);
			String autoPowerEQ = getString(message);
			String channel = getString(message);
			String stationFM = getString(message);
			String infoRDS = getString(message);
			String tunerSensitivity = getString(message);
			String equalizationMode = getString(message);
			String volumeFM = getString(message);	
			String keepFmOn = getString(message);
			Log.e("Password",password);
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
                    		askAll();
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
