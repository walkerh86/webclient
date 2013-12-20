package com.hcj.webclient;

import com.hcj.circlelayout.R;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PullToRefreshView extends LinearLayout{
	private static final String TAG = "PullToRefreshView";

	private ListView mListView;
	private View mHeaderView;
	private boolean mIsBeingDragged;
	private int mTouchSlop;
	private float mLastMotionX, mLastMotionY;
	private float mInitialMotionX, mInitialMotionY;
	private int mHeaderHeight;
	private int mRefreshState;
	private static final int REFRESH_STATE_PULL_REFRESH = 0;
	private static final int REFRESH_STATE_RELEASE_REFRESH = 1;
	private static final int REFRESH_STATE_REFRESHING = 2;
	
	private Handler mHandler = new Handler();
	
	public PullToRefreshView(Context context) {
		super(context);
	}
	
	public PullToRefreshView(Context context, AttributeSet attrs) {
		super(context,attrs);
		
		mIsBeingDragged = false;
		ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.i(TAG,"onMeasure");
		
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
        
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        
        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }
        
        int headerHeight = mHeaderHeight;
        if(mHeaderView == null){
        	mHeaderView = this.findViewById(R.id.header_view);        	
        }
        measureChildWithMargins(mHeaderView,widthMeasureSpec,0,heightMeasureSpec,0);
        mHeaderHeight = mHeaderView.getMeasuredHeight();
    	Log.i(TAG,"onMeasure, mHeaderHeight="+mHeaderHeight);
    	
        if(mListView == null){
        	mListView = (ListView)this.findViewById(R.id.list_view);   	
        }
        measureChildWithMargins(mListView,widthMeasureSpec,0,heightMeasureSpec,0);
        
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        
        if(mHeaderHeight != headerHeight){
        	scrollTo(0,mHeaderHeight);
        }
	}
		
	public void setViews(View header_view, ListView listview){
		mHeaderView = header_view;
		mListView = listview;
	}
	
	@Override
	public final boolean onInterceptTouchEvent(MotionEvent event){
		final int action = event.getAction();
		
		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			mIsBeingDragged = false;
			return false;
		}
		
		if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
			return true;
		}
		
		switch (action) {
			case MotionEvent.ACTION_MOVE: {
				if (isReadyForPull()) {
					final float y = event.getY(), x = event.getX();
					final float diff, oppositeDiff, absDiff;

					// We need to use the correct values, based on scroll
					// direction
					diff = y - mLastMotionY;
					oppositeDiff = x - mLastMotionX;				
					absDiff = Math.abs(diff);

					if (absDiff > mTouchSlop/* && (absDiff > Math.abs(oppositeDiff))*/) {
						if (diff >= 1f) {
							mLastMotionY = y;
							mLastMotionX = x;
							mIsBeingDragged = true;
						} 
					}
				}
				break;
			}
			case MotionEvent.ACTION_DOWN: {
				if (isReadyForPull()) {
					mLastMotionY = mInitialMotionY = event.getY();
					mLastMotionX = mInitialMotionX = event.getX();
					mIsBeingDragged = false;
				}
				break;
			}
		}
		
		return mIsBeingDragged;
	}
	
	@Override
	public final boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
			return false;
		}
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE: {
				if (mIsBeingDragged) {
					int deltaY = (int)(event.getY() - mLastMotionY);
					deltaY = (int)(deltaY*0.5f);
					
					mLastMotionY = event.getY();
					mLastMotionX = event.getX();
					
					int currScrollY = this.getScrollY();
					if(currScrollY-deltaY > mHeaderHeight){
						if(currScrollY != mHeaderHeight){
							scrollTo(0,mHeaderHeight);
						}
					}else{
						scrollBy(0,-deltaY);
					}
					
					currScrollY = this.getScrollY();
					if(currScrollY < 0){
						setRefreshState(REFRESH_STATE_RELEASE_REFRESH);
					}else if(currScrollY < mHeaderHeight){
						setRefreshState(REFRESH_STATE_PULL_REFRESH);
					}
					
					return true;
				}
				break;
			}
	
			case MotionEvent.ACTION_DOWN: {
				if (isReadyForPull()) {
					mLastMotionY = mInitialMotionY = event.getY();
					mLastMotionX = mInitialMotionX = event.getX();
					return true;
				}
				break;
			}
	
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				if (mIsBeingDragged) {
					mIsBeingDragged = false;
	
					if(mRefreshState == REFRESH_STATE_RELEASE_REFRESH){
						scrollTo(0,0);
						setRefreshState(REFRESH_STATE_REFRESHING);
						mHandler.postDelayed(new Runnable(){
							public void run(){
								scrollTo(0,mHeaderHeight);
							}
						}, 2000);
					}else{
						scrollTo(0,mHeaderHeight);
					}
	
					return true;
				}
				break;
			}
		}

		return false;
	}
	
	private boolean isReadyForPull(){
		if (mListView.getFirstVisiblePosition() <= 0) {
			final View firstVisibleChild = mListView.getChildAt(0);
			if (firstVisibleChild != null) {
				Log.i(TAG,"isReadyForPull firstchild top="+firstVisibleChild.getTop());
				Log.i(TAG,"isReadyForPull listview top="+mListView.getTop());
				return firstVisibleChild.getTop() >= 0;
			} 
		}
		return false;
	}
	
	private void setRefreshState(int state){
		if(mRefreshState != state){
			mRefreshState = state;
			TextView textV = (TextView)mHeaderView.findViewById(R.id.header_text);
			ImageView imgV = (ImageView)mHeaderView.findViewById(R.id.header_img);
			if(mRefreshState == REFRESH_STATE_PULL_REFRESH){
				textV.setText(R.string.pull_down_to_refresh);
				imgV.setImageResource(R.drawable.refresh_list_pull_down);
			}else if(mRefreshState == REFRESH_STATE_RELEASE_REFRESH){
				textV.setText(R.string.release_to_refresh);
				imgV.setImageResource(R.drawable.refresh_list_release_up);
			}else if(mRefreshState == REFRESH_STATE_REFRESHING){
				textV.setText(R.string.refreshing);
			}
		}
	}
}
