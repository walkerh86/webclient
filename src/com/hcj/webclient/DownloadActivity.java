package com.hcj.webclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Adapter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hcj.circlelayout.R;

public class DownloadActivity extends Activity{	
	private static final String TAG = "DownloadActivity";
	
	private static final String DOWNLOAD_FILE = "aa.html";
	private static final String SDPATH = Environment.getExternalStorageDirectory() + "/";
	private static final String DOWNLOAD_PATH = "zhcjtest/";
	private ListView mListView;
	private View mFooterView;
	private ArrayList<ArticleData> mArticleDatas = new ArrayList<ArticleData>();
	private ArrayList<ArticleData> mArticleAdd = new ArrayList<ArticleData>();
	private MyAdapter mMyAdapter;
	private int mLoadedPage;
	private int mLoadingPage;
	private ImageCache mImageCache;
	private Category mCurrCategory;
	private ArrayList<Category> mCategorys = new ArrayList<Category>(ConfigUtils.DEFAULT_CATEGORY_NUM);
	private boolean bCategoryMenuPrepared;
	
	private static final int HANDLER_MSG_LOAD_PAGE_DONE = 2;
	private static final int HANDLER_MSG_UPDATE_LIST = 3;
	private static final int HANDLER_MSG_UPDATE_TITLE = 4;
	private static final int HANDLER_MSG_LIST_LOADEDALL = 5;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what) {
				case 0:
					
					break;
				case 1:
					//Log.i(TAG,"progress="+mDownloadProgress);
					break;
				case HANDLER_MSG_LOAD_PAGE_DONE:
					parsePage(mLoadingPage);
					break;
				case HANDLER_MSG_UPDATE_LIST:
					int count = mArticleAdd.size();
					for(int i=0;i<count;i++){
						mArticleDatas.add(mArticleAdd.get(i));
					}
					mArticleAdd.clear();
					mMyAdapter.notifyDataSetChanged();
					break;
				case HANDLER_MSG_UPDATE_TITLE:					
					getWindow().setTitle(mCurrCategory.getTitle());
					break;		
				case HANDLER_MSG_LIST_LOADEDALL:
					mListView.removeFooterView(mFooterView);
					break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mLoadedPage = 0;
		mLoadingPage = -1;
		
		setContentView(R.layout.download_main);
		
		mFooterView = getLayoutInflater().inflate(R.layout.main_list_footer_bar, null);
			
		mListView = (ListView)findViewById(R.id.list_view);		
		mListView.addFooterView(mFooterView);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				ArticleData data = mArticleDatas.get(position);
				Intent intent = new Intent(DownloadActivity.this, ArticleActivity.class);
				intent.putExtra("url", data.article_url);				
				startActivity(intent);				
			}
		});
		
		mListView.setOnScrollListener(new AbsListView.OnScrollListener(){
			@Override  
	        public void onScrollStateChanged(AbsListView view, int scrollState) {  				
				if (mLoadingPage < 0 && mLoadedPage < mCurrCategory.getPageNum() && view.getLastVisiblePosition() == view.getCount() - 1) {
					loadPage(mLoadedPage+1);
	            }  
			}
			@Override  
	        public void onScroll(AbsListView view, int firstVisibleItem,  
	                int visibleItemCount, int totalItemCount) {  
	  
	        }  
		});	
		
		mMyAdapter = new MyAdapter(DownloadActivity.this,mArticleDatas,mHandler);
		mListView.setAdapter(mMyAdapter);
		
		initCache();
		initCategory();
		
		loadPage(1);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		super.onPrepareOptionsMenu(menu);
		
		int count = mCategorys.size();
		if(!bCategoryMenuPrepared && count >0){
			for(int i=0;i<count;i++){
				Category category = mCategorys.get(i);
				menu.add(0,i,0,category.getTitle());
			}
			
			bCategoryMenuPrepared = true;
		}	
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Category category = mCategorys.get(item.getItemId());
		if(mCurrCategory != category){
			mCurrCategory = category;
			mArticleDatas.clear();
			mListView.addFooterView(mFooterView);
			mListView.setAdapter(mMyAdapter);
			loadPage(1);
		}
		return true;
	}
	
	private void initCache(){
		mImageCache = ((BaseApplication)getApplication()).getImageCache();
	}	
	
	private ImageCache.onGetHtmlTextListener monGetHtmlTextListener = new ImageCache.onGetHtmlTextListener(){
		public void onGetHtmlText(String text){
			parsePage(mLoadingPage);
		}
	};
	
	private void loadPage(int page){	
		final String page_url = getPageUrl(page);	
		Log.i(TAG,"loadPage,page="+page+",page_url="+page_url);
		if(page_url == null){
			return;
		}
		
		mLoadingPage = page;
		mImageCache.getHtmlText(page_url, monGetHtmlTextListener);
	}
	
	private String getPageUrl(int page){
		if(page < 1){
			return null;
		}
		if(page == 1){
			return mCurrCategory.getUrl();
		}
		return mCurrCategory.getUrl()+"/page/"+page;
	}
	
	private void parsePage(final int page){
		final String page_url = getPageUrl(page);
		Log.i(TAG,"parseHtml page_url="+page_url);
		
		final String result = FileUtils.getTextString(ConfigUtils.APP_CACHE_PATH, page_url);
		if(result == null){
			Log.i(TAG,"parseHtml result=null");
			return;
		}
		
		new Thread(){
			public void run(){
				Document doc = Jsoup.parse(result);
				
				if(page_url.equals(ConfigUtils.MAIN_URL)){
					//init category
					Elements categorys = doc.select("#menu-main-menu > li");
					Log.i(TAG,"categorys size="+categorys.size());
					for(Element e : categorys){
						Category category = new Category();
						Element a = e.child(0);
						category.setUrl(a.attr("href"));
						category.setTitle(a.text());
						mCategorys.add(category);
					}
					
					mCurrCategory = mCategorys.get(0); //main page
				}
				Log.i(TAG,"curr cate url="+mCurrCategory.getUrl());
				if(page_url.equals(mCurrCategory.getUrl())){
					//new category
					
					//init page num
					Elements page_navis = doc.select("div.page_navi");
					if(page_navis != null){
						Element page_info = page_navis.first();
						if(page_info != null){
							//Log.i(TAG,"page_info="+page_info.toString());
							Element page_last = page_info.getElementsByTag("a").last();
							String pageUrl = page_last.attr("href");
							
							int start_idx = pageUrl.lastIndexOf("/")+1;
							String pageNum = pageUrl.substring(start_idx);			
							//mTotalPage = Integer.parseInt(pageNum);
							mCurrCategory.setPageNum(Integer.parseInt(pageNum));
							Log.i(TAG,"parseHtml,pageNum="+pageNum);
						}
					}
					
					//init title
					Elements titles = doc.select("head>title");
					if(titles != null){
						Element title = titles.first();
						String title_txt = title.text();
						Log.i(TAG,"parsePage, title_txt="+title_txt);
						if(title_txt != null){
							String[] title_txts = title_txt.split(" ");						
							mCurrCategory.setTitle(title_txts[0]);	
							mHandler.sendEmptyMessage(HANDLER_MSG_UPDATE_TITLE);
						}
					}
				}		
				
				Elements articles = doc.getElementsByTag("article");
				mArticleAdd.clear();
				Log.i(TAG,"parsePage,articles num ="+articles.size());
				for(Element e : articles){
					ArticleData data = new ArticleData();
					
					Elements title = e.getElementsByTag("h2");
					Element title_link = e.getElementsByTag("a").first();
					data.article_url = title_link.attr("href");
					data.title = title.text();			
					Elements summary = e.getElementsByClass("entry");
					data.summary = summary.text();
					Elements author = e.getElementsByClass("post-meta");
					data.author = author.text();
					Elements img = e.getElementsByClass("attachment-thumbnail");
					data.img_url = img.attr("src");
					//Log.i(TAG,"parseHtml,src="+src);
					
					mArticleAdd.add(data);
				}
				
				mHandler.sendEmptyMessage(HANDLER_MSG_UPDATE_LIST);	
				
				mLoadedPage = mLoadingPage;
				mLoadingPage = -1;
				
				if(mLoadedPage >= mCurrCategory.getPageNum()){
					//mListView.removeFooterView(mFooterView);
					mHandler.sendEmptyMessage(HANDLER_MSG_LIST_LOADEDALL);
				}			
			}
		}.start();
	}
	
	public class ArticleData{
		public String article_url;
		public String img_url;
		public String title;
		public String summary;
		public String author;
	}
	
	private class MyAdapter extends BaseAdapter{
		private ArrayList<ArticleData> mDatas;
		private Context mContext;
		private LayoutInflater mInflater;
		private Drawable mDefaultIcon;
		private Handler mHandler;
		
		public MyAdapter(Context context, ArrayList<ArticleData> datas, Handler handler){
			mDatas = datas;
			mContext = context;
			mHandler = handler;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mDefaultIcon = context.getResources().getDrawable(R.drawable.ic_launcher_jiong);
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
			if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.main_list_item, parent, false);
	        }
			
			ArticleData data = mDatas.get(position);
			
			TextView titleV = (TextView)convertView.findViewById(R.id.title);
			TextView summaryV = (TextView)convertView.findViewById(R.id.summary);
			TextView authorV = (TextView)convertView.findViewById(R.id.author);
			
			titleV.setText(data.title);
			//summaryV.setText(data.summary);
			authorV.setText(data.author);
			ImageView iconV = (ImageView)convertView.findViewById(R.id.icon);
			
			Bitmap b = mImageCache.getBitmap(data.img_url,mOnGetBitmapListener,true);
			if(b != null){				
				iconV.setImageBitmap(b);
			}else{
				iconV.setImageDrawable(mDefaultIcon);
			}
			
			return convertView;
		}
		
		private ImageCache.OnGetBitmapListener mOnGetBitmapListener = new ImageCache.OnGetBitmapListener(){
			public void onGetBitmap(Bitmap b){
				mHandler.sendEmptyMessage(HANDLER_MSG_UPDATE_LIST);
			}
		};
	}
	
	private void initCategory(){
		mCurrCategory = new Category();
		mCurrCategory.setUrl(ConfigUtils.MAIN_URL);
	}
	
	private class Category{
		private int mCategoryPages;
		private String mCategoryUrl;
		private String mTitle;
		
		public Category(){
			mCategoryPages = 0;
			mCategoryUrl = null;
			mTitle = null;
		}
		
		public void setUrl(String url){
			mCategoryUrl = url;
		}
		
		public void setPageNum(int num){
			mCategoryPages = num;
		}
		
		public void setTitle(String title){
			mTitle = title;
		}
		
		public String getUrl(){
			return mCategoryUrl;
		}
		
		public int getPageNum(){
			return mCategoryPages;
		}
		
		public String getTitle(){
			return mTitle;
		}
	}
}
