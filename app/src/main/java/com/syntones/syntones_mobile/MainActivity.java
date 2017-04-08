package com.syntones.syntones_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.syntones.remote.ScreenOnOffReceiver;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private String ipaddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorUserInfo = sharedPrefUserInfo.edit();
        final Long userID = sharedPrefUserInfo.getLong("userID", 0);
        if(userID!=0){

            String uniqueId;
            uniqueId = UUID.randomUUID().toString();

            Log.d("UUID", uniqueId);
            editorUserInfo.putString("userUUID", uniqueId);
            editorUserInfo.apply();
            Intent intent = new Intent(this, YourLibraryActivity.class);
            startActivity(intent);

            Log.e("SESH", "TRUE");
        }

/*        getIPAddress();*/
    }

/*    public String getIPAddress()  {

        WifiManager myWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        int myIp = myWifiInfo.getIpAddress();

        Log.d("IP ADDRESS", String.valueOf(myIp));

        return ipaddress;
    }*/


    public void showScreen(View view) {

        String btnText;

        btnText = ((Button) view).getText().toString();

        if (btnText.equals("LOG IN")) {

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

        } else if (btnText.equals("SIGN UP")) {

            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        }

    }


}
