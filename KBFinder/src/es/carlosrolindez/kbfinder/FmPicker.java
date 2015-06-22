package es.carlosrolindez.kbfinder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class FmPicker extends android.widget.NumberPicker {
	private final Context mContext;
	public FmPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@Override
	public void addView(View child) {
		super.addView(child);
		updateView(child);
	}

	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		updateView(child);
	}

	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, params);
		updateView(child);
	}

	private void updateView(View view) {
		if(view instanceof EditText){

			((EditText) view).setTextSize(35);
			((EditText) view).setTextColor(Color.parseColor("#333333"));

		}
	}

}
