package es.carlosrolindez.kbfinder;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class SwipeListViewTouchListener implements View.OnTouchListener {

	private static String TAG = "SwipeListViewTouchListener";
  
	private OnClickCallBack mCallBack;
	
	private int mSlop;
    private long mAnimationTime;
    private int mViewWidth = 1; 

    // Transient properties
    private float mDownX;
    private boolean mSwiping;
 
    public SwipeListViewTouchListener(View view, OnClickCallBack onClickCallBack) {
        ViewConfiguration vc = ViewConfiguration.get(view.getContext());
        mSlop = vc.getScaledTouchSlop();
        mAnimationTime = view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        mCallBack = onClickCallBack;
    }

    public interface OnClickCallBack {
        public void onClickSelectBT(View view);
    }
   
	@Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mViewWidth = view.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            {
                mDownX = motionEvent.getRawX();
                mSwiping = false;
                view.setPressed(true);
            }
            return true;

       case MotionEvent.ACTION_MOVE:
            {
                float deltaX = motionEvent.getRawX() - mDownX;
                if (Math.abs(deltaX) > mSlop) {
                    mSwiping = true;
                    view.setPressed(false);
                    view.setTranslationX(deltaX);
                }

                return true;
            }
 
       case MotionEvent.ACTION_UP:
       {
        view.setPressed(false);
       	if (mSwiping) {
               // cancel
               view.animate()
                   .translationX(0)
                   .alpha(1)
                   .setDuration(mAnimationTime)
                   .setListener(null);
               return true;
         } else mCallBack.onClickSelectBT(view);
       		
         return false;
       }

       case MotionEvent.ACTION_CANCEL:
        {
            view.setPressed(false);
           	if (mSwiping) {
                   // cancel
                   view.animate()
                       .translationX(0)
                       .alpha(1)
                       .setDuration(mAnimationTime)
                       .setListener(null);
               }
               return true;
          }

        }
        return false;
    }

}
