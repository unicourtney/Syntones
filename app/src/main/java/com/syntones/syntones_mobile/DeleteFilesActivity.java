package com.syntones.syntones_mobile;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.syntones.remote.CustomListAdapter;
import com.syntones.remote.SyntonesTimerTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class DeleteFilesActivity extends AppCompatActivity {
    private Button DeleteBtn, GetUUIDBtn, CreateFileBtn, Page2Btn;
    private DeleteFilesActivity context;
    private TextView LyricsTv;
    private boolean mBounded;
    private Timer timer;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> sampleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_files);

        DeleteBtn = (Button) findViewById(R.id.btnDelete);
        Page2Btn = (Button) findViewById(R.id.btnPage2);


//        arrayAdapter = new ArrayAdapter<String>(this, R.layout.mylist, R.id.Itemname, sampleList);
//        CustomListAdapter adapter=new CustomListAdapter(this, sampleList, imgid);
//        SampleLv.setAdapter(adapter);
        SyntonesTimerTask.getInstance().isPlaying(DeleteFilesActivity.this, "Delete");
        Page2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeleteFilesActivity.this, Page2Activity.class);
                startActivity(intent);

            }
        });
        DeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                File cacheDir = getExternalCacheDir();
                File cacheDir = getCacheDir();
                File extStore = Environment.getExternalStorageDirectory();
//                File downloadDir = new File(extStore+"/data/data/com.syntones.syntones_mobile/cache");
                File downloadDir = new File(extStore + getFilesDir().getPath() + "/Syntones/savedSongs/");

                File mp3File = new File(extStore + getFilesDir().getPath() + "/Syntones/savedSongssavedSongs/");
                mp3File.delete();
                Log.e("FILE LOC", mp3File.getPath());
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
/*                                File file = new File(currentFile.getAbsolutePath());*/


                            }
                        }
                    }
                }
            }
        });


    }


}


