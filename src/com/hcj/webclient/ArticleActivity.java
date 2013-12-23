package com.hcj.webclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.XMLReader;

import com.hcj.webclient.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
//import android.text.Html;
//import android.text.Html.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

public class ArticleActivity extends Activity{
	private static final String TAG = "ArticleActivity";
	private String mUrl;
	private int mStartIndex;
	private int mEndIndex;
	private ImageCache mImageCache;
	private TextView mArticleTextView;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
				case 0:
					parseHtml();
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		mImageCache = ((BaseApplication)getApplication()).getImageCache();
		
		setContentView(R.layout.article_main);
		mArticleTextView = (TextView)findViewById(R.id.article_text);		
		
		Intent intent = getIntent();
		mUrl = intent.getStringExtra("url");
		Log.i(TAG,"url="+mUrl);
		
		if(FileUtils.isFileExists(ConfigUtils.APP_CACHE_PATH, mUrl)){
			mHandler.sendEmptyMessage(0);
			Log.i(TAG,"get from cache");
		}else{
			new Thread(){
				@Override
				public void run(){
					File dest = FileUtils.getFileByUrlWithCreate(ConfigUtils.APP_CACHE_PATH, mUrl);
					if(dest.exists()){
						Log.i(TAG,"get from network");
						DownloadUtils.download(mUrl, dest, new DownloadUtils.DownloadListener() {					
							@Override
							public void onDownloadProgress(long totalSize, long downloadSize) {								
							}
							
							@Override
							public void onDownloadDone(int result) {
								mHandler.sendEmptyMessage(0);					
							}
						});
					}
				}
			}.start();
		}	
	}
	
	private void parseHtml(){
		String result = FileUtils.getTextString(ConfigUtils.APP_CACHE_PATH, mUrl);
		if(result == null){
			Log.i(TAG,"parseHtml result=null");
			return;
		}
		
		Document doc = Jsoup.parse(result);
		Element article = doc.select("div.entry").first();
		//Log.i(TAG,"text="+article.text());
		Elements titles = doc.select("head>title");
		if(titles != null){
			Element title = titles.first();
			String title_txt = title.text();
			Log.i(TAG,"parsePage, title_txt="+title_txt);
			if(title_txt != null){
				String[] title_txts = title_txt.split(" ");						
				getWindow().setTitle(title_txts[0]);
			}
		}
		
		mArticleTextView.setText(Html.fromHtml(article.toString(),new UrlImageGetter(mArticleTextView,this),new ArticleTagHandler()));
	}
	
	private class ArticleTagHandler implements Html.TagHandler{
		public void handleTag(boolean opening, String tag,Editable output, XMLReader xmlReader){
            if(tag.equals("style") || tag.equals("script")){
            	if(opening){
            		mStartIndex = output.length();
            	}else{
            		mEndIndex = output.length();
            		output.delete(mStartIndex,mEndIndex);
            	}
            }
		}
	}
	
	public class TextSpanUrlDrawable extends Drawable{  
	    private Drawable drawable;  
	    
	    public void setColorFilter(ColorFilter cf){
	    	
	    }
	    public void setAlpha(int alpha){
	    	
	    }
	    public int getOpacity(){
	    	return PixelFormat.OPAQUE;
	    }
	    
	    @Override
	    public void draw(Canvas canvas){
	    	if(drawable != null){
	    		drawable.draw(canvas);
	    	}
	    }
	}
	
	public class UrlImageData{
		public TextSpanUrlDrawable mDrawable;
		public int mWidth;
		public int mHeight;
	}
	
	public class UrlImageGetter implements Html.ImageGetter {  
	    Context c;  
	    TextView tv_image;  
	    TextSpanUrlDrawable urlDrawable; 
	    HashMap<String,UrlImageData> mUrlImageDatas = new HashMap<String,UrlImageData>();
	    Drawable mDefaultDrawable;
	  
	    public UrlImageGetter(TextView t, Context c) {  
	        this.tv_image = t;  
	        this.c = c;  
	        mDefaultDrawable = c.getResources().getDrawable(R.drawable.ic_launcher_jiong);
	    }  
	  
	    @Override  
	    public Drawable getDrawable(String url, int width, int height) {	
	    	String key = FileUtils.replaceUrlWithPlus(url);
	    	UrlImageData img_data = mUrlImageDatas.get(key);
	    	if(img_data == null){	    		
	    		img_data = new UrlImageData();
	    		img_data.mDrawable = new TextSpanUrlDrawable();
	    		img_data.mDrawable.setBounds(0, 0, width, height);
	    		img_data.mDrawable.drawable = mDefaultDrawable;
	    		img_data.mDrawable.drawable.setBounds(0, 0, width, height);	    		
	    		img_data.mWidth = width;
	    		img_data.mHeight = height;
	    		mUrlImageDatas.put(key,img_data);
	    		
	    		mImageCache.getBitmap(url, new OnGetBitmapDone(key,tv_image), false);
	    	}
	        
	        return img_data.mDrawable;  
	    }  
	   
		private class OnGetBitmapDone implements ImageCache.OnGetBitmapListener{
			String mKey;
			TextView mTextView;
			
			public OnGetBitmapDone(String key, TextView text_view){
				mKey = key;
				mTextView = text_view;
			}
			
			public void onGetBitmap(Bitmap b){
				Log.i(TAG,"onGetBitmap ,key="+mKey);
				UrlImageData img_data = mUrlImageDatas.get(mKey);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(c.getResources(),b);
				img_data.mDrawable.drawable = bitmapDrawable;
				img_data.mDrawable.drawable.setBounds(0, 0, img_data.mWidth, img_data.mHeight);
				mTextView.invalidate();
			}
		}
	} 
}
