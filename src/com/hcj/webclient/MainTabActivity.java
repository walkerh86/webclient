package com.hcj.webclient;

import com.hcj.webclient.R;
import com.hcj.webclient.widget.CheckTabWidget;

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
	private static final int TAB_INDEX_MAIN = 0;
	private static final int TAB_INDEX_FORUM = 1;
	private static final int TAB_INDEX_SETTING = 2;
	private ArticleListFragment mArticleListFragment;
	private ForumListFragment mForumListFragment;
	private SettingFragment mSettingFragment;
	CheckTabWidget mCheckTabWidget;
		
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
		
		mCheckTabWidget = (CheckTabWidget)findViewById(R.id.bottom_tab);
		mCheckTabWidget.setOnTabChangeListener(new CheckTabWidget.OnTabChangeListener() {			
			@Override
			public void OnTabChange(int index) {
				setCurrentTab(index);
			}
		});
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
	
	private void setCurrentTab(int index) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();

		switch (index) {
			case TAB_INDEX_MAIN:
				fragmentTransaction.replace(R.id.fragment_container,
						mArticleListFragment);
				fragmentTransaction.commit();
				break;				
				
			case TAB_INDEX_FORUM:	
				if (mForumListFragment == null) {
					mForumListFragment = new ForumListFragment();
				}
				fragmentTransaction.replace(R.id.fragment_container,
						mForumListFragment);
				fragmentTransaction.commit();
				break;
			
			case TAB_INDEX_SETTING:
				if (mSettingFragment == null) {
					mSettingFragment = new SettingFragment();
				}
				fragmentTransaction.replace(R.id.fragment_container,
						mSettingFragment);
				fragmentTransaction.commit();
				break;
				
						default:
				break;
		}
	};
}
