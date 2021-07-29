package com.dspread.demoui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.demoui.injectKey.DukptKeys;
import com.dspread.demoui.injectKey.Envelope;
import com.dspread.demoui.injectKey.Poskeys;
import com.dspread.demoui.injectKey.RSA;
import com.dspread.demoui.injectKey.TMKKey;
import com.dspread.demoui.task.CardExistQueryThread;
import com.dspread.demoui.utils.ConfigUtil;
import com.dspread.demoui.utils.FileUtils;
import com.dspread.demoui.utils.TLV;
import com.dspread.demoui.utils.TLVParser;
import com.dspread.demoui.xmlparse.BaseTag;
import com.dspread.demoui.xmlparse.SAXParserHandler;
import com.dspread.demoui.xmlparse.TagApp;
import com.dspread.demoui.xmlparse.TagCapk;
import com.dspread.xpos.CQPOSService;

import com.dspread.xpos.QPOSService;
import com.dspread.xpos.QPOSService.CommunicationMode;
import com.dspread.xpos.QPOSService.Display;
import com.dspread.xpos.QPOSService.DoTradeResult;
import com.dspread.xpos.QPOSService.DoTransactionType;
import com.dspread.xpos.QPOSService.EMVDataOperation;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.LcdModeAlign;
import com.dspread.xpos.QPOSService.TransactionResult;
import com.dspread.xpos.QPOSService.TransactionType;
import com.dspread.xpos.QPOSService.UpdateInformationResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

public class MainActivity extends Activity {

    private Button doTradeButton, serialBtn;
    private Button operateCardBtn;
    private Spinner cmdSp;
    private EditText amountEditText;
    private EditText statusEditText, blockAdd, status;
    private ListView appListView;
    private LinearLayout mafireLi, mafireUL;
    private Dialog dialog;
    private String nfcLog = "";
    private Spinner mafireSpinner;
    private Button pollBtn, pollULbtn, veriftBtn, veriftULBtn, readBtn, writeBtn, finishBtn, finishULBtn, getULBtn, readULBtn, fastReadUL, writeULBtn, transferBtn;
    private Button btnUSB;
    private Button btnQuickEMV;
    private Button btnEMVTest;
    private Button btnEMVAuto;
    private Button btnQuickEMVtrade;
    private Button btnBT;
    private Button btnDisconnect;
    private Button testMifare, doTradeA27Button;
    private EditText etApdu;
    private EditText mKeyIndex;
    private EditText mhipStatus;
    private Button updateFwBtn;
    private LinearLayout lin;
    //    private GridLayout grid;
    private QPOSService pos;
    private UpdateThread updateThread;
    private String pubModel;
    private String amount = "";
    private String cashbackAmount = "";
    private boolean isPinCanceled = false;
    private String blueTootchAddress = "";
    private boolean isUart = true;
    private boolean isPosComm = false;
    private boolean isQuickEmv = false;
    private int type;
    private UsbDevice usbDevice;
    private InnerListview m_ListView;
    private MyListViewAdapter m_Adapter = null;
    private ImageView imvAnimScan;
    private AnimationDrawable animScan;
    private List<BluetoothDevice> lstDevScanned = new ArrayList<>();
    private boolean isNormalBlu = false;//判断是否为普通蓝牙的标志
    private List<TagApp> appList;
    private static final int LOCATION_CODE = 101;
    private LocationManager lm;//【位置管理】
    private Boolean flag;//【位置管理】

    private void onBTPosSelected(Activity activity, View itemView, int index) {
        if (isNormalBlu) {
            pos.stopScanQPos2Mode();
        } else {
            pos.stopScanQposBLE();
        }
        start_time = System.currentTimeMillis();
        if (index == 0) {
            /*index for audio list*/
            open(CommunicationMode.AUDIO);
            posType = POS_TYPE.AUDIO;
            pos.openAudio();
        } else if (index == 1 && isUart) {
            /* COM portl list */
            open(CommunicationMode.UART);
            TRACE.d("+++++++UART");
            posType = POS_TYPE.UART;
            blueTootchAddress = "/dev/ttyS1";//使用串口，同方那边地址为/dev/ttyS1 ttyMT0
            pos.setDeviceAddress(blueTootchAddress);
            pos.openUart();
        } else {
            Map<String, ?> dev = (Map<String, ?>) m_Adapter.getItem(index);
            blueTootchAddress = (String) dev.get("ADDRESS");
            sendMsg(1001);
        }
    }

    protected List<Map<String, ?>> generateAdapterData() {
        if (isNormalBlu) {
            lstDevScanned = pos.getDeviceList();
//            TRCE.i("=====" + lstDevScanned.size());
        } else {
            lstDevScanned = pos.getBLElist();
        }
        TRACE.d("lstDevScanned----" + lstDevScanned);
        List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
        //
        Map<String, Object> itmAudio = new HashMap<String, Object>();
        itmAudio.put("ICON", Integer.valueOf(R.drawable.ic_headphones_on));
        itmAudio.put("TITLE", getResources().getString(R.string.audio));
        itmAudio.put("ADDRESS", getResources().getString(R.string.audio));

        data.add(itmAudio);

        if (isUart) {
            //
            Map<String, Object> itmSerialPort = new HashMap<String, Object>();
            itmSerialPort.put("ICON", Integer.valueOf(R.drawable.serialport));
            itmSerialPort.put("TITLE",
                    getResources().getString(R.string.serialport));
            itmSerialPort.put("ADDRESS",
                    getResources().getString(R.string.serialport));

            data.add(itmSerialPort);
            //
        }
//    if(lstDevScanned != null && lstDevScanned.size() > 0){
        for (BluetoothDevice dev : lstDevScanned) {
            if (dev.getName() == null) {
                continue;
            }
            Map<String, Object> itm = new HashMap<String, Object>();
            itm.put("ICON",
                    dev.getBondState() == BluetoothDevice.BOND_BONDED ? Integer
                            .valueOf(R.drawable.bluetooth_blue) : Integer
                            .valueOf(R.drawable.bluetooth_blue_unbond));
            itm.put("TITLE", dev.getName() + "(" + dev.getAddress() + ")");
            itm.put("ADDRESS", dev.getAddress());
            data.add(itm);
        }
//    }
        //
        return data;
    }

    private void refreshAdapter() {
        if (m_Adapter != null) {
            m_Adapter.clearData();
            m_Adapter = null;
        }
        List<Map<String, ?>> data = generateAdapterData();
        m_Adapter = new MyListViewAdapter(this, data);
        m_ListView.setAdapter(m_Adapter);
        //
        setListViewHeightBasedOnChildren(m_ListView);
    }

    private class MyListViewAdapter extends BaseAdapter {
        private List<Map<String, ?>> m_DataMap;
        private LayoutInflater m_Inflater;

        public void clearData() {
            m_DataMap.clear();
            // m_DataMap = null;
        }

        public void addData(Map<String, ?> map) {
            boolean a = false;
            for (int i = 0; i < m_DataMap.size(); i++) {
                TRACE.d("ADDRESS: " + map.get("ADDRESS"));
                if (map.get("ADDRESS").equals(m_DataMap.get(i).get("ADDRESS"))) {
//
                    a = true;
                    break;
                } else {
                    continue;
                }
            }
            if (!a) {
                m_DataMap.add(map);
            }

        }

        public MyListViewAdapter(Context context, List<Map<String, ?>> map) {
            this.m_DataMap = map;
            this.m_Inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return m_DataMap.size();
        }

        @Override
        public Object getItem(int position) {
            return m_DataMap.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (convertView == null) {
                convertView = m_Inflater.inflate(R.layout.bt_qpos_item, null);
            }
            ImageView m_Icon = (ImageView) convertView
                    .findViewById(R.id.item_iv_icon);
            TextView m_TitleName = (TextView) convertView
                    .findViewById(R.id.item_tv_lable);
            //
            Map<String, ?> itemdata = (Map<String, ?>) m_DataMap.get(position);
            int idIcon = (Integer) itemdata.get("ICON");
            String sTitleName = (String) itemdata.get("TITLE");
            //
            m_Icon.setBackgroundResource(idIcon);
            m_TitleName.setText(sTitleName);
            //
            return convertView;
        }
    }

    //设置listview的高度
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        ((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
        listView.setLayoutParams(params);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //当窗口为用户可见，保持设备常开，并保持亮度不变
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!isUart) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        setContentView(R.layout.activity_main);
        bluetoothRelaPer();  // local permission
        initView();
        initIntent();
        initListener();
    }

    private void initIntent() {
        Intent intent = getIntent();
        type = intent.getIntExtra("connect_type", 0);
        switch (type) {
            case 1:
                btnBT.setVisibility(View.GONE);
                open(CommunicationMode.AUDIO);
                posType = POS_TYPE.AUDIO;
                pos.openAudio();
                break;
            case 2:
                btnBT.setVisibility(View.GONE);
                serialBtn.setVisibility(View.VISIBLE);
                serialBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        open(CommunicationMode.UART);
                        TRACE.d("+++++++UART");
                        posType = POS_TYPE.UART;
//                        blueTootchAddress = "/dev/ttyMT0";//同方那边是s1，天波是s3
                        blueTootchAddress = "/dev/ttyS1";//D20
//                        blueTootchAddress = "/dev/ttyHSL1";//D1000
                        pos.setDeviceAddress(blueTootchAddress);
                        pos.openUart();
                    }
                });
                break;
            case 3://普通蓝牙
                btnBT.setVisibility(View.VISIBLE);
                this.isNormalBlu = true;
                break;
            case 4://其他蓝牙
                btnBT.setVisibility(View.VISIBLE);
                isNormalBlu = false;
                break;
        }
    }

    /**
     * 开始连接前上电，断开连接后下电
     * test the D20 device powerOn and powerOff
     *
     * @param path  \sys\devices\platform\charger\sp_ctrl
     * @param value 0 powerOff. 1 powerOn
     */
    private void d20Write(String path, int value) {
        try {
            java.io.File file = new java.io.File(path);
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write(String.valueOf(value));
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bluetoothRelaPer() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {//表示蓝牙不可用
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enabler);
        }
        lm = (LocationManager) MainActivity.this.getSystemService(MainActivity.this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("POS_SDK", "没有权限");
                // 没有权限，申请权限。
                // 申请授权。
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
//                        Toast.makeText(getActivity(), "没有权限", Toast.LENGTH_SHORT).show();

            } else {

                // 有权限了，去放肆吧。
                Toast.makeText(MainActivity.this, "有权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("BRG", "系统检测到未开启GPS定位服务");
            Toast.makeText(MainActivity.this, "系统检测到未开启GPS定位服务", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);

        }
    }

    private void initView() {
        lin = findViewById(R.id.lin);
        doTradeA27Button = findViewById(R.id.doTradeA27Button);
        doTradeA27Button.setEnabled(false);
        testMifare = findViewById(R.id.testMifare);
        imvAnimScan = (ImageView) findViewById(R.id.img_anim_scanbt);
        animScan = (AnimationDrawable) getResources().getDrawable(
                R.drawable.progressanmi);
        imvAnimScan.setBackgroundDrawable(animScan);
        mafireLi = (LinearLayout) findViewById(R.id.mifareid);
        mafireUL = (LinearLayout) findViewById(R.id.ul_ll);
        status = (EditText) findViewById(R.id.status);
        operateCardBtn = (Button) findViewById(R.id.operate_card);
        updateFwBtn = (Button) findViewById(R.id.updateFW);
        cmdSp = (Spinner) findViewById(R.id.cmd_spinner);

        String[] cmdList = new String[]{"add", "reduce", "restore"};
        ArrayAdapter<String> cmdAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, cmdList);
        cmdSp.setAdapter(cmdAdapter);
        mafireSpinner = (Spinner) findViewById(R.id.verift_spinner);
        blockAdd = (EditText) findViewById(R.id.block_address);
        String[] keyClass = new String[]{"Key A", "Key B"};
        ArrayAdapter<String> spinneradapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, keyClass);
        mafireSpinner.setAdapter(spinneradapter);
        doTradeButton = (Button) findViewById(R.id.doTradeButton);//开始交易
        serialBtn = (Button) findViewById(R.id.serialPort);
        amountEditText = (EditText) findViewById(R.id.amountEditText);
        statusEditText = (EditText) findViewById(R.id.statusEditText);
        btnBT = (Button) findViewById(R.id.btnBT);//选择设备开始扫描按钮
        btnUSB = (Button) findViewById(R.id.btnUSB);//扫描USB设备
        btnDisconnect = (Button) findViewById(R.id.disconnect);//断开连接

        btnQuickEMV = (Button) findViewById(R.id.btnQuickEMV);//隐藏按钮
        btnEMVTest = (Button) findViewById(R.id.btnEMVTest);//隐藏按钮
        btnEMVAuto = (Button) findViewById(R.id.btnAutoEMV);//隐藏按钮
        btnQuickEMVtrade = (Button) findViewById(R.id.btnQuickEMVtrade);
        pollBtn = (Button) findViewById(R.id.search_card);
        pollULbtn = (Button) findViewById(R.id.poll_ulcard);
        veriftBtn = (Button) findViewById(R.id.verify_card);
        veriftULBtn = (Button) findViewById(R.id.verify_ulcard);
        readBtn = (Button) findViewById(R.id.read_card);
        writeBtn = (Button) findViewById(R.id.write_card);
        finishBtn = (Button) findViewById(R.id.finish_card);
        finishULBtn = (Button) findViewById(R.id.finish_ulcard);
        getULBtn = (Button) findViewById(R.id.get_ul);
        readULBtn = (Button) findViewById(R.id.read_ulcard);
        fastReadUL = (Button) findViewById(R.id.fast_read_ul);
        writeULBtn = (Button) findViewById(R.id.write_ul);
        transferBtn = (Button) findViewById(R.id.transfer_card);
        ScrollView parentScrollView = (ScrollView) findViewById(R.id.parentScrollview);
        parentScrollView.smoothScrollTo(0, 0);
        m_ListView = (InnerListview) findViewById(R.id.lv_indicator_BTPOS);
        m_ListView.setAdapter(m_Adapter);
        mKeyIndex = ((EditText) findViewById(R.id.keyindex));
        mhipStatus = ((EditText) findViewById(R.id.chipStatus));
        etApdu = ((EditText) findViewById(R.id.et_apdu));
