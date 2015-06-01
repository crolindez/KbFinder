package es.carlosrolindez.kbfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SelectBtActivity extends FragmentActivity {
	
	public static final String LAUNCH_MAC = "Launcher MAC intent";
	private static SelectBtService service;
	private final SelectBtHandler  handler = new SelectBtHandler();
	
	private static boolean bootPending;
	private static boolean closeWhenPossible;
	private static int answerPending = 0;

	private static final int NO_QUESTION = 0;
	private static final int QUESTION_ALL = 1;
	
	private static SelectBtState selectBtState;
	
	private static ImageButton mainButton;

	// swipe fragments
    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mAdapter;
 //   private PagerAdapter mPagerAdapter;
	
	// animation
	private	static AnimationDrawable frameAnimation;
	private static ImageView splashImageView;
	
	private	static RelativeLayout splashLayout;
	private	static RelativeLayout controlLayout;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectbt);
		
		selectBtState = new SelectBtState();
		
		splashImageView = (ImageView) findViewById(R.id.SplashImageView);
		splashImageView.setBackgroundResource(R.drawable.on_animation);
		frameAnimation = (AnimationDrawable)splashImageView.getBackground(); 
		splashLayout = (RelativeLayout) findViewById(R.id.SplashLayout);
		controlLayout = (RelativeLayout) findViewById(R.id.ControlLoyaut);
		mainButton = (ImageButton) findViewById(R.id.MainPower);
		
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
 		
        splashImageView.post(new Runnable(){
		            @Override
		            public void run() {
		                frameAnimation.start();                
		            }            
		        }); 
		
		bootPending = true;
		closeWhenPossible = false;
          
		new Handler().postDelayed(new Runnable() {
		    @Override
		    public void run() {
		    	closeIfNotBooted();               
		    }
		}, 10000);

		mainButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switchOnOffState();				
			}
		});
		
		Intent myIntent = getIntent();
    	
	    String deviceMAC = myIntent.getStringExtra(SelectBtActivity.LAUNCH_MAC);	

		service = new SelectBtService(this, handler, deviceMAC);
		service.start();	
    }

	

	public void closeIfNotBooted() {
		if (bootPending) {
		    Toast.makeText(this, getString(R.string.device_not_availabe), Toast.LENGTH_LONG).show();
			finish();
		}
	}
		
	public static void askAll() {
		service.write(("ALL ? \r").getBytes());
		answerPending = QUESTION_ALL;
    }
	
	public static void writeOnOffState(boolean onOff) {
		if (onOff) 	service.write(("STB OFF \r").getBytes());
		else 		service.write(("STB ON \r").getBytes());
    }
	
	
	public static class MessageExtractor {
		private String message;
		
		public MessageExtractor(String m) {
			message = m;
		}
		
		public String getStringFromMessage() {
			if (message.isEmpty()) return "";
			String arr[] = message.trim().split(" ", 2);
			message = arr[1];
			return arr[0];		
		}
		
		public String getIdentifierFromMessage() {
			if (message.isEmpty()) return "";   // first "
			String arr[] = message.trim().split("\"", 2);
			message = arr[1]; 
			if (message.isEmpty()) return "";	// second "
			arr = message.split("\"", 2);
			message = arr[1]; 			        
			return arr[0];				
		}
		
		public String getRDSFromMessage() {
			if (message.length()<8) return "";
			String RDS = message.substring(0, 8);
			message = message.substring(8, message.length());
			return RDS;			
		}
		
	}

	public static void bootFinished() {
		splashImageView.setVisibility(View.INVISIBLE);
		splashLayout.setVisibility(View.INVISIBLE);
		controlLayout.setVisibility(View.VISIBLE);
		bootPending = false;

	}

	@Override
	protected void onDestroy() {
		service.stop();
		super.onDestroy();
	}
		
	public static void updateOnState(String OnOffString) {
		selectBtState.updateOnOffState(OnOffString);
		//mainButton.setBackgroundDrawable(background);
	}
	
	public static void switchOnOffState() {
		selectBtState.OnOff = !selectBtState.OnOff;
		writeOnOffState(selectBtState.OnOff);
		//mainButton.setBackgroundDrawable(background);	
	}

	public static void interpreter(String m) {

		MessageExtractor messageExtractor = new MessageExtractor(m);

		switch (answerPending) {
			case QUESTION_ALL:

				String password = messageExtractor.getStringFromMessage();						Log.e("Password",password);
				String identifier = messageExtractor.getIdentifierFromMessage();				Log.e("identifier",identifier);


				updateOnState(messageExtractor.getStringFromMessage());
				
				String standByMasterSettings = messageExtractor.getStringFromMessage();			Log.e("standByMasterSettings",standByMasterSettings);
				String standBySlaveSettings = messageExtractor.getStringFromMessage();			Log.e("standBySlaveSettings",standBySlaveSettings);
				
				String autoPowerMaster = messageExtractor.getStringFromMessage();				Log.e("autoPowerMaster",autoPowerMaster);
				String autoPowerSlave = messageExtractor.getStringFromMessage();				Log.e("autoPowerSlave",autoPowerSlave);
				String autoPowerVolume = messageExtractor.getStringFromMessage();				Log.e("autoPowerVolume",autoPowerVolume);
				String autoPowerFM = messageExtractor.getStringFromMessage();					Log.e("autoPowerFM",autoPowerFM);
				String autoPowerEQ = messageExtractor.getStringFromMessage();					Log.e("autoPowerEQ",autoPowerEQ);
				String channel = messageExtractor.getStringFromMessage();						Log.e("channel",channel);
				String stationFM = messageExtractor.getStringFromMessage();						Log.e("stationFM",stationFM);
				String infoRDS = messageExtractor.getRDSFromMessage();							Log.e("infoRDS",infoRDS);
				String tunerSensitivity = messageExtractor.getStringFromMessage();				Log.e("tunerSensitivity",tunerSensitivity);
				String equalizationMode = messageExtractor.getStringFromMessage();				Log.e("equalizationMode",equalizationMode);
				String volumeFM = messageExtractor.getStringFromMessage();						Log.e("volumeFM",volumeFM);
				String keepFmOn = messageExtractor.message;										Log.e("keepFmOn",keepFmOn);
	
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
        	                frameAnimation.stop();
        	        		splashImageView.setBackgroundResource(R.drawable.sec10);        	                
        	                
                    		new Handler().postDelayed(new Runnable() {
                    		    @Override
                    		    public void run() {
                    		    	askAll();               
                    		    }
                    		}, 300);
                    		break;

	                    case SelectBtService.STATE_DISCONNECTED:
	                    	closeWhenPossible = true;
	                    	break;
	                    case SelectBtService.STATE_CONNECTING:
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
					if (bootPending) bootFinished();
	                interpreter(readMessage);
	                break;
	            case MESSAGE_CONNECTING_FAILURE:
	            case MESSAGE_READING_FAILURE:
	            case MESSAGE_WRITING_FAILURE:
	                break;
	        }
	    }
	}

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	if (position == 1)
        		return new BtFragment("BT");
        	else
        		return new BtFragment("FM");       		
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
	
    
    private class SelectBtState {
    	boolean OnOff;
    	String channel;
    	
    	
    	public SelectBtState() {
    		OnOff = false;
    		channel = "BT"; 
    	}
    	
    	public void updateOnOffState(String onOffString) {
    		if (onOffString.equals("OFF"))
    			OnOff = true;
    		else
    			OnOff = false;
    	}
    }

}
