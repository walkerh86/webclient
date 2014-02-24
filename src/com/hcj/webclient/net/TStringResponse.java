package com.hcj.webclient.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.hcj.webclient.ConfigUtils;
import com.hcj.webclient.util.FileUtils;

import android.util.Log;

public class TStringResponse extends Response<String>{
	private static final String TAG = "TStringHttpResponse";
	//private String mCharSet = "UTF-8"; //default
	
	public TStringResponse(ResponseHandler handler,int step){
		super(handler,step);
	}
	
	public TStringResponse(ResponseHandler handler,int step,boolean isCache){
		super(handler,step,isCache);
	}
	
	@Override
	protected String parseContent(InputStream is){
		String result = null;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is/*,"UTF-8"*/));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			result = sb.toString();
		}catch(IOException e){
			Log.i(TAG,"StringHttpResponse,e="+e);
		}
		
		return result;
	}
	
	public byte[] getCacheData(){
		return mResult.getBytes();
	}	
}
