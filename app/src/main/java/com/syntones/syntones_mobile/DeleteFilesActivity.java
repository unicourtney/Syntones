package com.syntones.syntones_mobile;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import okhttp3.Cache;

public class DeleteFilesActivity extends AppCompatActivity {
    private Button DeleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_files);

        DeleteBtn = (Button) findViewById(R.id.btnDelete);

        DeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                File cacheDir = getExternalCacheDir();
                File cacheDir = getExternalCacheDir();
                File downloadDir = new File("/storage");

                File listAllFiles[] = downloadDir.listFiles();

                if (listAllFiles != null && listAllFiles.length > 0) {
                    for (File currentFile : listAllFiles) {
                        if (currentFile.isDirectory()) {

                                Log.d("DIR", currentFile.toString());


                        } else {
                            if (currentFile.getName().endsWith("")) {
                                Log.d("FILE", currentFile.toString());
/*                                File file = new File(currentFile.getAbsolutePath());

                                file.delete();*/
                            }
                        }
                    }
                }
            }
        });
    }


}