//        grid = findViewById(R.id.grid);
    }

    // View宽，高
    public String getLocation(View v, int i) {
        int[] loc = new int[4];
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        loc[0] = location[0];
        loc[1] = location[1];
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(w, h);

        loc[2] = v.getMeasuredWidth();
        loc[3] = v.getMeasuredHeight();
        int x = loc[0];
        int y = loc[1];

        int we = loc[2];
        int he = loc[3];

//        x = x + (i % 4) * we;
//        if(i >= 4){
//            y = y + he;
//        }else if(i > 7){
//            y = y + 2 * he;
//        }else{
//            y = y + 3 * he;
//        }
        int leb = y + he;
        int rit = x + we;
        int riby = y + he;

        Log.w("test ", v.getX() + " " + v.getY() + " " + v.getRotationX() + " " + v.getBottom());

        //base = computeWH();
        Log.i("test ", "left top(" + x + ", " + y + ") left bottom(" + x + ", " + leb + ") " + "right top(" + rit + ", " + y + ") right bottom(" + rit + ", " + riby + ")");
        String locationStr = "";
        return locationStr;
    }

    /**
     *
     */
    private void initListener() {
        m_ListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onBTPosSelected(MainActivity.this, view, position);
                m_ListView.setVisibility(View.GONE);
                animScan.stop();
                imvAnimScan.setVisibility(View.GONE);
            }
        });
        MyOnClickListener myOnClickListener = new MyOnClickListener();
        //以下是按钮的点击事件
        doTradeButton.setOnClickListener(myOnClickListener);//开始
        btnBT.setOnClickListener(myOnClickListener);
        btnDisconnect.setOnClickListener(myOnClickListener);
        btnUSB.setOnClickListener(myOnClickListener);
        testMifare.setOnClickListener(myOnClickListener);
        updateFwBtn.setOnClickListener(myOnClickListener);
        btnQuickEMV.setOnClickListener(myOnClickListener);
        btnEMVTest.setOnClickListener(myOnClickListener);
        btnEMVAuto.setOnClickListener(myOnClickListener);
        btnQuickEMVtrade.setOnClickListener(myOnClickListener);
        pollBtn.setOnClickListener(myOnClickListener);
        pollULbtn.setOnClickListener(myOnClickListener);
        finishBtn.setOnClickListener(myOnClickListener);
        finishULBtn.setOnClickListener(myOnClickListener);
        readBtn.setOnClickListener(myOnClickListener);
        writeBtn.setOnClickListener(myOnClickListener);
        veriftBtn.setOnClickListener(myOnClickListener);
        veriftULBtn.setOnClickListener(myOnClickListener);
        operateCardBtn.setOnClickListener(myOnClickListener);
        getULBtn.setOnClickListener(myOnClickListener);
        readULBtn.setOnClickListener(myOnClickListener);
        fastReadUL.setOnClickListener(myOnClickListener);
        writeULBtn.setOnClickListener(myOnClickListener);
        transferBtn.setOnClickListener(myOnClickListener);
        doTradeA27Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos != null) {
                    pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD);
                    pos.doTrade(30);
                }
            }
        });

        ((Button) findViewById(R.id.generateTransportKey)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos != null) {
                   pos.generateTransportKey(30);
                }
            }
        });

        ((Button) findViewById(R.id.updateIPEKByTransportKey)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos != null) {
                    statusEditText.setText("update ipek...");
                    TRACE.d("ipekgroup：" + "00" + "ipekgroup：" + "09118012400705E00000" +
                            "trackksn：" + "09118012400705E00000" + "trackipek：" + "1FF77D9FF1BA12671FF77D9FF1BA1267" + "trackipekcheckvalue：" + "82E13665B4624DF5" +
                            "emvksn：" + "09118012400705E00000" + "emvipek：" + "1FF77D9FF1BA12671FF77D9FF1BA1267" + "emvipekcheckvalue：" + "82E13665B4624DF5" +
                            "pinksn：" + "09118012400705E00000" + "pinipek：" + "1FF77D9FF1BA12671FF77D9FF1BA1267" + "pinipekcheckvalue：" + "82E13665B4624DF5");
                    pos.updateIPEKByTransportKey("00","09118012400705E00000","B7EB4B27B887A5A7B7EB4B27B887A5A7","82E13665B4624DF5",
                    "09118012400705E00000","B7EB4B27B887A5A7B7EB4B27B887A5A7","82E13665B4624DF5",
                    "09118012400705E00000","B7EB4B27B887A5A7B7EB4B27B887A5A7","82E13665B4624DF5");
                }
            }
        });

        ((Button) findViewById(R.id.updateWorkKeyByTransportKey)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos != null) {
                    statusEditText.setText("update work key...");
                    TRACE.d("pinKey：" + "42F53F7DE22B029842F53F7DE22B0298" + "pinKeyCheckValue：" + "82E13665B4624DF5" +
                            "trackKey：" + "42F53F7DE22B029842F53F7DE22B0298" + "trackKeyCheckValue：" + "82E13665B4624DF5" +
                            "macey：" + "42F53F7DE22B029842F53F7DE22B0298" + "macKeyCheckValue：" + "82E13665B4624DF5");
                    pos.updateWorkKeyByTransportKey("162D708C906FEF23162D708C906FEF23","82E13665B4624DF5",
                           "162D708C906FEF23162D708C906FEF23","82E13665B4624DF5",
                            "162D708C906FEF23162D708C906FEF23","82E13665B4624DF5",0, 30);
                }
            }
        });

    }

    private Handler conHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1)
                Toast.makeText(MainActivity.this, "connect success", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "connect failed", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void postponeEnterTransition() {
        super.postponeEnterTransition();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限被用户同意。
                    Toast.makeText(MainActivity.this, "定位权限被tongyi！", Toast.LENGTH_LONG).show();
                } else {
                    // 权限被用户拒绝了。
                    Toast.makeText(MainActivity.this, "定位权限被禁止，相关地图功能无法使用！", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    private POS_TYPE posType = POS_TYPE.BLUETOOTH;

    private enum POS_TYPE {
        BLUETOOTH, AUDIO, UART, USB, OTG, BLUETOOTH_BLE
    }

    /**
     * 打开设备，获取类对象，开始监听
     *
     * @param mode
     */
    private void open(CommunicationMode mode) {
        TRACE.d("open");
        //pos=null;
        MyPosListener listener = new MyPosListener();
        //实现类的单例模式
        pos = QPOSService.getInstance(mode);
        pos.setConext(MainActivity.this);
        if (pos == null) {
            statusEditText.setText("CommunicationMode unknow");
            return;
        }
        if (mode == CommunicationMode.USB_OTG_CDC_ACM) {
            pos.setUsbSerialDriver(QPOSService.UsbOTGDriver.CDCACM);
        }
        //通过handler处理，监听MyPosListener，实现QposService的接口，（回调接口）
        Handler handler = new Handler(Looper.myLooper());
        pos.initListener(handler, listener);
        String sdkVersion = pos.getSdkVersion();
        Toast.makeText(MainActivity.this, "sdkVersion--" + sdkVersion, Toast.LENGTH_SHORT).show();
    }

    /**
     * 关闭设备
     */
    private void close() {
        TRACE.d("close");
        if (pos == null) {
            return;
        } else if (posType == POS_TYPE.AUDIO) {
            pos.closeAudio();
        } else if (posType == POS_TYPE.BLUETOOTH) {
            pos.disconnectBT();
        } else if (posType == POS_TYPE.BLUETOOTH_BLE) {
            pos.disconnectBLE();
        } else if (posType == POS_TYPE.UART) {
            pos.closeUart();
        } else if (posType == POS_TYPE.USB) {
            pos.closeUsb();
        } else if (posType == POS_TYPE.OTG) {
            pos.closeUsb();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // MenuItem audioitem = menu.findItem(R.id.audio_test);
        if (pos != null) {
            if (pos.getAudioControl()) {
//				audioitem.setTitle("音效控制:打开");
                audioitem.setTitle(R.string.audio_open);
            } else {
//				audioitem.setTitle("音效控制:关闭");
                audioitem.setTitle(R.string.audio_close);
            }
        } else {
//			audioitem.setTitle("音效控制:未知");
            audioitem.setTitle(R.string.audio_unknow);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    MenuItem audioitem = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        audioitem = menu.findItem(R.id.audio_test);
        if (pos != null) {
            if (pos.getAudioControl()) {
                audioitem.setTitle("音效控制:打开");
            } else {
                audioitem.setTitle("音效控制:关闭");
            }
        } else {
            audioitem.setTitle("音效控制:点击查看");
        }
        return true;
    }

    class UpdateThread extends Thread {

        private boolean concelFlag = false;

        @Override
        public void run() {
            while (!concelFlag) {
                int i = 0;
                while (!concelFlag && i < 100) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    i++;
                }
                if (concelFlag) {
                    break;
                }
                if (pos == null) {
                    return;
                }
                final int progress = pos.getUpdateProgress();
                if (progress < 100) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusEditText.setText(progress + "%");
                        }
                    });
                    continue;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusEditText.setText("升级完成" + "%");
                    }
                });

                break;
            }
        }

        public void concelSelf() {
            concelFlag = true;
        }
    }

    /**
     * 菜单栏的点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (pos == null) {
            Toast.makeText(getApplicationContext(), "设备未连接", Toast.LENGTH_LONG).show();
            return true;
        } else if (item.getItemId() == R.id.reset_qpos) {
            boolean a = pos.resetPosStatus();
            pos.cancelTrade();
//            boolean a = pos.resetQPOS();
            if (a) {
                statusEditText.setText("pos reset");
            }
        } else if (item.getItemId() == R.id.get_ksn) {
            pos.getKsn();

        } else if (item.getItemId() == R.id.getEncryptData) {
            //获得加密数据
            //第一个参数是需要加密的数据, 第二个参数: 1代表 track key 3des加密, 2代表track key aes加密. 第三个参数是key索引
            pos.getEncryptData("70563".getBytes(), "2", "0", 10);

        } else if (item.getItemId() == R.id.addKsn) {
            pos.addKsn("00");
        } else if (item.getItemId() == R.id.doTradeLogOperation) {
            pos.doTradeLogOperation(DoTransactionType.GetAll, 0);
        } else if (item.getItemId() == R.id.injectKeys) {//inject key
            resetIpekFlag = true;
            resetIpekFlag = false;
            pos.getDevicePublicKey(5);
        } else if (item.getItemId() == R.id.get_update_key) {//get the key value
            pos.getUpdateCheckValue();

        } else if (item.getItemId() == R.id.get_device_public_key) {//get the key value

            pos.getDevicePublicKey(5);
        } else if (item.getItemId() == R.id.set_sleepmode_time) {//设置设备睡眠时间
//            0~Integer.MAX_VALUE

            pos.setSleepModeTime(20);//the time is in 10s and 10000s
        } else if (item.getItemId() == R.id.set_shutdowm_time) {
            pos.setShutDownTime(15 * 60);
        }
        //更新ipek
        else if (item.getItemId() == R.id.updateIPEK) {
            int keyIndex = getKeyIndex();
            String ipekGrop = "0" + keyIndex;


//			KSN: 09118012400705E00000
//			IPEK: 1501ECCD4B0A7C6E81D4D603C4E47CDA
//			CheckValue: 0000000000000000

            pos.doUpdateIPEKOperation(
                    ipekGrop, "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944",
                    "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944",
                    "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944");
        } else if (item.getItemId() == R.id.getSleepTime) {
            pos.getShutDownTime();
        } else if (item.getItemId() == R.id.getQuickEmvStatus) {
            pos.getQuickEMVStatus(EMVDataOperation.getEmv, "9F061000000000000000000000000000000000");
        } else if (item.getItemId() == R.id.setQuickEmvStatus) {
            pos.setQuickEmvStatus(true);
        } else if (item.getItemId() == R.id.updateEMVAPP) {
            statusEditText.setText("updating emvapp...");
//            sendMsg(1701);
        } else if (item.getItemId() == R.id.updateEMVCAPK) {
            statusEditText.setText("updating emvcapk...");
//            sendMsg(1702);
        } else if (item.getItemId() == R.id.audio_test) {
            if (pos.getAudioControl()) {
                pos.setAudioControl(false);
                item.setTitle("音效控制:关闭");
            } else {
                pos.setAudioControl(true);
                item.setTitle("音效控制:打开");
            }
        } else if (item.getItemId() == R.id.about) {
            statusEditText.setText("SDK版本：" + pos.getSdkVersion());
        } else if (item.getItemId() == R.id.setBuzzer) {
            pos.doSetBuzzerOperation(3);//显示设置蜂鸣器响3次
//            pos.testPosFunctionCommand(20, QPOSService.TestCommand.LED_TEST_STOP);
        } else if (item.getItemId() == R.id.menu_get_deivce_info) {
            statusEditText.setText(R.string.getting_info);
            pos.getQposInfo();
        } else if (item.getItemId() == R.id.menu_get_deivce_key_checkvalue) {
            statusEditText.setText("get_deivce_key_checkvalue..............");
            int keyIdex = getKeyIndex();
            pos.getKeyCheckValue(0, QPOSService.CHECKVALUE_KEYTYPE.DUKPT_MKSK_ALLTYPE);
        } else if (item.getItemId() == R.id.menu_get_pos_id) {
            pos.getQposId();
            statusEditText.setText(R.string.getting_pos_id);
        } else if (item.getItemId() == R.id.setMasterkey) {
            //key:0123456789ABCDEFFEDCBA9876543210
            //result；0123456789ABCDEFFEDCBA9876543210
            int keyIndex = getKeyIndex();
            pos.setMasterKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", keyIndex);
        } else if (item.getItemId() == R.id.menu_get_pin) {
            statusEditText.setText(R.string.input_pin);
            pos.getPin(1, 0, 6, "please input pin", "622262XXXXXXXXX4406", "", 20);
        } else if (item.getItemId() == R.id.isCardExist) {
            pos.isCardExist(30);
        } else if (item.getItemId() == R.id.menu_operate_mafire) {
            showSingleChoiceDialog();
        } else if (item.getItemId() == R.id.menu_operate_update) {
            if (updateFwBtn.getVisibility() == View.VISIBLE || btnQuickEMV.getVisibility() == View.VISIBLE ||
                    btnEMVTest.getVisibility() == View.VISIBLE || btnEMVAuto.getVisibility() == View.VISIBLE) {
                updateFwBtn.setVisibility(View.GONE);
                btnQuickEMV.setVisibility(View.GONE);
                btnEMVTest.setVisibility(View.GONE);
                btnEMVAuto.setVisibility(View.GONE);
            } else {
                updateFwBtn.setVisibility(View.VISIBLE);
                btnQuickEMV.setVisibility(View.VISIBLE);
                btnEMVTest.setVisibility(View.VISIBLE);
                btnEMVAuto.setVisibility(View.VISIBLE);
            }
        } else if (item.getItemId() == R.id.resetMasterKey) {
            resetMasterKeyFlag = true;
            resetIpekFlag = false;
            pos.getDevicePublicKey(5);
        } else if (item.getItemId() == R.id.resetSessionKey) {
            //key：0123456789ABCDEFFEDCBA9876543210
            //result：0123456789ABCDEFFEDCBA9876543210
            int keyIndex = getKeyIndex();

//            pos.updateWorkKey(
//                    "9B3A7B883A100F739B3A7B883A100F73", "82E13665B4624DF5",//PIN KEY
//                    "9B3A7B883A100F739B3A7B883A100F73", "82E13665B4624DF5",  //TRACK KEY
//                    "9B3A7B883A100F739B3A7B883A100F73", "82E13665B4624DF5", //MAC KEY
//                    keyIndex, 5);
        } else if (item.getItemId() == R.id.resetIpek) {
            resetIpekFlag = true;
            resetMasterKeyFlag = false;
            pos.getDevicePublicKey(5);

        } else if (item.getItemId() == R.id.cusDisplay) {
            deviceShowDisplay("test info");
        } else if (item.getItemId() == R.id.closeDisplay) {
            pos.lcdShowCloseDisplay();
        }
        return true;
    }

    private boolean resetIpekFlag;
    private boolean resetMasterKeyFlag;


    @Override
    public void onPause() {
        super.onPause();
        TRACE.d("onPause");
        if (type == 3 || type == 4) {
            if (pos != null) {
                if (isNormalBlu) {
                    //停止扫描普通蓝牙
                    pos.stopScanQPos2Mode();
                } else {
                    //停止扫描ble的蓝牙
                    pos.stopScanQposBLE();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        TRACE.d("onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TRACE.d("onDestroy");
//        d20Write("/sys/devices/platform/charger/sp_power_ctrl",0);
        if (updateThread != null) {
            updateThread.concelSelf();
        }
        if (cardExistQueryThread != null) {
            cardExistQueryThread.concelSelf();
        }
        if (pos != null) {
            close();
            pos = null;
        }
    }

    private int yourChoice = 0;

    private void showSingleChoiceDialog() {
        final String[] items = {"Mifare classic 1", "Mifare UL"};
//	    yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(MainActivity.this);
        singleChoiceDialog.setTitle("please select one");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (yourChoice == 0) {
                            mafireLi.setVisibility(View.VISIBLE);//display m1 mafire card
                            mafireUL.setVisibility(View.GONE);//display ul mafire card
                        } else if (yourChoice == 1) {
                            mafireLi.setVisibility(View.GONE);
                            mafireUL.setVisibility(View.VISIBLE);
                        }
                    }
                });
        singleChoiceDialog.show();
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public void onRequestSendCVV(){
        TRACE.d("onRequestSendCVV()");
        dismissDialog();
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.cvv_dialog);
        dialog.setTitle("Please enter cvv");
        dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String cvv = ((EditText) dialog.findViewById(R.id.cvvEditText)).getText().toString();
                if (cvv != null && cvv.length() > 0) {
                    TRACE.d("send cvv to pos");
                    Boolean result = pos.sendCVV(cvv);
                    dismissDialog();
                    if (result) {
                        TRACE.d("get encrypted data block");
                        Hashtable hashtable = pos.getEncryptedDataBlock(0);
                        TRACE.d("hashtable: " + hashtable);
                    }else{
                        TRACE.d("send cvv fail");
                    }
                }
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });
        dialog.show();
    }

    public void onRequestGoOnline() {
                    dismissDialog();
            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.request_data_to_server);
            if (isPinCanceled) {
                ((TextView) dialog.findViewById(R.id.messageTextView))
                        .setText(R.string.replied_failed);
            } else {
                ((TextView) dialog.findViewById(R.id.messageTextView))
                        .setText(R.string.replied_success);
            }
            dialog.findViewById(R.id.confirmButton).setOnClickListener(
                    new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (isPinCanceled) {
                                pos.sendOnlineProcessResult(null);
                            } else {
                                String str = "8A023030";//Currently the default value,
//                                     should be assigned to the server to return data,
//                                     the data format is TLV
                                pos.sendOnlineProcessResult(str);//脚本通知/55域/ICCDATA
                            }
                            dismissDialog();
                        }
                    });

            dialog.show();
    }

    private List<String> keyBoardList = new ArrayList<>();
    private int index = 0;
    private String key = "";

    /**
     * @author qianmengChen
     * @ClassName: MyPosListener
     * @Function: TODO ADD FUNCTION
     * @date: 2016-11-10 下午6:35:06
     */
    class MyPosListener extends CQPOSService {
        private boolean isSuccess;
        private String result;

        @Override
        public void onRequestGenerateTransportKey(Hashtable result) {
            TRACE.d("onRequestGenerateTransportKey result:" + result);
            try {
                InputStream priopen = getAssets().open("rsa_private_pkcs8_1024.pem");
                RSA rsa = new RSA();
                rsa.loadPrivateKey(priopen);
                byte[] bytes = rsa.decrypt(QPOSUtil.HexStringToByteArray((String) result.get("transportKey")));
                TRACE.d("decrypted result: " + QPOSUtil.byteArray2Hex(bytes));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onReturnRsaResult(String data) {
            statusEditText.setText("result is ：" + data);
        }

        @Override
        public void onReturnAESTransmissonKeyResult(boolean isSuccess, String result) {
            key = result.substring(0, 64);
            TRACE.d("onReturnAESTransmissonKeyResult result:" + result);
            if (isSuccess) {
                statusEditText.setText("get AES key success, result: " + result);
            } else {
                statusEditText.setText("get AES key fail, result: " + result);
            }
        }

        @Override
        public void onReturnSetAESResult(boolean isSuccess, String result) {
            TRACE.d("onReturnSetAESResult result:" + result);
            if (isSuccess) {
                statusEditText.setText("set AES key success");
            } else {
                statusEditText.setText("set AES key fail");
            }
        }

        @Override
        public void onRequestWaitingUser() {//等待卡片
            TRACE.d("onRequestWaitingUser()");
            dismissDialog();
            statusEditText.setText(getString(R.string.waiting_for_card));
        }

        /**
         * 返回选择的开始，返回交易的结果
         */
        @Override
        public void onDoTradeResult(DoTradeResult result, Hashtable<String, String> decodeData) {
            TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
            dismissDialog();
            String cardNo = "";
            if (result == DoTradeResult.NONE) {
                statusEditText.setText(getString(R.string.no_card_detected));
            } else if (result == DoTradeResult.ICC) {
                statusEditText.setText(getString(R.string.icc_card_inserted));
                TRACE.d("EMV ICC Start");
                pos.doEmvApp(EmvOption.START);
            } else if (result == DoTradeResult.NOT_ICC) {
                statusEditText.setText(getString(R.string.card_inserted));
                pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
                pos.doCheckCard();
            } else if (result == DoTradeResult.BAD_SWIPE) {
                statusEditText.setText(getString(R.string.bad_swipe));
            } else if (result == DoTradeResult.CARD_NOT_SUPPORT) {
                statusEditText.setText("GPO NOT SUPPORT");
            } else if (result == DoTradeResult.PLS_SEE_PHONE) {
                statusEditText.setText("PLS SEE PHONE");
            } else if (result == DoTradeResult.MCR) {//磁条卡
                String content = getString(R.string.card_swiped);
                String formatID = decodeData.get("formatID");
                if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17") || formatID.equals("11") || formatID.equals("10")) {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String trackblock = decodeData.get("trackblock");
                    String psamId = decodeData.get("psamId");
                    String posId = decodeData.get("posId");
                    String pinblock = decodeData.get("pinblock");
                    String macblock = decodeData.get("macblock");
                    String activateCode = decodeData.get("activateCode");
                    String trackRandomNumber = decodeData.get("trackRandomNumber");

                    content += getString(R.string.format_id) + " " + formatID + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                    content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
                    content += getString(R.string.service_code) + " " + serviceCode + "\n";
                    content += "trackblock: " + trackblock + "\n";
                    content += "psamId: " + psamId + "\n";
                    content += "posId: " + posId + "\n";
                    content += getString(R.string.pinBlock) + " " + pinblock + "\n";
                    content += "macblock: " + macblock + "\n";
                    content += "activateCode: " + activateCode + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    cardNo = maskedPAN;
                } else if (formatID.equals("FF")) {
                    String type = decodeData.get("type");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    content += "cardType:" + " " + type + "\n";
                    content += "track_1:" + " " + encTrack1 + "\n";
                    content += "track_2:" + " " + encTrack2 + "\n";
                    content += "track_3:" + " " + encTrack3 + "\n";
                } else {
                    String orderID = decodeData.get("orderId");
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
//					String ksn = decodeData.get("ksn");
                    String serviceCode = decodeData.get("serviceCode");
                    String track1Length = decodeData.get("track1Length");
                    String track2Length = decodeData.get("track2Length");
                    String track3Length = decodeData.get("track3Length");
                    String encTracks = decodeData.get("encTracks");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    String partialTrack = decodeData.get("partialTrack");
                    // TODO
                    String pinKsn = decodeData.get("pinKsn");
                    String trackksn = decodeData.get("trackksn");
                    String pinBlock = decodeData.get("pinBlock");
                    String encPAN = decodeData.get("encPAN");
                    String trackRandomNumber = decodeData.get("trackRandomNumber");
                    String pinRandomNumber = decodeData.get("pinRandomNumber");
                    if (orderID != null && !"".equals(orderID)) {
                        content += "orderID:" + orderID;
                    }
                    content += getString(R.string.format_id) + " " + formatID + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                    content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
//					content += getString(R.string.ksn) + " " + ksn + "\n";
                    content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
                    content += getString(R.string.trackksn) + " " + trackksn + "\n";
                    content += getString(R.string.service_code) + " " + serviceCode + "\n";
                    content += getString(R.string.track_1_length) + " " + track1Length + "\n";
                    content += getString(R.string.track_2_length) + " " + track2Length + "\n";
                    content += getString(R.string.track_3_length) + " " + track3Length + "\n";
                    content += getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
                    content += getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
                    content += getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
                    content += getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
                    content += getString(R.string.partial_track) + " " + partialTrack + "\n";
                    content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
                    content += "encPAN: " + encPAN + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    content += "pinRandomNumber:" + " " + pinRandomNumber + "\n";
                    cardNo = maskedPAN;
                    String realPan = null;
                }
                statusEditText.setText(content);
                onRequestSendCVV();
            } else if ((result == DoTradeResult.NFC_ONLINE) || (result == DoTradeResult.NFC_OFFLINE)) {
                nfcLog = decodeData.get("nfcLog");
                String content = getString(R.string.tap_card);
                String formatID = decodeData.get("formatID");
                if (formatID.equals("31") || formatID.equals("40")
                        || formatID.equals("37") || formatID.equals("17")
                        || formatID.equals("11") || formatID.equals("10")) {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String trackblock = decodeData.get("trackblock");
                    String psamId = decodeData.get("psamId");
                    String posId = decodeData.get("posId");
                    String pinblock = decodeData.get("pinblock");
                    String macblock = decodeData.get("macblock");
                    String activateCode = decodeData.get("activateCode");
                    String trackRandomNumber = decodeData
                            .get("trackRandomNumber");

                    content += getString(R.string.format_id) + " " + formatID
                            + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN
                            + "\n";
                    content += getString(R.string.expiry_date) + " "
                            + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " "
                            + cardHolderName + "\n";

                    content += getString(R.string.service_code) + " "
                            + serviceCode + "\n";
                    content += "trackblock: " + trackblock + "\n";
                    content += "psamId: " + psamId + "\n";
                    content += "posId: " + posId + "\n";
                    content += getString(R.string.pinBlock) + " " + pinblock
                            + "\n";
                    content += "macblock: " + macblock + "\n";
                    content += "activateCode: " + activateCode + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    cardNo = maskedPAN;
                } else {

                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
//					String ksn = decodeData.get("ksn");
                    String serviceCode = decodeData.get("serviceCode");
                    String track1Length = decodeData.get("track1Length");
                    String track2Length = decodeData.get("track2Length");
                    String track3Length = decodeData.get("track3Length");
                    String encTracks = decodeData.get("encTracks");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    String partialTrack = decodeData.get("partialTrack");
                    // TODO
                    String pinKsn = decodeData.get("pinKsn");
                    String trackksn = decodeData.get("trackksn");
                    String pinBlock = decodeData.get("pinBlock");
                    String encPAN = decodeData.get("encPAN");
                    String trackRandomNumber = decodeData
                            .get("trackRandomNumber");
                    String pinRandomNumber = decodeData.get("pinRandomNumber");

                    content += getString(R.string.format_id) + " " + formatID
                            + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN
                            + "\n";
                    content += getString(R.string.expiry_date) + " "
                            + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " "
                            + cardHolderName + "\n";
//					content += getString(R.string.ksn) + " " + ksn + "\n";
                    content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
                    content += getString(R.string.trackksn) + " " + trackksn
                            + "\n";
                    content += getString(R.string.service_code) + " "
                            + serviceCode + "\n";
                    content += getString(R.string.track_1_length) + " "
                            + track1Length + "\n";
                    content += getString(R.string.track_2_length) + " "
                            + track2Length + "\n";
                    content += getString(R.string.track_3_length) + " "
                            + track3Length + "\n";
                    content += getString(R.string.encrypted_tracks) + " "
                            + encTracks + "\n";
                    content += getString(R.string.encrypted_track_1) + " "
                            + encTrack1 + "\n";
                    content += getString(R.string.encrypted_track_2) + " "
                            + encTrack2 + "\n";
                    content += getString(R.string.encrypted_track_3) + " "
                            + encTrack3 + "\n";
                    content += getString(R.string.partial_track) + " "
                            + partialTrack + "\n";
                    content += getString(R.string.pinBlock) + " " + pinBlock
                            + "\n";
                    content += "encPAN: " + encPAN + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    content += "pinRandomNumber:" + " " + pinRandomNumber
                            + "\n";
                    cardNo = maskedPAN;

                }
                statusEditText.setText(content);
                sendMsg(8003);
            } else if ((result == DoTradeResult.NFC_DECLINED)) {
                statusEditText.setText(getString(R.string.transaction_declined));
            } else if (result == DoTradeResult.NO_RESPONSE) {
                statusEditText.setText(getString(R.string.card_no_response));
            } else if (result == DoTradeResult.PLS_SEE_PHONE) {
                statusEditText.setText("Please see phone");
                TRACE.d("Please see phone");
            }
        }

        @Override
        public void onQposInfoResult(Hashtable<String, String> posInfoData) {
            TRACE.d("onQposInfoResult" + posInfoData.toString());
            String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null ? "" : posInfoData.get("isSupportedTrack1");
            String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null ? "" : posInfoData.get("isSupportedTrack2");
            String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null ? "" : posInfoData.get("isSupportedTrack3");
            String bootloaderVersion = posInfoData.get("bootloaderVersion") == null ? "" : posInfoData.get("bootloaderVersion");
            String firmwareVersion = posInfoData.get("firmwareVersion") == null ? "" : posInfoData.get("firmwareVersion");
            String isUsbConnected = posInfoData.get("isUsbConnected") == null ? "" : posInfoData.get("isUsbConnected");
            String isCharging = posInfoData.get("isCharging") == null ? "" : posInfoData.get("isCharging");
            String batteryLevel = posInfoData.get("batteryLevel") == null ? "" : posInfoData.get("batteryLevel");
            String batteryPercentage = posInfoData.get("batteryPercentage") == null ? ""
                    : posInfoData.get("batteryPercentage");
            String hardwareVersion = posInfoData.get("hardwareVersion") == null ? "" : posInfoData.get("hardwareVersion");
            String SUB = posInfoData.get("SUB") == null ? "" : posInfoData.get("SUB");
            String pciFirmwareVersion = posInfoData.get("PCI_firmwareVersion") == null ? ""
                    : posInfoData.get("PCI_firmwareVersion");
            String pciHardwareVersion = posInfoData.get("PCI_hardwareVersion") == null ? ""
                    : posInfoData.get("PCI_hardwareVersion");

            String compileTime = posInfoData.get("compileTime") == null ? ""
                    : posInfoData.get("compileTime");
            String content = "";
            content += getString(R.string.bootloader_version) + bootloaderVersion + "\n";
            content += getString(R.string.firmware_version) + firmwareVersion + "\n";
            content += getString(R.string.usb) + isUsbConnected + "\n";
            content += getString(R.string.charge) + isCharging + "\n";
//			if (batteryPercentage==null || "".equals(batteryPercentage)) {
            content += getString(R.string.battery_level) + batteryLevel + "\n";
//			}else {
            content += getString(R.string.battery_percentage) + batteryPercentage + "\n";
//			}
            content += getString(R.string.hardware_version) + hardwareVersion + "\n";
            content += "SUB : " + SUB + "\n";
            content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
            content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
            content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";
            content += "PCI FirmwareVresion:" + pciFirmwareVersion + "\n";
            content += "PCI HardwareVersion:" + pciHardwareVersion + "\n";
            content += "compileTime:" + compileTime + "\n";
            statusEditText.setText(content);
        }

        String resultTansac = "";

        /**
         * 请求交易
         * TODO 简单描述该方法的实现功能（可选）
         *
         * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTransactionResult(com.dspread.xpos.QPOSService.TransactionResult)
         */
        @Override
        public void onRequestTransactionResult(TransactionResult transactionResult) {
            TRACE.d("onRequestTransactionResult()" + transactionResult.toString());
            if (transactionResult == TransactionResult.CARD_REMOVED) {
                clearDisplay();
            }
            // clearDisplay();
            dismissDialog();

            // statusEditText.setText("");
            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.transaction_result);
            TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);
            if (isQuickEmv) {
                messageTextView.setText("please remove card. and send data to online");
//                deviceShowDisplay("\nPLS REMOVE CARD");
            } else {
                if (transactionResult == TransactionResult.APPROVED) {
                    TRACE.d("TransactionResult.APPROVED");
                    String message = getString(R.string.transaction_approved) + "\n" + getString(R.string.amount) + ": $" + amount + "\n";
                    if (!cashbackAmount.equals("")) {
                        message += getString(R.string.cashback_amount) + ": INR" + cashbackAmount;
                    }
                    messageTextView.setText(message);
//                    deviceShowDisplay("APPROVED");

                } else if (transactionResult == TransactionResult.TERMINATED) {
                    clearDisplay();
                    messageTextView.setText(getString(R.string.transaction_terminated));
                } else if (transactionResult == TransactionResult.DECLINED) {
                    messageTextView.setText(getString(R.string.transaction_declined));
//                    deviceShowDisplay("DECLINED");

                } else if (transactionResult == TransactionResult.CANCEL) {
                    clearDisplay();
                    messageTextView.setText(getString(R.string.transaction_cancel));

                } else if (transactionResult == TransactionResult.CAPK_FAIL) {
                    messageTextView.setText(getString(R.string.transaction_capk_fail));
                } else if (transactionResult == TransactionResult.NOT_ICC) {
                    messageTextView.setText(getString(R.string.transaction_not_icc));
                } else if (transactionResult == TransactionResult.SELECT_APP_FAIL) {
                    messageTextView.setText(getString(R.string.transaction_app_fail));
                    pos.setCardTradeMode(QPOSService.CardTradeMode.ONLY_SWIPE_CARD);
                    pos.doTrade(30);
                } else if (transactionResult == TransactionResult.DEVICE_ERROR) {
                    messageTextView.setText(getString(R.string.transaction_device_error));
                } else if (transactionResult == TransactionResult.TRADE_LOG_FULL) {
                    statusEditText.setText("pls clear the trace log and then to begin do trade");
                    messageTextView.setText("the trade log has fulled!pls clear the trade log!");
                } else if (transactionResult == TransactionResult.CARD_NOT_SUPPORTED) {
                    messageTextView.setText(getString(R.string.card_not_supported));
                } else if (transactionResult == TransactionResult.MISSING_MANDATORY_DATA) {
                    messageTextView.setText(getString(R.string.missing_mandatory_data));
                } else if (transactionResult == TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS) {
                    messageTextView.setText(getString(R.string.card_blocked_or_no_evm_apps));
                } else if (transactionResult == TransactionResult.INVALID_ICC_DATA) {
                    messageTextView.setText(getString(R.string.invalid_icc_data));
                } else if (transactionResult == TransactionResult.FALLBACK) {
                    messageTextView.setText("trans fallback");
                } else if (transactionResult == TransactionResult.NFC_TERMINATED) {
                    clearDisplay();
                    messageTextView.setText("NFC Terminated");
                } else if (transactionResult == TransactionResult.CARD_REMOVED) {
                    clearDisplay();
                    messageTextView.setText("CARD REMOVED");
                }
            }

            dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismissDialog();
                    amount = "";
                    cashbackAmount = "";
                    amountEditText.setText("");
                }
            });

            dialog.show();
        }

        @Override
        public void onRequestBatchData(String tlv) {
            TRACE.d("ICC交易结束");
            // dismissDialog();
            String content = getString(R.string.batch_data);
            TRACE.d("onRequestBatchData(String tlv):" + tlv);
            content += tlv;
            statusEditText.setText(content);
        }

        @Override
        public void onQposIdResult(Hashtable<String, String> posIdTable) {
            TRACE.w("onQposIdResult():" + posIdTable.toString());
            String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
            String csn = posIdTable.get("csn") == null ? "" : posIdTable.get("csn");
            String psamId = posIdTable.get("psamId") == null ? "" : posIdTable
                    .get("psamId");
            String NFCId = posIdTable.get("nfcID") == null ? "" : posIdTable
                    .get("nfcID");
            String content = "";
            content += getString(R.string.posId) + posId + "\n";
            content += "csn: " + csn + "\n";
            content += "conn: " + pos.getBluetoothState() + "\n";
            content += "psamId: " + psamId + "\n";
            content += "NFCId: " + NFCId + "\n";
            statusEditText.setText(content);
        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> appList) {
            TRACE.d("onRequestSelectEmvApp():" + appList.toString());
            TRACE.d("请选择App -- S，emv卡片的多种配置");
            dismissDialog();
            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.emv_app_dialog);
            dialog.setTitle(R.string.please_select_app);

            String[] appNameList = new String[appList.size()];
            for (int i = 0; i < appNameList.length; ++i) {

                appNameList[i] = appList.get(i);
            }

            appListView = (ListView) dialog.findViewById(R.id.appList);
            appListView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, appNameList));
            appListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    pos.selectEmvApp(position);
                    TRACE.d("请选择App -- 结束 position = " + position);
                    dismissDialog();
                }

            });
            dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    pos.cancelSelectEmvApp();
                    dismissDialog();
                }
            });
            dialog.show();
        }

        @Override
        public void onRequestSetAmount() {
            TRACE.d("输入金额 -- S");
            TRACE.d("onRequestSetAmount()");
            if (isPosComm) {
                TransactionType transactionType = TransactionType.GOODS;
                String cashbackAmount = "";
                pos.setAmount("10", "156", currencyCode, TransactionType.GOODS);
//				isPosComm = false;
                return;
            }
            dismissDialog();
            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.amount_dialog);
            dialog.setTitle(getString(R.string.set_amount));

            final String[] transactionTypes = new String[]{"GOODS", "SERVICES", "CASH", "CASHBACK", "INQUIRY",
                    "TRANSFER", "ADMIN", "CASHDEPOSIT",
                    "PAYMENT", "PBOCLOG||ECQ_INQUIRE_LOG", "SALE",
                    "PREAUTH", "ECQ_DESIGNATED_LOAD", "ECQ_UNDESIGNATED_LOAD",
                    "ECQ_CASH_LOAD", "ECQ_CASH_LOAD_VOID", "CHANGE_PIN", "REFOUND", "SALES_NEW"};
            ((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item,
                    transactionTypes));

            dialog.findViewById(R.id.setButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    String amount = ((EditText) (dialog.findViewById(R.id.amountEditText))).getText().toString();
                    String cashbackAmount = ((EditText) (dialog.findViewById(R.id.cashbackAmountEditText))).getText().toString();
                    String transactionTypeString = (String) ((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).getSelectedItem();

                    if (transactionTypeString.equals("GOODS")) {
                        transactionType = TransactionType.GOODS;
                    } else if (transactionTypeString.equals("SERVICES")) {
                        transactionType = TransactionType.SERVICES;
                    } else if (transactionTypeString.equals("CASH")) {
                        transactionType = TransactionType.CASH;
                    } else if (transactionTypeString.equals("CASHBACK")) {
                        transactionType = TransactionType.CASHBACK;
                    } else if (transactionTypeString.equals("INQUIRY")) {
                        transactionType = TransactionType.INQUIRY;
                    } else if (transactionTypeString.equals("TRANSFER")) {
                        transactionType = TransactionType.TRANSFER;
                    } else if (transactionTypeString.equals("ADMIN")) {
                        transactionType = TransactionType.ADMIN;
                    } else if (transactionTypeString.equals("CASHDEPOSIT")) {
                        transactionType = TransactionType.CASHDEPOSIT;
                    } else if (transactionTypeString.equals("PAYMENT")) {
                        transactionType = TransactionType.PAYMENT;
                    } else if (transactionTypeString.equals("PBOCLOG||ECQ_INQUIRE_LOG")) {
                        transactionType = TransactionType.PBOCLOG;
                    } else if (transactionTypeString.equals("SALE")) {
                        transactionType = TransactionType.SALE;
                    } else if (transactionTypeString.equals("PREAUTH")) {
                        transactionType = TransactionType.PREAUTH;
                    } else if (transactionTypeString.equals("ECQ_DESIGNATED_LOAD")) {
                        transactionType = TransactionType.ECQ_DESIGNATED_LOAD;
                    } else if (transactionTypeString.equals("ECQ_UNDESIGNATED_LOAD")) {
                        transactionType = TransactionType.ECQ_UNDESIGNATED_LOAD;
                    } else if (transactionTypeString.equals("ECQ_CASH_LOAD")) {
                        transactionType = TransactionType.ECQ_CASH_LOAD;
                    } else if (transactionTypeString.equals("ECQ_CASH_LOAD_VOID")) {
                        transactionType = TransactionType.ECQ_CASH_LOAD_VOID;
                    } else if (transactionTypeString.equals("CHANGE_PIN")) {
                        transactionType = TransactionType.UPDATE_PIN;
                    } else if (transactionTypeString.equals("REFOUND")) {
                        transactionType = TransactionType.REFUND;
//                        pos.setForceCVMNotRequired(true);
                    } else if (transactionTypeString.equals("SALES_NEW")) {
                        transactionType = TransactionType.SALES_NEW;
                    }

                    MainActivity.this.amount = amount;
                    MainActivity.this.cashbackAmount = cashbackAmount;
                    pos.setAmount(amount, cashbackAmount, "0156", transactionType);
                    TRACE.d("输入金额  -- 结束");
                    dismissDialog();
                }

            });

            dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    pos.cancelSetAmount();
                    dialog.dismiss();
                }

            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        public void onRequestOnlineProcess(final String tlv) {
            TRACE.d("onRequestOnlineProcess()");
            dismissDialog();
            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.cvv_dialog);
            dialog.setTitle("Please enter cvv");
            dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String cvv = ((EditText) dialog.findViewById(R.id.cvvEditText)).getText().toString();
                    if (cvv != null && cvv.length() > 0) {
                        TRACE.d("send cvv to pos");
                        Boolean result = pos.sendCVV(cvv);
                        dismissDialog();
                        if (result) {
                            Hashtable hashtable = pos.getEncryptedDataBlock(0);
                            TRACE.d("hashtable: " + hashtable);
                            onRequestGoOnline();
                        }else{
                            TRACE.d("send cvv fail");
                        }
                    }
                }
            });

            dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismissDialog();
                }
            });
            dialog.show();
        }

        @Override
        public void onRequestTime() {
            TRACE.d("onRequestTime");
            TRACE.d("要求终端时间。已回覆");
            dismissDialog();
            String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            pos.sendTime(terminalTime);
            statusEditText.setText(getString(R.string.request_terminal_time) + " " + terminalTime);
        }

        @Override
        public void onRequestDisplay(Display displayMsg) {
            TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());

            dismissDialog();
            String msg = "";
            if (displayMsg == Display.CLEAR_DISPLAY_MSG) {
                msg = "";
            } else if (displayMsg == Display.MSR_DATA_READY) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("音频");
                builder.setMessage("Success,Contine ready");
                builder.setPositiveButton("确定", null);
                builder.show();
            } else if (displayMsg == Display.PLEASE_WAIT) {
                msg = getString(R.string.wait);
            } else if (displayMsg == Display.REMOVE_CARD) {
                msg = getString(R.string.remove_card);
            } else if (displayMsg == Display.TRY_ANOTHER_INTERFACE) {
                msg = getString(R.string.try_another_interface);
            } else if (displayMsg == Display.PROCESSING) {
                msg = getString(R.string.processing);
            } else if (displayMsg == Display.INPUT_PIN_ING) {
                msg = "please input pin on pos";
            } else if (displayMsg == Display.INPUT_OFFLINE_PIN_ONLY || displayMsg == Display.INPUT_LAST_OFFLINE_PIN) {
                msg = "please input offline pin on pos";
            } else if (displayMsg == Display.MAG_TO_ICC_TRADE) {
                msg = "please insert chip card on pos";
            } else if (displayMsg == Display.CARD_REMOVED) {
                msg = "card removed";
            }
            statusEditText.setText(msg);
        }

        @Override
        public void onRequestFinalConfirm() {
            TRACE.d("onRequestFinalConfirm() ");
            TRACE.d("onRequestFinalConfirm+确认金额-- S");
            dismissDialog();
            if (!isPinCanceled) {
                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.confirm_dialog);
                dialog.setTitle(getString(R.string.confirm_amount));

                String message = getString(R.string.amount) + ": $" + amount;
                if (!cashbackAmount.equals("")) {
                    message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmount;
                }

                ((TextView) dialog.findViewById(R.id.messageTextView)).setText(message);

                dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos.finalConfirm(true);
                        dialog.dismiss();
                        TRACE.d("确认金额-- 结束");
                    }
                });

                dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos.finalConfirm(false);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            } else {
                pos.finalConfirm(false);
            }
        }

        @Override
        public void onRequestNoQposDetected() {
            TRACE.d("onRequestNoQposDetected()");
            dismissDialog();
            statusEditText.setText(getString(R.string.no_device_detected));
        }

        @Override
        public void onRequestQposConnected() {
            TRACE.d("onRequestQposConnected()");
            Toast.makeText(MainActivity.this, "onRequestQposConnected", Toast.LENGTH_LONG).show();
            dismissDialog();
            long use_time = System.currentTimeMillis() - start_time;
            // statusEditText.setText(getString(R.string.device_plugged));
            statusEditText.setText(getString(R.string.device_plugged) + "--" + getResources().getString(R.string.used) + QPOSUtil.formatLongToTimeStr(use_time, MainActivity.this));
            doTradeButton.setEnabled(true);
            btnDisconnect.setEnabled(true);
            btnQuickEMV.setEnabled(true);
            btnEMVTest.setEnabled(true);
            doTradeA27Button.setEnabled(true);
            btnQuickEMVtrade.setEnabled(true);
        }

        @Override
        public void onRequestQposDisconnected() {
            dismissDialog();
            TRACE.d("onRequestQposDisconnected()");
            statusEditText.setText(getString(R.string.device_unplugged));
            btnDisconnect.setEnabled(false);
            doTradeButton.setEnabled(false);
            doTradeA27Button.setEnabled(false);
        }

        @Override
        public void onError(QPOSService.Error errorState) {
            if (updateThread != null) {
                updateThread.concelSelf();

            }
            if (cardExistQueryThread != null) {
                cardExistQueryThread.concelSelf();

            }
            index = 0;
            TRACE.d("onError" + errorState.toString());
            dismissDialog();
            amountEditText.setText("");
            if (errorState == QPOSService.Error.CMD_NOT_AVAILABLE) {
                statusEditText.setText(getString(R.string.command_not_available));
            } else if (errorState == QPOSService.Error.TIMEOUT) {
                statusEditText.setText(getString(R.string.device_no_response));
            } else if (errorState == QPOSService.Error.DEVICE_RESET) {
                statusEditText.setText(getString(R.string.device_reset));
            } else if (errorState == QPOSService.Error.UNKNOWN) {
                statusEditText.setText(getString(R.string.unknown_error));
            } else if (errorState == QPOSService.Error.DEVICE_BUSY) {
                statusEditText.setText(getString(R.string.device_busy));
            } else if (errorState == QPOSService.Error.INPUT_OUT_OF_RANGE) {
                statusEditText.setText(getString(R.string.out_of_range));
            } else if (errorState == QPOSService.Error.INPUT_INVALID_FORMAT) {
                statusEditText.setText(getString(R.string.invalid_format));
            } else if (errorState == QPOSService.Error.INPUT_ZERO_VALUES) {
                statusEditText.setText(getString(R.string.zero_values));
            } else if (errorState == QPOSService.Error.INPUT_INVALID) {
                statusEditText.setText(getString(R.string.input_invalid));
            } else if (errorState == QPOSService.Error.CASHBACK_NOT_SUPPORTED) {
                statusEditText.setText(getString(R.string.cashback_not_supported));
            } else if (errorState == QPOSService.Error.CRC_ERROR) {
                statusEditText.setText(getString(R.string.crc_error));
            } else if (errorState == QPOSService.Error.COMM_ERROR) {
                statusEditText.setText(getString(R.string.comm_error));
            } else if (errorState == QPOSService.Error.MAC_ERROR) {
                statusEditText.setText(getString(R.string.mac_error));
            } else if (errorState == QPOSService.Error.APP_SELECT_TIMEOUT) {
                statusEditText.setText(getString(R.string.app_select_timeout_error));
            } else if (errorState == QPOSService.Error.CMD_TIMEOUT) {
                statusEditText.setText(getString(R.string.cmd_timeout));
            } else if (errorState == QPOSService.Error.ICC_ONLINE_TIMEOUT) {
                if (pos == null) {
                    return;
                }
                pos.resetPosStatus();
                statusEditText.setText(getString(R.string.device_reset));
            }
        }

        @Override
        public void onReturnReversalData(String tlv) {
            String content = getString(R.string.reversal_data);
            content += tlv;
            TRACE.d("onReturnReversalData(): " + tlv);
            statusEditText.setText(content);
        }

        @Override
        public void onReturnGetPinResult(Hashtable<String, String> result) {
            TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
            String pinBlock = result.get("pinBlock");
            String pinKsn = result.get("pinKsn");
            String content = "get pin result\n";
            content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
            content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
            statusEditText.setText(content);
            TRACE.i(content);
        }

        @Override
        public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
        }

        @Override
        public void onReturnPowerOffIccResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnPowerOffIccResult(boolean arg0):" + arg0);

        }

        @Override
        public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) :" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);

