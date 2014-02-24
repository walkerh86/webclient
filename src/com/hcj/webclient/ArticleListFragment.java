package com.hcj.webclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hcj.webclient.R;
import com.hcj.webclient.ImageCache.ImageLoadRequest;
import com.hcj.webclient.net.ResponseHandler;
import com.hcj.webclient.net.Response;
import com.hcj.webclient.net.Request;
import com.hcj.webclient.net.TStringResponse;
import com.hcj.webclient.util.DownloadUtils;
import com.hcj.webclient.util.FileUtils;
import com.hcj.webclient.util.FragmentUtil;


public class ArticleListFragment extends Fragment implements ResponseHandler{	
	private static final String TAG = "ArticleListFragment";
	
	private ListView mListView;
	private View mFooterView;
	private ArrayList<ArticleData> mArticleDatas = new ArrayList<ArticleData>();
	private ArrayList<ArticleData> mArticleAdd = new ArrayList<ArticleData>();
	private ArticleAdapter mArticleAdapter;
	private int mLoadedPage;
	private int mLoadingPage;
	private ImageCache mImageCache;
	private boolean bPaused;
	private String mCurrentUrl;
	private int mCurrentPageNum;
	private String mCurrentTitle;
	
	private BlockingQueue<Request> mRequestQueue;
	
	private static final int REQUEST_LOAD_PAGE = 0;
	private static final int REQUEST_IMAGE = 6;
	
	private static final int HANDLER_MSG_LOAD_PAGE_DONE = 2;
	private static final int HANDLER_MSG_UPDATE_LIST = 3;
	private static final int HANDLER_MSG_UPDATE_TITLE = 4;
	private static final int HANDLER_MSG_LIST_LOADEDALL = 5;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			if(bPaused){
				mLoadingPage = -1;
				return;
			}
			
			switch (msg.what) {
				case REQUEST_LOAD_PAGE:
					//parsePage(mLoadingPage);
					parsePage((String)msg.obj);
					break;
				case REQUEST_IMAGE:
					ImageView imgView = (ImageView)((ImageCache.ImageLoadRequest)msg.obj).mView;
					Bitmap b = ((ImageCache.ImageLoadRequest)msg.obj).mBitmap;
					Log.i(TAG,"get bitmap,w="+b.getWidth()+",h="+b.getHeight());
					imgView.setImageBitmap(b);
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
					mArticleAdapter.notifyDataSetChanged();
					break;
				case HANDLER_MSG_UPDATE_TITLE:					
					FragmentUtil.updateActivityTitle(ArticleListFragment.this,mCurrentTitle);
					break;		
				case HANDLER_MSG_LIST_LOADEDALL:
					Log.i(TAG,"removeFooter view");
					mListView.removeFooterView(mFooterView);
					break;
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		
		mRequestQueue = ((BaseApplication)getActivity().getApplication()).getRequestQueue();
		
		mLoadedPage = 0;
		mLoadingPage = -1;
		mImageCache = ((BaseApplication)getActivity().getApplication()).getImageCache();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG,"onCreateView");
		return inflater.inflate(R.layout.download_main, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG,"onActivityCreated");
		super.onCreate(savedInstanceState);
		
		if(mCurrentTitle != null){
			FragmentUtil.updateActivityTitle(this,mCurrentTitle);
		}
				
		mFooterView = getActivity().getLayoutInflater().inflate(R.layout.main_list_footer_bar, null);
			
