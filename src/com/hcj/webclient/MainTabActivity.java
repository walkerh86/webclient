package com.hcj.webclient;

import com.hcj.circlelayout.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabWidget;
import android.widget.TextView;

public class MainTabActivity extends FragmentActivity{
	private static final String TAG = "MainTabActivity";
	private TextView mTab1;
	private TextView mTab2;
	ArticleListFragment mArticleListFragment;
	SettingFragment mSettingFragment;
	private View.OnClickListener mOnClickListner = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			int view_id = v.getId();
			Log.i(TAG,"onClick id="+view_id);
			FragmentManager fragmentManager = getSupportFragmentManager();  
			FragmentTransaction fragmentTransaction =fragmentManager.beginTransaction(); 
			
			switch(view_id){
				case R.id.tab1:
					fragmentTransaction.replace(R.id.fragment_container,mArticleListFragment);  
					fragmentTransaction.commit();  
					break;
				case R.id.tab2:
					if(mSettingFragment == null){
						mSettingFragment = new SettingFragment();
					}
					fragmentTransaction.replace(R.id.fragment_container,mSettingFragment);  
					fragmentTransaction.commit();  
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ViewGroup root_view = (ViewGroup)((ViewGroup)(getWindow().getDecorView())).getChildAt(0);
		//Log.i(TAG,"root_view="+root_view);
		getLayoutInflater().inflate(R.layout.bottom_tab, root_view);
		
		setContentView(R.layout.main_tab_main);
		
		mArticleListFragment = new ArticleListFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();  
		FragmentTransaction fragmentTransaction =fragmentManager.beginTransaction();  
		fragmentTransaction.add(R.id.fragment_container,mArticleListFragment);  
		fragmentTransaction.commit();  
		
		mTab1 = (TextView)findViewById(R.id.tab1);
		mTab2 = (TextView)findViewById(R.id.tab2);
		mTab1.setOnClickListener(mOnClickListner);
		mTab2.setOnClickListener(mOnClickListner);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		//Log.i(TAG,"activity onCreateOptionsMenu");
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onPrepareOptionsMenu(Menu menu){
		//Log.i(TAG,"activity onPrepareOptionsMenu");
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		return super.onOptionsItemSelected(item);
	}
}
