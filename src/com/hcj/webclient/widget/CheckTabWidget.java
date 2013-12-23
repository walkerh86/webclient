package com.hcj.webclient.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckTabWidget extends LinearLayout{
	private static final String TAG = "CheckTabWidget";
	private static final int DEFAULT_TAB_INDEX = 0;
	private int mCurrTabIndex;
	private OnTabChangeListener mOnTabChangeListener;
	private Checkable mLastCheckable;
	
	public CheckTabWidget(Context context){
		super(context);
	}
	
	public CheckTabWidget(Context context, AttributeSet attrs){
		this(context,attrs,0);
	}
	
	public CheckTabWidget(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs);
	}
	
	@Override
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
		
		initTabWidget();
	}
	
	private void initTabWidget(){
		mCurrTabIndex = DEFAULT_TAB_INDEX;
		
		int count = getChildCount();
		View child;
		for(int i=0;i<count;i++){
			child = getChildAt(i);
			if(mCurrTabIndex == i){
				mLastCheckable = (Checkable)child;
				mLastCheckable.setChecked(true);
			}
			child.setOnClickListener(new TabOnClickListener(i));
		}
	}
	
	private class TabOnClickListener implements View.OnClickListener{
		private int mTabIndex;
		
		public TabOnClickListener(int index){
			mTabIndex = index;
		}
		
		public void onClick(View v){
			setCurrentTab(mTabIndex);
		}
	}
	
	public void setOnTabChangeListener(OnTabChangeListener listener){
		mOnTabChangeListener = listener;
	}
	
	public void setCurrentTab(int index){
		if(mCurrTabIndex == index || index >= getChildCount()){
			return;
		}
		
		mCurrTabIndex = index;
		
		if(mLastCheckable != null){
			mLastCheckable.setChecked(false);
		}
		
		mLastCheckable = (Checkable)getChildAt(index);
		mLastCheckable.setChecked(true);
		if(mOnTabChangeListener != null){
			mOnTabChangeListener.OnTabChange(mCurrTabIndex);
		}
	}
	
	public interface OnTabChangeListener{
		public void OnTabChange(int index);
	}
}
