package com.hcj.webclient.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.hcj.webclient.ConfigUtils;

import android.util.Log;

public class HttpDispatcher{
	private static final String TAG="HttpDispatcher";
	private boolean mQuit = false;
	private final BlockingQueue<Request> mRequestQueue;
	//private SSLSocketFactory mSslSocketFactory;
	private static final CookieManager mCookieManager = new CookieManager();
	
	private ExecutorService mDispatcherServices;
	private Runnable mDispatcherRunnable = new Runnable(){
		@Override
		public void run(){			
			Request request;
			
			while(true){
				try {
	                // Take a request from the queue.
	                request = mRequestQueue.take();
	            } catch (InterruptedException e) {
	                // We may have been interrupted because it was time to quit.
	                if (mQuit) {
	                    return;
	                }
	                continue;
	            }
				
				boolean success = false;
				int retryTimes = 0;
				while(!success){
					success = processRequest(request);
					if (mQuit) {
	                    return;
	                }
					if(!success){
						retryTimes++;
						if(retryTimes > 2){
							break;
						}
						
						try{
							Thread.sleep(500);
						}catch(InterruptedException e){
							
						}
					}
				}
			}
		}
	};
	
	public HttpDispatcher(BlockingQueue<Request> requestQueue){
		mRequestQueue = requestQueue;		
		mDispatcherServices = Executors.newFixedThreadPool(ConfigUtils.MAX_DOWNLOAD_THREAD);
		for(int i=0;i<ConfigUtils.MAX_DOWNLOAD_THREAD;i++){
			mDispatcherServices.submit(mDispatcherRunnable);
		}
		/*
		try{
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[] { tm }, null);
			mSslSocketFactory = sslcontext.getSocketFactory();		
			HttpsURLConnection.setDefaultSSLSocketFactory(mSslSocketFactory);
		}catch( KeyManagementException e){
			System.out.print("init http e="+e+"\n");
		}catch(NoSuchAlgorithmException e){
			System.out.print("init http e="+e+"\n");
		}
		*/
	}
	
	private boolean processRequest(Request request){
		InputStream is = null;
		boolean success = false;
		
		try{			
			Map<String,String> params = request.getParams();			
			String paramStr = null;
			if(params != null && params.size() > 0){
				StringBuilder sb = new StringBuilder();
				for(Map.Entry<String, String>entry : params.entrySet()){
					if(sb.length() > 0){
						sb.append("&");
					}
					sb.append(entry.getKey());
					sb.append("=");
					sb.append(entry.getValue());
				}
				paramStr = sb.toString();
			}
			
			String requestUrl = request.getUrl();
			if(request.getType().equals("GET") && paramStr != null){
				requestUrl += "?"+paramStr;				
			}
			Log.i(TAG,"httpDispatcher requestUrl="+requestUrl);
			URL url = new URL(requestUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			//add headers&cookie
			Map<String,String>headers = request.getHeaders();
			if(headers != null && headers.size() > 0){					
				for(Map.Entry<String, String>entry : headers.entrySet()){
					connection.addRequestProperty(entry.getKey(),entry.getValue());
				}
			}
			String cookie = mCookieManager.getCookiesString();
			if(cookie != null){
				Log.i(TAG,"cookie="+cookie);
				connection.addRequestProperty("Cookie",cookie);
			}
			//add parameters	
			if(request.getType().equals("POST") && paramStr != null){
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);				
				byte[] bypes = paramStr.getBytes();
				connection.getOutputStream().write(bypes);
			}
			//
			is = connection.getInputStream();
			String encoding = connection.getContentEncoding();
			if(encoding != null && encoding.equals("gzip")){
				is = new GZIPInputStream(is);
			}
			//
			Response<?> response = request.getResponse();
			if(response != null){
				mCookieManager.saveCookie(connection.getHeaderFields());
				response.parseResponse(requestUrl,connection.getResponseCode(),connection.getResponseMessage()
						,is,connection.getHeaderFields());
				response.performCache(requestUrl, is);
				response.deliverResponse();
			}
			success = true;
			Log.i(TAG,"httpDispatcher end\n");
		}catch(MalformedURLException e){
			Log.i(TAG,"httpDispatcher e="+e);
		}catch(IOException e){
			Log.i(TAG,"httpDispatcher e="+e);
		}finally{
			try{
				if(is != null){
					is.close();
				}
			}catch(IOException e){
				Log.i(TAG,"httpDispatcher e="+e);
			}
		}
		
		return success;
	}
	
	private String getUrlEncodeString(String src){
		String dst = null;
		try{
			dst = URLEncoder.encode(src, "UTF-8");
		}catch(UnsupportedEncodingException e){
			Log.i(TAG,"getUrlEncodeString e="+e);
		}
		
		return dst;
	}
	
	private static X509TrustManager tm = new X509TrustManager() {
		public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};
}
