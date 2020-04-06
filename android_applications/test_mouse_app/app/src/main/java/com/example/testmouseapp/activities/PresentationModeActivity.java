package com.example.testmouseapp.activities;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;

import com.example.testmouseapp.R;

public class PresentationModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation_mode);

        Button button_nextslide = findViewById(R.id.button_nextslide);
        Button button_prevslide = findViewById(R.id.button_prevslide);

        button_nextslide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //implement bluetooth send function here
            }
        });

        button_prevslide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //implement bluetooth send function here
            }
        });
    }
}
