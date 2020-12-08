package com.shadhin.experiment.voice;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class VoiceCommandServiceActivity extends Service implements RecognitionListener {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private SpeechRecognizer speechRecognizer;

    @Nullable
    @Override

    public IBinder onBind(Intent intent) {


        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "start Service.", Toast.LENGTH_SHORT).show();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(this);

        Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voice.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                .getPackage().getName());
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voice.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        speechRecognizer.startListening(voice);

        return START_REDELIVER_INTENT;

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }


    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle results) {

        String wordStr = null;
        String[] words = null;
        String firstWord = null;
        String secondWord = null;

        ArrayList<String> matches = results
                .getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
        wordStr = matches.get(0);
        words = wordStr.split(" ");
        firstWord = words[0];
        secondWord = words[1];

        if (firstWord.equals("open")) {
            PackageManager packageManager = getPackageManager();
            List<PackageInfo> packs = packageManager
                    .getInstalledPackages(0);
            int size = packs.size();
            boolean uninstallApp = false;
            boolean exceptFlg = false;
            for (int v = 0; v < size; v++) {
                PackageInfo p = packs.get(v);
                String tmpAppName = p.applicationInfo.loadLabel(
                        packageManager).toString();
                String pname = p.packageName;
                //URL urlAddress = urlAddress.toLowerCase();
                tmpAppName = tmpAppName.toLowerCase();
                if (tmpAppName.trim().toLowerCase().equals(secondWord.trim().toLowerCase())) {
                    PackageManager pm = this.getPackageManager();
                    Intent appStartIntent = pm.getLaunchIntentForPackage(pname);
                    if (null != appStartIntent) {
                        try {
                            this.startActivity(appStartIntent);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } // end of open app code
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}