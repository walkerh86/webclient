package com.hcj.webclient.util;

import android.app.Activity;
import android.support.v4.app.Fragment;

public class FragmentUtil {
	public static void updateActivityTitle(Fragment fragment,int resId){
		Activity activity = fragment.getActivity();
		if(activity != null){
			activity.getWindow().setTitle(activity.getResources().getString(resId));
		}
	}
}
