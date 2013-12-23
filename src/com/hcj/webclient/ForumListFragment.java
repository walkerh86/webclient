package com.hcj.webclient;

import java.io.File;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hcj.webclient.ArticleListFragment.ArticleData;

//import com.hcj.webclient.ArticleListFragment.ArticleData;
//import com.hcj.webclient.ArticleListFragment.Category;

//import com.hcj.webclient.ArticleListFragment.Category;
//import com.hcj.webclient.ArticleListFragment.ArticleData;
//import com.hcj.webclient.ArticleListFragment.Category;

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

public class ForumListFragment extends Fragment{
	private static final String TAG = "ForumListFragment";
	private View mFooterView;
	private ListView mListView;
	private ArrayList<Category> mCategorys = new ArrayList<Category>(ConfigUtils.DEFAULT_CATEGORY_NUM);	
	private ArrayList<Category> mCategorysAdd = new ArrayList<Category>(ConfigUtils.DEFAULT_CATEGORY_NUM);	
	private MyAdapter mMyAdapter;
	
	private static final int HANDLER_MSG_LOAD_PAGE_DONE = 2;
	private static final int HANDLER_MSG_UPDATE_LIST = 3;
	private static final int HANDLER_MSG_UPDATE_TITLE = 4;
	private static final int HANDLER_MSG_LIST_LOADEDALL = 5;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what) {				
				case HANDLER_MSG_LOAD_PAGE_DONE:
					parsePage();
					break;
				case HANDLER_MSG_UPDATE_LIST:
					int count = mCategorysAdd.size();
					for(int i=0;i<count;i++){
						mCategorys.add(mCategorysAdd.get(i));
					}
					mCategorysAdd.clear();
					mMyAdapter.notifyDataSetChanged();
					
					mListView.removeFooterView(mFooterView);
					break;
				default:					
					break;
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.download_main, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mListView = (ListView) getActivity().findViewById(R.id.list_view);

		
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				/*
				 * ArticleData data = mArticleDatas.get(position); Intent intent
				 * = new Intent(getActivity(), ArticleActivity.class);
				 * intent.putExtra("url", data.article_url);
				 * startActivity(intent);
				 */
			}
		});

		mMyAdapter = new MyAdapter(getActivity(), mCategorys);
		mListView.setAdapter(mMyAdapter);

		if(mCategorys.size() <= 0){
			loadPage(ConfigUtils.MAIN_URL);
		}else{
			mListView.removeFooterView(mFooterView);
		}
	}	
	
	private void loadPage(final String page_url){
		new Thread(){
			@Override
			public void run(){
				Log.i(TAG,"thread run");
				boolean bUseCache = false;
				File cacge_file = FileUtils.getFileByUrl(ConfigUtils.APP_CACHE_PATH, page_url);
				if(cacge_file.exists()){
					long duration = System.currentTimeMillis() - cacge_file.lastModified();
					if(duration > 0 && duration < ConfigUtils.CACHE_DURATION){
						bUseCache = true;
					}					
				}
				
				if(bUseCache){
					mHandler.sendEmptyMessage(HANDLER_MSG_LOAD_PAGE_DONE);
					Log.i(TAG,"get from cache");
				}else{
					File dest = FileUtils.getFileByUrlWithCreate(
							ConfigUtils.APP_CACHE_PATH, page_url);
					if (dest.exists()) {
						DownloadUtils.download(page_url, dest,new DownloadUtils.DownloadListener() {								
									@Override
									public void onDownloadProgress(
											long totalSize, long downloadSize) {
										mHandler.sendEmptyMessage(1);
									}

									@Override
									public void onDownloadDone(int result) {
										mHandler.sendEmptyMessage(HANDLER_MSG_LOAD_PAGE_DONE);
									}
						});
					}
				}
			}
		}.start();
	}
	
	private void parsePage(){
		final String page_url = ConfigUtils.MAIN_URL;
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
						mCategorysAdd.add(category);
					}
				}
				
				mHandler.sendEmptyMessage(HANDLER_MSG_UPDATE_LIST);
			}
		}.start();
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
	
	private class MyAdapter extends BaseAdapter{
		private ArrayList<Category> mDatas;
		private Context mContext;
		private LayoutInflater mInflater;
		
		public MyAdapter(Context context, ArrayList<Category> datas){
			mDatas = datas;
			mContext = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	            convertView = mInflater.inflate(R.layout.forum_list_item, parent, false);
	        }
			
			Category data = mDatas.get(position);
			
			TextView titleV = (TextView)convertView.findViewById(R.id.title);
			
			titleV.setText(data.mTitle);		
			
			return convertView;
		}
	}
}
