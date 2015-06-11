package es.carlosrolindez.kbfinder;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



public class FmFragment extends Fragment {
	public String fragmentName;
	private final Context mContext;
	
	private static TextView frequencyText;
	
	
	
	public FmFragment(Context context) {
		mContext = context;
		fragmentName =  "FM";
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fm_fragment, container, false);
      	((TextView) rootView.findViewById(R.id.fragment_text)).setText(fragmentName);
        Typeface myTypeface = Typeface.createFromAsset(mContext.getAssets(), "seven_segments.ttf");
        frequencyText = (TextView)rootView.findViewById(R.id.frequency);
        frequencyText.setTypeface(myTypeface);
        
        return rootView;	        
    }
    
    public void setFrequency(String frequency) {
        frequencyText.setText(frequency);    	
    }
    

}
