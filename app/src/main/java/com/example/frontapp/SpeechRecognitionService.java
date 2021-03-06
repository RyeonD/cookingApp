package com.example.frontapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.service.autofill.UserData;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telecom.Call;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static android.speech.SpeechRecognizer.ERROR_AUDIO;
import static android.speech.SpeechRecognizer.ERROR_CLIENT;
import static android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS;
import static android.speech.SpeechRecognizer.ERROR_NETWORK;
import static android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT;
import static android.speech.SpeechRecognizer.ERROR_NO_MATCH;
import static android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY;
import static android.speech.SpeechRecognizer.ERROR_SERVER;
import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;
import static com.example.frontapp.App.CHANEL_ID;

public class SpeechRecognitionService extends RecognitionService {
    private String TAG = "SpeechRecognitionService";

    public static final int MSG_VOICE_RECO_READY = 0;
    public static final int MSG_VOICE_RECO_END = 1;
    public static final int MSG_VOICE_RECO_RESTART = 2;

    private SpeechRecognizer mSrRecognizer;
    boolean mBoolVoiceRecoStarted;
    protected AudioManager mAudioManager;
    Intent itIntent;//???????????? Intent
    boolean end = false;
    public static boolean yobi = false;

    Intent showIntent;

    TextToSpeech textToSpeech;

    public static void endYobi() {
        yobi = false;
    }

    public static void onDestory() {
        onDestory();
    }

