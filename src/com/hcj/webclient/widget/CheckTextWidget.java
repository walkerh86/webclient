package com.hcj.webclient.widget;

import com.hcj.webclient.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Checkable;
import android.widget.TextView;

public class CheckTextWidget extends TextView implements Checkable{
	private static final String TAG = "CheckTextWidget";
	private Drawable[] mCompoundDrawables;
	private boolean mChecked;
	private static final int[] CHECKED_STATE_SET = {
        R.attr.state_checked
    };
	
	public CheckTextWidget(Context context){
		super(context);
	}
	
	public CheckTextWidget(Context context, AttributeSet attrs){
		this(context,attrs,0);
	}
	
	public CheckTextWidget(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
		
		mCompoundDrawables = getCompoundDrawables();
	}
		
	@Override
	public void toggle(){
		setChecked(!mChecked);
	}
	
	@Override
	public boolean isChecked() {
        return mChecked;
    }
    
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();        
        }
    }
    
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
    	//Log.i(TAG,"onCreateDrawableState extraSpace="+extraSpace);
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
        	//Log.i(TAG,"onCreateDrawableState checked");
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        int[] myDrawableState = getDrawableState();
        //Log.i(TAG,"drawableStateChanged"+"state num="+mCompoundDrawables.length);
        for(int i=0;i<mCompoundDrawables.length;i++){
	        if (mCompoundDrawables[i] != null) {
	        	//Log.i(TAG,"drawableStateChanged drawable");
	            mCompoundDrawables[i].setState(myDrawableState);
	        }
        }
        
        invalidate();
    }
}
