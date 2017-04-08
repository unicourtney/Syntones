package com.syntones.syntones_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.syntones.model.Playlist;
import com.syntones.model.User;
import com.syntones.remote.SyntonesTimerTask;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.LoginResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.Timer;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private LoginActivity lContext;
    private EditText UsernameEt, PasswordEt;
    @BindView(R.id.btnLogIn)
    private Button login;
    private Button btn_signUp;
    private TextView LoginMessageTv, SignUpTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        UsernameEt = (EditText) findViewById(R.id.etUsername);
        PasswordEt = (EditText) findViewById(R.id.etPassword);
        LoginMessageTv = (TextView) findViewById(R.id.tvLoginMessage);
        SignUpTv = (TextView) findViewById(R.id.tvSignUp);

        UsernameEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsernameEt.setText("");
            }
        });

        PasswordEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordEt.setText("");
            }
        });

        SignUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

    }

    @OnClick(R.id.btnLogIn)
    public void logIn(View view) {

        final String username = UsernameEt.getText().toString().trim();
        String password = new String(Hex.encodeHex(DigestUtils.md5(PasswordEt.getText().toString())));

        SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(lContext);

        User user = new User();

        user.setUsername(username);
        user.setPassword(password);

        syntonesWebAPI.logInUser(user);

        SyntonesWebAPI.Factory.getInstance(lContext).logInUser(user).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();


                SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorUserInfo = sharedPrefUserInfo.edit();

                if (loginResponse.getMessage().getFlag() == true) {
                    User user = loginResponse.getUser();
                    editorUserInfo.putString("username", username);
                    editorUserInfo.putLong("userID", user.getUserId());


                    String uniqueId = null;
                    if (uniqueId == null) {
                        uniqueId = UUID.randomUUID().toString();

                        Log.d("UUID", uniqueId);
                        editorUserInfo.putString("userUUID", uniqueId);
                    }

                    Log.d("USER ID", String.valueOf(user.getUserId()));
                    editorUserInfo.apply();

                    LoginMessageTv.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(LoginActivity.this, YourLibraryActivity.class);
                    startActivity(intent);
                } else {
                    LoginMessageTv.setVisibility(View.VISIBLE);
                }


//                Log.e("Login Response: ", loginResponse.getMessage().getMessage());

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

            }
        });

    }


}