    // ????????? ?????? ?????? ???
    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate");

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) { //??????????????? ???????????? ????????? ????????? ???????????????
            itIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            itIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            itIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toString());
            itIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            itIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            itIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        }
    }

    // ????????? ?????? ???
    @Override
    public void onDestroy() {
        super.onDestroy();

        end = true;
        if(mSrRecognizer != null) {
            mSrRecognizer.destroy();
        }

        if(mAudioManager != null) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        }

        Log.e(TAG, "onDestory");

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.KOREAN);
                }
            }
        });

        textToSpeech.setPitch(1.0f); //1.5??? ?????????
        textToSpeech.setSpeechRate(0.9f); //1???????????? ??????
        textToSpeech.speak("?????????????????????", TextToSpeech.QUEUE_FLUSH, null);
    }

    // ????????? ?????? ????????? ?????? ???
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");

        startListening();

        return START_STICKY;
    }

    @Override
    protected void onStartListening(Intent intent, Callback callback) {

    }

    @Override
    protected void onCancel(Callback callback) {

    }

    @Override
    protected void onStopListening(Callback callback) {

    }

    public void startListening() {
        Log.e(TAG, "Listening Start");
        if(!end){
            //??????????????? ???????????? ?????? Mute
            // Build.VERSION.SDK_INT: ?????? ????????? SDK ??????
            // Build.VERSION_CODES.M: ?????? ??????????????? ????????? ??????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                }
            } else {
                // ????????? ??????(true??? ????????? ??????)
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }


            if (!mBoolVoiceRecoStarted) { // ????????? ??????????????? ????????? ????????? ?????? ?????? ????????? ???????????? ??? ???
                if (mSrRecognizer == null) {
                    mSrRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                    mSrRecognizer.setRecognitionListener(mClsRecoListener);
                }
                mSrRecognizer.startListening(itIntent);
            }
            mBoolVoiceRecoStarted = true;  //???????????? ????????? ?????? ???
        }
    }

    public void stopListening() //Override ????????? ?????? ????????? ???????????? ?????? ??????????????? ????????? ???
    {
        try {
            if (mSrRecognizer != null && mBoolVoiceRecoStarted) {
                mSrRecognizer.stopListening(); //???????????? Override ????????? ??????
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mBoolVoiceRecoStarted = false;  //???????????? ??????
    }

    // handler
    private Handler mHdrVoiceRecoState = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VOICE_RECO_READY:
                    break;
                case MSG_VOICE_RECO_END: {
                    stopListening();
                    sendEmptyMessageDelayed(MSG_VOICE_RECO_RESTART, 1000);
                    break;
                }
                case MSG_VOICE_RECO_RESTART:
                    startListening();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    };

    // SpeechRecognizer??? ?????? ??????
    private RecognitionListener mClsRecoListener = new RecognitionListener() {
        @Override
        public void onRmsChanged(float rmsdB) {
        }


        // ?????? ?????? ????????? ????????? ??? ??????
        @Override
        public void onResults(Bundle results) {
            //Recognizer KEY??? ???????????? ????????? ???????????? ???????????? ??????
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            final String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            Log.e(TAG, "?????? + " + yobi + ">>>>>>>> "+rs[0]);

            if(yobi) {

                if(rs[0].contains("?????????")) {
                    Log.e(TAG, "????????? ???????????? ?????? ????????????.");
                    setShowIntent(getCurrentActivity());
                    speechAction("?????????");

                }
                else if(rs[0].contains("??????")) {
                    yobi = false;
                }
                else if(rs[0].contains("??????") || rs[0].contains("??????")) {
                    Log.e(TAG, "????????? ??????????????? ?????????.");

                    setShowIntent(getCurrentActivity());
                    speechAction("????????????");
                }
                else if(rs[0].contains("??????") || rs[0].contains("??????") || rs[0].contains("???")) {
                    Log.e(TAG, "????????? ????????? ???????????????.");

                    setShowIntent(getCurrentActivity());
                    speechAction("???????????????");
                }
                else {
                    Log.e(TAG, "?????? ??????????????????");
                    mBoolVoiceRecoStarted = false;
                    getApplicationContext().startService(new Intent(getApplicationContext(), SpeechRecognitionService.class));
                }
            }
            else {
                if(rs[0].contains("??????")) {
                    Log.e(TAG, "????????? ???????????? ????????? ???????????????. ?????? ?????? ????????? ?????????????????? ???????????? ??????????????????.");

                    yobi = true;

                    setShowIntent(getCurrentActivity());
                    speechAction("??????");

                    mBoolVoiceRecoStarted = false;
                }
                else {
                    mBoolVoiceRecoStarted = false;
                    getApplicationContext().startService(new Intent(getApplicationContext(), SpeechRecognitionService.class));
                }

            }
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onEndOfSpeech() {

        }

        // ?????? ?????? ???
        @Override
        public void onError(int intError) {

            switch (intError) {

                case ERROR_NETWORK_TIMEOUT:
                    //???????????? ????????????
                    break;
                case ERROR_NETWORK:
                    break;

                case ERROR_AUDIO:
                    //?????? ??????
                    break;
                case ERROR_SERVER:
                    //???????????? ????????? ??????
                    break;
                case ERROR_CLIENT:
                    //??????????????? ??????
                    break;
                case ERROR_SPEECH_TIMEOUT:
                    //?????? ????????? ?????? ????????? ???
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                    Log.e(TAG, "?????? ????????");
                    break;
                case ERROR_NO_MATCH:
                    //????????? ????????? ?????? ????????? ???
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                    Log.e(TAG, "?????? ??????");
                    break;
                case ERROR_RECOGNIZER_BUSY:
                    //RecognitionService??? ?????? ???
                    break;
                case ERROR_INSUFFICIENT_PERMISSIONS:
                    //uses - permission(??? RECORD_AUDIO) ??? ?????? ???
                    break;

            }
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onPartialResults(Bundle partialResults) { //?????? ????????? ?????? ?????? ???

        }
    };

    private String getCurrentActivity() {
        ActivityManager manager = (ActivityManager )getSystemService(Activity.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> list = manager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo info = list.get(0);

        return info.topActivity.getClassName();
    }

    private void setShowIntent(String activityName) {
        Log.e(TAG, activityName);
        if(activityName.contains("CookInfoPageActivity")) {
            showIntent = new Intent(getApplicationContext(), CookInfoPageActivity.class);
        }
        else if(activityName.contains("CookRecipePageActivity")){
            showIntent = new Intent(getApplicationContext(), CookRecipePageActivity.class);
        }
    }

    private void speechAction(String action) {
        showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        showIntent.putExtra("action",action);
        startActivity(showIntent);
    }

}