//            if (arg0) {
//                pos.sendApdu("123456");
//            }
        }

        @Override
        public void onReturnSetSleepTimeResult(boolean isSuccess) {
            TRACE.d("onReturnSetSleepTimeResult(boolean isSuccess):" + isSuccess);

            String content = "";
            if (isSuccess) {
                content = "set the sleep time success.";
            } else {
                content = "set the sleep time failed.";
            }
            statusEditText.setText(content);
        }

        @Override
        public void onGetCardNoResult(String cardNo) {//获取卡号的回调
            TRACE.d("onGetCardNoResult(String cardNo):" + cardNo);
            statusEditText.setText("cardNo: " + cardNo);
        }

        @Override
        public void onRequestCalculateMac(String calMac) {
            // statusEditText.setText("calMac: " + calMac);
            // TRACE.d("calMac_result: calMac=> " + calMac);
            TRACE.d("onRequestCalculateMac(String calMac):" + calMac);

            if (calMac != null && !"".equals(calMac)) {
                calMac = QPOSUtil.byteArray2Hex(calMac.getBytes());
            }
            statusEditText.setText("calMac: " + calMac);
            TRACE.d("calMac_result: calMac=> e: " + calMac);
        }

        @Override
        public void onRequestSignatureResult(byte[] arg0) {
            TRACE.d("onRequestSignatureResult(byte[] arg0):" + arg0.toString());
        }

        @Override
        public void onRequestUpdateWorkKeyResult(UpdateInformationResult result) {
            TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);

            if (result == UpdateInformationResult.UPDATE_SUCCESS) {
                statusEditText.setText("update work key success");
            } else if (result == UpdateInformationResult.UPDATE_FAIL) {
                statusEditText.setText("update work key fail");
            } else if (result == UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
                statusEditText.setText("update work key packet vefiry error");
            } else if (result == UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
                statusEditText.setText("update work key packet len error");
            }
        }

        @Override
        public void onReturnCustomConfigResult(boolean isSuccess, String result) {
            TRACE.d("onReturnCustomConfigResult(boolean isSuccess, String result):" + isSuccess + TRACE.NEW_LINE + result);

            String reString = "Failed";
            if (isSuccess) {
                reString = "Success";
            }
            statusEditText.setText("result: " + reString + "\ndata: " + result);
//			pos.getEncryptData("70533".getBytes(), "0", "0", 15);
        }

        @Override
        public void onRequestSetPin() {
            TRACE.d("onRequestSetPin()===");
            dismissDialog();
            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.pin_dialog);
            dialog.setTitle(getString(R.string.enter_pin));
            dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String pin = ((EditText) dialog.findViewById(R.id.pinEditText)).getText().toString();
                    if (pin.length() >= 4 && pin.length() <= 12) {
                        if (pin.equals("000000")) {
                            pos.sendEncryptPin("00000510F3C36060");

                        } else {
                            pos.sendPin(pin);
                        }
                        dismissDialog();
                    }
                }
            });

            dialog.findViewById(R.id.bypassButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
//					pos.bypassPin();
                    pos.sendPin("");

                    dismissDialog();
                }
            });

            dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    isPinCanceled = true;
                    pos.cancelPin();
                    dismissDialog();
                }
            });

            dialog.show();
        }

        @Override
        public void onReturnSetMasterKeyResult(boolean isSuccess) {
            TRACE.d("onReturnSetMasterKeyResult(boolean isSuccess) : " + isSuccess);
            statusEditText.setText("result: " + isSuccess);
        }

        @Override
        public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult) {
            TRACE.d("onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult):" + batchAPDUResult.toString());
            StringBuilder sb = new StringBuilder();
            sb.append("APDU Responses: \n");
            for (HashMap.Entry<Integer, String> entry : batchAPDUResult.entrySet()) {
                sb.append("[" + entry.getKey() + "]: " + entry.getValue() + "\n");
            }
            statusEditText.setText("\n" + sb.toString());
        }

        @Override
        public void onBluetoothBondFailed() {
            TRACE.d("onBluetoothBondFailed()");
            statusEditText.setText("bond failed");
        }

        @Override
        public void onBluetoothBondTimeout() {
            TRACE.d("onBluetoothBondTimeout()");
            statusEditText.setText("bond timeout");
        }

        @Override
        public void onBluetoothBonded() {
            TRACE.d("onBluetoothBonded()");
            statusEditText.setText("bond success");

        }

        @Override
        public void onBluetoothBonding() {
            TRACE.d("onBluetoothBonding()");
            statusEditText.setText("bonding .....");

        }

        @Override
        public void onReturniccCashBack(Hashtable<String, String> result) {
            TRACE.d("onReturniccCashBack(Hashtable<String, String> result):" + result.toString());
            String s = "serviceCode: " + result.get("serviceCode");
            s += "\n";
            s += "trackblock: " + result.get("trackblock");
            s += "\n";
            s += "pinblock: " + result.get("pinblock");
            statusEditText.setText(s);
        }

        @Override
        public void onLcdShowCustomDisplay(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onLcdShowCustomDisplay(boolean arg0):" + arg0);
        }

        @Override
        public void onUpdatePosFirmwareResult(UpdateInformationResult arg0) {
            TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):" + arg0.toString());
            if (arg0 != UpdateInformationResult.UPDATE_SUCCESS) {
                updateThread.concelSelf();
            }
            statusEditText.setText("onUpdatePosFirmwareResult" + arg0.toString());
        }

        @Override
        public void onReturnDownloadRsaPublicKey(HashMap<String, String> map) {
            TRACE.d("onReturnDownloadRsaPublicKey(HashMap<String, String> map):" + map.toString());
            if (map == null) {
                TRACE.d("MainActivity++++++++++++++map == null");
                return;
            }
            String randomKeyLen = map.get("randomKeyLen");
            String randomKey = map.get("randomKey");
            String randomKeyCheckValueLen = map.get("randomKeyCheckValueLen");
            String randomKeyCheckValue = map.get("randomKeyCheckValue");
            TRACE.d("randomKey" + randomKey + "    \n    randomKeyCheckValue" + randomKeyCheckValue);
            statusEditText.setText("randomKeyLen:" + randomKeyLen + "\nrandomKey:" + randomKey + "\nrandomKeyCheckValueLen:" + randomKeyCheckValueLen + "\nrandomKeyCheckValue:"
                    + randomKeyCheckValue);
        }

        @Override
        public void onGetPosComm(int mod, String amount, String posid) {
            TRACE.d("onGetPosComm(int mod, String amount, String posid):" + mod + TRACE.NEW_LINE + amount + TRACE.NEW_LINE + posid);
            if (mod == 1) {
                isPosComm = false;
                // MainActivity.this.amount = amount;
                // sendMsg(1003);
                MainActivity.this.amount = "FFFFFFFF";
                pos.doTrade(30);
//                pos.getMIccCardData("");
            } else {
                dismissDialog();
                statusEditText.setText("user cancel");
            }
        }

        @Override
        public void onPinKey_TDES_Result(String arg0) {
            TRACE.d("onPinKey_TDES_Result(String arg0):" + arg0);
            statusEditText.setText("result:" + arg0);

        }

        @Override
        public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
            // TODO Auto-generated method stub
            TRACE.d("onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());

        }

        @Override
        public void onEmvICCExceptionData(String arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onEmvICCExceptionData(String arg0):" + arg0);

        }

        @Override
        public void onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1) {
            // TODO Auto-generated method stub
            TRACE.d("onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());

        }

        @Override
        public void onGetInputAmountResult(boolean arg0, String arg1) {
            // TODO Auto-generated method stub
            TRACE.d("onGetInputAmountResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());

        }

        @Override
        public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
            statusEditText.setText("onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
//			pos.powerOffNFC(20);
        }

        @Override
        public void onReturnPowerOffNFCResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
            statusEditText.setText(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
        }

        @Override
        public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
            statusEditText.setText("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
        }


        @Override
        public void onCbcMacResult(String result) {
            TRACE.d("onCbcMacResult(String result):" + result);

            if (result == null || "".equals(result)) {
                statusEditText.setText("cbc_mac:false");
            } else {
                statusEditText.setText("cbc_mac: " + result);
            }
        }

        @Override
        public void onReadBusinessCardResult(boolean arg0, String arg1) {
            // TODO Auto-generated method stub
            TRACE.d(" onReadBusinessCardResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);

        }

        @Override
        public void onWriteBusinessCardResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d(" onWriteBusinessCardResult(boolean arg0):" + arg0);

        }

        @Override
        public void onConfirmAmountResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onConfirmAmountResult(boolean arg0):" + arg0);

        }

        @Override
        public void onQposIsCardExist(boolean cardIsExist) {
            TRACE.d("onQposIsCardExist(boolean cardIsExist):" + cardIsExist);

            if (cardIsExist) {
                statusEditText.setText("cardIsExist:" + cardIsExist);
            } else {
                statusEditText.setText("cardIsExist:" + cardIsExist);
//                if (cardExistQueryThread != null) {
//                    cardExistQueryThread.concelSelf();
//                    deviceShowDisplay("Card removed");
//                }
            }

        }

        @Override
        public void onSearchMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onSearchMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());

                String statuString = arg0.get("status");
                String cardTypeString = arg0.get("cardType");
                String cardUidLen = arg0.get("cardUidLen");
                String cardUid = arg0.get("cardUid");
                String cardAtsLen = arg0.get("cardAtsLen");
                String cardAts = arg0.get("cardAts");
                String ATQA = arg0.get("ATQA");
                String SAK = arg0.get("SAK");
                statusEditText.setText("statuString:" + statuString + "\n" + "cardTypeString:" + cardTypeString + "\ncardUidLen:" + cardUidLen
                        + "\ncardUid:" + cardUid + "\ncardAtsLen:" + cardAtsLen + "\ncardAts:" + cardAts
                        + "\nATQA:" + ATQA + "\nSAK:" + SAK);
            } else {
                statusEditText.setText("poll card failed");
            }
        }

        @Override
        public void onBatchReadMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
            if (cardData != null) {
                TRACE.d("onBatchReadMifareCardResult(boolean arg0):" + msg + cardData.toString());
            }
        }

        @Override
        public void onBatchWriteMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
            if (cardData != null) {
                TRACE.d("onBatchWriteMifareCardResult(boolean arg0):" + msg + cardData.toString());
            }
        }

        @Override
        public void onSetBuzzerResult(boolean arg0) {
            TRACE.d("onSetBuzzerResult(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("蜂鸣器设置成功");
            } else {
                statusEditText.setText("蜂鸣器设置失败");
            }

        }

        @Override
        public void onSetBuzzerTimeResult(boolean b) {
            TRACE.d("onSetBuzzerTimeResult(boolean b):" + b);

        }

        @Override
        public void onSetBuzzerStatusResult(boolean b) {
            TRACE.d("onSetBuzzerStatusResult(boolean b):" + b);

        }

        @Override
        public void onGetBuzzerStatusResult(String s) {
            TRACE.d("onGetBuzzerStatusResult(String s):" + s);

        }

        @Override
        public void onSetManagementKey(boolean arg0) {
            TRACE.d("onSetManagementKey(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("设置主密钥成功");
            } else {
                statusEditText.setText("设置主密钥失败");
            }
        }

        @Override
        public void onReturnUpdateIPEKResult(boolean arg0) {
            TRACE.d("onReturnUpdateIPEKResult(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("update IPEK success");
            } else {
                statusEditText.setText("update IPEK fail");
            }
        }

        @Override
        public void onReturnUpdateEMVRIDResult(boolean arg0) {
            TRACE.d("onReturnUpdateEMVRIDResult(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("operation RID EMV success");
//                if (mEvCapk)
//                    sendMsgDelay(1705);
            } else {
                statusEditText.setText("operation RID EMV fail");
                ConfigUtil.putReadXmlStatus(MainActivity.this, false);
            }
        }

        @Override
        public void onReturnUpdateEMVResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnUpdateEMVResult(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("operation EMV app success");
                if (mEmvApp)
                    sendMsgDelay(1704);

            } else {
                statusEditText.setText("operation emv app fail~");
                ConfigUtil.putReadXmlStatus(MainActivity.this, false);
            }
        }

        @Override
        public void onBluetoothBoardStateResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onBluetoothBoardStateResult(boolean arg0):" + arg0);
        }

        @Override
        public void onDeviceFound(BluetoothDevice arg0) {
            // TODO Auto-generated method stub
            if (arg0 != null && arg0.getName() != null) {
                TRACE.d("onDeviceFound(BluetoothDevice arg0):" + arg0.getName() + ":" + arg0.toString());
                m_ListView.setVisibility(View.VISIBLE);
                animScan.start();
                imvAnimScan.setVisibility(View.VISIBLE);
                if (m_Adapter != null) {
                    Map<String, Object> itm = new HashMap<String, Object>();
                    itm.put("ICON",
                            arg0.getBondState() == BluetoothDevice.BOND_BONDED ? Integer
                                    .valueOf(R.drawable.bluetooth_blue) : Integer
                                    .valueOf(R.drawable.bluetooth_blue_unbond));
                    itm.put("TITLE", arg0.getName() + "(" + arg0.getAddress() + ")");
                    itm.put("ADDRESS", arg0.getAddress());
                    m_Adapter.addData(itm);
                    m_Adapter.notifyDataSetChanged();
                }
                String address = arg0.getAddress();
                String name = arg0.getName();
                name += address + "\n";
                statusEditText.setText(name);
                TRACE.d("发现有新设备" + name);
            } else {
                statusEditText.setText("没有发现新设备");
                TRACE.i("onDeviceFound(BluetoothDevice arg0):" + arg0.getName() + ":" + arg0.toString());
                TRACE.d("没有发现新设备");
            }
        }


        @Override
        public void onSetSleepModeTime(boolean arg0) {
            TRACE.d("onSetSleepModeTime(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("set the Sleep timee Success");
            } else {
                statusEditText.setText("set the Sleep timee unSuccess");
            }
        }

        @Override
        public void onReturnGetEMVListResult(String arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnGetEMVListResult(String arg0):" + arg0);

            if (arg0 != null && arg0.length() > 0) {
                statusEditText.setText("The emv list is : " + arg0);
            }
        }

        @Override
        public void onWaitingforData(String arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onWaitingforData(String arg0):" + arg0);

        }

        @Override
        public void onRequestDeviceScanFinished() {
            // TODO Auto-generated method stub
            TRACE.d("onRequestDeviceScanFinished()");

            Toast.makeText(MainActivity.this, R.string.scan_over, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestUpdateKey(String arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onRequestUpdateKey(String arg0):" + arg0);
            statusEditText.setText("update checkvalue : " + arg0);
        }

        @Override
        public void onReturnGetQuickEmvResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnGetQuickEmvResult(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("emv已配置");
//				isQuickEmv=true;
                pos.setQuickEmv(true);
            } else {
                statusEditText.setText("emv未配置");
            }
        }

        @Override
        public void onQposDoGetTradeLogNum(String arg0) {
            TRACE.d("onQposDoGetTradeLogNum(String arg0):" + arg0);

            int a = Integer.parseInt(arg0, 16);
            if (a >= 188) {
                statusEditText.setText("the trade num has become max value!!");
                return;
            }
            statusEditText.setText("get log num:" + a);
        }

        @Override
        public void onQposDoTradeLog(boolean arg0) {
            TRACE.d("onQposDoTradeLog(boolean arg0) :" + arg0);

            // TODO Auto-generated method stub
            if (arg0) {
                statusEditText.setText("clear log success!");
            } else {
                statusEditText.setText("clear log fail!");
            }
        }

        @Override
        public void onAddKey(boolean arg0) {
            TRACE.d("onAddKey(boolean arg0) :" + arg0);

            if (arg0) {
                statusEditText.setText("ksn add 1 success");
            } else {
                statusEditText.setText("ksn add 1 failed");
            }
        }

        @Override
        public void onQposKsnResult(Hashtable<String, String> arg0) {
            TRACE.d("onQposKsnResult(Hashtable<String, String> arg0):" + arg0.toString());

            // TODO Auto-generated method stub
            String pinKsn = arg0.get("pinKsn");
            String trackKsn = arg0.get("trackKsn");
            String emvKsn = arg0.get("emvKsn");
            TRACE.d("get the ksn result is :" + "pinKsn" + pinKsn + "\ntrackKsn" + trackKsn + "\nemvKsn" + emvKsn);

        }

        @Override
        public void onQposDoGetTradeLog(String arg0, String arg1) {
            TRACE.d("onQposDoGetTradeLog(String arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);

            // TODO Auto-generated method stub
            arg1 = QPOSUtil.convertHexToString(arg1);
            statusEditText.setText("orderId:" + arg1 + "\ntrade log:" + arg0);
        }

        @Override
        public void onRequestDevice() {
            List<UsbDevice> deviceList = getPermissionDeviceList();
            UsbManager mManager = (UsbManager) MainActivity.this.getSystemService(Context.USB_SERVICE);
            for (int i = 0; i < deviceList.size(); i++) {
                UsbDevice usbDevice = deviceList.get(i);
                if (usbDevice.getVendorId() == 2965 || usbDevice.getVendorId() == 0x03EB) {
                    if (mManager.hasPermission(usbDevice)) {
                        pos.setPermissionDevice(usbDevice);
                    } else {
                        devicePermissionRequest(mManager, usbDevice);
                    }
                }

            }
        }

        @Override
        public void onGetKeyCheckValue(List<String> checkValue) {
            if (checkValue != null) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("{");
                for (int i = 0; i < checkValue.size(); i++) {
                    buffer.append(checkValue.get(i)).append(",");
                }
                buffer.append("}");
                statusEditText.setText(buffer.toString());
            }
        }

        @Override
        public void onGetDevicePubKey(String clearKeys) {
            TRACE.d("onGetDevicePubKey(clearKeys):" + clearKeys);
            statusEditText.setText(clearKeys);
            String lenStr = clearKeys.substring(0, 4);
            int sum = 0;
            for (int i = 0; i < 4; i++) {
                int bit = Integer.parseInt(lenStr.substring(i, i + 1));
                sum += bit * Math.pow(16, (3 - i));
            }
            pubModel = clearKeys.substring(4, 4 + sum * 2);
            if (resetIpekFlag || resetMasterKeyFlag) {
                sendMsg(1703);
            }
        }

//        @Override
//        public void onSetPosBlePinCode(boolean b) {
//            TRACE.d("onSetPosBlePinCode(b):" + b);
//
//            if (b) {
//                statusEditText.setText("onSetPosBlePinCode success");
//            } else {
//                statusEditText.setText("onSetPosBlePinCode fail");
//            }
//        }

        @Override
        public void onTradeCancelled() {
            TRACE.d("onTradeCancelled");
            dismissDialog();

        }

        //@Override
        //public void onGet8583Message(boolean b, String s) {
        //    if (b)
        //    TRACE.d("onGet8583Message:"+s);
        //
        //}

        @Override
        public void onReturnSignature(boolean b, String signaturedData) {
            if (b) {
                BASE64Encoder base64Encoder = new BASE64Encoder();
                String encode = base64Encoder.encode(signaturedData.getBytes());
                statusEditText.setText("signature data (Base64 encoding):" + encode);
            }
        }

        @Override
        public void onReturnConverEncryptedBlockFormat(String result) {
            statusEditText.setText(result);
        }

        @Override
        public void onQposIsCardExistInOnlineProcess(boolean haveCard) {

        }


        @Override
        public void onFinishMifareCardResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onFinishMifareCardResult(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("finish success");
            } else {
                statusEditText.setText("finish fail");
            }
        }

        @Override
        public void onVerifyMifareCardResult(boolean arg0) {
            TRACE.d("onVerifyMifareCardResult(boolean arg0):" + arg0);

            // TODO Auto-generated method stub
//			String msg = pos.getMifareStatusMsg();
            if (arg0) {
                statusEditText.setText(" onVerifyMifareCardResult success");

            } else {

                statusEditText.setText("onVerifyMifareCardResult fail");
            }
        }

        @Override
        public void onReadMifareCardResult(Hashtable<String, String> arg0) {
            // TODO Auto-generated method stub
//			String msg = pos.getMifareStatusMsg();
            if (arg0 != null) {
                TRACE.d("onReadMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());

                String addr = arg0.get("addr");
                String cardDataLen = arg0.get("cardDataLen");
                String cardData = arg0.get("cardData");
                statusEditText.setText("addr:" + addr + "\ncardDataLen:" + cardDataLen + "\ncardData:" + cardData);
            } else {
//				statusEditText.setText("onReadWriteMifareCardResult fail"+msg);
            }
        }

        @Override
        public void onWriteMifareCardResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onWriteMifareCardResult(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("write data success!");
            } else {
                statusEditText.setText("write data fail!");
            }
        }

        @Override
        public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
            // TODO Auto-generated method stub

            if (arg0 != null) {
                TRACE.d("onOperateMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());

                String cmd = arg0.get("Cmd");
                String blockAddr = arg0.get("blockAddr");
                statusEditText.setText("Cmd:" + cmd + "\nBlock Addr:" + blockAddr);
            } else {
                statusEditText.setText("operate failed");
            }
        }

        @Override
        public void getMifareCardVersion(Hashtable<String, String> arg0) {

            // TODO Auto-generated method stub
            if (arg0 != null) {
                TRACE.d("getMifareCardVersion(Hashtable<String, String> arg0):" + arg0.toString());

                String verLen = arg0.get("versionLen");
                String ver = arg0.get("cardVersion");
                statusEditText.setText("versionLen:" + verLen + "\nverison:" + ver);
            } else {
                statusEditText.setText("get mafire UL version failed");
            }
        }

        @Override
        public void getMifareFastReadData(Hashtable<String, String> arg0) {
            // TODO Auto-generated method stub

            if (arg0 != null) {
                TRACE.d("getMifareFastReadData(Hashtable<String, String> arg0):" + arg0.toString());

                String startAddr = arg0.get("startAddr");
                String endAddr = arg0.get("endAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
                statusEditText.setText("startAddr:" + startAddr + "\nendAddr:" + endAddr + "\ndataLen:" + dataLen
                        + "\ncardData:" + cardData);
            } else {
                statusEditText.setText("read fast UL failed");
            }
        }

        @Override
        public void getMifareReadData(Hashtable<String, String> arg0) {

            if (arg0 != null) {
                TRACE.d("getMifareReadData(Hashtable<String, String> arg0):" + arg0.toString());

                String blockAddr = arg0.get("blockAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
                statusEditText.setText("blockAddr:" + blockAddr + "\ndataLen:" + dataLen + "\ncardData:" + cardData);
            } else {
                statusEditText.setText("read mafire UL failed");
            }
        }

        @Override
        public void writeMifareULData(String arg0) {

            if (arg0 != null) {
                TRACE.d("writeMifareULData(String arg0):" + arg0.toString());

                statusEditText.setText("addr:" + arg0);
            } else {
                statusEditText.setText("write UL failed");
            }
        }

        @Override
        public void verifyMifareULData(Hashtable<String, String> arg0) {

            if (arg0 != null) {
                TRACE.d("verifyMifareULData(Hashtable<String, String> arg0):" + arg0.toString());

                String dataLen = arg0.get("dataLen");
                String pack = arg0.get("pack");
                statusEditText.setText("dataLen:" + dataLen + "\npack:" + pack);
            } else {
                statusEditText.setText("verify UL failed");
            }
        }

        @Override
        public void onGetSleepModeTime(String arg0) {
            // TODO Auto-generated method stub

            if (arg0 != null) {
                TRACE.d("onGetSleepModeTime(String arg0):" + arg0.toString());

                int time = Integer.parseInt(arg0, 16);
                statusEditText.setText("time is ： " + time + " seconds");
            } else {
                statusEditText.setText("get the time is failed");
            }
        }

        @Override
        public void onGetShutDownTime(String arg0) {

            if (arg0 != null) {
                TRACE.d("onGetShutDownTime(String arg0):" + arg0.toString());

                statusEditText.setText("shut down time is : " + Integer.parseInt(arg0, 16) + "s");
            } else {
                statusEditText.setText("get the shut down time is fail!");
            }
        }

        @Override
        public void onQposDoSetRsaPublicKey(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onQposDoSetRsaPublicKey(boolean arg0):" + arg0);

            if (arg0) {
                statusEditText.setText("set rsa is successed!");

            } else {
                statusEditText.setText("set rsa is failed!");
            }
        }

        @Override
        public void onQposGenerateSessionKeysResult(Hashtable<String, String> arg0) {

            if (arg0 != null) {
                TRACE.d("onQposGenerateSessionKeysResult(Hashtable<String, String> arg0):" + arg0.toString());
                String rsaFileName = arg0.get("rsaReginString");
                String enPinKeyData = arg0.get("enPinKey");
                String enKcvPinKeyData = arg0.get("enPinKcvKey");
                String enCardKeyData = arg0.get("enDataCardKey");
                String enKcvCardKeyData = arg0.get("enKcvDataCardKey");
                statusEditText.setText("rsaFileName:" + rsaFileName + "\nenPinKeyData:" + enPinKeyData + "\nenKcvPinKeyData:" +
                        enKcvPinKeyData + "\nenCardKeyData:" + enCardKeyData + "\nenKcvCardKeyData:" + enKcvCardKeyData);
            } else {
                statusEditText.setText("get key failed,pls try again!");
            }
        }

        @Override
        public void transferMifareData(String arg0) {
            TRACE.d("transferMifareData(String arg0):" + arg0.toString());

            // TODO Auto-generated method stub
            if (arg0 != null) {
                statusEditText.setText("response data:" + arg0);
            } else {
                statusEditText.setText("transfer data failed!");
            }
        }

        @Override
        public void onReturnRSAResult(String arg0) {
            TRACE.d("onReturnRSAResult(String arg0):" + arg0.toString());

            if (arg0 != null) {
                statusEditText.setText("rsa data:\n" + arg0);
            } else {
                statusEditText.setText("get the rsa failed");
            }
        }

        @Override
        public void onRequestNoQposDetectedUnbond() {
            // TODO Auto-generated method stub
            TRACE.d("onRequestNoQposDetectedUnbond()");
        }
    }

    public void getMissingTag() {
        Log.e("pos", "Missing Tag Start...");
        String tags = "9F09" +
                "9F109F1A9F1E9F269F279F359F369F379F415F2A5F34" +
                "50" +
                "82" +
                "84" +
                "95" +
                "98" +
                "9B" +
                "9C";
        int tagcount = 19;
        String tlv;

        Hashtable<String, String> terminalCabapality;
        terminalCabapality = pos.getICCTag(1, 1, "5F25");

        tlv = terminalCabapality.get("tlv");

        if (tlv != null && tlv.length() > 0) {
            Log.e("pos", "TLV: " + tlv);
        }
        Log.e("pos", "Missing Tag END");
    }

    private void deviceShowDisplay(String diplay) {

        Log.e("execut start:", "deviceShowDisplay");
        String customDisplayString = "";
        try {
            byte[] paras = diplay.getBytes("GBK");
            customDisplayString = QPOSUtil.byteArray2Hex(paras);
            pos.lcdShowCustomDisplay(LcdModeAlign.LCD_MODE_ALIGNCENTER, customDisplayString, 60);
        } catch (Exception e) {
            e.printStackTrace();
            TRACE.d("gbk error");
            Log.e("execut error:", "deviceShowDisplay");

        }
        Log.e("execut end:", "deviceShowDisplay");
    }

    private String transformDevice(UsbDevice usbDevice) {
        String deviceName = new String();
        UsbManager mManager = (UsbManager) MainActivity.this.getSystemService(Context.USB_SERVICE);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(
                "com.android.example.USB_PERMISSION"), 0);
        mManager.requestPermission(usbDevice, mPermissionIntent);
        UsbDeviceConnection connection = mManager.openDevice(usbDevice);
        byte rawBuf[] = new byte[255];
        int len = connection.controlTransfer(0x80, 0x06, 0x0302,
                0x0409, rawBuf, 0x00FF, 60);
        rawBuf = Arrays.copyOfRange(rawBuf, 2, len);
        deviceName = new String(rawBuf);
        return deviceName;
    }

    private void devicePermissionRequest(UsbManager mManager, UsbDevice usbDevice) {
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(
                "com.android.example.USB_PERMISSION"), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        mManager.requestPermission(usbDevice, mPermissionIntent);
    }

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // call method to set up device communication
                            TRACE.i("usb" + "permission granted for device "
                                    + device);
                            pos.setPermissionDevice(device);
                        }
                    } else {
                        TRACE.i("usb" + "permission denied for device " + device);

                    }
                    MainActivity.this.unregisterReceiver(mUsbReceiver);
                }
            }
        }
    };

    private List getPermissionDeviceList() {
        UsbManager mManager = (UsbManager) MainActivity.this.getSystemService(Context.USB_SERVICE);
        List deviceList = new ArrayList<UsbDevice>();
        // check for existing devices
        for (UsbDevice device : mManager.getDeviceList().values()) {

            deviceList.add(device);


        }
        return deviceList;

    }

    TLV cardAidTlv = null;
    TLV applicationLabelTlv = null;
    TLV tvrTlv = null;
    TLV transactionDateTlv = null;
    TLV cardholderNameTlv = null;
    TLV applicationExpireDataTlv = null;
    TLV countryCodeTlv = null;
    TLV currencyCodeTlv = null;
    TLV issuerActionCodeDefaultTlv = null;
    TLV issuerActionCodeOnlineTlv = null;
    TLV issuerActionCodeDenialTlv = null;
    TLV applicationPreferredNameTlv = null;
    TLV terminalCapabilitiesTlv = null;
    TLV cvmResultTlv = null;
    TLV unpredictableNumberTlv = null;
    TLV maskPanTlv = null;
    TLV panSeqTlv = null;
    TLV applicationCryptogramTlv = null;
    TLV cryptogramInformationDataTlv = null;
    TLV issuerApplicationDataTlv = null;
    TLV applicationTransactionCounterTlv = null;
    TLV applicationInterchangeProfileTlv = null;
    TLV transactionStatusInformationTlv = null;
    TLV POSEntryModeTlv = null;


    private void analyData(String tlv) {
        List<TLV> list = TLVParser.parse(tlv);// get the tag list
        String[] tlvStrArr = {"4F", "50", "9A", "95", "9F34", "5F20", "5F24", "5F2A", "5F28", "9F0D", "9F0E", "9F0F", "9F33", "9F37", "C4", "5F34", "9F26", "9F27", "9F10", "9F36", "82", "9B", "9F39"};
        cardAidTlv = TLVParser.searchTLV(list, tlvStrArr[0]); // get the  value which tag name "c0"
        applicationLabelTlv = TLVParser.searchTLV(list, tlvStrArr[1]);
        transactionDateTlv = TLVParser.searchTLV(list, tlvStrArr[2]);
        tvrTlv = TLVParser.searchTLV(list, tlvStrArr[3]);
        cvmResultTlv = TLVParser.searchTLV(list, tlvStrArr[4]);
        cardholderNameTlv = TLVParser.searchTLV(list, tlvStrArr[5]);
        applicationExpireDataTlv = TLVParser.searchTLV(list, tlvStrArr[6]);
        currencyCodeTlv = TLVParser.searchTLV(list, tlvStrArr[7]);
        countryCodeTlv = TLVParser.searchTLV(list, tlvStrArr[8]);
        issuerActionCodeDefaultTlv = TLVParser.searchTLV(list, tlvStrArr[9]);
        issuerActionCodeDenialTlv = TLVParser.searchTLV(list, tlvStrArr[10]);
        issuerActionCodeOnlineTlv = TLVParser.searchTLV(list, tlvStrArr[11]);
        terminalCapabilitiesTlv = TLVParser.searchTLV(list, tlvStrArr[12]);
        unpredictableNumberTlv = TLVParser.searchTLV(list, tlvStrArr[13]);
        maskPanTlv = TLVParser.searchTLV(list, tlvStrArr[14]);
        panSeqTlv = TLVParser.searchTLV(list, tlvStrArr[15]);
        applicationCryptogramTlv = TLVParser.searchTLV(list, tlvStrArr[16]);
        applicationCryptogramTlv = TLVParser.searchTLV(list, tlvStrArr[17]);
        issuerApplicationDataTlv = TLVParser.searchTLV(list, tlvStrArr[18]);
        applicationTransactionCounterTlv = TLVParser.searchTLV(list, tlvStrArr[19]);
        applicationInterchangeProfileTlv = TLVParser.searchTLV(list, tlvStrArr[20]);
        transactionStatusInformationTlv = TLVParser.searchTLV(list, tlvStrArr[21]);
        POSEntryModeTlv = TLVParser.searchTLV(list, tlvStrArr[22]);
    }


    private void clearDisplay() {
        statusEditText.setText("");
    }

    private String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    private String currencyCode = "0156";
    private TransactionType transactionType = TransactionType.GOODS;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_CONNECTED_DEVICE = 2;
    private static final int REQUEST_SELECT_USB_DEVICE = 3;


    class MyOnClickListener implements OnClickListener {

        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            statusEditText.setText("");
            if (selectBTFlag) {
                statusEditText.setText(R.string.wait);
                return;
            } else if (v == doTradeButton) {//开始按钮
                if (pos == null) {
                    statusEditText.setText(R.string.scan_bt_pos_error);
                    return;
                }

                if (posType == POS_TYPE.BLUETOOTH) {
                    if (blueTootchAddress == null || "".equals(blueTootchAddress)) {
                        statusEditText.setText(R.string.scan_bt_pos_error);
                        return;
                    }
                }

                isPinCanceled = false;
                amountEditText.setText("");
                statusEditText.setText(R.string.starting);

                terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());

                if (posType == POS_TYPE.UART) {//通用异步收发报机
                    pos.doTrade(terminalTime, 0, 30);
                } else {
                    int keyIdex = getKeyIndex();
                    pos.setCardTradeMode(QPOSService.CardTradeMode.ONLY_TAP_CARD);
//                    pos.setForceCVMRequired(true);
                    pos.doTrade(30);
                }
            } else if (v == btnUSB) {
                USBClass usb = new USBClass();
                ArrayList<String> deviceList = usb.GetUSBDevices(getBaseContext());
                if (deviceList == null) {
                    Toast.makeText(MainActivity.this, "没有权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select a Reader");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String selectedDevice = (String) items[item];
                        dialog.dismiss();
                        usbDevice = USBClass.getMdevices().get(selectedDevice);
                        open(CommunicationMode.USB_OTG_CDC_ACM);
                        posType = POS_TYPE.OTG;
                        pos.openUsb(usbDevice);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else if (v == btnBT) {
                TRACE.d("trype==" + type);
                pos = null;//在连接前一定要保证之前的连接类型已经重置。
                if (pos == null) {
                    if (type == 3) {
                        open(CommunicationMode.BLUETOOTH);
                        posType = POS_TYPE.BLUETOOTH;
                    } else if (type == 4) {
                        open(CommunicationMode.BLUETOOTH_BLE);
                        posType = POS_TYPE.BLUETOOTH_BLE;
                    }
                }
                pos.clearBluetoothBuffer();

                //	close();//扫描前断开蓝牙
                if (isNormalBlu) {//普通蓝牙的扫描
//					pos.stopQPos2Mode();//每次开始扫描，需要先停止再开始
                    TRACE.d("begin scan====");
                    pos.scanQPos2Mode(MainActivity.this, 20);//等到扫描结束后再进行下次点击扫描

                } else {//其他蓝牙的扫描
                    pos.startScanQposBLE(6);
                }
                animScan.start();
                imvAnimScan.setVisibility(View.VISIBLE);
                if (m_Adapter != null) {
                    TRACE.d("+++++=" + m_Adapter);
                    m_Adapter.notifyDataSetChanged();//刷新一下
                } else {
                    refreshAdapter();
                }
            } else if (v == btnDisconnect) {
                close();

            } else if (v == testMifare) {
//                TestMafire testMafire = new TestMafire(pos);
            } else if (v == btnQuickEMV) {
                statusEditText.setText("updating emv config, please wait...");
                updateEmvConfig();


            } else if (v == btnEMVTest) {
                statusEditText.setText("updating emv config, please wait...");
                couFlag = 0;
                couFlagCapk = 0;
                updateEmvConfigTest();
            } else if (v == btnEMVAuto) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);

                } else {
                    Hashtable<String, Object> result = pos.syncGetQposId(5);
                    if (result != null && result.size() > 0) {
//                        Hashtable<String, String> msg = (Hashtable<String, String>) result.get(SyncUtil.CONTENT);
                        String posId = "";
                        String emvAppCfg = null;
                        String emvCapkCfg = null;
                        if (posId.startsWith("2")) {
                            //老平台
                            List<String> allFiles = FileUtils.getAllFiles(FileUtils.POS_Storage_Dir + "A27");
                            if (allFiles == null || allFiles.size() < 1) {
                                statusEditText.setText(" No files under the path");
                                return;
                            }
                            for (String fileName : allFiles) {
                                if (fileName.endsWith(".bin")) {
                                    if (fileName.contains("app")) {
                                        emvAppCfg = QPOSUtil.byteArray2Hex(FileUtils.readLine("A27" + File.separator + fileName));
                                    } else if (fileName.contains("capk")) {
                                        emvCapkCfg = QPOSUtil.byteArray2Hex(FileUtils.readLine("A27" + File.separator + fileName));
                                    }
                                } else {
                                    statusEditText.setText(" No files under the path");
                                    return;
                                }
                            }
                            TRACE.d("emvAppCfg: " + emvAppCfg);
                            TRACE.d("emvCapkCfg: " + emvCapkCfg);
                            int i = checkBin(-1, emvAppCfg, emvCapkCfg);
                            if (i == 0) {
                                statusEditText.setText("Inconsistency");
                                return;
                            } else if (i > 0) {
                                statusEditText.setText("old platform updating emv config, please wait...");
                                pos.updateEmvConfig(emvAppCfg, emvCapkCfg);
                            } else {
                                statusEditText.setText("File and platform do not match");

                            }
                        } else if (posId.startsWith("4")) {
                            //新平台
                            List<String> allFiles = FileUtils.getAllFiles(FileUtils.POS_Storage_Dir + "A45");
                            if (allFiles == null || allFiles.size() < 1) {
                                statusEditText.setText(" No files under the path");
                                return;
                            }
                            for (String fileName : allFiles) {
                                if (fileName.endsWith(".bin")) {
                                    if (fileName.contains("app")) {
                                        emvAppCfg = QPOSUtil.byteArray2Hex(FileUtils.readLine("A45" + File.separator + fileName));
                                    } else if (fileName.contains("capk")) {
                                        emvCapkCfg = QPOSUtil.byteArray2Hex(FileUtils.readLine("A45" + File.separator + fileName));
                                    }
                                } else {
                                    statusEditText.setText(" No files under the path");
                                    return;
                                }
                            }
                            TRACE.d("emvAppCfg: " + emvAppCfg);
                            TRACE.d("emvCapkCfg: " + emvCapkCfg);
                            int i = checkBin(1, emvAppCfg, emvCapkCfg);
                            if (i == 0) {
                                statusEditText.setText("Inconsistency");
                                return;
                            } else if (i > 0) {
                                statusEditText.setText("new platform updating emv config, please wait...");
                                pos.updateEmvConfig(emvAppCfg, emvCapkCfg);

                            } else {
                                statusEditText.setText("File and platform do not match");

                            }
                        }
                    }
                }
            } else if (v == btnQuickEMVtrade) {
                pos.doTrade();
                isQuickEmv = true;
            } else if (v == pollBtn) {
                statusEditText.setText("begin to poll card!");
                sendMsg(3000);
            } else if (v == pollULbtn) {
                statusEditText.setText("begin to poll UL card!");
                sendMsg(3000);
            } else if (v == finishBtn) {
                pos.doMifareCard("0E", 20);
            } else if (v == finishULBtn) {
                pos.doMifareCard("0E", 20);
            } else if (v == veriftBtn) {
                String keyValue = status.getText().toString();
                String blockaddr = blockAdd.getText().toString();
                String keyclass = (String) mafireSpinner.getSelectedItem();
                pos.setBlockaddr(blockaddr);
                pos.setKeyValue(keyValue);
//                FF860000050100016000
//                pos.sendApdu("FFCA000000");
//                pos.doMifareCard("02" + keyclass, 20);
                pos.authenticateMifareCard(QPOSService.MifareCardType.CLASSIC, keyclass, blockaddr, keyValue, 20);
            } else if (v == veriftULBtn) {
                String keyValue = status.getText().toString();
                pos.setKeyValue(keyValue);
                pos.doMifareCard("0D", 20);
            } else if (v == readBtn) {
                String blockaddr = blockAdd.getText().toString();
                pos.setBlockaddr(blockaddr);
                pos.doMifareCard("03", 20);
            } else if (v == writeBtn) {
                String blockaddr = blockAdd.getText().toString();
                String cardData = status.getText().toString();
//				SpannableString s = new SpannableString("please input card data");
//		        status.setHint(s);
                pos.setBlockaddr(blockaddr);
                pos.setKeyValue(cardData);
                pos.doMifareCard("04", 20);
            } else if (v == operateCardBtn) {
                String blockaddr = blockAdd.getText().toString();
                String cardData = status.getText().toString();
                String cmd = (String) cmdSp.getSelectedItem();
                pos.setBlockaddr(blockaddr);
                pos.setKeyValue(cardData);
                pos.doMifareCard("05" + cmd, 20);
            } else if (v == getULBtn) {
                pos.doMifareCard("06", 20);
            } else if (v == readULBtn) {
                String blockaddr = blockAdd.getText().toString();
                pos.setBlockaddr(blockaddr);
                pos.doMifareCard("07", 20);
            } else if (v == fastReadUL) {
                String endAddr = blockAdd.getText().toString();
                String startAddr = status.getText().toString();
                pos.setKeyValue(startAddr);
                pos.setBlockaddr(endAddr);
                pos.doMifareCard("08", 20);
            } else if (v == writeULBtn) {
                String addr = blockAdd.getText().toString();
                String data = status.getText().toString();
                pos.setKeyValue(data);
                pos.setBlockaddr(addr);
                pos.doMifareCard("0B", 20);
            } else if (v == transferBtn) {//透传数据
                String data = status.getText().toString();
                String len = blockAdd.getText().toString();
                pos.setMafireLen(Integer.valueOf(len, 16));
                pos.setKeyValue(data);
                pos.doMifareCard("0F", 20);
            } else if (v == updateFwBtn) {//update firmware
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);

                } else {
//                    LogFileConfig.getInstance().setWriteFlag(true);
                    byte[] data = null;
                    List<String> allFiles = null;
//                    allFiles = FileUtils.getAllFiles(FileUtils.POS_Storage_Dir);
                    if (allFiles != null) {
                        for (String fileName : allFiles) {
                            if (!TextUtils.isEmpty(fileName)) {
                                if (fileName.toUpperCase().endsWith(".asc".toUpperCase())) {
                                    data = FileUtils.readLine(fileName);
                                    Toast.makeText(MainActivity.this, "Upgrade package path:" +
                                            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dspread" + File.separator + fileName, Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        }
                    }

                    if (data == null || data.length == 0) {
                        data = FileUtils.readAssetsLine("upgrader.asc", MainActivity.this);
                    }

                    int a = pos.updatePosFirmware(data, blueTootchAddress);
                    if (a == -1) {
                        Toast.makeText(MainActivity.this, "please keep the device charging", Toast.LENGTH_LONG).show();
                        return;
                    }
                    updateThread = new UpdateThread();
                    updateThread.start();
                }
            }
        }
    }

    private int checkBin(int i, String emvAppCfg, String emvCapkCfg) {
        int flag = -1;

        if (!TextUtils.isEmpty(emvAppCfg) && !TextUtils.isEmpty(emvCapkCfg)) {

            if (TLVParser.VerifyTLV(emvAppCfg) && TLVParser.VerifyTLV(emvCapkCfg)) {
                //新平台
                flag = 1;
            } else {
                if (!TLVParser.VerifyTLV(emvAppCfg) && !TLVParser.VerifyTLV(emvCapkCfg)) {
                    flag = -1;
                } else {
                    flag = 0;
                }
            }
        } else {
            if (!TextUtils.isEmpty(emvAppCfg)) {

                if (TLVParser.VerifyTLV(emvAppCfg)) {
                    //新平台
                    flag = 1;
                }
            } else {

                if (TLVParser.VerifyTLV(emvCapkCfg)) {
                    //新平台
                    flag = 1;
                }
            }
        }

        return i * flag;

    }


    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;


    private void updateEmvConfigTest() {
        pos.updateEmvAPPByTlv(EMVDataOperation.Clear, null);
        statusEditText.setText("Clear EMV app");
        mEmvApp = true;
    }

    private int getKeyIndex() {
        String s = mKeyIndex.getText().toString();
        if (TextUtils.isEmpty(s))
            return 0;
        int i = 0;
        try {
            i = Integer.parseInt(s);
            if (i > 9 || i < 0)
                i = 0;
        } catch (Exception e) {
            i = 0;
            return i;
        }
        return i;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE://提示：该demo中自动扫描连接过程中用不到该连接方法
                // When DeviceListActivity returns with a device to connect
			/*if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				int index = data.getExtras().getInt("index");
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				start_time = new Date().getTime();
				if (index == 0) {

					open(CommunicationMode.AUDIO);
					posType = POS_TYPE.AUDIO;
					pos.openAudio();
				} else if (index == 1 && isUart) {

					open(CommunicationMode.UART);
					TRACE.d(" =====UART");
					posType = POS_TYPE.UART;
					pos.openUart();
				} else if (index == 1 && isUsb) {

					open(CommunicationMode.USB);
					TRACE.d("=====USB");
					posType = POS_TYPE.USB;
					pos.setDeviceAddress("/dev/ttyS1");
					pos.openUsb();
				} else {
					if (address.equals("")) {
						Intent settintIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
						startActivity(settintIntent);
						return;
					}
					// open(CommunicationMode.BLUETOOTH_VER2);
					open(CommunicationMode.BLUETOOTH_2Mode);
					TRACE.d("------------>"+pos.isQposPresent());
					posType = POS_TYPE.BLUETOOTH;
					blueTootchAddress = address;
					sendMsg(1001);
				}
			}*/
                break;
            case REQUEST_CONNECTED_DEVICE:
                //断开
			/*if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(ConnectedDeviceListActivity.EXTRA_CONNECTED_ADDRESS);
				if(address.equals("no_devices")){
					return;
				}
				pos.disconnectBT(address);
			}*/
                pos.disconnectBT();
                break;

        }

    }

    public void onSelectBluetoothName(final ArrayList<String> btList) {
        dismissDialog();
        TRACE.d("onSelectBluetoothName");

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.search_bt_name);
        dialog.setTitle(R.string.please_select_bt_name);

        String[] appNameList = new String[btList.size()];
        for (int i = 0; i < appNameList.length; ++i) {
            TRACE.d("i=" + i + "," + btList.get(i));
            appNameList[i] = btList.get(i).split(",")[0];
        }

        ListView btListView = (ListView) dialog.findViewById(R.id.btList);
        btListView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, appNameList));
        btListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                blueTootchAddress = btList.get(position).split(",")[1];
                dismissDialog();
                TRACE.d("blueTootchAddress:" + blueTootchAddress);
                sendMsg(1001);

            }

        });
        dialog.show();
    }

    private void sendMsg(int what) {
        Message msg = new Message();
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    private void sendMsgDelay(int what) {
        Message msg = new Message();
        msg.what = what;
        mHandler.sendMessageDelayed(msg, 500);
    }

    private boolean selectBTFlag = false;
    private long start_time = 0L;
    private List<TagCapk> capkList;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    btnBT.setEnabled(false);
                    btnQuickEMV.setEnabled(false);
                    btnEMVTest.setEnabled(false);
                    doTradeButton.setEnabled(false);
                    selectBTFlag = true;
                    statusEditText.setText(R.string.connecting_bt_pos);
                    sendMsg(1002);
                    break;
                case 1002:
//				pos.stopQPos2Mode();
//                    pos.getBluetoothState()
                    if (isNormalBlu) {
                        boolean a = pos.connectBluetoothDevice(false, 25, blueTootchAddress);
//                        boolean a =  pos.syncConnectBluetooth(true,20,blueTootchAddress);
//					pos.connectBluetoothDevice(true, 25, 3, blueTootchAddress);
                    } else {
//                        pos.connectBLE(blueTootchAddress, 3);
                    }
                    btnBT.setEnabled(true);
                    selectBTFlag = false;
                    break;
                case 8003:
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String content = "";
                    if (nfcLog == null) {
                        Hashtable<String, String> h = pos.getNFCBatchData();
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + h.get("tlv");
                    } else {
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + nfcLog;
                    }
                    onRequestSendCVV();
                    statusEditText.setText(content);
                    break;
                case 3000:
//                    pos.doMifareCard("01", 20);
                    pos.pollOnMifareCard(20);
                    break;
                case 1701:
//                    updateEMVCfgByXML();
//                    if (appList == null) {
//                        Toast.makeText(MainActivity.this, "File read failed", Toast.LENGTH_SHORT).show();
//                        ConfigUtil.putReadXmlStatus(MainActivity.this, false);
//
//                        return;
//                    }
//                    if (appList.size() < 1) {
//                        Toast.makeText(MainActivity.this, R.string.updateEMVAppStatus, Toast.LENGTH_SHORT).show();
//                        statusEditText.setText(R.string.updateEMVAppStatus);
//                        return;
//                    }
//                    TagApp tagApp = appList.get(0);
//                    ArrayList<String> emvApp = new ArrayList<>();
//                    int appLen = BaseTag.TAG_APP.length > tagApp.getDatasLength() ? tagApp.getDatasLength() : BaseTag.TAG_APP.length;
//                    for (int i = 0; i < appLen; i++) {
//                        String data = tagApp.getData(i);
//                        if (TextUtils.isEmpty(data))
//                            continue;
//                        if (data.contains(EmvAppTag.Currency_conversion_factor))
//                            continue;
//                        emvApp.add(data);
//                        emvApp.add("9F06");
//                    }
                    ArrayList<String> emvList = new ArrayList<>();
                    emvList.add("7F10DF811801F8");
                    pos.updateEmvAPP(EMVDataOperation.Add, emvList);
                    break;
                case 1702:
                    updateEMVCfgByXML();
                    if (capkList == null) {
                        Toast.makeText(MainActivity.this, "File read failed", Toast.LENGTH_SHORT).show();
                        ConfigUtil.putReadXmlStatus(MainActivity.this, false);

                        return;
                    }

                    if (capkList.size() < 1) {
                        Toast.makeText(MainActivity.this, R.string.updateEMVCapkStatus, Toast.LENGTH_SHORT).show();
                        statusEditText.setText(R.string.updateEMVCapkStatus);
                        return;
                    }
                    TagCapk tagCapk = capkList.get(0);
                    ArrayList<String> emvCapk = new ArrayList<>();
                    int caLen = BaseTag.TAG_APP.length > tagCapk.getDatasLength() ? tagCapk.getDatasLength() : BaseTag.TAG_APP.length;

                    for (int i = 0; i < caLen; i++) {
                        emvCapk.add(tagCapk.getData(i));
                    }
                    pos.updateEmvCAPK(EMVDataOperation.Add, emvCapk);
                    break;

                case 1703:
                    int keyIndex = getKeyIndex();
                    String digEnvelopStr = null;
                    Poskeys posKeys = null;
                    try {
                        if (resetIpekFlag) {
                            posKeys = new DukptKeys();
                        }
                        if (resetMasterKeyFlag) {
                            posKeys = new TMKKey();
                        }
                        posKeys.setRSA_public_key(pubModel); //Model of device public key
                        digEnvelopStr = Envelope.updateTokenTest(getAssets().open("rsa_private_pkcs8_1024.pem"),
                                posKeys, Poskeys.RSA_KEY_LEN.RSA_KEY_1024, keyIndex);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    pos.updateWorkKey(digEnvelopStr);

                    break;

                case 1704:
                    if (appTLVList == null) {
                        String mEmvAppCfg = QPOSUtil.byteArray2Hex(FileUtils.readAssetsLine("emv_app.bin", MainActivity.this));
                        TRACE.d("更新的数据：" + mEmvAppCfg);
                        int cun = mEmvAppCfg.length() % 768;
                        if (cun == 0) {
                            cun = mEmvAppCfg.length() / 768;
                            appTLVList = new String[cun];
                        } else {
                            cun = mEmvAppCfg.length() / 768;
                            appTLVList = new String[cun + 1];
                        }
                        TRACE.d("列表长度：" + appTLVList.length);

                        String substring = "";
                        for (int i = 0; i < appTLVList.length; i++) {
                            if (i == appTLVList.length - 1) {
                                if (i * 768 < mEmvAppCfg.length())
                                    substring = mEmvAppCfg.substring(i * 768, mEmvAppCfg.length());
                            } else {
                                substring = mEmvAppCfg.substring(i * 768, (i + 1) * 768);
                            }
                            appTLVList[i] = substring;
                            TRACE.d("角标：" + i + "数据 " + appTLVList[i]);

                        }
                    }

                    String appTLV = null;
                    if (couFlag < appTLVList.length) {
                        appTLV = appTLVList[couFlag];
                        TRACE.d("马上要跟新的数据 " + appTLV);
                    }
                    if (QPOSUtil.checkStringAllZero(appTLV)) {
                        mEmvApp = false;
                        couFlag = 0;
                        mEvCapk = true;
                        statusEditText.setText("Clear EMV CAPK ");
                        pos.updateEmvCAPKByTlv(EMVDataOperation.Clear, null);
                    } else {
                        statusEditText.setText("Add EMV app :" + couFlag);
                        pos.updateEmvAPPByTlv(EMVDataOperation.update, "9F1A020978");
                        couFlag++;
                    }
                    break;
                case 1705:
                    if (capkTLVList == null) {
                        String mEmvCapkCfg = QPOSUtil.byteArray2Hex(FileUtils.readAssetsLine("emv_capk.bin", MainActivity.this));
                        TRACE.d("更新的数据：" + mEmvCapkCfg);
                        int cunCapk = mEmvCapkCfg.length() % 768;
                        if (cunCapk == 0) {
                            cunCapk = mEmvCapkCfg.length() / 768;
                            capkTLVList = new String[cunCapk];
                        } else {
                            cunCapk = mEmvCapkCfg.length() / 768;
                            capkTLVList = new String[cunCapk + 1];
                        }
                        TRACE.d("列表长度：" + capkTLVList.length);

                        String sub = "";
                        for (int i = 0; i < capkTLVList.length; i++) {
                            if (i == capkTLVList.length - 1) {
                                if (mEmvCapkCfg.length() > i * 768)
                                    sub = mEmvCapkCfg.substring(i * 768, mEmvCapkCfg.length());
                            } else {
                                sub = mEmvCapkCfg.substring(i * 768, (i + 1) * 768);
                            }
                            capkTLVList[i] = sub;
                            TRACE.d("角标：" + i + "数据 " + capkTLVList[i]);

                        }

                    }

                    String capkTLV = null;
                    if (couFlagCapk < capkTLVList.length) {
                        capkTLV = capkTLVList[couFlagCapk];

                    }

                    if (QPOSUtil.checkStringAllZero(capkTLV)) {
                        mEmvApp = false;
                        mEvCapk = false;
                        couFlagCapk = 0;
                        statusEditText.setText("UPDATE EMV SUCCESS");

                    } else {
                        statusEditText.setText("Add EMV CAPK :" + couFlagCapk);

                        pos.updateEmvCAPKByTlv(EMVDataOperation.Add, capkTLV);

                        couFlagCapk++;
                    }

                    break;

                case 1706:
                    if (pos == null)
                        return;
                    cardExistQueryThread = new CardExistQueryThread();
                    cardExistQueryThread.initPos(pos);
                    cardExistQueryThread.start();
                    break;
                default:
                    break;
            }
        }
    };

    private CardExistQueryThread cardExistQueryThread;
    String[] capkTLVList = null;
    String[] appTLVList = null;

    private int couFlag = 0;
    private int couFlagCapk = 0;
    private boolean mEmvApp = false;
    private boolean mEvCapk = false;

    private void updateEMVCfgByXML() {
        InputStream open = null;
        try {
            if (!ConfigUtil.hasReadXml(MainActivity.this)) {
                AssetManager assets = getAssets();
                open = assets.open("emv_profile_tlv(2).xml");
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                SAXParserHandler handler = new SAXParserHandler();
                parser.parse(open, handler);
                capkList = handler.getCapkList();
                appList = handler.getAppList();
                ConfigUtil.putReadXmlStatus(MainActivity.this, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, R.string.failedRead, Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (open != null) {
                    open.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateEmvConfig() {
        String emvAppCfg = QPOSUtil.byteArray2Hex(FileUtils.readAssetsLine("emv_app.bin", MainActivity.this));
        String emvCapkCfg = QPOSUtil.byteArray2Hex(FileUtils.readAssetsLine("emv_capk.bin", MainActivity.this));
        TRACE.d("emvAppCfg: " + emvAppCfg);
        TRACE.d("emvCapkCfg: " + emvCapkCfg);
        pos.updateEmvConfig(emvAppCfg, emvCapkCfg);

    }

    /*The following methods used in China*/
    public void calcMacSingle(String cal) {//The calculation of unionpay MAC(Haploid mac key)
        if (cal.length() % 2 != 0) {
            cal += "0";
        }
        byte[] mab = QPOSUtil.HexStringToByteArray(cal);
        byte[] ecb = QPOSUtil.ecb(mab);
        pos.calcMacSingleAll(QPOSUtil.byteArray2Hex(ecb), 10);
    }

    public void calcMacDouble(String cal) {//The calculation of unionpay MAC(Double mac key)
        byte[] mab = QPOSUtil.HexStringToByteArray(cal);
        byte[] ecb = QPOSUtil.ecb(mab);
        pos.calcMacDoubleAll(QPOSUtil.byteArray2Hex(ecb), 0, 10);
    }

    public void tdesPin(String s) {// Encrypted pin
//		pos.pinKey_TDES_ALL(0, "0123456789012345", 10);
        pos.pinKey_TDES_ALL(0, s, 5);
    }

    /*---------------------------------------------*/
    private static final String FILENAME = "dsp_axdd";

    /**
     * desc:保存对象
     *
     * @param context
     * @param key
     * @param obj     要保存的对象，只能保存实现了serializable的对象
     *                modified:
     */
    public static void saveObject(Context context, String key, Object obj) {
        try {
            // 保存对象
            SharedPreferences.Editor sharedata = context.getSharedPreferences(FILENAME, 0).edit();
            //先将序列化结果写到byte缓存中，其实就分配一个内存空间
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            //将对象序列化写入byte缓存
            os.writeObject(obj);
            //将序列化的数据转为16进制保存
            String bytesToHexString = QPOSUtil.byteArray2Hex(bos.toByteArray());
            //保存该16进制数组
            sharedata.putString(key, bytesToHexString);
            sharedata.commit();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("", "保存obj失败");
        }
    }


    /**
     * desc:获取保存的Object对象
     *
     * @param context
     * @param key
     * @return modified:
     */
    public Object readObject(Context context, String key) {
        try {
            SharedPreferences sharedata = context.getSharedPreferences(FILENAME, 0);
            if (sharedata.contains(key)) {
                String string = sharedata.getString(key, "");
                if (string == null || "".equals(string)) {
                    return null;
                } else {
                    //将16进制的数据转为数组，准备反序列化
                    byte[] stringToBytes = QPOSUtil.HexStringToByteArray(string);
                    ByteArrayInputStream bis = new ByteArrayInputStream(stringToBytes);
                    ObjectInputStream is = new ObjectInputStream(bis);
                    //返回反序列化得到的对象
                    Object readObject = is.readObject();
                    return readObject;
                }
            }
        } catch (StreamCorruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //所有异常返回null
        return null;

    }

}