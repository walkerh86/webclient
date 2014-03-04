package com.hcj.webclient.widget;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hcj.webclient.R;

public class TitleWidget extends FrameLayout{
	private static final String TAG = "TitleWidget";
	private Context mContext;
	private TextView mTitleText;
	private ImageView mTitleLeft;
	private ImageView mTitleRight;

	public TitleWidget(Context context) {
		super(context);
		
		init(context);
	}
	
	public TitleWidget(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public TitleWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}
	
	private void init(Context context){
		mContext = context;
		
		LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.title_widget, this, true);
        mTitleText = (TextView)findViewById(R.id.title_text);
        mTitleLeft = (ImageView)findViewById(R.id.title_left);
        mTitleRight = (ImageView)findViewById(R.id.title_right);
	}
	
	public void setTitle(String title){
		mTitleText.setText(title);
	}
	
	public void setTitle(CharSequence title){
		mTitleText.setText(title);
	}
	
	public void setTitle(int resId){
		mTitleText.setText(resId);
	}
	
	public void setLeft(int resId, View.OnClickListener listener){
		mTitleLeft.setImageResource(resId);
		mTitleLeft.setOnClickListener(listener);
	}
	
	public void setRight(int resId, View.OnClickListener listener){
		mTitleRight.setImageResource(resId);
		mTitleRight.setOnClickListener(listener);
	}
	
	public void enableLeftBack(int resId){
		mTitleLeft.setImageResource(resId);
		mTitleLeft.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				((Activity)mContext).finish();
			}
		});
	}
}
