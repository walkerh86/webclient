package com.hcj.webclient;

import com.hcj.circlelayout.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class SplashActivity extends Activity{	
	private static final String TAG = "SplashActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash_main);
		
		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... params){
				int result = 0;		
				long start_time = System.currentTimeMillis();
				
				//FileUtils.init(ConfigUtils.APP_ROOT_PATH);
				//FileUtils.initSdDir(ConfigUtils.APP_CACHE_PATH);
				
				long initTime = System.currentTimeMillis() - start_time;
				if(initTime < ConfigUtils.SPLASH_SHOW_MIN_TIME){
					try{
						Thread.sleep(ConfigUtils.SPLASH_SHOW_MIN_TIME - initTime);
					}catch(InterruptedException e){
						
					}
				}
				
				return result;
			}
			
			@Override
			protected void onPostExecute(Integer result){
				Intent intent = new Intent(SplashActivity.this,MainTabActivity.class);
                startActivity(intent);
                finish();
			}
		}.execute();
	}
}
