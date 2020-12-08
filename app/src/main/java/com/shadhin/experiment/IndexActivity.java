package com.shadhin.experiment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.shadhin.experiment.custom_camera.CustomCamera2Activity;
import com.shadhin.experiment.custom_camera.CustomCameraActivity;
import com.shadhin.experiment.voice.VoiceCommandActivity;
import com.shadhin.experiment.voice.VoiceCommandServiceActivity;

public class IndexActivity extends AppCompatActivity {

    Button btnBlink;
    Button btnCustomCamera;
    Button btnVoice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        btnBlink=findViewById(R.id.btnBlink);
        btnCustomCamera=findViewById(R.id.btnCustomCamera);
        btnVoice=findViewById(R.id.btnVoice);
        btnBlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(IndexActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        btnCustomCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(IndexActivity.this, CustomCameraActivity.class);
                startActivity(intent);
            }
        }); btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(IndexActivity.this, VoiceCommandServiceActivity.class);
                startActivity(intent);
            }
        });
    }
}