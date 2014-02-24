package com.hcj.webclient.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hcj.webclient.ConfigUtils;

import android.util.Log;

public class DownloadUtils {
	private static final String TAG = "DownloadUtils";
	public static final int DATA_BUFFER_SIZE = 8192;
	
	public static final int DOWNLOAD_SUCCESS = 0;
	public static final int DOWNLOAD_FILE_NONEXIST = 1;
	public static final int DOWNLOAD_TIMEOUT = 2;
	public static final int DOWNLOAD_UPDATE_PROGRESS = 3;
	public static CookieManager mCookieManager = new CookieManager();
	
	public interface DownloadListener{
		public void onDownloadProgress(long totalSize, long downloadSize);
        public void onDownloadDone(int result);
	}
	
	public static void download(String url, File dest, DownloadListener listener){
		InputStream input = null;
		
		try {
			HttpGet httpRequest = new HttpGet(url);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			Log.i(TAG,"download, url="+url);			
			mCookieManager.setCookie(httpRequest);
			
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				mCookieManager.saveCookie(httpResponse);
				int totalSize = 0;
				int downloadSize = 0;

				HttpEntity httpEntity = httpResponse.getEntity();				
				input = httpEntity.getContent();
				Header header = httpEntity.getContentEncoding ();
				if(header != null){
					Log.i(TAG,"header="+header.toString());
					if(header.getValue().equals("gzip")){
						input = new GZIPInputStream(input);
					}
				}
				totalSize = (int) httpEntity.getContentLength();
				
				FileOutputStream fos = new FileOutputStream(dest);
				byte b[] = new byte[DATA_BUFFER_SIZE];
				int len = 0;
				while ((len = input.read(b)) != -1) {
					fos.write(b, 0, len);
					downloadSize += len;
					listener.onDownloadProgress(totalSize, downloadSize);
				}
				fos.flush();
				fos.close();
				
				listener.onDownloadDone(DOWNLOAD_SUCCESS);
			}
		} catch (ClientProtocolException e) {

		} catch (IOException e) {
			Log.i(TAG, "e=" + e);
		} finally {
			try{
				if(input != null){
					input.close();
				}
			}catch(IOException e){
				
			}
		}
	}
}
