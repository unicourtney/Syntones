package com.syntones.syntones_mobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void bottomBar(View view) {
        String btnText;

        btnText = ((Button) view).getText().toString();

        if (btnText.equals("Search")) {

            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);

        } else if (btnText.equals("Your Library")) {

            Intent intent = new Intent(this, YourLibraryActivity.class);
            startActivity(intent);
        }

    }
}
