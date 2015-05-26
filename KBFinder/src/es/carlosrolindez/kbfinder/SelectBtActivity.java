package es.carlosrolindez.kbfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SelectBtActivity extends Activity {
	
	public static final String LAUNCH_MAC = "Launcher MAC intent";
	SelectBtService service;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent myIntent = getIntent();
    	
	    String deviceMAC = myIntent.getStringExtra(SelectBtActivity.LAUNCH_MAC);	
		
		SelectBtHandler handler = new SelectBtHandler();
		service = new SelectBtService(this, handler, deviceMAC);
		service.start();		
    }
	
	@Override
	protected void onDestroy() {
		service.stop();
		super.onDestroy();
	}
	
	public static class SelectBtHandler extends Handler {
		public static final int MESSAGE_STATE_CHANGE = 1;
		public static final int MESSAGE_CONNECTING_FAILURE = 2;
		public static final int MESSAGE_READ = 3;
		public static final int MESSAGE_READING_FAILURE = 4;
		public static final int MESSAGE_WRITE = 5;
		public static final int MESSAGE_WRITING_FAILURE = 6;

		
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case SelectBtHandler.MESSAGE_STATE_CHANGE:
	                switch (msg.arg1) {
	                    case SelectBtService.STATE_CONNECTED:
	                    case SelectBtService.STATE_CONNECTING:
	                    case SelectBtService.STATE_DISCONNECTED:
	                    case SelectBtService.STATE_NONE:

	                }
	                break;
	            case SelectBtHandler.MESSAGE_WRITE:
	                byte[] writeBuf = (byte[]) msg.obj;
	                String writeMessage = new String(writeBuf);
	                break;
	            case SelectBtHandler.MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                break;
	            case SelectBtHandler.MESSAGE_CONNECTING_FAILURE:
	            case SelectBtHandler.MESSAGE_READING_FAILURE:
	            case SelectBtHandler.MESSAGE_WRITING_FAILURE:
	                break;
	        }
	    }
	}

}
