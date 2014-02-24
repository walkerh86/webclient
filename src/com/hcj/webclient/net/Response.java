package com.hcj.webclient.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.hcj.webclient.ConfigUtils;
import com.hcj.webclient.util.FileUtils;

public class Response<T> {
	private static final String TAG = "MyHttpResponse";
	
	public int mResponseCode;
	public String mResponseMsg;
	//private byte[] mContents;
	public Map<String,List<String>>mHeaders;
	public T mResult;
	private ResponseHandler mHandler;
	public int mStep;
	public boolean mCache;
	public String mUrl;
	
	public Response(ResponseHandler handler, int step){
		mHandler = handler;
		mStep = step;
	}
	
	public Response(ResponseHandler handler, int step, boolean isCache){
		this(handler,step);
		mCache = isCache;
	}
	
	public void parseResponse(String url, int code, String msg, InputStream is, Map<String,List<String>> headers){
		mUrl = url;
		mResponseCode = code;
		mResponseMsg = new String(msg);
		mHeaders = headers;
		mResult = parseContent(is);
	}
	
	public void performCache(String url, InputStream is){
		if(!mCache){
			return;
		}
		
		FileOutputStream fos = null;	
		try{
			final File dest = FileUtils.getFileByUrlWithCreate(ConfigUtils.APP_CACHE_PATH, url);
			fos = new FileOutputStream(dest);
			byte[] b = getCacheData();
			fos.write(b);
			fos.flush();			
		}catch(IOException e){
			Log.i(TAG,"performCache e="+e);
		}finally{
			if(fos != null){
				try{
					fos.close();
				}catch (Exception e){
					
				}
			}
		}
	}
	
	public byte[] getCacheData(){
		return null;
	}
	
	public void deliverResponse(){
		mHandler.handleResponse(this);
	}
	
	protected T parseContent(InputStream is){
		return null;
	}	
}
