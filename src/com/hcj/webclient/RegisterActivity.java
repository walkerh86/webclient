package com.hcj.webclient;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;

import com.hcj.webclient.net.ResponseHandler;
import com.hcj.webclient.net.TStringResponse;
import com.hcj.webclient.net.TBitmapResponse;
import com.hcj.webclient.net.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.hcj.webclient.net.Request;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class RegisterActivity extends Activity implements ResponseHandler{
	private static final String TAG = "RegisterActivity";
	private ImageView mCaptchaView;
	private EditText mUserInput;
	private EditText mEmailInput;
	private EditText mCaptchaInput;
	private String mCaptchaUrlBase;
	private int mCaptchaRefNo;
	private BlockingQueue<Request> mRequestQueue;
	private String mCaptchaId;
		
	private static final int REQUEST_REGISTER = 0;
	private static final int REQUEST_CAPTCHA = 1;
	private static final int REQUEST_SUBMIT = 2;
	
	private static final String REGISTER_URL = "http://www.shenyisyn.org/wp-login.php?action=register";
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){	
				case REQUEST_REGISTER:
					String s = (String)msg.obj;
					parseRequestRegister(s);
					break;	
				case REQUEST_CAPTCHA:
					Bitmap b = (Bitmap)msg.obj;
					mCaptchaView.setImageBitmap(b);
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mRequestQueue = ((BaseApplication)getApplication()).getRequestQueue();
		//mCache = mRequestQueue.getCache();
		
		setContentView(R.layout.register_main);
		
		mUserInput = (EditText)findViewById(R.id.user_name_input);
		mEmailInput = (EditText)findViewById(R.id.user_email_input);
		mCaptchaInput = (EditText)findViewById(R.id.captcha_input);
		
		mCaptchaView = (ImageView)findViewById(R.id.blcap_img);
		Button refreshBtn = (Button)findViewById(R.id.refresh_btn);
		refreshBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				refreshCaptcha();
			}
		});
		Button submitBtn = (Button)findViewById(R.id.register_btn);
		submitBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				requestSubmit();
			}
		});		
		
		requestRegister();
	}
	
	private void parseRequestRegister(String result){
		if(result == null){
			Log.i(TAG,"parseHtml result=null");
			return;
		}
		
		Document doc = Jsoup.parse(result);
		Element blcapImg = doc.getElementById ("blcap_img");
		if(blcapImg != null){
			mCaptchaUrlBase = blcapImg.attr("src");
			requestCaptchaImage(mCaptchaRefNo);
		}
		Element captchaId = doc.getElementsByAttributeValueMatching("name", "captcha_id").first();
		if(captchaId != null){
			mCaptchaId = captchaId.attr("value");
			Log.i(TAG,"mCaptchaId="+mCaptchaId);
		}
	}
	
	private void refreshCaptcha(){
		mCaptchaRefNo++;
		requestCaptchaImage(mCaptchaRefNo);
	}
	
	private void requestRegister(){
		mRequestQueue.add(new Request(REGISTER_URL,"GET",null,null,				
				new TStringResponse(this,REQUEST_REGISTER)));
	}
	
	private void requestCaptchaImage(int refNo){
		String url = mCaptchaUrlBase;
		if(refNo > 0){
			url += "&refresh="+refNo;
		}
		
		mRequestQueue.add(new Request(url,"GET",null,null,				
				new TBitmapResponse(this,REQUEST_CAPTCHA)));
	}
	
	private void requestSubmit(){
		HashMap<String,String> params = new LinkedHashMap<String,String>();
		params.put("user_login", mUserInput.getText().toString());
		params.put("user_email", mEmailInput.getText().toString());
		params.put("user_captcha", mCaptchaInput.getText().toString());
		params.put("captcha_id", mCaptchaId);
		params.put("redirect_to", "");
		params.put("wp-submit", "%E6%B3%A8%E5%86%8C");
		mRequestQueue.add(new Request(REGISTER_URL,"POST",
				null,params,new TStringResponse(this,REQUEST_SUBMIT)));				
	}
	
	@Override
	public void handleResponse(Response<?> response){
		Message msg = mHandler.obtainMessage(response.mStep, response.mResult);
		msg.sendToTarget();
	}
}
