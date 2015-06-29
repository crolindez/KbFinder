package es.carlosrolindez.kbfinder;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;




public class FmFragment extends Fragment {
    private static final String TAG = "FmFragment";
	
	public String fragmentName;
	private final Context mContext;
	
	private static TextView frequencyText;
	private static TextView RDSText;
	
	private SppBridge spp;
	
	public static interface SppBridge {
		public void sppMessage(String message,boolean delayed);
	}
	
	
	public FmFragment(Context context) {
		mContext = context;
		fragmentName =  "FM";
		spp = (SppBridge) context;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fm_fragment, container, false);
      	((TextView) rootView.findViewById(R.id.fragment_text)).setText(fragmentName);
        Typeface myTypeface = Typeface.createFromAsset(mContext.getAssets(), "seven_segments.ttf");
        frequencyText = (TextView)rootView.findViewById(R.id.frequency);
        frequencyText.setTypeface(myTypeface);
        RDSText = (TextView)rootView.findViewById(R.id.RDS);

		ImageView button_scan_down_FM = (ImageView)rootView.findViewById(R.id.scan_down_FM);
		ImageView button_dial_FM = (ImageView)rootView.findViewById(R.id.dial_FM);
		ImageView button_scan_up_FM = (ImageView)rootView.findViewById(R.id.scan_up_FM);
		
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
				spp.sppMessage("SCN DOWN\r",false);
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
				spp.sppMessage("SCN UP\r",false);
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
    
    public void showFmDialog() {
    	final Dialog fmDialog = new Dialog(mContext, R.style.CustomDialog);
    	fmDialog.setTitle(getResources().getString(R.string.fm_dial));
    	fmDialog.setContentView(R.layout.fm_dial);
        final FmPicker megaherzPicker = (FmPicker) fmDialog.findViewById(R.id.megaherzs);
        final FmPicker kiloherzPicker = (FmPicker) fmDialog.findViewById(R.id.kiloherzs);
 
        String freq[] = frequencyText.getText().toString().trim().split(".", 2);
        Log.e(TAG,frequencyText.getText().toString().trim());
        
        megaherzPicker.setMaxValue(108);
        megaherzPicker.setMinValue(87);
        megaherzPicker.setWrapSelectorWheel(true); 
        Log.e(TAG,"x"+freq[0]);
        Log.e(TAG,"y"+freq[1]);
        megaherzPicker.setValue(Integer.parseInt(freq[0]));
        megaherzPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {

                Toast.makeText(mContext, "Value was: " + Integer.toString(i) + " is now: " + Integer.toString(i2), Toast.LENGTH_SHORT).show();

            }
        });
        kiloherzPicker.setMaxValue(9);
        kiloherzPicker.setMinValue(0);
        kiloherzPicker.setWrapSelectorWheel(true);
        megaherzPicker.setValue(Integer.parseInt(freq[1]));
        megaherzPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {

                Toast.makeText(mContext, "Value was: " + Integer.toString(i) + " is now: " + Integer.toString(i2), Toast.LENGTH_SHORT).show();

            }
        });

        fmDialog.show();

    	
    }

}
