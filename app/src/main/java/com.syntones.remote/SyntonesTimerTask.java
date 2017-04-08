package com.syntones.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;

import com.syntones.response.LogoutResponse;
import com.syntones.syntones_mobile.PlayerActivity;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by CourtneyLove on 1/31/2017.
 */

public class SyntonesTimerTask {

    private CountDownTimer countDownTimer, playerCountDownTimer;

    private boolean isRunning, isPlayerRunning;

    private static SyntonesTimerTask mInstance = null;


    public SyntonesTimerTask() {
    }

    public static SyntonesTimerTask getInstance() {

        if (mInstance == null) {
            mInstance = new SyntonesTimerTask();
        }
        return mInstance;
    }

    public boolean startCounter(final Context context, final String tag) {

        countDownTimer = new CountDownTimer(3600000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e(tag, String.valueOf(millisUntilFinished / 1000));
                isRunning = true;
            }

            @Override
            public void onFinish() {
                Log.e(tag, "DONE");

                doAnalysis(context);
                isRunning = false;


            }
        }.start();
        return isRunning;
    }

    public boolean startPlayerCounter(final Context context, final String tag) {

        playerCountDownTimer = new CountDownTimer(3600000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e(tag + " - Counter", String.valueOf(millisUntilFinished / 1000));
                isPlayerRunning = true;
            }

            @Override
            public void onFinish() {
                Log.e(tag, "DONE");
                SharedPreferences sharedPrefStorage = context.getSharedPreferences("storage", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorStorage = sharedPrefStorage.edit();
                editorStorage.clear();
                editorStorage.apply();
                doAnalysis(context);
                isPlayerRunning = false;


            }
        }.start();
        return isRunning;
    }

    public boolean startOfflineCounter(final Context context, final String tag) {

        countDownTimer = new CountDownTimer(50000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e(tag, String.valueOf(millisUntilFinished / 1000));
                isRunning = true;
            }

            @Override
            public void onFinish() {
                Log.e(tag, "DONE");
                convertAllSongsToText(context);
                isRunning = false;


            }
        }.start();
        return isRunning;
    }

    public void stopCounter() {


        if (countDownTimer != null) {
            countDownTimer.cancel();
            Log.e("CDT", "STOP");

        }

    }

    public void stopPlayerCounter() {


        if (playerCountDownTimer != null) {
            playerCountDownTimer.cancel();
            Log.e("PCDT", "STOP");

        }

    }

    public void isPlaying(Context context, String tag) {

        PlayerActivity playerActivity = new PlayerActivity();
        boolean isPlaying = playerActivity.isPlaying();
        stopCounter();
        stopPlayerCounter();
        if (isPlaying == true) {
            if (tag.equals("Player")) {
                stopPlayerCounter();
                Log.e("IS PLAY", "True - Player");
            } else {

                stopCounter();
                Log.e("IS PLAY", "True");
            }


        } else {
            if (tag.equals("Player")) {
                startPlayerCounter(context, tag);
                Log.e("IS PLAY", "False - Player");
            } else {
//               SyntonesTimerTask.getInstance().stopPlayerCounter();
                startCounter(context, tag);
                Log.e("IS PLAY", "False");
            }

        }
    }


    public void doAnalysis(Context sContext) {
        String userUUID;

        SyntonesWebAPI.Factory.getInstance(sContext).logout().enqueue(new Callback<LogoutResponse>() {
            @Override
            public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {

            }

            @Override
            public void onFailure(Call<LogoutResponse> call, Throwable t) {

            }
        });

        SharedPreferences sharedPrefUserInfo = sContext.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorUserInfo = sharedPrefUserInfo.edit();
        userUUID = UUID.randomUUID().toString();

        Log.d("UUID", userUUID);
        editorUserInfo.putString("userUUID", userUUID);
        editorUserInfo.apply();

    }

    public void convertAllSongsToText(Context context) {

        File extStore = Environment.getExternalStorageDirectory();
        File downloadDir = new File(extStore + context.getFilesDir().getPath() + "/Syntones/savedSongs/");

        File listAllFiles[] = downloadDir.listFiles();

        if (listAllFiles != null && listAllFiles.length > 0) {
            for (File currentFile : listAllFiles) {
                if (currentFile.isDirectory()) {

                    Log.d("DIR", currentFile.toString());


                } else {
                    if (currentFile.getName().endsWith(".mp3")) {
                        Log.d("FILE", currentFile.getName().toString());
                        File getFileDir = currentFile.getAbsoluteFile();
                        File renameFile = new File(downloadDir + "", currentFile.getName().replace(".mp3", ".txt"));
                        getFileDir.renameTo(renameFile);
                        Log.e("Path", renameFile.getAbsolutePath());


                    }
                }
            }
        }
    }


}
