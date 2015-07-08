package es.carlosrolindez.kbfinder;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;




public class FmFragment extends Fragment {
    private static final String TAG = "FmFragment";
	
	private String fragmentName;
	private final Context mContext;
	
	private static TextView frequencyText;
	private static TextView RDSText;
	private static ImageView mono;
	private static boolean mStereo;
	private static ImageView favorite;
	
	public static SettingsClass.KBdeviceSettings mDevice;
	
	private SppBridge spp;
	
	public static interface SppBridge {
		public void sppMessage(String message);
	}
	
	
	public FmFragment(Context context,boolean stereo,SettingsClass.KBdeviceSettings device) {
		mContext = context;
		fragmentName =  "FM";
		spp = (SppBridge) context;
		mStereo = stereo;
		mDevice = device;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fm_fragment, container, false);
      	((TextView) rootView.findViewById(R.id.fragment_text)).setText(fragmentName);
        Typeface myTypeface = Typeface.createFromAsset(mContext.getAssets(), "seven_segments.ttf");
        frequencyText = (TextView)rootView.findViewById(R.id.frequency);
        frequencyText.setTypeface(myTypeface);
        RDSText = (TextView)rootView.findViewById(R.id.RDS);
        
		mono = (ImageView)rootView.findViewById(R.id.mono_FM);
		favorite = (ImageView)rootView.findViewById(R.id.favorite_FM);
		
		setStereo(mStereo);
		
		if (mDevice.isFreqInArray(frequencyText.getText().toString()))
			favorite.setBackgroundResource(R.drawable.favorite);
		else
			favorite.setBackgroundResource(R.drawable.nofavorite);			
		

		ImageView button_scan_down_FM = (ImageView)rootView.findViewById(R.id.scan_down_FM);
		ImageView button_dial_FM = (ImageView)rootView.findViewById(R.id.dial_FM);
		ImageView button_scan_up_FM = (ImageView)rootView.findViewById(R.id.scan_up_FM);
		
		favorite.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				if (mDevice.isFreqInArray(frequencyText.getText().toString())) {
					mDevice.removeFreqFromArray(frequencyText.getText().toString());
					favorite.setBackgroundResource(R.drawable.nofavorite);
				} else {
					mDevice.addFreqFromArray(frequencyText.getText().toString(),RDSText.getText().toString());
					favorite.setBackgroundResource(R.drawable.favorite);		
				}
			}
		});
		
		frequencyText.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				showFmDialog();
			}
		});
		
		button_scan_down_FM.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				spp.sppMessage("SCN DOWN\r");
				setFrequency("___._");
				setRDS("");				
			}
		});

		button_dial_FM.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				showFmDialog();
			}
		});

		button_scan_up_FM.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				spp.sppMessage("SCN UP\r");
				setFrequency("___._");
				setRDS("");
			}
		});
           
        
        
        return rootView;	        
    }
    
    public void setFrequency(String frequency) {
        frequencyText.setText(frequency);    	
    }
    
    public void setRDS(String RDS) {
    	RDSText.setText(RDS);    	
    }  
    
    public void setStereo(boolean stereo) {   
    	mStereo = stereo;
		if (mStereo) 
			mono.setBackgroundResource(R.drawable.stereo);
		else
			mono.setBackgroundResource(R.drawable.mono);
    }
    
	public void showFmDialog() {
    	final int minFreq = 87;
    	final int maxFreq = 108;
    	final String dialMegaherz[] = new String[maxFreq-minFreq+1];

    	for (int frInt=maxFreq, index=0; frInt>=minFreq; frInt--,index++){
    		dialMegaherz[index] = Integer.toString(frInt);
    	}
 
    	final String dialKiloherz[] = new String[10];
    	for (int frInt=9, index=0; frInt>=0; frInt--,index++){
    		dialKiloherz[index] = Integer.toString(frInt);
    	}

        String freq[] = frequencyText.getText().toString().trim().split("\\.");
        final String newFreq[] = {freq[0], freq[1]};
        
    	final CountDownTimer countDownFm = new CountDownTimer(800,800){
    		public void onTick(long millisUntilFinished) {
		    }
		     	
		    public void onFinish() {
		    	spp.sppMessage("TUN "+newFreq[0]+"."+newFreq[1]+"\r");
				if (newFreq[0].length()<3)
					setFrequency(" "+newFreq[0]+"."+newFreq[1]);
				else
					setFrequency(newFreq[0]+"."+newFreq[1]);
				setRDS("");
		    }
    	};
    	  	
    	
    	final Dialog fmDialog = new Dialog(mContext, R.style.CustomDialog);
    	fmDialog.setTitle(getResources().getString(R.string.fm_dial));
    	fmDialog.setContentView(R.layout.fm_dial);

 /*   	Window window = fmDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.RIGHT;
//        wlp.width = LayoutParams.MATCH_PARENT;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);   	
    	*/
    	
    	
        final FmPicker megaherzPicker = (FmPicker) fmDialog.findViewById(R.id.megaherzs);
        final FmPicker kiloherzPicker = (FmPicker) fmDialog.findViewById(R.id.kiloherzs);
        
        megaherzPicker.setMaxValue(maxFreq-minFreq);
        megaherzPicker.setMinValue(0);
        megaherzPicker.setWrapSelectorWheel(false); 
        megaherzPicker.setDisplayedValues(dialMegaherz);
        megaherzPicker.setValue(maxFreq-Integer.parseInt(freq[0]));
        megaherzPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
            	newFreq[0] = Integer.toString(maxFreq-i2);
            	if (newFreq[0].equals("108")) {
            		newFreq[1] = "0";
            	} else if ( (newFreq[0].equals("87")) && (Integer.parseInt(newFreq[1])<5) ) {
            		newFreq[1] = "5";
            	} else newFreq[1] = Integer.toString(9-kiloherzPicker.getValue());      		
            	countDownFm.cancel();
            	countDownFm.start();
            }
        });
        kiloherzPicker.setMaxValue(9);
        kiloherzPicker.setMinValue(0);
        kiloherzPicker.setWrapSelectorWheel(true);
        kiloherzPicker.setDisplayedValues(dialKiloherz);
        kiloherzPicker.setValue(9-Integer.parseInt(freq[1]));
        kiloherzPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
            	newFreq[1] = Integer.toString(9-i2);
            	if (newFreq[0].equals("108")) {
            		newFreq[1] = "0";
            	} else if ( (newFreq[0].equals("87")) && (Integer.parseInt(newFreq[1])<5) ) {
            		newFreq[1] = "5";
            	}            		
            	
            	countDownFm.cancel();
            	countDownFm.start();
            }
        });

        fmDialog.show();

    	
    }

}
