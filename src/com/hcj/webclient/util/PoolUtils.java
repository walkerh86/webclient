package com.hcj.webclient.util;

import java.util.ArrayList;

import android.util.Log;

public class PoolUtils<T> {
	private static final String TAG = "PoolUtils";
	
	private ArrayList<T> mCache = new ArrayList<T>();
	
	public T getFromCache(){
		T item = (mCache.size() > 0) ? mCache.get(0) : null;
		if(item != null){
			mCache.remove(0);
		}
		//Log.i(TAG,"getFromCache,item="+item);
		return item;
	}
	
	public synchronized void returnToCache(T item){
		Log.i(TAG,"returnToCache,item="+item);
		mCache.add(item);
	}
}
