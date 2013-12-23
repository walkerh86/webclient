package com.hcj.webclient;

import com.hcj.webclient.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnClickListener{
	private static final String TAG = "MainActivity";
	CircleLayout mCircleLayout;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		//setContentView(new PathView(this));
		//setContentView(new ColorMatrixView(this));
		setContentView(new BorderView(this));
		Log.i(TAG,"onCreate");
		mCircleLayout = (CircleLayout)findViewById(R.id.circle_layout);
		//mCircleLayout.setChildOnClickListener(this);
		//mCircleLayout.setTotalDegree(360);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v){
		int id = v.getId();
		//if(id == R.id.circle_center_view){
			mCircleLayout.toggleAnim();
		//}
	}
	
	private class PathView extends View{
		Path mPath;
		Paint mPaint;
		BlurMaskFilter mBlurFilter;
		
		public PathView(Context context){
			super(context);
			
			mPath = new Path();
			mPath.reset();
			mPath.moveTo(50, 0);
			mPath.quadTo(100, 80, 50, 100);
			//mPath.quadTo(50, 120, 0, 200);
			//mPath.lineTo(50, 250);
			
			mPaint = new Paint();
			
			mPaint.setAntiAlias(true);
			mPaint.setColor(0xFFFF0000);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(10.0f);
			
			mBlurFilter = new BlurMaskFilter(12.0f,BlurMaskFilter.Blur.NORMAL);
			mPaint.setMaskFilter(mBlurFilter);

		}
		
		@Override
		public void onDraw(Canvas canvas){
			//super.onDraw(canvas);
			//canvas.drawRect(0,0,50,100,mPaint);
			canvas.drawPath(mPath, mPaint);
		}
	}
	
	private class BorderView extends View{
		private Bitmap mBmp;
		private Paint mBlurPaint = new Paint();
		private Bitmap mRelectionBmp;
		private Bitmap mNegativeBmp;
		private Bitmap mEmbossBmp;
		Paint shaderPaint;
		public BorderView(Context context){
			super(context);
			
			mBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
			mRelectionBmp = getReflectionBmp(mBmp);	
			mNegativeBmp = getNegativeBmp(mBmp);
			mEmbossBmp = getEmbossBmp(mBmp);			
		}
		
		private Bitmap getEmbossBmp(Bitmap src){
			Bitmap dst;			
			
			dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(dst);
			canvas.drawBitmap(src, 0, 0,null);
			
			int width = dst.getWidth();
			int height = dst.getHeight();  
			int[]oldPixels = new int[width*height];   
			int[]newPixels = new int[width*height];  
			int color;  
			int color2;  
			int pixelsR,pixelsG,pixelsB,pixelsA=0,pixelsR2,pixelsG2,pixelsB2;  
			dst.getPixels(oldPixels, 0, width, 0, 0, width, height);  
	          
	        for(int i = 1;i < height*width; i++){  
	                color = oldPixels[i-1];  
	                //前一个像素  
	                pixelsR = Color.red(color);  
	                pixelsG = Color.green(color);  
	                pixelsB = Color.blue(color);  
	                //当前像素  
	                color2 = oldPixels[i];  
	                pixelsR2 = Color.red(color2);  
	                pixelsG2 = Color.green(color2);  
	                pixelsB2 = Color.blue(color2);  
	                  
	                pixelsR = (pixelsR - pixelsR2 + 127);  
	                pixelsG = (pixelsG - pixelsG2 + 127);  
	                pixelsB = (pixelsB - pixelsB2 + 127);  
	                //均小于等于255  
	                if(pixelsR > 255){  
	                    pixelsR = 255;  
	                }  
	                  
	                if(pixelsG > 255){  
	                    pixelsG = 255;  
	                }  
	                  
	                if(pixelsB > 255){  
	                    pixelsB = 255;  
	                }  
	                  
	                newPixels[i] = Color.argb(pixelsA, pixelsR, pixelsG, pixelsB);  
	                  
	        }  
	        dst.setPixels(newPixels, 0, width, 0, 0, width, height);  
			
			return dst;
		}
		
		private Bitmap getNegativeBmp(Bitmap src){
			Bitmap dst;			
			
			dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(dst);
			canvas.drawBitmap(src, 0, 0,null);
			
			int width = dst.getWidth();
			int height = dst.getHeight();  
			int[]oldPixels = new int[width*height];   
			int[]newPixels = new int[width*height];  
			int color;  
			int color2;  
		    int pixelsR,pixelsG,pixelsB,pixelsA,pixelsR2,pixelsG2,pixelsB2;  
		    dst.getPixels(oldPixels, 0, width, 0, 0, width, height);  
	          
	        for(int i = 1;i < height*width; i++){  
	                color = oldPixels[i];  
	                //获取RGB分量  
	                pixelsA = Color.alpha(color);  
	                pixelsR = Color.red(color);  
	                pixelsG = Color.green(color);  
	                pixelsB = Color.blue(color);  
	                  
	                //转换  
	                pixelsR = (255 - pixelsR);  
	                pixelsG = (255 - pixelsG);  
	                pixelsB = (255 - pixelsB);  
	                //均小于等于255大于等于0  
	                if(pixelsR > 255){  
	                    pixelsR = 255;  
	                }  
	                else if(pixelsR < 0){  
	                    pixelsR = 0;  
	                }  
	                if(pixelsG > 255){  
	                    pixelsG = 255;  
	                }  
	                else if(pixelsG < 0){   
	                    pixelsG = 0;  
	                }  
	                if(pixelsB > 255){  
	                    pixelsB = 255;  
	                }  
	                else if(pixelsB < 0){  
	                    pixelsB = 0;  
	                }  
	                //根据新的RGB生成新像素  
	                newPixels[i] = Color.argb(pixelsA, pixelsR, pixelsG, pixelsB);  
	                  
	        }  
	        //根据新像素生成新图片  
	        dst.setPixels(newPixels, 0, width, 0, 0, width, height); 
	        
	        return dst;
		}
		
		private Bitmap getReflectionBmp(Bitmap src){
			Bitmap dst;
			
			Matrix m = new Matrix();
			m.preScale(1, -1);
			dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
			
			Canvas canvas = new Canvas(dst);
			Paint paint = new Paint();  
	        LinearGradient shader = new LinearGradient(0, 0,   
	                0, dst.getHeight(), 0x70000000, 0x00000000,  
	                TileMode.MIRROR);  
	        paint.setShader(shader);  
	        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));  
		  
	        // 覆盖效果  
	        canvas.drawRect(0, 0, dst.getWidth(), dst.getHeight(), paint);  

			return dst;
		}
		
		@Override
		public void onDraw(Canvas canvas){
			super.onDraw(canvas);
			
			//canvas.drawColor(0xFFAAAAAA);
			canvas.drawBitmap(mBmp,100,100,null);
			int top = 100+mBmp.getHeight()+2;
			canvas.drawBitmap(mRelectionBmp,100,top,null);			
			canvas.drawBitmap(mNegativeBmp, 200,100, null);
			canvas.drawBitmap(mEmbossBmp, 300,100, null);
		}
	}
	
	private class ColorMatrixView extends View{
		private Bitmap mBmp;
		private Paint mPaint;
		private Paint mPaint1;
		private Paint mPaint3;
		public ColorMatrixView(Context context){
			super(context);		
			mBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
			
			mPaint = new Paint();
			mPaint.setColor(0xFFFF0000);
			//BlurMaskFilter blurFilter = new BlurMaskFilter(10.0f,BlurMaskFilter.Blur.NORMAL);
			//mPaint.setMaskFilter(blurFilter);
			float[] colorArray = new float[] {
	                   1, 0, 0, 0, 0,
	                   0, 1, 0, 0, 0,
	                   0, 0, 1, 0, 100,
	                   0, 0, 0, 1, 0 };
			ColorMatrix colorMatrix = new ColorMatrix();
			colorMatrix.set(colorArray);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
			mPaint.setColorFilter(filter);
			
			mPaint1 = new Paint();
			mPaint1.setColor(0xFFFF0000);
			//BlurMaskFilter blurFilter = new BlurMaskFilter(10.0f,BlurMaskFilter.Blur.NORMAL);
			//mPaint.setMaskFilter(blurFilter);
			colorArray = new float[] {
	                   1, 0, 0, 0, 0,
	                   0, 1, 0, 0, 0,
	                   0, 0, 1, 0, 0,
	                   0, 0, 0, 1, 200 };
			colorMatrix = new ColorMatrix();
			colorMatrix.set(colorArray);
			filter = new ColorMatrixColorFilter(colorMatrix);
			mPaint1.setColorFilter(filter);
			
			mPaint3 = new Paint();
			mPaint3.setColor(0xFFFF0000);
			mPaint3.setStyle(Paint.Style.STROKE);
			mPaint3.setStrokeWidth(16);
			BlurMaskFilter blurFilter = new BlurMaskFilter(4.0f,BlurMaskFilter.Blur.INNER);
			mPaint3.setMaskFilter(blurFilter);
		}
		
		@Override
		public void onDraw(Canvas canvas){
			//super.onDraw(canvas);
			canvas.save();
			canvas.translate(100,100);
			canvas.drawBitmap(mBmp, 100,100,mPaint);
			canvas.drawBitmap(mBmp, 100,200,mPaint1);
			canvas.drawLine(20,20,220,20,mPaint3);
			canvas.restore();
		}
	}
}
