package com.hcj.webclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.XMLReader;

import com.hcj.webclient.R;
import com.hcj.webclient.ArticleListFragment.ViewHolder;
import com.hcj.webclient.ImageCache.ImageLoadRequest;
import com.hcj.webclient.net.ResponseHandler;
import com.hcj.webclient.net.Response;
import com.hcj.webclient.net.Request;
import com.hcj.webclient.net.TStringResponse;
import com.hcj.webclient.util.DownloadUtils;
import com.hcj.webclient.util.FileUtils;
import com.hcj.webclient.util.FragmentUtil;
import com.hcj.webclient.util.Html;
import com.hcj.webclient.widget.TitleWidget;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ArticleActivity extends Activity implements ResponseHandler{
	private static final String TAG = "ArticleActivity";
	private String mUrl;
	private int mStartIndex;
	private int mEndIndex;
	private ImageCache mImageCache;
	private TextView mArticleTextView;
	private BlockingQueue<Request> mRequestQueue;
	private TitleWidget mTitleWidget;
	private ListView mListView;
	private ArrayList<CommentItem> mComments = new ArrayList<CommentItem>();
	private ArticleContentAdapter mArticleAdapter;
	
	private static final int REQUEST_ARTICLE = 0;
		
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
				case REQUEST_ARTICLE:
					parseHtml((String)msg.obj);
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		mRequestQueue = ((BaseApplication)getApplication()).getRequestQueue();
		
		mImageCache = ((BaseApplication)getApplication()).getImageCache();
		
		setContentView(R.layout.article_main);
		//mArticleTextView = (TextView)findViewById(R.id.article_text);
		mTitleWidget = (TitleWidget)findViewById(R.id.title_widget);
		mTitleWidget.enableLeftBack(R.drawable.title_back);
		
		mListView = (ListView)findViewById(R.id.comment_list);
		mArticleAdapter = new ArticleContentAdapter(this,mComments);
		mListView.setAdapter(mArticleAdapter);
		
		Intent intent = getIntent();
		mUrl = intent.getStringExtra("url");
		Log.i(TAG,"url="+mUrl);
		/*
		DownloadManager.loadPage(mUrl, true, new DownloadUtils.DownloadListener() {								
			@Override
			public void onDownloadProgress(long totalSize, long downloadSize) {					
			}

			@Override
			public void onDownloadDone(int result) {
				mHandler.sendEmptyMessage(0);
			}
		});		
		*/
		requestArticle();
	}
	
	private void requestArticle(){
		mRequestQueue.add(new Request(mUrl,"GET",null,null,				
				new TStringResponse(this,REQUEST_ARTICLE,true)));
	}
	
	@Override
	public void handleResponse(Response<?> response){
		Message msg = mHandler.obtainMessage(response.mStep, response.mResult);
		msg.sendToTarget();
	}
	
	private void parseHtml(){
		String result = FileUtils.getTextString(ConfigUtils.APP_CACHE_PATH, mUrl);
		parseHtml(result);
	}
	
	private void parseHtml(String result){		
		if(result == null){
			Log.i(TAG,"parseHtml result=null");
			return;
		}
		
		Document doc = Jsoup.parse(result);		
		//Log.i(TAG,"text="+article.text());
		Elements titles = doc.select("head>title");
		if(titles != null){
			Element title = titles.first();
			String title_txt = title.text();
			Log.i(TAG,"parsePage, title_txt="+title_txt);
			if(title_txt != null){
				String[] title_txts = title_txt.split(" ");						
				//getWindow().setTitle(title_txts[0]);
				mTitleWidget.setTitle(title_txts[0]);
			}
		}
		
		CommentItem mainItem = new CommentItem();
		
		Element articleEntry = doc.select("div.post-inner").first();
		Element postMeta = articleEntry.getElementsByClass("post-meta").first();
		Elements info = postMeta.getElementsByTag("span");
		mainItem.mAuthor = info.get(0).text();
		mainItem.mDate = info.get(1).text();
		Element article = doc.select("div.entry").first();
		mainItem.mContent = article.toString();
		mComments.clear();
		mComments.add(mainItem);
		
		Element commentList = doc.select("ol.commentlist").first();
		Elements comments =  commentList.getElementsByTag("li");
		Log.i(TAG,"comment size="+comments.size());
		for(Element comment : comments){
			CommentItem item = new CommentItem();
			
			item.mId = comment.id();			
			Element authorComment = comment.getElementsByClass("author-comment").first();
			item.mAuthor = authorComment.child(0).text();
			item.mDate = authorComment.child(1).text();
			item.mContent = comment.getElementsByClass("comment-content").first().text();
			//Log.i(TAG,"item, id="+item.mId+",author="+item.mAuthor);
			mComments.add(item);
		}
		
		mArticleAdapter.notifyDataSetChanged();
		//mArticleTextView.setText(Html.fromHtml(article.toString(),new UrlImageGetter(mArticleTextView,this),new ArticleTagHandler()));
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
		//public int mWidth;
		//public int mHeight;
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
	    	UrlImageData img_data = mUrlImageDatas.get(url);
	    	if(img_data == null){	    		
	    		img_data = new UrlImageData();
	    		img_data.mDrawable = new TextSpanUrlDrawable();
	    		img_data.mDrawable.setBounds(0, 0, width, height);
	    		img_data.mDrawable.drawable = mDefaultDrawable;
	    		img_data.mDrawable.drawable.setBounds(0, 0, width, height);	    		
	    		//img_data.mWidth = width;
	    		//img_data.mHeight = height;
	    		mUrlImageDatas.put(url,img_data);
	    		
	    		ImageLoadRequest request = mImageCache.new ImageLoadRequest();
	    		request.mView = tv_image;
	    		request.mUrl = url;
	    		request.mIsCacheMem = false;
	    		request.mWidth = -1;
	    		request.mHeight = -1;
	    		request.mListener = mOnGetBitmapListener;
	    		request.mIsCanceled = false;
	    		
	    		mImageCache.getBitmap(request);
	    	}
	        
	        return img_data.mDrawable;  
	    }  
	   
	    private ImageCache.OnGetBitmapListener mOnGetBitmapListener = new ImageCache.OnGetBitmapListener(){
			public void onGetBitmap(ImageLoadRequest request){
				UrlImageData img_data = mUrlImageDatas.get(request.mUrl);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(c.getResources(),request.mBitmap);
				img_data.mDrawable.drawable = bitmapDrawable;
				img_data.mDrawable.drawable.setBounds(img_data.mDrawable.getBounds());
				request.mView.invalidate();
			}
		};	   
	} 
	
	private class CommentItem{
		public String mId;
		public String mAvatarUrl;
		public String mAuthor;
		public String mDate;
		public String mContent;
	}
	
	private class ArticleContentAdapter extends BaseAdapter{
		ArrayList<CommentItem> mDatas;
		Context mContext;
		private LayoutInflater mInflater;
		private Drawable mDefaultIcon;
		private String mFloorFormatStr;
		private String mFloorMain;
		
		public ArticleContentAdapter(Context context, ArrayList<CommentItem> data){
			mContext = context;
			mDatas = data;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mDefaultIcon = context.getResources().getDrawable(R.drawable.avatar_default);
			mFloorFormatStr = context.getResources().getString(R.string.num_floor);
			mFloorMain = context.getResources().getString(R.string.floor_main);
		}
		
		public int getCount(){
			return mDatas.size();
		}
		
		public long getItemId(int position){
			return 0;
		}
		
		public Object getItem(int position){
			return null;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			ViewHolder viewHolder = null;
			
			if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.comment_list_item, parent, false);
	            viewHolder = new ViewHolder();
	            //Log.i(TAG,"viewHolder="+viewHolder);
	            viewHolder.iconView = (ImageView)convertView.findViewById(R.id.avatar);
	            viewHolder.dateView = (TextView)convertView.findViewById(R.id.date);
	            viewHolder.authorView = (TextView)convertView.findViewById(R.id.author);
	            viewHolder.contentView = (TextView)convertView.findViewById(R.id.content);
	            viewHolder.floorView = (TextView)convertView.findViewById(R.id.floor);
	            convertView.setTag(viewHolder);
	        }else{
	        	viewHolder = (ViewHolder)convertView.getTag();
	        }
			
			CommentItem data = mDatas.get(position);
			
			viewHolder.dateView.setText(data.mDate);
			viewHolder.authorView.setText(data.mAuthor);
			viewHolder.contentView.setText(Html.fromHtml(data.mContent,new UrlImageGetter(viewHolder.contentView,mContext),new ArticleTagHandler()));
			if(position == 0){
				viewHolder.floorView.setText(mFloorMain);
			}else{
				viewHolder.floorView.setText(String.format(mFloorFormatStr,position));
			}
			
			viewHolder.iconView.setImageDrawable(mDefaultIcon);
			
			return convertView;
		}
		
		private ImageCache.OnGetBitmapListener mOnGetBitmapListener = new ImageCache.OnGetBitmapListener(){
			public void onGetBitmap(ImageLoadRequest cb){
				((ImageView)cb.mView).setImageBitmap(cb.mBitmap);
			}
		};
		
		private class ViewHolder{
			ImageView iconView;
			TextView dateView;
			TextView authorView;
			TextView contentView;
			TextView floorView;
		}
	}
}
