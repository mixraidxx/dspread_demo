package com.dspread.demoui.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Time:2020/4/26
 * Author:Qianmeng Chen
 * Description:
 */
public class GetScreenWhUtils {
    public static void  getScreenWigth(Activity context){
        DisplayMetrics outMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int widthPixels = outMetrics.widthPixels;
        int heightPixels = outMetrics.heightPixels;
        Log.i("TAG", "widthPixels = " + widthPixels + ",heightPixels = " + heightPixels);
        //widthPixels = 1440, heightPixels = 2768
    }
}
