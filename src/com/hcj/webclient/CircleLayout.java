package com.hcj.webclient;

import android.view.ViewGroup;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

public class CircleLayout extends ViewGroup implements Animation.AnimationListener{
	private static final String TAG = "CircleLayout";
	private int mChildRadius = 58/2;
	private int mRadius;
	private int mTotalDegree = 180;
	private int mGapDegree;
	private int mOffsetDegree=0; //for rotate
	private View.OnClickListener mListener;
	private boolean bExpanded;
	//private boolean bAnimating=false;
	//private TranslateAnimation[] mZoomInAnims;
	//private TranslateAnimation[] mZoomOutAnims;
	private RotateAnimation mRotateAnim;
	private AnimationSet[] mExpandAnims;
	private AnimationSet[] mCollapseAnims;
	private int mCenterX;
	private int mCenterY;
	//private int[][] mChildCenterXYs;
	
	
	public CircleLayout(Context context){
		super(context);
		init(context);
	}
	
	public CircleLayout(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public CircleLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		bExpanded = false;
	}
	
	@Override
	protected void onAttachedToWindow() {
		Log.i(TAG,"onAttachedToWindow");
		if(!bExpanded){	
			int childCount = getChildCount();
			for(int i=0;i<childCount-1;i++){
				getChildAt(i).setVisibility(View.GONE);
			}
		}
	}
	
	@Override
	protected void onFinishInflate() {
		Log.i(TAG,"onFinishInflate");
    }
		
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
		Log.i(TAG,"onMeasure,widthSpecSize="+widthSpecSize+",heightSpecSize="+heightSpecSize);
		
		int minSize = Math.min(widthSpecSize, heightSpecSize);
		mRadius = minSize/2 - mChildRadius;
		
