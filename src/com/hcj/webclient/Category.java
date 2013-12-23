package com.hcj.webclient;

public class Category{
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