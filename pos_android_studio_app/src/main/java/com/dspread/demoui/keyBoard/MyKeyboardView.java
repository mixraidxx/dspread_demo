package com.dspread.demoui.keyBoard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.dspread.demoui.KeyBoardNumInterface;
import com.dspread.demoui.QPOSUtil;
import com.dspread.demoui.R;
import com.dspread.demoui.TRACE;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * ****************************************************************
 * 文件名称: MyKeyboardView
 * 作    者: Created by gyd
 * 创建时间: 2018/11/29 17:20
 * 文件描述: 自定义键盘，支持多种键盘切换
 * 注意事项: 密码输入
 * ****************************************************************
 */
public class MyKeyboardView extends KeyboardView {
    public static final int KEYBOARDTYPE_Num = 0;//数字键盘
    public static final int KEYBOARDTYPE_Num_Pwd = 1;//数字键盘（密码）
    public static final int KEYBOARDTYPE_ABC = 2;//字母键盘
    public static final int KEYBOARDTYPE_Symbol = 4;//符号键盘
    public static final int KEYBOARDTYPE_Only_Num_Pwd = 5;//数字键盘（密码）(不能切换其他键盘)

    private final String strLetter = "abcdefghijklmnopqrstuvwxyz";//字母

    private EditText mEditText;
    private PopupWindow mWindow;
    private Activity mActivity;

    private Keyboard keyboardNum;
    private Keyboard keyboardNumPwd;
    private Keyboard keyboardOnlyNumPwd;
    private Keyboard keyboardABC;
    private Keyboard keyboardSymbol;
    private int mHeightPixels;//屏幕高度

    public boolean isSupper = false;//字母键盘 是否大写
    public boolean isPwd = false;//数字键盘 是否随机
    private int keyBoardType;//键盘类型
    private List<String> dataList = new ArrayList<>();

    public MyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setHeight(int mHeightPixels){
        this.mHeightPixels = mHeightPixels;
    }

    public void setContext(Activity mActivity){
        this.mActivity = mActivity;
    }

    public void init(EditText editText, PopupWindow window, int keyBoardType,List<String> dataList) {
        this.dataList = dataList;
        this.mEditText = editText;
        this.mWindow = window;
        this.keyBoardType = keyBoardType;
        if (keyBoardType == KEYBOARDTYPE_Num_Pwd || keyBoardType == KEYBOARDTYPE_Only_Num_Pwd) {
            isPwd = true;
        }
        setEnabled(true);
        setPreviewEnabled(false);
        setOnKeyboardActionListener(mOnKeyboardActionListener);
        setKeyBoardType(keyBoardType);
    }

    public EditText getEditText() {
        return mEditText;
    }

    /**
     * 设置键盘类型
     */
    public void setKeyBoardType(int keyBoardType) {
        switch (keyBoardType) {
            case KEYBOARDTYPE_Num:
                if (keyboardNum == null) {
                    keyboardNum = new Keyboard(getContext(), R.xml.keyboard_number);
                }
                setKeyboard(keyboardNum);
                break;
            case KEYBOARDTYPE_ABC:
                if (keyboardABC == null)
                    keyboardABC = new Keyboard(getContext(), R.xml.keyboard_abc);
                setKeyboard(keyboardABC);
                break;
            case KEYBOARDTYPE_Num_Pwd:
                if (keyboardNumPwd == null)
                    keyboardNumPwd = new Keyboard(getContext(), R.xml.keyboard_number);
                randomKey(keyboardNumPwd);
                setKeyboard(keyboardNumPwd);
                break;
            case KEYBOARDTYPE_Symbol:
                if (keyboardSymbol == null)
                    keyboardSymbol = new Keyboard(getContext(), R.xml.keyboard_symbol);
                setKeyboard(keyboardSymbol);
                break;
            case KEYBOARDTYPE_Only_Num_Pwd:
                if (keyboardOnlyNumPwd == null)
                    keyboardOnlyNumPwd = new Keyboard(getContext(), R.xml.keyboard_only_number);
                randomKey(keyboardOnlyNumPwd);
                setKeyboard(keyboardOnlyNumPwd);
                break;
        }
    }

    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