		mListView = (ListView)getActivity().findViewById(R.id.list_view);
		mListView.addFooterView(mFooterView);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				ArticleData data = mArticleDatas.get(position);
				Intent intent = new Intent(getActivity(), ArticleActivity.class);
				intent.putExtra("url", data.article_url);				
				startActivity(intent);				
			}
		});
		
		mListView.setOnScrollListener(new AbsListView.OnScrollListener(){
			@Override  
	        public void onScrollStateChanged(AbsListView view, int scrollState) {  				
				if (mLoadingPage < 0 && mLoadedPage < mCurrentPageNum && view.getLastVisiblePosition() == view.getCount() - 1) {
					//loadPage(mLoadedPage+1);
					requestLoadArticleList(mLoadedPage+1);
	            }  
				mImageCache.setListFling(scrollState == SCROLL_STATE_FLING);
			}
			@Override  
	        public void onScroll(AbsListView view, int firstVisibleItem,  
	                int visibleItemCount, int totalItemCount) {  
	  
	        }  
		});			
		
		if(mArticleAdapter == null){
			mArticleAdapter = new ArticleAdapter(getActivity(),mArticleDatas,mHandler);
		}
		mListView.setAdapter(mArticleAdapter);
		
		Bundle bundle = getArguments();
		if(bundle != null){
			String url = bundle.getString("url");
			setCategoryUrl(url);
			Log.i(TAG,"getArguments url="+url);
		}
	}
	
	public void onPause(){
		super.onPause();
		bPaused = true;
	}
	
	public void onResume(){
		super.onResume();
		bPaused = false;
	}
	
	public void setCategoryUrl(String url){
		if(url == null || url.equals(mCurrentUrl)){
			return;
		}
		
		FragmentUtil.updateActivityTitle(this, R.string.app_name);
		mArticleDatas.clear();
		if(mArticleAdapter != null){
			mArticleAdapter.notifyDataSetChanged();
		}
		mCurrentPageNum = 1;
		mCurrentTitle = null;
		mCurrentUrl = url;
		
		//loadPage(1);
		requestLoadArticleList(1);
	}
	
	private void requestLoadArticleList(int page){
		final String pageUrl = getPageUrl(page);	
		Log.i(TAG,"loadPage,page="+page+",page_url="+pageUrl);
		if(pageUrl == null){
			return;
		}
		mLoadingPage = page;
		mRequestQueue.add(new Request(pageUrl,"GET",null,null,				
				new TStringResponse(this,REQUEST_LOAD_PAGE,true)));
	}
		
	@Override
	public void handleResponse(Response<?> response){
		Message msg = mHandler.obtainMessage(response.mStep, response.mResult);
		msg.sendToTarget();
	}
		
	private String getPageUrl(int page){
		if(page < 1){
			return null;
		}
		if(page == 1){
			return mCurrentUrl;
		}
		return mCurrentUrl+"/page/"+page;
	}
	
	private void parsePage(final String result){
		final String page_url = getPageUrl(mLoadingPage);
		
		new Thread(){
			public void run(){
				Document doc = Jsoup.parse(result);
				
				if(page_url.equals(mCurrentUrl)){
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
							mCurrentPageNum = Integer.parseInt(pageNum);
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
							mCurrentTitle = title_txts[0];	
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
				
				if(mLoadedPage >= mCurrentPageNum){
					//mListView.removeFooterView(mFooterView);
					mHandler.sendEmptyMessage(HANDLER_MSG_LIST_LOADEDALL);
				}			
			}
		}.start();
	}
	
	private void parsePage(final int page){
		if(bPaused){
			return;
		}
		
		final String page_url = getPageUrl(page);
		Log.i(TAG,"parseHtml page="+page+", page_url="+page_url);
		
		final String result = FileUtils.getTextString(ConfigUtils.APP_CACHE_PATH, page_url);
		if(result == null){
			Log.i(TAG,"parseHtml result=null");
			return;
		}
		
		parsePage(result);
	}
	
	private class ArticleData{
		public String article_url;
		public String img_url;
		public String title;
		public String summary;
		public String author;
	}
	
	private class ArticleAdapter extends BaseAdapter{
		private ArrayList<ArticleData> mDatas;
		private LayoutInflater mInflater;
		private Drawable mDefaultIcon;
		
		public ArticleAdapter(Context context, ArrayList<ArticleData> datas, Handler handler){
			mDatas = datas;
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
			ViewHolder viewHolder = null;
			
			if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.main_list_item, parent, false);
	            viewHolder = new ViewHolder();
	            //Log.i(TAG,"viewHolder="+viewHolder);
	            viewHolder.iconView = (ImageView)convertView.findViewById(R.id.icon);
	            viewHolder.titleView = (TextView)convertView.findViewById(R.id.title);
	            viewHolder.authorView = (TextView)convertView.findViewById(R.id.author);
	            convertView.setTag(viewHolder);
	        }else{
	        	viewHolder = (ViewHolder)convertView.getTag();
	        }
			
			ArticleData data = mDatas.get(position);
			
			viewHolder.titleView.setText(data.title);
			viewHolder.authorView.setText(data.author);
			
			Bitmap b = null;
			if((data.img_url != null) && (data.img_url.length() > 0)){
				b = mImageCache.getBitmap(data.img_url,viewHolder.iconView, 75,75,mOnGetBitmapListener,true);						
			}
			if(b != null){				
				viewHolder.iconView.setImageBitmap(b);				
			}else{
				viewHolder.iconView.setImageDrawable(mDefaultIcon);					
			}
			
			return convertView;
		}
		
		private ImageCache.OnGetBitmapListener mOnGetBitmapListener = new ImageCache.OnGetBitmapListener(){
			public void onGetBitmap(ImageLoadRequest cb){
				((ImageView)cb.mView).setImageBitmap(cb.mBitmap);
			}
		};
	}
	
	static class ViewHolder{
		ImageView iconView;
		TextView titleView;
		TextView authorView;
	}
}
