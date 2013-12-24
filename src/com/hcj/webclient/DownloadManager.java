package com.hcj.webclient;

import java.io.File;

import android.util.Log;

import com.hcj.webclient.util.DownloadUtils;
import com.hcj.webclient.util.FileUtils;
import com.hcj.webclient.util.DownloadUtils.DownloadListener;

public class DownloadManager {
	private static final String TAG = "DownloadManager";	
	
	public static void loadPage(final String pageUrl, boolean bPermanentCache, final DownloadListener listener){
		File cacge_file = FileUtils.getFileByUrl(ConfigUtils.APP_CACHE_PATH, pageUrl);
		if(cacge_file.exists()){
			long duration = System.currentTimeMillis() - cacge_file.lastModified();
			if(bPermanentCache || (duration > 0 && duration < ConfigUtils.CACHE_DURATION)){
				listener.onDownloadDone(DownloadUtils.DOWNLOAD_SUCCESS);
				Log.i(TAG,"get from cache");
				return;
			}					
		}
		
		final File dest = FileUtils.getFileByUrlWithCreate(ConfigUtils.APP_CACHE_PATH, pageUrl);				
		if (dest.exists()) {
			new Thread(){
				@Override
				public void run(){
					Log.i(TAG,"start download from network");
					DownloadUtils.download(pageUrl, dest, listener);
				}
			}.start();
		}else{
			Log.i(TAG,"create file fail");
			listener.onDownloadDone(DownloadUtils.DOWNLOAD_FILE_NONEXIST);
		}
	}
}
