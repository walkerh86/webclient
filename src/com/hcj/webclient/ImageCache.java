package com.hcj.webclient;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ImageCache {	
	private static final String TAG = "ImageCache";
	private LruCache<String, Bitmap> mMemoryCache;
	private HashSet<String> mDownloadingSet;	
	private ExecutorService mDownloadingExecutorService;
	private HashSet<String> mDecodingBmpSet;
	private ExecutorService mDecodingBmpExecutorService;
	private Context mContext;
	
	public ImageCache(Context context, int cacheSize){
		mContext = context;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
		mDownloadingSet = new HashSet<String>(ConfigUtils.MAX_DOWNLOAD_THREAD);
		mDownloadingExecutorService = Executors.newFixedThreadPool(ConfigUtils.MAX_DOWNLOAD_THREAD);
		mDecodingBmpSet = new HashSet<String>(ConfigUtils.MAX_DOWNLOAD_THREAD);
		mDecodingBmpExecutorService = Executors.newFixedThreadPool(ConfigUtils.MAX_DOWNLOAD_THREAD);
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	public Bitmap getBitmap(final String url, final OnGetBitmapListener listener, final boolean cache_to_mem){
		Bitmap b = null;
		String key = FileUtils.replaceUrlWithPlus(url);
		b = getBitmapFromMemCache(key);
		if(b != null){
			//Log.i(TAG,"GC get from mem cache");
			return b;
		}
		if(getBitmapFromFileCache(url,listener,cache_to_mem)){
			return null;
		}
		
		if(NetworkUtils.getNetworkState(mContext) == NetworkUtils.NETWORN_NONE){
			return null;
		}
		
		getBitmapFromRemote(url,listener,cache_to_mem);
		
		return null;
	}
	
	private boolean getBitmapFromFileCache(final String url, final OnGetBitmapListener listener, final boolean cache_to_mem){
		if(!FileUtils.isFileExists(ConfigUtils.APP_CACHE_PATH, url)){
			return false;
		}
		
		final String key = FileUtils.replaceUrlWithPlus(url);		
		if(mDecodingBmpSet.contains(key)){
			return true;
		}
		
		mDecodingBmpExecutorService.submit(new Runnable(){
			@Override
			public void run(){
				Bitmap b = decodeBitmapFromFile(url);
				
				if(cache_to_mem){
					addBitmapToMemoryCache(key, b);
				}
				
				if(listener != null){
					listener.onGetBitmap(b);
				}
				
				mDecodingBmpSet.remove(key);
			}
		});
		
		return true;
	}	
	
	private void getBitmapFromRemote(final String url, final OnGetBitmapListener listener, final boolean cache_to_mem){	
		final String key = FileUtils.replaceUrlWithPlus(url);
		
		if(mDownloadingSet.contains(key)){
			return;
		}
		
		mDownloadingExecutorService.submit(new Runnable(){
			@Override
			public void run(){
				mDownloadingSet.add(key);
				
				File dest = FileUtils.getFileByUrlWithCreate(ConfigUtils.APP_CACHE_PATH, url);
				DownloadUtils.download(url, dest, new DownloadUtils.DownloadListener(){
					public void onDownloadProgress(long totalSize, long downloadSize){
						
					}
			        public void onDownloadDone(int result){
			        	Bitmap b = decodeBitmapFromFile(url);
			        	if(cache_to_mem){
							addBitmapToMemoryCache(key, b);
						}
			        	if(listener != null){
							listener.onGetBitmap(b);
						}
			        	mDownloadingSet.remove(key);
			        }
				});
			}
		});
	}
	
	private Bitmap decodeBitmapFromFile(final String url){
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.outWidth = 96;
		opts.outHeight = 96;
		Bitmap b = BitmapFactory.decodeFile(FileUtils.getFilePathByUrl(ConfigUtils.APP_CACHE_PATH, url),opts);
		return b;
	}

	public interface OnGetBitmapListener{
		public void onGetBitmap(Bitmap b);
	}
	
	public interface onGetHtmlTextListener{
		public void onGetHtmlText(String text);
	}
	
	public void getHtmlText(final String url, final onGetHtmlTextListener listener){
		boolean bUseCache = false;
		File cacge_file = FileUtils.getFileByUrl(ConfigUtils.APP_CACHE_PATH, url);
		if(cacge_file.exists()){
			long duration = System.currentTimeMillis() - cacge_file.lastModified();
			if(duration > 0 && duration < ConfigUtils.CACHE_DURATION){
				bUseCache = true;
			}					
		}
		
		if(bUseCache && getHtmlTextFromFileCache(url,listener)){
			return;
		}
		
		if(NetworkUtils.getNetworkState(mContext) == NetworkUtils.NETWORN_NONE){
			return;
		}
		
		getHtmlTextFromRemote(url,listener);
	}
	
	private boolean getHtmlTextFromFileCache(final String url, final onGetHtmlTextListener listener){
		if(!FileUtils.isFileExists(ConfigUtils.APP_CACHE_PATH, url)){
			return false;
		}
		
		final String key = FileUtils.replaceUrlWithPlus(url);		
		if(mDecodingBmpSet.contains(key)){
			return true;
		}
		
		mDecodingBmpExecutorService.submit(new Runnable(){
			@Override
			public void run(){
				String s = FileUtils.getTextString(ConfigUtils.APP_CACHE_PATH,url);
								
				if(listener != null){
					listener.onGetHtmlText(s);
				}
				
				mDecodingBmpSet.remove(key);
			}
		});
		
		return true;
	}
	
	private void getHtmlTextFromRemote(final String url, final onGetHtmlTextListener listener){	
		final String key = FileUtils.replaceUrlWithPlus(url);
		
		if(mDownloadingSet.contains(key)){
			return;
		}
		
		mDownloadingExecutorService.submit(new Runnable(){
			@Override
			public void run(){
				mDownloadingSet.add(key);
				
				File dest = FileUtils.getFileByUrlWithCreate(ConfigUtils.APP_CACHE_PATH, url);
				DownloadUtils.download(url, dest, new DownloadUtils.DownloadListener(){
					public void onDownloadProgress(long totalSize, long downloadSize){
						
					}
			        public void onDownloadDone(int result){
			        	String s = FileUtils.getTextString(ConfigUtils.APP_CACHE_PATH,url);
						
						if(listener != null){
							listener.onGetHtmlText(s);
						}
						
			        	mDownloadingSet.remove(key);
			        }
				});
			}
		});
	}
}
