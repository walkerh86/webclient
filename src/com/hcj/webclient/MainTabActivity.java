package com.hcj.webclient;

import com.hcj.webclient.R;
import com.hcj.webclient.widget.CheckTabWidget;
import com.hcj.webclient.widget.TitleWidget;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class MainTabActivity extends FragmentActivity implements CategoryListFragment.OnCategorySelectedListener{
	private static final String TAG = "MainTabActivity";
	private static final int TAB_INDEX_MAIN = 0;
	private static final int TAB_INDEX_FORUM = 1;
	private static final int TAB_INDEX_SETTING = 2;
	private int mCurrentTabIndex;
	private ArticleListFragment mArticleListFragment;
	private CategoryListFragment mForumListFragment;
	private SettingFragment mSettingFragment;
	private CheckTabWidget mCheckTabWidget;
	private TitleWidget mTitleWidget;
	private String mCategoryUrl;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_tab_main);
		
		mCurrentTabIndex = TAB_INDEX_MAIN;
		mCategoryUrl = ConfigUtils.MAIN_URL;
		
		mCheckTabWidget = (CheckTabWidget)findViewById(R.id.bottom_tab);
		mCheckTabWidget.setOnTabChangeListener(new CheckTabWidget.OnTabChangeListener() {			
			@Override
			public void OnTabChange(int index) {
				if(mCurrentTabIndex == index){
					return;
				}
				mCurrentTabIndex = index;
				
				setCurrentTab(index);
			}
		});
		setCurrentTab(mCurrentTabIndex);
		
		mTitleWidget = (TitleWidget)findViewById(R.id.title_widget);
		mTitleWidget.setLeft(R.drawable.title_more, new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				//getActivity().finish();	
			}
		});
	}
	
	@Override
	public void OnCategorySelected(String url){		
		mCategoryUrl = url;
		mCheckTabWidget.setCurrentTab(TAB_INDEX_MAIN);
	}
		
	private void replaceFragment(Fragment fragment){
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();				
		fragmentTransaction.replace(R.id.fragment_container,fragment);				
		fragmentTransaction.commit();
	}
	
	private void setCurrentTab(int index) {
		switch (index) {
			case TAB_INDEX_MAIN:
				if(mArticleListFragment == null){
					mArticleListFragment = new ArticleListFragment();
				}
				if(mCategoryUrl != null){
					Bundle bundle = new Bundle();  
			        bundle.putString("url", mCategoryUrl);  
			        mArticleListFragment.setArguments(bundle); 
			        mCategoryUrl = null;
				}else{
					mArticleListFragment.setArguments(null); 
				}
				replaceFragment(mArticleListFragment);
				break;				
				
			case TAB_INDEX_FORUM:	
				if (mForumListFragment == null) {
					mForumListFragment = new CategoryListFragment();
				}
				replaceFragment(mForumListFragment);
				break;
			
			case TAB_INDEX_SETTING:
				if (mSettingFragment == null) {
					mSettingFragment = new SettingFragment();
				}
				replaceFragment(mSettingFragment);
				break;
				
			default:
				break;
		}
	};
}
