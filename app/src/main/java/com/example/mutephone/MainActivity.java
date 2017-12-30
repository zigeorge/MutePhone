package com.example.mutephone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

    TextView textView;
    SensorManager sensorManager;
    Sensor sensor, sensor_accelerometer;
    static boolean faceDown, flipOver;
    private AudioManager myAudioManager;
    int deviceRingingMode;

    TelephonyManager telephonyManager;

    MainActivity context;

    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        textView = findViewById(R.id.withText);
        myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    PhoneStateListener phoneStateListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.e("incomingNumber","State "+state);
            if(state == 1){
                deviceRingingMode = myAudioManager.getRingerMode();
                sensorManager.registerListener(context, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(context, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
            }else {
                sensorManager.unregisterListener(context);
                myAudioManager.setRingerMode(deviceRingingMode);
            }
        }
    };

    BroadcastReceiver phoneStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Broadcast", "Receive");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (sensorEvent.values[2] < 0) {
                flipOver = true;
            } else {
                flipOver = false;
            }
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (sensorEvent.values[0] < 5) {
                faceDown = true;
            } else {
                faceDown = false;
            }
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
//                while (true){
                synchronized (this) {
                    try {
                        if (faceDown || flipOver) {
                            Log.e("SENSOR", "sensor changed occurred");
                            if (myAudioManager != null) {
                                myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                sensorManager.unregisterListener(context);
                            } else {
                                Toast.makeText(getApplicationContext(), "AudioManager is null", Toast.LENGTH_SHORT).show();
                            }
                        } else if (faceDown) {
                            if (myAudioManager != null) {
                                myAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                sensorManager.unregisterListener(context);
                            } else {
                                Toast.makeText(getApplicationContext(), "AudioManager is null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
