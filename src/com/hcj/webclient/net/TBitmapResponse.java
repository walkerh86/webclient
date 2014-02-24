package com.hcj.webclient.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hcj.webclient.util.BitmapUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class TBitmapResponse extends Response<Bitmap>{
	private static final String TAG = "TBitmapResponse";
	private byte[] mData;
	private int mMaxWidth;
	private int mMaxHeight;
	
	public TBitmapResponse(ResponseHandler handler, int step){		
		super(handler,step);
		mMaxWidth = -1;
		mMaxHeight = -1;
	}
	
	public TBitmapResponse(ResponseHandler handler,int step,boolean isCache){
		super(handler,step,isCache);
		mMaxWidth = -1;
		mMaxHeight = -1;
	}
	
	public TBitmapResponse(ResponseHandler handler,int step,boolean isCache,int width,int height){
		super(handler,step,isCache);
		mMaxWidth = width;
		mMaxHeight = height;
	}
	
	@Override
	protected Bitmap parseContent(InputStream is){
		Bitmap b = null;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] data = new byte[1024];  
        int count = -1;  
        try{
	        while((count = is.read(data,0,1024)) != -1){
	            outStream.write(data, 0, count);  
	        }
	        mData = outStream.toByteArray();  
        }catch(IOException e){        	
        }finally{
        	try{
        		outStream.close();
        	}catch(IOException e){            	
            }
        }
        
        BitmapFactory.Options options = BitmapUtils.getBitmapDecodeConfing(mData, mMaxWidth, mMaxHeight);
		b = BitmapFactory.decodeByteArray(mData, 0, mData.length, options);
		return b;
	}
	
	@Override
	public byte[] getCacheData(){
		return mData;
	}	
}