        @Override
        public void onPress(int primaryCode) {
//            List<Keyboard.Key> keys = keyboardOnlyNumPwd.getKeys();
//            for(int i = 0 ; i < keys.size(); i++){
//                Keyboard.Key key = keys.get(i);
////                key.
//                new FancyShowCaseView.Builder(mActivity)
//                        .focusOn()
//                        .title("Focus on View")
//                        .build()
//                        .show();
//            }
        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Editable editable = mEditText.getText();
            int start = mEditText.getSelectionStart();
            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE://回退
                    if (editable != null && editable.length() > 0) {
                        if (start > 0) {
                            editable.delete(start - 1, start);
                        }
                    }
                    break;
                case Keyboard.KEYCODE_SHIFT://大小写切换
                    changeKey();
                    setKeyBoardType(KEYBOARDTYPE_ABC);
                    break;
                case Keyboard.KEYCODE_CANCEL:// 隐藏
                case Keyboard.KEYCODE_DONE:// 确认
                    mWindow.dismiss();
                    break;
                case 123123://切换数字键盘
                    if (isPwd) {
                        setKeyBoardType(KEYBOARDTYPE_Num_Pwd);
                    } else {
                        setKeyBoardType(KEYBOARDTYPE_Num);
                    }
                    break;
                case 456456://切换字母键盘
                    if (isSupper)//如果当前为大写键盘，改为小写
                    {
                        changeKey();
                    }
                    setKeyBoardType(KEYBOARDTYPE_ABC);
                    break;
                case 789789://切换符号键盘
                    setKeyBoardType(KEYBOARDTYPE_Symbol);
                    break;
                case 666666://人名分隔符·
                    editable.insert(start, "·");
                    break;
                default://字符输入
                    editable.insert(start, Character.toString((char) primaryCode));
            }
        }

        @Override
        public void onText(CharSequence text) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    };

    /**
     * 键盘大小写切换
     */
    private void changeKey() {
        List<Keyboard.Key> keylist = keyboardABC.getKeys();
        if (isSupper) {// 大写切小写
            for (Keyboard.Key key : keylist) {
                if (key.label != null && strLetter.contains(key.label.toString().toLowerCase())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
            }
        } else {// 小写切大写
            for (Keyboard.Key key : keylist) {
                if (key.label != null && strLetter.contains(key.label.toString().toLowerCase())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
            }
        }
        isSupper = !isSupper;
    }

    public static KeyBoardNumInterface keyBoardNumInterface;
    /**
     * 数字键盘随机
     * code 48-57 (0-9)
     */
    public void randomKey(Keyboard pLatinKeyboard) {
        int[] ayRandomKey = new int[13];
        for(int i = 0; i < dataList.size() ; i ++){
            ayRandomKey[i]=Integer.valueOf(dataList.get(i),16);
        }
//        int[] ayRandomKey = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
//        Random random = new Random();
//        for (int i = 0; i < ayRandomKey.length; i++) {
//            int a = random.nextInt(ayRandomKey.length);
//            int temp = ayRandomKey[i];
//            ayRandomKey[i] = ayRandomKey[a];
//            ayRandomKey[a] = temp;
//        }
        List<Keyboard.Key> pKeyLis = pLatinKeyboard.getKeys();
        int index = 0;
        int sy = 0;
//        int sy = mHeightPixels-80*5-8*4;//D20是60和6，D1000是80和8
//        Tip.i("sy = "+sy);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < pKeyLis.size(); i++) {
            if(i == 0){
                sy = mHeightPixels-pKeyLis.get(i).height*5-pKeyLis.get(i).x*6;//动态计算间隔值
                TRACE.i("sy = "+sy);
            }
            int code = pKeyLis.get(i).codes[0];
            TRACE.i("x: "+pKeyLis.get(i).x+" y: "+pKeyLis.get(i).y);
            int y = sy + pKeyLis.get(i).y;
            int x = pKeyLis.get(i).x;
            int rit = x + pKeyLis.get(i).width;
            int riby = y + pKeyLis.get(i).height;
            TRACE.i("xy ="+y);
//            TRACE.e("wi: "+pKeyLis.get(i).width+" he: "+pKeyLis.get(i).height);
            String label;
            if (code >= 0) {//number 值
                pKeyLis.get(i).label = ayRandomKey[index] + "";
                pKeyLis.get(i).codes[0] = 48 + ayRandomKey[index];
                TRACE.i("label = "+ayRandomKey[index]);
                String locationStr = QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(ayRandomKey[index]))+QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(x))+QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(y))
                        +QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(rit)) +QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(riby));
                s.append(locationStr);
                index++;
            }else{
                if(code == -3){
                    label = QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(13));
                }else if(code == -4){
                    label = QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(15));
                }else{
                    label = QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(14));
                }
                String locationStr = label +QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(x))+QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(y))
                        +QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(rit)) +QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(riby));
                s.append(locationStr);
            }
        }
        TRACE.i("s = "+s.toString());
        keyBoardNumInterface.getNumberValue(s.toString());
    }

    public static void setKeyBoardListener(KeyBoardNumInterface mkeyBoardNumInterface){
        keyBoardNumInterface = mkeyBoardNumInterface;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (keyBoardType == KEYBOARDTYPE_Only_Num_Pwd) {//纯数字键盘，删除按钮特殊绘制
            List<Keyboard.Key> keys = getKeyboard().getKeys();
            for (Keyboard.Key key : keys) {
                if (key.codes[0] == -5) {//删除按钮
                    Drawable dr = getContext().getResources().getDrawable(R.drawable
                            .keyboard_white);
                    dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                    dr.draw(canvas);
                    int drawableX = key.x + (key.width - key.icon.getIntrinsicWidth()) / 2;
                    int drawableY = key.y + (key.height - key.icon.getIntrinsicHeight()) / 2;
                    key.icon.setBounds(drawableX, drawableY, drawableX + key.icon
                            .getIntrinsicWidth(), drawableY + key.icon.getIntrinsicHeight());
                    key.icon.draw(canvas);
                    Log.i("test","drawableX: " +drawableX+" drawableY: "+drawableY);
                }
//                Log.i("test","x: " +key.x+" y: "+key.y+" wi:"+key.width+" he:"+key.height);

            }
        }
    }
}
