package com.syntones.syntones_mobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }



    public void showScreen(View view){

      String btnText;

         btnText = ((Button)view).getText().toString();

            if(btnText.equals("LOG IN")){

                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);

            }

            else if(btnText.equals("SIGN UP")){

                    Intent intent = new Intent(this, SignupActivity.class);
                    startActivity(intent);
             }

    }



}
