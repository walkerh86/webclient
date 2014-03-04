package com.hcj.webclient.util;

import com.hcj.webclient.R;
import com.hcj.webclient.widget.TitleWidget;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

public class FragmentUtil {
	public static void updateActivityTitle(Fragment fragment,int resId){
		Activity activity = fragment.getActivity();
		TitleWidget titleWidget = (TitleWidget)activity.findViewById(R.id.title_widget);
		if(titleWidget != null){
			titleWidget.setTitle(resId);
		}
		/*
		if(activity != null){
			activity.getWindow().setTitle(activity.getResources().getString(resId));
		}*/
	}
	
	public static void updateActivityTitle(Fragment fragment,CharSequence text){
		Activity activity = fragment.getActivity();
		TitleWidget titleWidget = (TitleWidget)activity.findViewById(R.id.title_widget);
		if(titleWidget != null){
			titleWidget.setTitle(text);
		}
		/*
		if(activity != null){
			activity.getWindow().setTitle(text);
		}*/
	}
	
	public static void updateActivityTitleLeft(Fragment fragment,int resId, View.OnClickListener listener){
		Activity activity = fragment.getActivity();
		TitleWidget titleWidget = (TitleWidget)activity.findViewById(R.id.title_widget);
		if(titleWidget != null){
			titleWidget.setLeft(resId,listener);
		}
		/*
		if(activity != null){
			activity.getWindow().setTitle(text);
		}*/
	}
}
