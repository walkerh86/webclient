package com.hcj.webclient;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.hcj.webclient.net.HttpDispatcher;
import com.hcj.webclient.net.Request;

import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.hcj.webclient.util.FileUtils;

import android.app.Application;
import android.util.Log;

public class BaseApplication extends Application{
	private static final String TAG = "BaseApplication";
	private ImageCache mImageCache;
	//private RequestQueue mRequestQueue;	
	private BlockingQueue<Request> mRequestQueue;
	private HttpDispatcher mHttpDispatcher;
	
	@Override
    public void onCreate() {
		super.onCreate();
		
		init();
	}
	
	public ImageCache getImageCache(){
		return mImageCache;
	}
	
	public BlockingQueue<Request> getRequestQueue(){
		return mRequestQueue;
	}
		
	private void init(){		
		FileUtils.init(ConfigUtils.APP_ROOT_PATH);
		FileUtils.initSdDir(ConfigUtils.APP_CACHE_PATH);
		
		//mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		//mRequestQueue.start();
		mRequestQueue = new ArrayBlockingQueue<Request>(10);	
		mHttpDispatcher = new HttpDispatcher(mRequestQueue);
		//mHttpDispatcher.start();
		
		int maxMemory = (int)(Runtime.getRuntime().maxMemory());
		int cacheSize = maxMemory/8;
		Log.i(TAG,"initCache,maxMem="+maxMemory+",cacheSize="+cacheSize);
		mImageCache = new ImageCache(this,cacheSize);
	}
}
