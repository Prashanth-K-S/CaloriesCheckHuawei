package com.example.caloriescheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class SelectTypeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_type);

        //To initialize views
        initViews();
    }

    private void initViews() {

        MaterialCardView mFood = findViewById(R.id.cv_detectFood);
        mFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectTypeActivity.this, MainActivity.class));
            }
        });

        MaterialCardView mSteps = findViewById(R.id.cv_steps);
        mSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectTypeActivity.this, StepRecognitionActivity.class));
            }
        });

        MaterialCardView mFoodDetect = findViewById(R.id.cv_prescription);
        mFoodDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectTypeActivity.this, OCRActivity.class));
            }
        });
    }
}