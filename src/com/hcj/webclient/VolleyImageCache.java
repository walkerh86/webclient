package com.hcj.webclient;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.hcj.webclient.util.FileUtils;

public class VolleyImageCache implements ImageCache{
	private LruCache<String, Bitmap> mImgCache;
	
	public VolleyImageCache(int maxSize){
		mImgCache = new LruCache<String, Bitmap>(maxSize);
	}
	
	@Override
	public Bitmap getBitmap(String url){
		final String key = FileUtils.replaceUrlWithPlus(url);
		return mImgCache.get(key);
	}
	
	@Override
	public void putBitmap(String url, Bitmap bitmap){
		final String key = FileUtils.replaceUrlWithPlus(url);
		mImgCache.put(key, bitmap);
	}
}
