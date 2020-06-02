package com.example.androidtermproject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        Intent passed_intent = getIntent();

        Bundle bundle = new Bundle();

        bundle = passed_intent.getExtras();

        Toast.makeText(getApplicationContext(), bundle.getString("id")+ "님 로그인 되셨습니다.", Toast.LENGTH_SHORT).show();

        Button button = (Button)findViewById(R.id.btn_closet);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RegisterPictureActivity.class);
                startActivity(intent);
            }
        });

    }
}
