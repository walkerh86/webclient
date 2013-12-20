package com.hcj.webclient;

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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class DownloadUtils {
	private static final String TAG = "DownloadUtils";
	public static final int DATA_BUFFER_SIZE = 8192;
	
	public interface DownloadListener{
		public void onDownloadProgress(long totalSize, long downloadSize);
        public void onDownloadDone(int result);
	}
	
	public static void download(String url, File dest, DownloadListener listener){
		InputStream input = null;
		
		try {
			HttpGet httpRequest = new HttpGet(url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
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
				
				listener.onDownloadDone(0);
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