		int childCount = getChildCount() - 1;
		if(childCount > 0){
			int gapCount = (mTotalDegree == 360) ? childCount : (childCount-1);
			mGapDegree = mTotalDegree/gapCount;
		}
		//Log.i(TAG,"onMeasure,mRadius="+mRadius+",mItemDegree="+mGapDegree);
		
		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}
	
	@Override
	public void onLayout(boolean changed,int l, int t, int r, int b){   
		Log.i(TAG,"onLayout");
		int childCount = getChildCount();	
		mCenterX = (r-l)/2;
		mCenterY = (b-t)/2;
		int[] childCenterXY = new int[2];
		int childX;
		int childY;
		int radius = mRadius;
		//layout center view
		View child = getChildAt(childCount-1);
		childX = mCenterX-child.getWidth()/2;
		childY = mCenterY-child.getHeight()/2;	
		child.layout(childX, childY, childX+mChildRadius*2, childY+mChildRadius*2);
		//layout circle view
		for(int i=0;i<childCount-1;i++){			
			getCenterX(radius,getchildDegree(i),childCenterXY);
			childX = childCenterXY[0]-mChildRadius;
			childY = childCenterXY[1]-mChildRadius;			
			child = getChildAt(i);
			child.layout(mCenterX+childX, mCenterY+childY, mCenterX+childX+mChildRadius*2, mCenterY+childY+mChildRadius*2);			
		}
	}
	
	private int getchildDegree(int index){
		int offsetDegree = mOffsetDegree;
		int itemDegree = offsetDegree + mGapDegree*index;
		//Log.i(TAG,"getDegree, itemDegree="+itemDegree+",offsetDegree="+offsetDegree);
		return itemDegree;
	}
	
	public void setTotalDegree(int d){
		mTotalDegree = d;
	}
	
	public void setChildOnClickListener(View.OnClickListener listener){
		mListener = listener;
		int childCount = getChildCount();
		for(int i=0;i<childCount;i++){
			View childView = getChildAt(i);
			childView.setOnClickListener(mListener);
		}
	}
	
	public void toggleAnim(){
		//Log.i(TAG,"toggleAnim,bExpanded="+bExpanded);
		if(bExpanded){
			collapseAnim();	
			bExpanded = false;
		}else{
			expandAnim();
			bExpanded = true;
		}
	}
		
	private void expandAnim(){
		initAnim();
		
		int childCount = getChildCount();
		for(int i=0;i<childCount-1;i++){
			if(mExpandAnims[i] != null){
				getChildAt(i).startAnimation(mExpandAnims[i]);
			}
		}
		getChildAt(childCount-1).startAnimation(mRotateAnim);
	}
	
	private void collapseAnim(){
		initAnim();
		
		int childCount = getChildCount();
		for(int i=0;i<childCount-1;i++){
			if(mCollapseAnims[i] != null){
				getChildAt(i).startAnimation(mCollapseAnims[i]);
			}
		}
		getChildAt(childCount-1).startAnimation(mRotateAnim);
	}
	
	private void initAnim(){
		int childCount = getChildCount();		
		int[] childCenterXY = new int[2];
		if(mExpandAnims == null){
			mExpandAnims = new AnimationSet[childCount-1];
			TranslateAnimation translateAnim;
			RotateAnimation rotateAnim;
			AlphaAnimation alphaAnim;
			for(int i=0;i<childCount-1;i++){
				getCenterX(mRadius,getchildDegree(i),childCenterXY);
				mExpandAnims[i] = new AnimationSet(true);
				//mExpandAnims[i].setFillAfter(true);
				mExpandAnims[i].setDuration(200);
				translateAnim = new TranslateAnimation(-childCenterXY[0],0,-childCenterXY[1],0);
				mExpandAnims[i].addAnimation(translateAnim);
				rotateAnim = new RotateAnimation(180,0,-childCenterXY[0]+mChildRadius,-childCenterXY[1]+mChildRadius);
				mExpandAnims[i].addAnimation(rotateAnim);
				alphaAnim = new AlphaAnimation(0.0f,1.0f);
				mExpandAnims[i].addAnimation(alphaAnim);
			}
			mExpandAnims[0].setAnimationListener(this);
			Log.i(TAG,"expandanim0="+mExpandAnims[0]);
		}
		
		if(mCollapseAnims == null){
			mCollapseAnims = new AnimationSet[childCount-1];
			TranslateAnimation translateAnim;
			RotateAnimation rotateAnim;
			AlphaAnimation alphaAnim;
			for(int i=0;i<childCount-1;i++){
				getCenterX(mRadius,getchildDegree(i),childCenterXY);
				mCollapseAnims[i] = new AnimationSet(true);
				//mCollapseAnims[i].setFillAfter(true);
				mCollapseAnims[i].setDuration(500);
				translateAnim = new TranslateAnimation(0,-childCenterXY[0],0,-childCenterXY[1]);
				mCollapseAnims[i].addAnimation(translateAnim);
				rotateAnim = new RotateAnimation(0,180,-childCenterXY[0]+mChildRadius,-childCenterXY[1]+mChildRadius);
				mCollapseAnims[i].addAnimation(rotateAnim);
				alphaAnim = new AlphaAnimation(1.0f,0.0f);
				mCollapseAnims[i].addAnimation(alphaAnim);
			}
			mCollapseAnims[0].setAnimationListener(this);
			Log.i(TAG,"collapseanim0="+mCollapseAnims[0]);
		}
		/*
		if(mZoomInAnims == null){
			mZoomInAnims = new TranslateAnimation[childCount];
			for(int i=0;i<childCount-1;i++){
				getCenterX(mRadius,getDegree(i),childCenterXY);
				mZoomInAnims[i] = new TranslateAnimation(0,-childCenterXY[0],0,-childCenterXY[1]);
				mZoomInAnims[i].setDuration(500);
				mZoomInAnims[i].setFillAfter(true);
			}
		}
		if(mZoomOutAnims == null){
			mZoomOutAnims = new TranslateAnimation[childCount];
			for(int i=0;i<childCount-1;i++){
				getCenterX(mRadius,getDegree(i),childCenterXY);
				mZoomOutAnims[i] = new TranslateAnimation(-childCenterXY[0],0,-childCenterXY[1],0);
				mZoomOutAnims[i].setDuration(500);
				mZoomOutAnims[i].setFillAfter(true);
			}
		}
		*/
		if(mRotateAnim == null){
			mRotateAnim = new RotateAnimation(0,360,mChildRadius,mChildRadius);
			mRotateAnim.setDuration(500);
		}
	}
	
	private void computeChildCenterXYs(){
		
	}
	
	private static void getCenterX(int radius, int degree, int[] centerXY){
		double d;		
		if(degree < 90){
			d = Math.toRadians(degree);
			centerXY[0] = (int)(radius*Math.sin(d));
			centerXY[1] = 0-(int)(radius*Math.cos(d));
		}else if(degree < 180){
			d = Math.toRadians(180-degree);
			centerXY[0] = (int)(radius*Math.sin(d));
			centerXY[1] = (int)(radius*Math.cos(d));
		}else if(degree <= 270){
			d = Math.toRadians(degree-180);
			centerXY[0] = 0 - (int)(radius*Math.sin(d));
			centerXY[1] = (int)(radius*Math.cos(d));
		}else if(degree < 360){
			d = Math.toRadians(360-degree);
			centerXY[0] = 0 - (int)(radius*Math.sin(d));
			centerXY[1] = 0 - (int)(radius*Math.cos(d));
		}
		
		//Log.i(TAG,"getCenterX, degree="+degree+",x="+centerXY[0]+",y="+centerXY[1]);
	}
	
	public void onAnimationStart(Animation animation){
		
		if(animation == mExpandAnims[0]){
			int childCount = getChildCount();
			for(int i=0;i<childCount-1;i++){
				getChildAt(i).setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void onAnimationEnd(Animation animation){		
		if(animation == mCollapseAnims[0]){
			int childCount = getChildCount();
			for(int i=0;i<childCount-1;i++){
				getChildAt(i).setVisibility(View.GONE);
			}
		}
	}
	
	public void onAnimationRepeat(Animation animation){
		
	}
}
