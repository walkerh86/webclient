package com.hcj.webclient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hcj.webclient.net.ResponseHandler;
import com.hcj.webclient.net.Response;
import com.hcj.webclient.net.Request;
import com.hcj.webclient.net.TBitmapResponse;
import com.hcj.webclient.net.TStringResponse;
import com.hcj.webclient.util.BitmapUtils;
import com.hcj.webclient.util.DownloadUtils;
import com.hcj.webclient.util.FileUtils;
import com.hcj.webclient.util.NetworkUtils;
import com.hcj.webclient.util.PoolUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageCache implements ResponseHandler{	
	private static final String TAG = "ImageCache";
	private LruCache<String, Bitmap> mMemoryCache;
	private ExecutorService mDecodingBmpExecutorService;
	private Context mContext;
	private BlockingQueue<Request> mRequestQueue;
	
	private boolean mIsListFling;
	
	private static final int MSG_REQUEST_BMP_DONE = 0;
	private static final int MSG_DECODE_BMP_DONE = 1;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case MSG_REQUEST_BMP_DONE:
				ImageLoadRequest request = (ImageLoadRequest)msg.obj;
				if(!request.mIsCanceled){
					request.mListener.onGetBitmap(request);
				}
				mImageRequests.remove(request.mLoadUrl);
				mRequestCachePool.returnToCache(request);
				break;
			default:
				break;
			}
		}
	};
	
	public ImageCache(Context context, int cacheSize){
		mContext = context;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
		mDecodingBmpExecutorService = Executors.newFixedThreadPool(ConfigUtils.MAX_DOWNLOAD_THREAD);
		
		mRequestQueue = ((BaseApplication)mContext.getApplicationContext()).getRequestQueue();
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}
	
	private PoolUtils<ImageLoadRequest> mRequestCachePool = new PoolUtils<ImageLoadRequest>();
	private HashMap<ImageView,ImageLoadRequest> mImageRequestsQueue = new LinkedHashMap<ImageView,ImageLoadRequest>();
	public Bitmap getBitmap(String url, ImageView view, int outWidth, int outHeight,
			OnGetBitmapListener listener, boolean cache_to_mem){
		if(url == null || url.length() == 0){
			return null;
		}
		
		Bitmap b = null;
		b = getBitmapFromMemCache(url);
		if(b != null){
			//Log.i(TAG,"get from mem cache,url="+url);
			return b;
		}
		
		//Log.i(TAG,"getBitmap,view="+view+",url="+url);
		
		ImageLoadRequest oldRequest = (ImageLoadRequest)view.getTag();
		
		//this url is processing
		if(oldRequest != null && oldRequest.mLoadUrl.equals(url)){
			return null;
		}
		
		//the view load different url, so cancel last url processing
		if(oldRequest != null){			
			oldRequest.mIsCanceled = true;
		}
		
		ImageLoadRequest newRequest = null;
		if(mIsListFling){
			newRequest = mImageRequestsQueue.get(view);
		}
		if(newRequest == null){
			newRequest = mRequestCachePool.getFromCache();
		}
		if(newRequest == null){
			newRequest = new ImageLoadRequest();
			Log.i(TAG,"new ImageLoadRequest");
		}
		
		newRequest.mView = view;
		newRequest.mLoadUrl = url;
		newRequest.mIsCacheMem = cache_to_mem;
		newRequest.mWidth = outWidth;
		newRequest.mHeight = outHeight;
		newRequest.mListener = listener;
		newRequest.mIsCanceled = false;
		
		view.setTag(newRequest);
		
		//Log.i(TAG,"getBitmap,mIsListFling="+mIsListFling+",url="+url);
		if(mIsListFling){
			mImageRequestsQueue.put(view, newRequest);
		}else{			
			getBitmapAsync(newRequest);
		}
		return b;
	}
	
	public Bitmap getBitmap(final String url, final ImageView view, 
			final OnGetBitmapListener listener, final boolean addToMemCache){
		return getBitmap(url,view,-1,-1,listener,addToMemCache);
	}
	
	public Bitmap getBitmap(ImageLoadRequest request){
		if(request.mLoadUrl == null || request.mLoadUrl.length() == 0){
			return null;
		}
		
		Bitmap b = null;
		b = getBitmapFromMemCache(request.mLoadUrl);
		if(b != null){
			//Log.i(TAG,"get from mem cache,url="+url);
			return b;
		}
		
		getBitmapAsync(request);
		
		return null;
	}
	
	private void getBitmapAsync(ImageLoadRequest request){
		if(request.mLoadUrl == null || request.mLoadUrl.length() == 0){
			return;
		}
		mImageRequests.put(request.mLoadUrl, request);
		
		if(getBitmapFromFileCache(request.mLoadUrl,request,request.mIsCacheMem)){
			return;
		}
		
		if(NetworkUtils.getNetworkState(mContext) == NetworkUtils.NETWORN_NONE){
			mImageRequests.remove(request.mLoadUrl);
			return;
		}
		
		requestImage(request);
	}	
	
	private class DecodeBmpRunnable implements Runnable{
		ImageLoadRequest mRequest;		
		
		public DecodeBmpRunnable(ImageLoadRequest request){
			mRequest = request;
		}
		@Override
		public void run(){
			Bitmap b = decodeBitmapFromFile(mRequest.mLoadUrl, mRequest.mWidth,mRequest.mHeight);					
			mRequest.onGetBitmap(b);
		}
	}
	
	private boolean getBitmapFromFileCache(String url, ImageLoadRequest cb, boolean cache_to_mem){
		if(!FileUtils.isFileExists(ConfigUtils.APP_CACHE_PATH, url)){
			return false;
		}
			
		mDecodingBmpExecutorService.submit(new DecodeBmpRunnable(cb));		
		return true;
	}	
	
	private HashMap<String,ImageLoadRequest> mImageRequests = new HashMap<String,ImageLoadRequest>();
	private void requestImage(ImageLoadRequest request){
		mRequestQueue.add(new Request(request.mLoadUrl,"GET",null,null,				
				new TBitmapResponse(this,0,true,request.mWidth,request.mHeight)));
	}

	@Override
	public void handleResponse(Response<?> response){
		ImageLoadRequest request = mImageRequests.get(response.mUrl);
		request.onGetBitmap(((TBitmapResponse)response).mResult);
	}	
		
	private synchronized static Bitmap decodeBitmapFromFile(final String url, int width, int height){
		String fileUrl = FileUtils.getFilePathByUrl(ConfigUtils.APP_CACHE_PATH, url);
		BitmapFactory.Options opts = null;
		if(width > 0 && height > 0){
			opts = BitmapUtils.getBitmapDecodeConfing(fileUrl,width,height);
		}
		
		Bitmap b = BitmapFactory.decodeFile(fileUrl,opts);
		return b;
	}
	
	private synchronized static Bitmap decodeBitmapFromFile(final String url){
		return decodeBitmapFromFile(url,-1,-1);
	}

	public interface OnGetBitmapListener{
		public void onGetBitmap(ImageLoadRequest cb);
	}
	
	public class ImageLoadRequest{
		public View mView;		
		public String mLoadUrl;
		public boolean mIsCacheMem;
		public int mWidth;
		public int mHeight;
		public OnGetBitmapListener mListener;
		
		public Bitmap mBitmap;
		public boolean mIsCanceled;
				
		public void onGetBitmap(Bitmap b){
			if(mIsCacheMem && b != null){
				addBitmapToMemoryCache(mLoadUrl, b);
			}
			
			if(b != null && !mIsCanceled){
				mBitmap = b;
				
				Message msg = mHandler.obtainMessage(MSG_REQUEST_BMP_DONE, this);
				msg.sendToTarget();
			}
		}
	}
	/*
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
	*/
	public void setListFling(boolean isFling){
		if(mIsListFling == isFling){
			return;
		}
		
		mIsListFling = isFling;
		if(!mIsListFling){
			for(Map.Entry<ImageView, ImageLoadRequest> entry : mImageRequestsQueue.entrySet()){
				ImageLoadRequest cb = entry.getValue();
				getBitmapAsync(cb);
			}
			mImageRequestsQueue.clear();
		}
	}
}
