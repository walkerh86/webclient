package com.hcj.webclient.util;

import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

public class BitmapUtils {
	private static final String TAG = "BitmapUtils";
	
	private static void calDecodeSizeOptions(Options opts, int maxWidth, int maxHeight){
		if(maxWidth > opts.outWidth && maxHeight > opts.outHeight){
			opts = null;
		}else{
			float ratioW = opts.outWidth/maxWidth;
			float ratioH = opts.outHeight/maxHeight;
			float ratioFinal = Math.min(ratioW, ratioH);
			float n = 1.0f;
			while((n*2) <= ratioFinal){
				n *= 2;
			}
			opts.inSampleSize = (int)n;
			if(opts.inSampleSize <= 1){
				opts = null;
			}
		}
	}
	
	public static Options getBitmapDecodeConfing(byte[] data, int maxWidth, int maxHeight){
		Options opts = null;
		if(maxWidth > 0 && maxHeight > 0){
			opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, opts);
			opts.inJustDecodeBounds = false;
			calDecodeSizeOptions(opts,maxWidth,maxHeight);
		}
		return opts;
	}
	public static Options getBitmapDecodeConfing(String fileUrl, int maxWidth, int maxHeight){
		Options opts = null;
		if(maxWidth > 0 && maxHeight > 0){
			opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(fileUrl,opts);
			opts.inJustDecodeBounds = false;
			calDecodeSizeOptions(opts,maxWidth,maxHeight);
		}
		return opts;
	}
}
