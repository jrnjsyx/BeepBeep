package com.example.jrnjsyx.beepbeep;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import physical.AudioRecorder;
import physical.PlayThread;
import processing.ADiffThread;
import processing.BDiffThread;
import processing.DecodThread;
import utils.FlagVar;


public class MainActivity extends AppCompatActivity implements AudioRecorder.RecordingCallback{



    @BindView(R.id.a_button)
    Button aButton;
    @BindView(R.id.b_button)
    Button bButton;
    @BindView(R.id.text)
    TextView textView;
    @BindView(R.id.omit_button)
    Button omitButton;

    private AudioRecorder audioRecorder = new AudioRecorder();
    private PlayThread playThread = new PlayThread();
    private DecodThread decodThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initParams();
    }
    public void initParams(){
        audioRecorder.recordingCallback(this);
        decodThread = new DecodThread(myHandler);
        decodThread.setProcessBufferSize(AudioRecorder.getBufferSize() / 2);
        new Thread(decodThread).start();
        playThread.start();
        aButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bButton.setEnabled(false);
                decodThread.decodeStart();
                audioRecorder.startRecord();
                sleep(500);
                playThread.omitChirpSignal();

                new Thread(new ADiffThread(decodThread, audioRecorder, myHandler)).start();


            }
        });
        bButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aButton.setEnabled(false);
                new Thread(new BDiffThread(decodThread, audioRecorder,playThread, myHandler)).start();

            }
        });
        omitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playThread.omitChirpSignal();
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
            textView.setText("sampleCnt:"+msg.arg1+"    distance:"+distance);
            switch (msg.arg2){
                case FlagVar.A_BUTTON_ENABLE:{
                    aButton.setEnabled(true);
                    break;
                }
                case FlagVar.B_BUTTON_ENABLE:{
                    bButton.setEnabled(true);
                    break;
                }
            }
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
}
