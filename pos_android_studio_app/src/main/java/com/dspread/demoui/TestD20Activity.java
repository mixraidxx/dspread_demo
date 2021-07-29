package com.dspread.demoui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dspread.xpos.QPOSService;

/**
 * Time:2020/4/21
 * Author:Qianmeng Chen
 * Description:
 */
public class TestD20Activity extends Activity implements View.OnClickListener {
    private Button poweron,wake,poweroff,rst;
    private TextView text_wake,text_rst,text_power;

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_d20);
        poweron = findViewById(R.id.poweron);
        text_wake = findViewById(R.id.text_wake);
        text_rst = findViewById(R.id.text_rst);
        text_power = findViewById(R.id.text_power);
        wake = findViewById(R.id.wake);
        poweroff = findViewById(R.id.poweroff);
        rst = findViewById(R.id.rst);
//        if(QPOSService.d20WakeRead().equals("1")){
//            wake.setText("Wake Off");
//            text_wake.setText(String.format(getString(R.string.wake_pin),"1"));
//        }else{
//            wake.setText("Wake On");
//            text_wake.setText(String.format(getString(R.string.wake_pin),"0"));
//        }
//        if(QPOSService.d20RstRead().equals("1")){
//            rst.setText("Rst Off");
//            text_rst.setText(String.format(getString(R.string.rst_pin),"1"));
//        }else{
//            rst.setText("Rst On");
//            text_rst.setText(String.format(getString(R.string.rst_pin),"0"));
//        }
//
//        if(QPOSService.d20PowerOnAndOffRead().equals("1")){
//            text_power.setText(String.format(getString(R.string.power_pin),"1"));
//        }else{
//            text_power.setText(String.format(getString(R.string.power_pin),"0"));
//        }

        poweron.setOnClickListener(this);
        wake.setOnClickListener(this);
        poweroff.setOnClickListener(this);
        rst.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.poweron:
//                QPOSService.powerOnAndOffD20(1);
//                break;
//            case R.id.wake:
//                if(wake.getText().equals("Wake On")){
//                    QPOSService.d20Wake(1);
//                    wake.setText("Wake Off");
//                    text_wake.setText(String.format(getString(R.string.wake_pin),"1"));
//                }else{
//                    QPOSService.d20Wake(0);
//                    wake.setText("Wake On");
//                    text_wake.setText(String.format(getString(R.string.wake_pin),"0"));
//                }
//
//                break;
//            case R.id.rst:
//                if(rst.getText().equals("Rst On")){
//                    QPOSService.d20Rst(1);
//                    rst.setText("Rst Off");
//                    text_rst.setText(String.format(getString(R.string.rst_pin),"1"));
//                }else{
//                    QPOSService.d20Rst(0);
//                    rst.setText("Rst On");
//                    text_rst.setText(String.format(getString(R.string.rst_pin),"0"));
//                }
//                break;
//            case R.id.poweroff:
//                QPOSService.powerOnAndOffD20(0);
//                break;
//        }
//        if(QPOSService.d20PowerOnAndOffRead().equals("1")){
//            text_power.setText(String.format(getString(R.string.power_pin),"1"));
//        }else{
//            text_power.setText(String.format(getString(R.string.power_pin),"0"));
//        }
    }
}
