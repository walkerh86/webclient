package com.hcj.webclient.net;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import android.util.Log;

public class CookieManager {
	private static final String TAG = "CookieManager";
	private HashMap<String,String> mCookies = new HashMap<String,String>();
	
	public CookieManager(){
		
	}
	
	public void setCookie(HttpUriRequest httprequest){
		httprequest.setHeader("Cookie", getCookiesString());
	}
	
	public void saveCookie(HttpResponse httpResponse){
		Header[] headers = httpResponse.getAllHeaders();
		for(int i=0;i<headers.length;i++){
			if(headers[i].getName().equals("Set-Cookie")){
				String cookieStr = headers[i].getValue();
				String[] cookieStrs = cookieStr.split("[;]");
				for(int j=0;j<cookieStrs.length;j++){
					String[] cookie = cookieStrs[j].split("[=]");
					String key = cookie[0].trim();
					if(!isPreserveKey(key) && (cookie.length > 1)){
						if(mCookies.containsKey(key)){
							continue;
						}
						mCookies.put(key, cookieStrs[j]);
						Log.i(TAG,"saveCookie,key="+key+",value="+cookieStrs[j]);
					}
				}
			}
		}		
	}
	
	public void saveCookie(Map<String,List<String>>headers){
		for(Map.Entry<String, List<String>>entry : headers.entrySet()){
			if((entry.getKey() != null) && entry.getKey().equals("Set-Cookie")){				
				for(int j=0;j<entry.getValue().size();j++){
					String cookieStr = entry.getValue().get(j);
					String[] cookieStrs = cookieStr.split("[;]");
					String[] cookie = cookieStrs[j].split("[=]");
					String key = cookie[0].trim();
					if(!isPreserveKey(key) && (cookie.length > 1)){
						if(mCookies.containsKey(key)){
							continue;
						}
						mCookies.put(key, cookieStrs[j]);
						Log.i(TAG,"saveCookie,key="+key+",value="+cookieStrs[j]);
					}
				}
			}
		}		
	}
	
	public String getCookiesString(){
		String cookies = (mCookies.size() > 0) ? new String() : null;
		for(Map.Entry<String, String> entry : mCookies.entrySet()){
			if(cookies.length() > 0){
				cookies += ";";
			}
			cookies += entry.getValue();
		}
		Log.i(TAG,"getCookiesString,cookie="+cookies);
		return cookies;
	}
	
	private boolean isPreserveKey(String key){
		if(key == null){
			return false;
		}
		if(key.equals("path") || key.equals("domain") || key.equals("expires")){
			return true;
		}
		return false;
	}
}
