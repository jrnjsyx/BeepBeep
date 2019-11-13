package com.example.jrnjsyx.beepbeep.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jrnjsyx.beepbeep.R;
import com.example.jrnjsyx.beepbeep.physical.thread.PlayThread;
import com.example.jrnjsyx.beepbeep.physical.thread.RecordThread;
import com.example.jrnjsyx.beepbeep.processing.Algorithm;
import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.processing.thread.ADiffThread;
import com.example.jrnjsyx.beepbeep.processing.thread.BDiffThread;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.ejml.data.DMatrixRMaj;
import org.ejml.equation.Equation;
import org.ejml.equation.Sequence;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements DirectActionListener {


    protected static final String TAG = "MainActivity";

    @BindView(R.id.a_button)
    Button aButton;
    @BindView(R.id.b_button)
    Button bButton;
    @BindView(R.id.text)
    TextView networkTextView;
    @BindView(R.id.start_button)
    Button startButton;
    @BindView(R.id.connect_button)
    Button connectButton;
    @BindView(R.id.connect_ready_button)
    Button connectReadyButton;
    @BindView(R.id.main_linear)
    LinearLayout mainLinear;
    @BindView(R.id.end_button)
    Button endButton;
    @BindView(R.id.distance)
    TextView distanceTextView;
    @BindView(R.id.debug)
    TextView debugTextView;
    @BindView(R.id.textAll)
    LinearLayout textLinear;
    private EditText q11EditText;
    private EditText q12EditText;
    private EditText q22EditText;
    private EditText r11EditText;
    private EditText r12EditText;
    private EditText r22EditText;

    private GraphView distanceGraph;
    private GraphView speedGraph;

    private WifiP2pManager wifiP2pManager;

    private WifiP2pManager.Channel channel;

    private BroadcastReceiver broadcastReceiver;



    private RecordThread recordThread;
    private PlayThread playThread;
    private DecodeThread decodeThread;
    private WifiP2pThread serverThread;
    private WifiP2pThread clientThread;
    private WifiP2pThread currentP2pThread;
    private ConnetJudgeThread connetJudgeThread;
    private ADiffThread aDiffThread;
    private BDiffThread bDiffThread;

    private WifiP2pInfo wifiP2pInfo;
    private boolean aMode = false;
    private boolean bMode = false;
    private int connectionMode = NO_SERVER_CLIENT_MODE;
    static int SERVER_MODE = 1;
    static int CLIENT_MODE = 2;
    static int NO_SERVER_CLIENT_MODE = 0;
    private boolean isConnected = false;
    private int aCnt = 0;
    private int bCnt = 0;
    private boolean isAppReady = false;
    private boolean started = false;

    private LineGraphSeries<DataPoint> unhandledDistanceSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> unhandledSpeedSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> distanceSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> speedSeries = new LineGraphSeries<>();
    private int graphSize = 50;
    private DataPoint[] unhandledDistancePoints = new DataPoint[graphSize];
    private DataPoint[] unhandledSpeedPoints = new DataPoint[graphSize];
    private DataPoint[] distancePoints = new DataPoint[graphSize];
    private DataPoint[] speedPoints = new DataPoint[graphSize];

    private int graphCnt = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        test();
        initParams();

    }

    private void test(){
        System.out.println("test");
        if(Common.isDebug){
            int[] data1 = new int[100];
            float[] data2 = new float[data1.length];
            for(int i=0;i<data1.length;i++){
                data1[i] = (int) (1000*Math.random());
                data2[i] = 1000*(float)Math.random();
            }
            Algorithm.quickSort(data1,0,data1.length-1);
            Algorithm.quickSort(data2,0,data1.length-1);
            System.out.println(Arrays.toString(data1));
            System.out.println(Arrays.toString(data2));
            DMatrixRMaj x = new DMatrixRMaj(3,3);
            DMatrixRMaj y = new DMatrixRMaj(3,3);
            x.set(0,0,2.0);
            x.set(1,1,4.0);
            x.set(2,2,5.0);
            System.out.println(x);
            Equation eq = new Equation();
            eq.alias(x,"x",y,"y");

            Sequence predictX = eq.compile("x = inv(x)");
            predictX.perform();
            System.out.println(x);


        }
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
                    afterMainLinearPress();
                    break;
                }
                case R.id.end_button:{
                    afterEndButtonPress();
                    break;
                }
            }
        }
    };



    //初始化参数
    public void initParams(){
        distanceGraph = (GraphView)findViewById(R.id.distanceGraph);
        speedGraph = (GraphView)findViewById(R.id.speedGraph);
        distanceGraph.addSeries(distanceSeries);
        distanceGraph.addSeries(unhandledDistanceSeries);
        speedGraph.addSeries(speedSeries);
        speedGraph.addSeries(unhandledSpeedSeries);
        unhandledDistanceSeries.setColor(Color.RED);
        unhandledSpeedSeries.setColor(Color.RED);
        distanceSeries.setColor(Color.BLUE);
        distanceSeries.setColor(Color.BLUE);


        aButton.setOnClickListener(onClickListener);
        bButton.setOnClickListener(onClickListener);
        startButton.setOnClickListener(onClickListener);
        connectButton.setOnClickListener(onClickListener);
        connectReadyButton.setOnClickListener(onClickListener);
        mainLinear.setOnClickListener(onClickListener);
        endButton.setOnClickListener(onClickListener);

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), this);
        broadcastReceiver = new DirectBroadcastReceiver(wifiP2pManager, channel, this);
        registerReceiver(broadcastReceiver, DirectBroadcastReceiver.getIntentFilter());
    }

    private void afterEndButtonPress(){
        currentP2pThread.setMessage(FlagVar.rangingEndStr);
        sleep(100);
        stopRanging();
    }

    private void afterMainLinearPress(){
        if(checkPermission()){
            setButtonEnabled(aButton,true);
            setButtonEnabled(bButton,true);
            setButtonEnabled(connectButton,true);
            mainLinear.setClickable(false);
            isAppReady = true;
        }
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
                currentP2pThread.setMessage("a pressed "+aCnt);
                Common.println("a pressed");
            }else{
                showToast("not connected");
            }
        }
        //假如b按下
        else if(aButton.isEnabled() == false){
            if(currentP2pThread != null){
                bCnt++;
                currentP2pThread.setMessage("b pressed "+bCnt);
                Common.println("b pressed");
            }else{
                showToast("not connected");
            }
        }
    }

    private void afterStartButtonPress(){
        if(aMode){
            currentP2pThread.setMessage(FlagVar.aModeStr);
        }
        else if(bMode){
            currentP2pThread.setMessage(FlagVar.bModeStr);
        }
    }

    private void aModeStart(){

        currentP2pThread.setMessage(FlagVar.aModeStr);
        showToast("开始测距。");
        if(FlagVar.currentRangingMode == FlagVar.BEEP_BEEP_MODE) {
            playThread = new PlayThread(Decoder.lowUpChirp);
        }
        else if(FlagVar.currentRangingMode == FlagVar.MY_NEW_MODE){
//            playThread = new PlayThread(Decoder.lowChirp);
            playThread = new PlayThread(Decoder.lowUpChirp);
        }else if(FlagVar.currentRangingMode == FlagVar.MY_ORIGINAL_MODE){
            playThread = new PlayThread(Decoder.lowUpChirp);
        }
        playThread.start();
        decodeThread = new DecodeThread(myHandler,currentP2pThread,playThread,true);
        new Thread(decodeThread).start();
        recordThread = new RecordThread(decodeThread);
        recordThread.start();
        setButtonEnabled(connectReadyButton,false);
        setButtonEnabled(startButton,false);
        setButtonEnabled(aButton,false);
        setButtonEnabled(bButton,false);
        setButtonEnabled(endButton,true);
        currentP2pThread.removeListener(FlagVar.rangingStartStr);
        aDiffThread = new ADiffThread(decodeThread,myHandler,FlagVar.playBufferSize);
        aDiffThread.start();
        started = true;
    }
    private void bModeStart(){
        currentP2pThread.setMessage(FlagVar.bModeStr);
        showToast("开始测距。");
        if(FlagVar.currentRangingMode == FlagVar.BEEP_BEEP_MODE) {
            playThread = new PlayThread(Decoder.highDownChirp);
        }else if(FlagVar.currentRangingMode == FlagVar.MY_NEW_MODE){
//            playThread = new PlayThread(Decoder.highDownChirp);
            playThread = new PlayThread(Decoder.highChirp);
        }else if(FlagVar.currentRangingMode == FlagVar.MY_ORIGINAL_MODE){
            playThread = new PlayThread(Decoder.chirpAndSine);
        }
        playThread.start();
        decodeThread = new DecodeThread(myHandler,currentP2pThread,playThread,false);
        new Thread(decodeThread).start();
        recordThread = new RecordThread(decodeThread);
        recordThread.start();
        setButtonEnabled(connectReadyButton,false);
        setButtonEnabled(startButton,false);
        setButtonEnabled(aButton,false);
        setButtonEnabled(bButton,false);
        setButtonEnabled(endButton,true);
        currentP2pThread.removeListener(FlagVar.rangingStartStr);
        bDiffThread = new BDiffThread(decodeThread,myHandler,FlagVar.playBufferSize);
        bDiffThread.start();
        started = true;
    }

    private NetworkMsgListener rangingStartlistener = new NetworkMsgListener() {
        private long startTime;
        private long delay = FlagVar.playThreadDelay;//毫秒
        private boolean recved = false;
        @Override
        public void handleMsg(String msg) {
            Common.println("rangingStartlistener");
            if(aMode){
                if(msg.equals(FlagVar.bModeStr)){
                    Common.println("get aModeStr.");
                    startTime = (new Date()).getTime();
                    recved = true;
                }
                else if(msg.equals(FlagVar.aModeStr)){
                    showToast("未能同时开启A、B两种模式。");
                }

            }
            else if(bMode){
                if(msg.equals(FlagVar.aModeStr)){
                    Common.println("get bModeStr.");
                    startTime = (new Date()).getTime();
                    recved = true;
                }
                else if(msg.equals(FlagVar.bModeStr)){
                    showToast("未能同时开启A、B两种模式。");
                }

            }
        }

        @Override
        public void doPerTurn(){
            if(aMode){
                if(recved){
                    Common.println("recved");
                    long time = (new Date()).getTime();
                    if(time-startTime > delay){
                        Common.println("aModeStart");
                        recved = false;
                        aModeStart();
                    }
                }
            }else if(bMode){
                if(recved){
                    Common.println("recved");
                    long time = (new Date()).getTime();
                    if(time-startTime > delay){
                        Common.println("bModeStart");
                        recved = false;
                        bModeStart();
                    }
                }
            }
        }
    };

    private void judgeCanStart(){
        if(isConnected && (aMode || bMode)){
            setButtonEnabled(startButton,true);
            Common.println("judegeCanStart.");
            currentP2pThread.addListener(FlagVar.rangingStartStr,rangingStartlistener);
            NetworkMsgListener rangingEndListener = new NetworkMsgListener() {
                @Override
                public void handleMsg(String msg) {
                    if(msg.equals(FlagVar.rangingEndStr)) {
                        currentP2pThread.setMessage(FlagVar.rangingEndStr);
                        stopRanging();
                        currentP2pThread.removeListener(FlagVar.rangingEndStr);

                    }
                }
            };
            currentP2pThread.addListener(FlagVar.rangingEndStr,rangingEndListener);
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
                Common.println("server start listener.");
                if(msg.equals(FlagVar.connectionStartStr)){
                    currentP2pThread.setMessage(FlagVar.connectionStartStr);
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
               sleep(10);
                Common.println("client start listener.");

                clientThread = new ClientThread(myHandler, wifiP2pInfo.groupOwnerAddress.getHostAddress());
                clientThread.start();
                currentP2pThread = clientThread;

                currentP2pThread.setMessage(FlagVar.connectionStartStr);
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

    private DecimalFormat decimalFormat = new DecimalFormat("##0.0");

    public Handler myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == FlagVar.NETWORK_TEXT) {
                String str = (String) msg.obj;
                networkTextView.setText(str);

                if (str.equals(FlagVar.connectionThreadEndStr)) {
                    isConnected = false;
                    judgeCanStart();
                }
            }
            else if(msg.what == FlagVar.DISTANCE_TEXT){
                Bundle bundle = msg.getData();
                int distanceCnt = bundle.getInt("unhandledDistanceCnt");
                float unhandledDistance = FlagVar.cSample * distanceCnt;
                String unhandledDistanceStr = decimalFormat.format(unhandledDistance);
                String unhandledSpeedStr = decimalFormat.format(bundle.getFloat("unhandledSpeed"));

                String speedStr = decimalFormat.format(bundle.getFloat("speed"));
                String distanceStr = decimalFormat.format(bundle.getFloat("distance"));

                insertToLineGraphSeries(unhandledDistanceSeries, unhandledDistancePoints,unhandledDistance);
                insertToLineGraphSeries(unhandledSpeedSeries, unhandledSpeedPoints,bundle.getFloat("unhandledSpeed"));
                insertToLineGraphSeries(distanceSeries,distancePoints,bundle.getFloat("distance"));
                insertToLineGraphSeries(speedSeries,speedPoints,bundle.getFloat("speed"));
                if(graphCnt > graphSize){
                    distanceGraph.getViewport().setXAxisBoundsManual(true);
                    speedGraph.getViewport().setXAxisBoundsManual(true);
                    distanceGraph.getViewport().setMinX(graphCnt-graphSize);
                    distanceGraph.getViewport().setMaxX(graphCnt);
                    speedGraph.getViewport().setMinX(graphCnt-graphSize);
                    speedGraph.getViewport().setMaxX(graphCnt);
                }

                graphCnt++;
                String str = "distanceCnt:"+distanceCnt+"\nunhandledDistance:"+unhandledDistanceStr+"\nunhandledSpeed:"+unhandledSpeedStr+"\ndistance:"+distanceStr+"\nspeed:"+speedStr;
                distanceTextView.setText(str);
            }
            else if(msg.what == FlagVar.DEBUG_TEXT){
                int diff1 = msg.arg1;
                int diff2 = msg.arg2;
                String str = "diff:"+diff1+"\nremoteDiff:"+diff2;
                debugTextView.setText(str);
                Boolean isAdjusted = (Boolean)msg.obj;
                if(isAdjusted){
                    debugTextView.setTextColor(Color.RED);
                }
                else{
                    debugTextView.setTextColor(Color.BLACK);
                }
            }


        }
    };

    private void insertToLineGraphSeries(LineGraphSeries<DataPoint> series,DataPoint[] points,float val){
        DataPoint dataPoint = new DataPoint(graphCnt,val);
        if(graphCnt < graphSize){
            points[graphCnt] = dataPoint;
            DataPoint[] buffer = new DataPoint[graphCnt+1];
            System.arraycopy(points,0,buffer,0,graphCnt+1);
            series.resetData(buffer);
        }else{
            System.arraycopy(points,1,points,0,graphSize-1);
            points[graphSize-1] = dataPoint;
            series.resetData(points);
        }

    }



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
        if(!isAppReady){
            return;
        }
        Log.e(TAG, "onDisconnection");
        stopRanging();

    }

    private void stopRanging(){

        if(currentP2pThread != null) {
            currentP2pThread.stopRunning();
        }
//        currentP2pThread = null;
//        serverThread = null;
//        clientThread = null;
        aMode = false;
        bMode = false;
        setButtonEnabled(connectButton,true);
        setButtonEnabled(aButton,true);
        setButtonEnabled(bButton,true);
        setButtonEnabled(endButton,false);
        setButtonEnabled(startButton,false);
        isConnected = false;
        started = false;
        judgeCanStart();
        if(currentP2pThread != null) {
            currentP2pThread.removeListener(FlagVar.connectionStartStr);
            currentP2pThread.removeListener(FlagVar.rangingStartStr);
        }
        if(playThread != null) {
            playThread.stopRunning();
            Common.println("play stop.");
        }
        if(recordThread != null){
            recordThread.stopRunning();
            Common.println("record stop.");
        }
        if(decodeThread != null) {
            if(Common.isDebug) {
                Common.saveShorts(decodeThread.savedData, "savedData");
            }
            decodeThread.stopRunning();
            Common.println("decode stop.");
        }
        if(aDiffThread != null){
            aDiffThread.stopRunning();
            Common.println("aDiff stop.");
        }
        if(bDiffThread != null){
            bDiffThread.stopRunning();
            Common.println("bDiff stop.");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        /**
         * add(int groupId, int itemId, int order, CharSequence title)，为菜单添加选项
         * 1）groupId：菜单组 标识
         * 2）itemId：菜单项标识，当用户点击菜单的时候，需要根据这个标识来判断，点击的是谁
         * 3）order：菜单排序，数值越小越排在前
         * 4）title：菜单名称
         */
        SubMenu showSettingSubMenu = menu.addSubMenu("显示模式");
        showSettingSubMenu.add(FlagVar.SHOW_SETTING,FlagVar.GRAPH_MODE, 0, "图表模式");
        showSettingSubMenu.add(FlagVar.SHOW_SETTING,FlagVar.STRING_MODE,1,"文字模式");
        menu.add(FlagVar.METRIC_SETTING,FlagVar.METRIC_MODE,0,"参数设置");
        SubMenu seriesSettingSubMenu = menu.addSubMenu("隐藏/显示数据");
        seriesSettingSubMenu.add(FlagVar.SERIES_SETTING,FlagVar.UNHANDLED_SERIES_MODE,0,"显示原始数据");
        seriesSettingSubMenu.add(FlagVar.SERIES_SETTING,FlagVar.HANDLED_SERIES_MODE,1,"显示新数据");
        seriesSettingSubMenu.add(FlagVar.SERIES_SETTING,FlagVar.ALL_SERIES_MODE,2,"显示全部数据");
        SubMenu selfAdaptionSettingMenu = menu.addSubMenu("自适应模式");
        selfAdaptionSettingMenu.add(FlagVar.SELF_ADAPTION_SETTING,FlagVar.SELF_ADAPTION_MODE,0,"自适应模式");
        selfAdaptionSettingMenu.add(FlagVar.SELF_ADAPTION_SETTING,FlagVar.FREE_MODE,1,"手动模式");
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case FlagVar.GRAPH_MODE:
                FlagVar.currentShowMode = FlagVar.GRAPH_MODE;
                distanceGraph.setVisibility(View.VISIBLE);
                speedGraph.setVisibility(View.VISIBLE);
                textLinear.setVisibility(View.GONE);
                break;
            case FlagVar.STRING_MODE:
                FlagVar.currentShowMode = FlagVar.STRING_MODE;
                distanceGraph.setVisibility(View.GONE);
                speedGraph.setVisibility(View.GONE);
                textLinear.setVisibility(View.VISIBLE);
                break;
            case FlagVar.METRIC_MODE:
                if(FlagVar.currentSelfAdaptionMode == FlagVar.FREE_MODE) {
                    LoginDialog();
                }
                else{
                    showToast("请取消自适应模式");
                }
                break;
            case FlagVar.UNHANDLED_SERIES_MODE:
                FlagVar.currentShowMode = FlagVar.UNHANDLED_SERIES_MODE;
                distanceGraph.removeAllSeries();
                distanceGraph.addSeries(unhandledDistanceSeries);
                speedGraph.removeAllSeries();
                speedGraph.addSeries(unhandledSpeedSeries);
                break;
            case FlagVar.HANDLED_SERIES_MODE:
                FlagVar.currentShowMode = FlagVar.HANDLED_SERIES_MODE;
                distanceGraph.removeAllSeries();
                distanceGraph.addSeries(distanceSeries);
                speedGraph.removeAllSeries();
                speedGraph.addSeries(speedSeries);
                break;
            case FlagVar.ALL_SERIES_MODE:
                FlagVar.currentShowMode = FlagVar.HANDLED_SERIES_MODE;
                distanceGraph.removeAllSeries();
                distanceGraph.addSeries(distanceSeries);
                distanceGraph.addSeries(unhandledDistanceSeries);
                speedGraph.removeAllSeries();
                speedGraph.addSeries(speedSeries);
                speedGraph.addSeries(unhandledSpeedSeries);
                break;
            case FlagVar.SELF_ADAPTION_MODE:
                FlagVar.currentSelfAdaptionMode = FlagVar.SELF_ADAPTION_MODE;
                break;
            case FlagVar.FREE_MODE:
                FlagVar.currentSelfAdaptionMode = FlagVar.FREE_MODE;
                break;

        }
        return true;
    }




    private DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    FlagVar.Q.set(0,0,getNumber(q11EditText.getText().toString()));
                    FlagVar.Q.set(0,1,getNumber(q12EditText.getText().toString()));
                    FlagVar.Q.set(1,0,getNumber(q12EditText.getText().toString()));
                    FlagVar.Q.set(1,1,getNumber(q22EditText.getText().toString()));
                    FlagVar.R.set(0,0,getNumber(r11EditText.getText().toString()));
                    FlagVar.R.set(0,1,getNumber(r12EditText.getText().toString()));
                    FlagVar.R.set(1,0,getNumber(r12EditText.getText().toString()));
                    FlagVar.R.set(1,1,getNumber(r22EditText.getText().toString()));
                    break;
                }
                case DialogInterface.BUTTON_NEUTRAL: {

                    break;
                }
            }
        }

    };

    private double getNumber(String str){
        if(str == null || str.length() == 0){
            return 0;
        }
        return Double.parseDouble(str);

    }

    public void LoginDialog () {


        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View longinDialogView = layoutInflater.inflate(R.layout.metric_setting, null);

        q11EditText = longinDialogView.findViewById(R.id.edit_q11);
        q12EditText = longinDialogView.findViewById(R.id.edit_q12);
        q22EditText = longinDialogView.findViewById(R.id.edit_q22);
        r11EditText = longinDialogView.findViewById(R.id.edit_r11);
        r12EditText = longinDialogView.findViewById(R.id.edit_r12);
        r22EditText = longinDialogView.findViewById(R.id.edit_r22);

        Dialog dialog = new AlertDialog.Builder(this)
             .setTitle("登录框")
             .setView(longinDialogView)                //加载自定义的对话框式样
             .setPositiveButton("确定", dialogOnClickListener)
             .setNeutralButton("取消", dialogOnClickListener)
             .create();

        q11EditText.setText(FlagVar.Q.get(0,0)+"");
        q12EditText.setText(FlagVar.Q.get(0,1)+"");
        q22EditText.setText(FlagVar.Q.get(1,1)+"");
        r11EditText.setText(FlagVar.R.get(0,0)+"");
        r12EditText.setText(FlagVar.R.get(0,1)+"");
        r22EditText.setText(FlagVar.R.get(1,1)+"");

        dialog.show();
    }




}
