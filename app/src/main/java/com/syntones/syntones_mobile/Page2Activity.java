package com.syntones.syntones_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.syntones.remote.SyntonesTimerTask;


public class Page2Activity extends AppCompatActivity {

    private Button DeletePageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);


        SyntonesTimerTask.getInstance().stopCounter();
        DeletePageBtn = (Button) findViewById(R.id.btnPageDelete);

        DeletePageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Page2Activity.this, DeleteFilesActivity.class);
                startActivity(intent);
            }
        });
    }
}
