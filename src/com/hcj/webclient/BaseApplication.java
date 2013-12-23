package com.hcj.webclient;

import com.hcj.webclient.util.FileUtils;

import android.app.Application;
import android.util.Log;

public class BaseApplication extends Application{
	private static final String TAG = "BaseApplication";
	ImageCache mImageCache;
	
	@Override
    public void onCreate() {
		super.onCreate();
		
		init();
	}
	
	public ImageCache getImageCache(){
		return mImageCache;
	}
	
	private void init(){
		int maxMemory = (int)(Runtime.getRuntime().maxMemory());
		int cacheSize = maxMemory/8;
		Log.i(TAG,"initCache,maxMem="+maxMemory+",cacheSize="+cacheSize);
		mImageCache = new ImageCache(this,cacheSize);
		
		FileUtils.init(ConfigUtils.APP_ROOT_PATH);
		FileUtils.initSdDir(ConfigUtils.APP_CACHE_PATH);
	}
}
