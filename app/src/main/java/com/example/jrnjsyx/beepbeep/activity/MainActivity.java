package com.example.jrnjsyx.beepbeep.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jrnjsyx.beepbeep.R;
import com.example.jrnjsyx.beepbeep.physical.AudioRecorder;
import com.example.jrnjsyx.beepbeep.physical.thread.PlayThread;
import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.processing.thread.DecodeThread;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;
import com.example.jrnjsyx.beepbeep.wifip2p.DirectActionListener;
import com.example.jrnjsyx.beepbeep.wifip2p.DirectBroadcastReceiver;
import com.example.jrnjsyx.beepbeep.wifip2p.NetworkMsgListener;
import com.example.jrnjsyx.beepbeep.wifip2p.thread.ConnetJudgeThread;
import com.example.jrnjsyx.beepbeep.wifip2p.thread.WifiP2pThread;
import com.example.jrnjsyx.beepbeep.wifip2p.thread.ClientThread;
import com.example.jrnjsyx.beepbeep.wifip2p.thread.ServerThread;

import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements AudioRecorder.RecordingCallback,DirectActionListener {


    protected static final String TAG = "MainActivity";

    @BindView(R.id.a_button)
    Button aButton;
    @BindView(R.id.b_button)
    Button bButton;
    @BindView(R.id.text)
    TextView textView;
    @BindView(R.id.start_button)
    Button startButton;
    @BindView(R.id.connect_button)
    Button connectButton;
    @BindView(R.id.connect_ready_button)
    Button connectReadyButton;
    @BindView(R.id.connection_linear)
    LinearLayout connectionLinear;
    @BindView(R.id.create_group)
    Button createGroupButton;
    @BindView(R.id.remove_group)
    Button removeGroupButton;
    @BindView(R.id.main_linear)
    LinearLayout mainLinear;

    private WifiP2pManager wifiP2pManager;

    private WifiP2pManager.Channel channel;

    private BroadcastReceiver broadcastReceiver;



    private AudioRecorder audioRecorder;
    private PlayThread playThread;
    private DecodeThread decodeThread;
    private WifiP2pThread serverThread;
    private WifiP2pThread clientThread;
    private WifiP2pThread currentP2pThread;
    private ConnetJudgeThread connetJudgeThread;

    private WifiP2pInfo wifiP2pInfo;
    private boolean aMode = false;
    private boolean bMode = false;
    private int connectionMode = NO_SERVER_CLIENT_MODE;
    static int SERVER_MODE = 1;
    static int CLIENT_MODE = 2;
    static int NO_SERVER_CLIENT_MODE = 0;
    private boolean isConnected = false;
    //wifi直连后，是否接收到对面发来的确认信号
    private boolean aModeStrRecv = false;
    private boolean bModeStrRecv = false;
    private int aCnt = 0;
    private int bCnt = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initParams();

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.a_button: {
                    afterAButtonPress();
                    break;
                }
                case R.id.b_button: {
                    afterBButtonPress();
                    break;
                }
                case R.id.connect_button: {
                    afterConnectButtonPress();
                    break;
                }
                case R.id.connect_ready_button: {
                    afterTestButtonPress();
                    break;
                }
                case R.id.start_button: {
                    afterStartButtonPress();
                    break;
                }
                case R.id.main_linear:{
                    if(checkPermission()){
                        setButtonEnabled(aButton,true);
                        setButtonEnabled(bButton,true);
                        setButtonEnabled(connectButton,true);
                        mainLinear.setClickable(false);
                    }
                    break;
                }
            }
        }
    };

    //初始化参数
    public void initParams(){

        aButton.setOnClickListener(onClickListener);
        bButton.setOnClickListener(onClickListener);
        startButton.setOnClickListener(onClickListener);
        connectButton.setOnClickListener(onClickListener);
        connectReadyButton.setOnClickListener(onClickListener);
        mainLinear.setOnClickListener(onClickListener);

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), this);
        broadcastReceiver = new DirectBroadcastReceiver(wifiP2pManager, channel, this);
        registerReceiver(broadcastReceiver, DirectBroadcastReceiver.getIntentFilter());
    }

    private void afterAButtonPress(){
        //判断a按钮是否按下
        if(bButton.isEnabled() == true){
            bButton.setEnabled(false);
            aMode = true;
            afterAorBPress();
        }else{
            aMode = false;
            bButton.setEnabled(true);
            afterAandBUnpressed();
        }
    }

    private void afterBButtonPress(){
        //判断b按钮是否按下
        if(aButton.isEnabled() == true){
            aButton.setEnabled(false);
            bMode = true;
            afterAorBPress();
        }else{
            aButton.setEnabled(true);
            bMode = false;
            afterAandBUnpressed();
        }
    }

    private void afterTestButtonPress(){
        //假如a按下
        if(bButton.isEnabled() == false){
            if(currentP2pThread != null){
                aCnt++;
                currentP2pThread.setStr("a pressed "+aCnt);
                Common.println("a pressed");
            }else{
                showToast("not connected");
            }
        }
        //假如b按下
        else if(aButton.isEnabled() == false){
            if(currentP2pThread != null){
                bCnt++;
                currentP2pThread.setStr("b pressed "+bCnt);
                Common.println("b pressed");
            }else{
                showToast("not connected");
            }
        }
    }

    private void afterStartButtonPress(){
        aModeStrRecv = false;
        bModeStrRecv = false;
        if(aMode){
            currentP2pThread.setStr(FlagVar.aModeStr);
            NetworkMsgListener listener = new NetworkMsgListener() {
                @Override
                public void handleMsg(String msg) {
                    aModeStart(msg);
                }
            };
            currentP2pThread.addListener(FlagVar.aModeStr,listener);
        }
        else if(bMode){
            currentP2pThread.setStr(FlagVar.bModeStr);

            NetworkMsgListener listener = new NetworkMsgListener() {
                @Override
                public void handleMsg(String msg) {
                    bModeStart(msg);
                }
            };
            currentP2pThread.addListener(FlagVar.bModeStr,listener);
        }
    }

    private void aModeStart(String msg){

        if(msg.equals(FlagVar.bModeStr)){
            currentP2pThread.setStr(FlagVar.aModeStr);
            showToast("开始测距。");
            playThread = new PlayThread(Decoder.lowChirp);
            playThread.start();
            decodeThread = new DecodeThread(myHandler, AudioRecorder.getBufferSize() / 2, true);
            new Thread(decodeThread).start();
            audioRecorder = new AudioRecorder();
            audioRecorder.recordingCallback(MainActivity.this);
            audioRecorder.startRecord();
            setButtonEnabled(connectReadyButton,false);
            setButtonEnabled(startButton,false);
            currentP2pThread.removeListener(FlagVar.aModeStr);
            currentP2pThread.removeListener(FlagVar.bModeStr);
        }
        else if(msg.equals(FlagVar.aModeStr)){
            showToast("未能同时开启A、B两种模式。");
        }
    }
    private void bModeStart(String msg){
        if(msg.equals(FlagVar.aModeStr)){
            currentP2pThread.setStr(FlagVar.bModeStr);
            showToast("开始测距。");
            playThread = new PlayThread(Decoder.highChirp);
            playThread.start();
            decodeThread = new DecodeThread(myHandler, AudioRecorder.getBufferSize() / 2, false);
            new Thread(decodeThread).start();
            audioRecorder = new AudioRecorder();
            audioRecorder.recordingCallback(MainActivity.this);
            audioRecorder.startRecord();
            setButtonEnabled(connectReadyButton,false);
            setButtonEnabled(startButton,false);
            currentP2pThread.removeListener(FlagVar.aModeStr);
            currentP2pThread.removeListener(FlagVar.bModeStr);
        }
        else if(msg.equals(FlagVar.bModeStr)){

            showToast("未能同时开启A、B两种模式。");
        }
    }

    private void judgeCanStart(){
        if(isConnected && (aMode || bMode)){
            setButtonEnabled(startButton,true);
            NetworkMsgListener listener = new NetworkMsgListener() {
                @Override
                public void handleMsg(String msg) {
                    if(aMode){
                        aModeStart(msg);
                    }
                    else if(bMode){
                        bModeStart(msg);
                    }
                }
            };
            currentP2pThread.addListener(FlagVar.aModeStr,listener);
            currentP2pThread.addListener(FlagVar.bModeStr,listener);
        }else{
            setButtonEnabled(startButton,false);
        }
    }

    private void afterConnectButtonPress(){
        wifiP2pManager.requestConnectionInfo(channel,new WifiP2pManager.ConnectionInfoListener(){
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if(info.groupOwnerAddress == null){
                    onNothingConnected();
                }
                else {
                    wifiP2pInfo = info;
                    if (info.isGroupOwner && info.groupFormed) {
                        onServerConnection();
                    } else {
                        onClientConnection(info);
                    }
                }
            }
        });
    }



    private void onNothingConnected(){

        if (wifiP2pManager != null && channel != null) {
            startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            showToast("请在wifi设置中，设置wifi直连对象。");
        } else {
            showToast("当前设备不支持Wifi直连");
        }
    }
    private void onServerConnection(){
        connectionMode = SERVER_MODE;
        connectButton.setEnabled(false);
        serverThread = new ServerThread(myHandler);
        serverThread.start();
        currentP2pThread = serverThread;;
        NetworkMsgListener listener = new NetworkMsgListener() {
            @Override
            public void handleMsg(String msg) {
                if(msg.equals(FlagVar.connectionStartStr)){
                    currentP2pThread.setStr(FlagVar.connectionStartStr);
                    showToast("服务端连接已打开。");
                    isConnected = true;
                    judgeCanStart();
                    currentP2pThread.removeListener(FlagVar.connectionStartStr);
                }
            }
        };
        currentP2pThread.addListener(FlagVar.connectionStartStr,listener);

    }
    private void onClientConnection(WifiP2pInfo info){
        connectionMode = CLIENT_MODE;
        connectButton.setEnabled(false);
        showToast("正在等待服务端连接打开。");
        NetworkMsgListener listener = new NetworkMsgListener() {
            @Override
            public void handleMsg(String msg) {
                try {
                    Thread.currentThread().sleep(10);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                clientThread = new ClientThread(myHandler, wifiP2pInfo.groupOwnerAddress.getHostAddress());
                clientThread.start();
                currentP2pThread = clientThread;

                currentP2pThread.setStr(FlagVar.connectionStartStr);
                NetworkMsgListener listener = new NetworkMsgListener() {
                    @Override
                    public void handleMsg(String msg) {
                        if(msg.equals(FlagVar.connectionStartStr)){
                            showToast("客户端连接已打开。");
                            isConnected = true;
                            judgeCanStart();
                            currentP2pThread.removeListener(FlagVar.connectionStartStr);
                        }
                    }
                };
                currentP2pThread.addListener(FlagVar.connectionStartStr,listener);
            }
        };
        connetJudgeThread = new ConnetJudgeThread(info.groupOwnerAddress.getHostAddress(), FlagVar.PORT,listener);
        connetJudgeThread.start();



    }

    private void afterAorBPress(){
        connectReadyButton.setEnabled(true);
        judgeCanStart();
    }

    private void afterAandBUnpressed(){
        connectReadyButton.setEnabled(false);
        judgeCanStart();

    }





    public void sleep(int time){
        try {
            Thread.currentThread().sleep(time);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public Handler myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int distance = (int)(FlagVar.cSample*msg.arg1);
//            textView.setText("sampleCnt:"+msg.arg1+"    distance:"+distance);
            String str = (String)msg.obj;
            textView.setText(str);

            if(str.equals(FlagVar.connectionThreadEndStr)){
                isConnected = false;
                judgeCanStart();
            }


        }
    };



    private void setButtonEnabled(Button bt,boolean enabled){
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            this.runOnUiThread(new ButtonEnabledThread(bt,enabled));
        }else{
            bt.setEnabled(enabled);
        }
    }

    private class ButtonEnabledThread implements Runnable{
        Button bt;
        boolean enabled;
        public ButtonEnabledThread(Button bt, boolean enabled){
            this.bt = bt;
            this.enabled = enabled;
        }
        @Override
        public void run() {
            bt.setEnabled(enabled);
        }
    }

    @Override
    public void onDataReady(short[] data, int len) {
        Common.println("onDataReady");
        short[] data1 = new short[len];
        short[] data2 = new short[len];
        for (int i = 0; i < len; i++) {
            data1[i] = data[2 * i];
            data2[i] = data[2 * i + 1];
        }
        decodeThread.fillSamples(data2);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            finish();
            System.exit(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static String getDeviceStatus(int deviceStatus) {
            switch (deviceStatus) {
                case WifiP2pDevice.AVAILABLE:
                    return "可用的";
                case WifiP2pDevice.INVITED:
                    return "邀请中";
                case WifiP2pDevice.CONNECTED:
                    return "已连接";
                case WifiP2pDevice.FAILED:
                    return "失败的";
                case WifiP2pDevice.UNAVAILABLE:
                    return "不可用的";
                default:
                    return "未知";
            }
    }

    private void showToast(String message) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            this.runOnUiThread(new ToastThread(message));
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private class ToastThread implements Runnable{
        String str;
        public ToastThread(String str){
            this.str = str;
        }
        @Override
        public void run() {
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void wifiP2pEnabled(boolean enabled) {
        Log.e(TAG, "wifiP2pEnabled: " + enabled);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Log.e(TAG, "onConnectionInfoAvailable");
    }

    @Override
    public void onDisconnection() {
        Log.e(TAG, "onDisconnection");
        connectButton.setEnabled(true);
        if(currentP2pThread != null) {
            currentP2pThread.stopRunning();
        }
        currentP2pThread = null;
        serverThread = null;
        clientThread = null;
        aMode = false;
        bMode = false;
        aButton.setEnabled(true);
        bButton.setEnabled(true);
        isConnected = false;
        aModeStrRecv = false;
        bModeStrRecv = false;
        judgeCanStart();
        if(currentP2pThread != null) {
            currentP2pThread.removeListener(FlagVar.connectionStartStr);
            currentP2pThread.removeListener(FlagVar.aModeStr);
            currentP2pThread.removeListener(FlagVar.bModeStr);
        }

    }

    @Override
    public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
        Log.e(TAG, "onSelfDeviceAvailable");
    }

    @Override
    public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {
        Log.e(TAG, "onPeersAvailable");
    }

    @Override
    public void onChannelDisconnected() {
        Log.e(TAG, "onChannelDisconnected");
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
            for(String permission:permissions){
                int i = ContextCompat.checkSelfPermission(this, permission);
                if (i != PackageManager.PERMISSION_GRANTED) {
                    showToast("需要在设置中打开麦克风、存储权限。");
                    return false;
                }
            }
            return true;

        }else{
            showToast("您的android版本过低，无法判断权限是否打开，请自行在设置中打开麦克风、存储权限。");
            return true;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    }


}