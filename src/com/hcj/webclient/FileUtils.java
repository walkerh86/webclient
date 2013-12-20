package com.hcj.webclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import android.os.Environment;
import android.util.Log;

public class FileUtils { 
	private static final String TAG = "FileUtils";
	private static String mRootPath;
	
	public static void init(String root_path){
		mRootPath = Environment.getExternalStorageDirectory() + "/" + root_path;
	}
	
	public static void initSdDir(String dir_name){
		Log.i(TAG,"initSdDir,dir="+dir_name);
		File dir = new File(mRootPath+"/"+dir_name);
		if(!dir.exists()){
			dir.mkdirs();
		}
	}
	
	public static File getFileByUrl(String dir, String url){
		String result_url = replaceUrlWithPlus(url);
		File file = new File(mRootPath+"/"+dir+"/"+result_url);
		return file;		
	}
	
	public static File getFileByUrlWithCreate(String dir, String url){
		String result_url = replaceUrlWithPlus(url);
		File file = null;
		try{
			file = new File(mRootPath+"/"+dir+"/"+result_url);
			if(!file.exists()){
				file.createNewFile();
			}
		}catch(IOException e){
			Log.i(TAG,"getFileByUrlWithCreate, e="+e);
		}
		
		return file;
	}	
	
	public static boolean isFileExists(String dir, String url){
		File file = getFileByUrl(dir,url);
		return (file != null) ? file.exists() : false;
	}
	
	public static String getFilePathByUrl(String dir, String url){
		return mRootPath+"/"+dir+"/"+replaceUrlWithPlus(url);
	}
	
	public static String replaceUrlWithPlus(String url) {
         if (url != null) {
        	 return url.replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
        }
        return null;
    }
	
	public static String getTextString(String dir, String url){
		String result_url = replaceUrlWithPlus(url);
		File file = null;
		BufferedReader reader = null;
		InputStream is = null;
		StringBuffer str_buffer = null;
		try{
			file = new File(mRootPath+"/"+dir+"/"+result_url);
			if(!file.exists()){
				return null;
			}
			
			is = new FileInputStream(file);
			str_buffer = new StringBuffer();
			reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = reader.readLine()) != null){
				str_buffer.append(line);
			}
		}catch(IOException e){
			Log.i(TAG,"getUrlFile, e="+e);
		}finally {
			try{
	            if(reader != null) {
	                reader.close();
	            }
	            if(is != null){
	            	is.close();
	            }
            }catch(IOException e){
    			Log.i(TAG,"getUrlFile, e="+e);
    		}
        }
		
		return (str_buffer != null) ? str_buffer.toString() : null;
	}	
}
