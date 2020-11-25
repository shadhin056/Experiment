package com.shadhin.experiment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.shadhin.experiment.custom_camera.CustomCameraActivity;

public class IndexActivity extends AppCompatActivity {

    Button btnBlink;
    Button btnCustomCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        btnBlink=findViewById(R.id.btnBlink);
        btnCustomCamera=findViewById(R.id.btnCustomCamera);
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
        });
    }
}