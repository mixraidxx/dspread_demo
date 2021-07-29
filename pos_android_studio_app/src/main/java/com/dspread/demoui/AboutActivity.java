package com.dspread.demoui;

import com.dspread.demoui.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView content = (TextView) findViewById(R.id.content);
//		content.setText("当前版本号："+QPOSService.getSdkVersion()+"\n日期："+QPOSService.getSdkDate()+"\n主要解决问题：增加USB OTG通信方式\n");
	}
}
