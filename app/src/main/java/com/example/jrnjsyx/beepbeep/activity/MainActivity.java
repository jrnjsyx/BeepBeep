package com.example.jrnjsyx.beepbeep.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
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
import com.example.jrnjsyx.beepbeep.physical.PlayThread;
import com.example.jrnjsyx.beepbeep.processing.DecodThread;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;
import com.example.jrnjsyx.beepbeep.wifip2p.DirectActionListener;
import com.example.jrnjsyx.beepbeep.wifip2p.DirectBroadcastReceiver;
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
    @BindView(R.id.omit_button)
    Button omitButton;
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

    private WifiP2pManager wifiP2pManager;

    private WifiP2pManager.Channel channel;

    private BroadcastReceiver broadcastReceiver;



    private AudioRecorder audioRecorder = new AudioRecorder();
    private PlayThread playThread = new PlayThread();
    private DecodThread decodThread;
    private ServerThread serverThread;
    private ClientThread clientThread;

    private boolean aPressed = false;
    private boolean bPressed = false;
    private boolean bConnectionPressed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initParams();

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        private int aCnt = 0;
        private int bCnt = 0;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.a_button: {
                    if(aPressed == false){
                        aPressed = true;
                        bButton.setEnabled(false);
                        afterAorBPress();

                    }else{
                        afterAandBUnpressed();
                        aPressed = false;
                        bButton.setEnabled(true);

                    }
                    /*
                    bButton.setEnabled(false);
                    decodThread.decodeStart();
                    audioRecorder.startRecord();
                    sleep(500);
                    playThread.omitChirpSignal();
                    new Thread(new ADiffThread(decodThread, audioRecorder, myHandler)).start();
                    */
                    break;
                }
                case R.id.b_button: {
                    if(bPressed == false){
                        bPressed = true;
                        aButton.setEnabled(false);
                        afterAorBPress();
                    }else{
                        bPressed = false;
                        aButton.setEnabled(true);
                        afterAandBUnpressed();
                    }
                    /*
                    aButton.setEnabled(false);
                    new Thread(new BDiffThread(decodThread, audioRecorder,playThread, myHandler)).start();
                    */
                    break;
                }
                case R.id.connect_button: {
                    if(aPressed == true){
                        startActivityForResult(new Intent(MainActivity.this, SendFileActivity.class),2);
                    }
                    else if(bPressed == true){
                        if(bConnectionPressed == false){
                            bConnectionPressed = true;
                            connectButton.setText("disconnect");
                            connectButton.setTextColor(Color.RED);
                            createGroup();
                        }else{
                            bConnectionPressed = false;
                            connectButton.setText("connect");
                            connectButton.setTextColor(Color.GREEN);
                            if(serverThread != null) {
                                serverThread.stopRunning();
                                serverThread = null;
                            }
                            removeGroup();
                        }

                    }
                    break;
                }
                case R.id.connect_ready_button: {

                    if(aPressed == true){
                        if(clientThread != null){
                            aCnt++;
                            clientThread.setStr("a pressed "+aCnt);
                        }else{
                            showToast("not connected");
                        }

                    }else if(bPressed == true){
                        if(serverThread != null){
                            bCnt++;
                            serverThread.setStr("b pressed "+bCnt);
                        }else{
                            showToast("not connected");
                        }
                    }
                    break;
                }
                case R.id.omit_button: {
                    playThread.omitChirpSignal();
                    break;
                }
            }
        }
    };

    public void initParams(){


        audioRecorder.recordingCallback(this);
        decodThread = new DecodThread(myHandler);
        decodThread.initialize(AudioRecorder.getBufferSize() / 2);
        new Thread(decodThread).start();
        playThread.start();
        aButton.setOnClickListener(onClickListener);
        bButton.setOnClickListener(onClickListener);
        omitButton.setOnClickListener(onClickListener);
        connectButton.setOnClickListener(onClickListener);
        connectReadyButton.setOnClickListener(onClickListener);




        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), this);
        broadcastReceiver = new DirectBroadcastReceiver(wifiP2pManager, channel, this);
        registerReceiver(broadcastReceiver, DirectBroadcastReceiver.getIntentFilter());
    }

    private void afterAorBPress(){
        connectButton.setEnabled(true);
        connectReadyButton.setEnabled(true);
        connectButton.setTextColor(Color.GREEN);
    }

    private void afterAandBUnpressed(){
        connectReadyButton.setEnabled(false);
        connectButton.setEnabled(false);
        bConnectionPressed = false;
        connectButton.setText("connect");
        connectButton.setTextColor(Color.GRAY);
        if(bConnectionPressed == true) {
            removeGroup();
        }
        if(aPressed == true){
            if(SendFileActivity.instance != null) {
                SendFileActivity.instance.disconnectOutside();
            }
        }
        if(serverThread != null) {
            serverThread.stopRunning();
            serverThread = null;
        }
        if(clientThread != null) {
            clientThread.stopRunning();
            clientThread = null;
        }

    }

    public void createGroup() {

        wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "createGroup onSuccess");
                showToast("onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "createGroup onFailure: " + reason);
                showToast("onFailure");
            }
        });
    }

    private void removeGroup() {
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "removeGroup onSuccess");
                showToast("onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "removeGroup onFailure");
                showToast("onFailure");
            }
        });
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
            textView.setText((String)msg.obj);
        }
    };

    @Override
    public void onDataReady(short[] data, int len) {
        short[] data1 = new short[len];
        short[] data2 = new short[len];
        for (int i = 0; i < len; i++) {
            data1[i] = data[2 * i];
            data2[i] = data[2 * i + 1];
        }
        decodThread.fillSamples(data2);
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void wifiP2pEnabled(boolean enabled) {
        Log.e(TAG, "wifiP2pEnabled: " + enabled);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Log.e(TAG, "onConnectionInfoAvailable");
        Log.e(TAG, "isGroupOwner：" + wifiP2pInfo.isGroupOwner);
        Log.e(TAG, "groupFormed：" + wifiP2pInfo.groupFormed);
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            if(serverThread == null){
                serverThread = new ServerThread(myHandler);
            }
            if(!serverThread.isAlive()){
                serverThread.start();
            }

        }
    }

    @Override
    public void onDisconnection() {
        Log.e(TAG, "onDisconnection");
        if(serverThread != null) {
            serverThread.stopRunning();
            serverThread = null;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        removeGroup();
        if(serverThread != null) {
            serverThread.stopRunning();
        }
        if(clientThread != null) {
            clientThread.stopRunning();
        }
        if(decodThread != null){
            decodThread.stopRunning();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == 1)
        {
            WifiP2pInfo wifiP2pInfo = data.getParcelableExtra("wifiP2pInfo");
            if(clientThread != null){
                clientThread.stopRunning();
            }
            clientThread = new ClientThread(myHandler,wifiP2pInfo);
            clientThread.start();

        }
    }
}
