package com.example.frontapp;

import android.annotation.SuppressLint;
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
import android.telecom.Call;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

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
    Intent itIntent;//음성인식 Intent
    boolean end = false;

    String CHANNEL_ID = "channel_1";

    // 서비스 처음 실행 시
    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate");

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) { //시스템에서 음성인식 서비스 실행이 가능하다면
            itIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            itIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            itIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toString());
            itIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            itIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            itIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        }
    }

    // 서비스 종료 시
    @Override
    public void onDestroy() {
        super.onDestroy();

        end = true;
        mSrRecognizer.destroy();

        if(mAudioManager != null) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        }
        Log.e(TAG, "onDestory");
    }

    // 서비스 다른 곳에서 실행 시
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
            //음성인식을 시작하기 위해 Mute
            // Build.VERSION.SDK_INT: 현재 기기의 SDK 버전
            // Build.VERSION_CODES.M: 현재 안드로이드 플랫폼 버전
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                }
            } else {
                // 음소거 설정(true라 음소거 해제)
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }


            if (!mBoolVoiceRecoStarted) { // 최초의 실행이거나 인식이 종료된 후에 다시 인식을 시작하려 할 때
                if (mSrRecognizer == null) {
                    mSrRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                    mSrRecognizer.setRecognitionListener(mClsRecoListener);
                }
                mSrRecognizer.startListening(itIntent);
            }
            mBoolVoiceRecoStarted = true;  //음성인식 서비스 실행 중
        }
    }

    public void stopListening() //Override 함수가 아닌 한번만 호출되는 함수 음성인식이 중단될 때
    {
        try {
            if (mSrRecognizer != null && mBoolVoiceRecoStarted) {
                mSrRecognizer.stopListening(); //음성인식 Override 중단을 호출
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mBoolVoiceRecoStarted = false;  //음성인식 종료
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

    // SpeechRecognizer의 객체 정의
    private RecognitionListener mClsRecoListener = new RecognitionListener() {
        @Override
        public void onRmsChanged(float rmsdB) {
        }


        // 음성 인식 결과가 나왔을 때 호출
        @Override
        public void onResults(Bundle results) {
            //Recognizer KEY를 사용하여 인식한 결과값을 가져오는 코드
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            final String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            Log.e(TAG, "결과>>>>>>>> "+rs[0]);
            mBoolVoiceRecoStarted = false;
            getApplicationContext().startService(new Intent(getApplicationContext(), SpeechRecognitionService.class));
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onEndOfSpeech() {

        }

        // 에러 발생 시
        @Override
        public void onError(int intError) {

            switch (intError) {

                case ERROR_NETWORK_TIMEOUT:
                    //네트워크 타임아웃
                    break;
                case ERROR_NETWORK:
                    break;

                case ERROR_AUDIO:
                    //녹음 에러
                    break;
                case ERROR_SERVER:
                    //서버에서 에러를 보냄
                    break;
                case ERROR_CLIENT:
                    //클라이언트 에러
                    break;
                case ERROR_SPEECH_TIMEOUT:
                    //아무 음성도 듣지 못했을 때
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                    Log.e(TAG, "뭐라 했니??");
                    break;
                case ERROR_NO_MATCH:
                    //적당한 결과를 찾지 못했을 때
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                    Log.e(TAG, "결과 없음");
                    break;
                case ERROR_RECOGNIZER_BUSY:
                    //RecognitionService가 바쁠 때
                    break;
                case ERROR_INSUFFICIENT_PERMISSIONS:
                    //uses - permission(즉 RECORD_AUDIO) 이 없을 때
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
        public void onPartialResults(Bundle partialResults) { //부분 인식을 성공 했을 때

        }
    };

}