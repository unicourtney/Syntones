package com.syntones.syntones_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.LoggingEventHandler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.syntones.model.Genre;
import com.syntones.model.Playlist;
import com.syntones.model.Song;
import com.syntones.model.Tag;
import com.syntones.model.User;
import com.syntones.remote.CustomListAdapter;
import com.syntones.remote.ScreenOnOffReceiver;
import com.syntones.remote.SyntonesTimerTask;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.GeneratePlaylistResponse;
import com.syntones.response.GenreResponse;
import com.syntones.response.LogoutResponse;
import com.syntones.response.PlaylistResponse;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateBySongsActivity extends AppCompatActivity {

    private EditText GeneratedPlaylistNameEt;
    private Button SaveGeneratedPlaylistBtn, CancelGeneratedPlaylistBtn;
    private ListView GeneratedSongsByLv;
    private ArrayList<String> songs = new ArrayList<>();
    private List<Song> generatedSongs = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private GenerateBySongsActivity sContext;
    private SyntonesTimerTask syntonesTimerTask = new SyntonesTimerTask();
    private RelativeLayout GenerateBySongsRl;
    private ScreenOnOffReceiver onoffReceiver = new ScreenOnOffReceiver("GENERATE PLAYLIST");
    private CustomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_by_songs);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(onoffReceiver, filter);

        GeneratedPlaylistNameEt = (EditText) findViewById(R.id.etGeneratedPlaylistName);
        SaveGeneratedPlaylistBtn = (Button) findViewById(R.id.btnSaveGeneratedPlaylist);
        CancelGeneratedPlaylistBtn = (Button) findViewById(R.id.btnCancelGeneratedPlaylist);
        GeneratedSongsByLv = (ListView) findViewById(R.id.lvGeneratedSongsBy);
        GenerateBySongsRl = (RelativeLayout) findViewById(R.id.rlGenerateBySongs);

/*        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songs);
        GeneratedSongsByLv.setAdapter(arrayAdapter);*/

        insertGeneratedSongs();


        GeneratedPlaylistNameEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneratedPlaylistNameEt.setText("");

            }
        });
        SaveGeneratedPlaylistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGeneratedPlaylist();
            }
        });

        CancelGeneratedPlaylistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelGeneratedPlaylist();
            }
        });


        GeneratedSongsByLv.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int mLastFirstVisibleItem;


            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.i("SCROLLING DOWN", "TRUE");

                    SyntonesTimerTask.getInstance().isPlaying(GenerateBySongsActivity.this, "GENERATE PLAYLIST");
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.i("SCROLLING UP", "TRUE");
                    SyntonesTimerTask.getInstance().isPlaying(GenerateBySongsActivity.this, "GENERATE PLAYLIST");
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });
    }


    public void displayGeneratedSongs() {

/*        GeneratedSongsByLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(GenerateBySongsActivity.this, PlayListActivity.class);
                syntonesTimerTask.stopCounter();
                startActivity(intent);
            }
        });*/
    }

    public class ScreenOnOffReceiver extends BroadcastReceiver {

        private String tag;


        public ScreenOnOffReceiver(String tag) {
            this.tag = tag;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.e("Screen mode", "Screen is in off State");

                SyntonesTimerTask.getInstance().isPlaying(context, tag);
                //Your logic comes here whatever you want perform when screen is in off state                                                   }

            } else {
                Log.e("Screen mode", " Screen is in on State");

                SyntonesTimerTask.getInstance().isPlaying(context, tag);
                //Your logic comes here whatever you want perform when screen is in on state

            }

        }

    }

    @Override
    public void onDestroy() {

        SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

        SyntonesWebAPI.Factory.getInstance(sContext).logout().enqueue(new Callback<LogoutResponse>() {
            @Override
            public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {

            }

            @Override
            public void onFailure(Call<LogoutResponse> call, Throwable t) {

            }
        });


        try {
            if (onoffReceiver != null)
                unregisterReceiver(onoffReceiver);
        } catch (Exception e) {

        }
        super.onDestroy();

    }


    public void insertGeneratedSongs() {

        SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        final Long userID = sharedPrefUserInfo.getLong("userID", 0);
        User user = new User();

        user.setUserId(userID);
        syntonesWebAPI.generatePlaylist(user).enqueue(new Callback<GeneratePlaylistResponse>() {
            @Override
            public void onResponse(Call<GeneratePlaylistResponse> call, Response<GeneratePlaylistResponse> response) {
                GeneratePlaylistResponse generatePlaylistResponse = response.body();
                generatedSongs = generatePlaylistResponse.getSongs();

                Integer[] imgid = {
                        R.drawable.aggressive,
                        R.drawable.brooding,
                        R.drawable.cool,
                        R.drawable.defiant,
                        R.drawable.easygoing,
                        R.drawable.empowering,
                        R.drawable.fiery,
                        R.drawable.lively,
                        R.drawable.romantic,
                        R.drawable.rowdy,
                        R.drawable.sensual,
                        R.drawable.somber,
                        R.drawable.sophisticated,
                        R.drawable.urgent


                };

                adapter = new CustomListAdapter(GenerateBySongsActivity.this, generatedSongs, imgid);
                GeneratedSongsByLv.setAdapter(adapter);
/*                for (Song a : generatedSongs) {
                    arrayAdapter.add(a.getSongTitle() + " by " + a.getArtist().getArtistName());
                    arrayAdapter.notifyDataSetChanged();

                    Log.e("GENRE", String.valueOf(a.getGenreId()));
                    Log.e("MOOD", a.getMood());
//                    song.setMood(a.getMood());

                }*/

//                Log.e("GP Response", generatePlaylistResponse.getMessage().getMessage());

            }

            @Override
            public void onFailure(Call<GeneratePlaylistResponse> call, Throwable t) {

            }
        });

        SyntonesTimerTask.getInstance().isPlaying(GenerateBySongsActivity.this, "GENERATE PLAYLIST");
        GenerateBySongsRl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SyntonesTimerTask.getInstance().isPlaying(GenerateBySongsActivity.this, "GENERATE PLAYLIST");
                return true;
            }
        });
    }


    public void saveGeneratedPlaylist() {

        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String username = sharedPrefUserInfo.getString("username", "");
        Long userID = sharedPrefUserInfo.getLong("userID", 0);
        User user = new User();
        user.setUsername(username);

        Playlist playlist = new Playlist();
        playlist.setUser(user);
        playlist.setPlaylistName(GeneratedPlaylistNameEt.getText().toString());

        playlist.setSongs(generatedSongs);

        for (Song a : generatedSongs) {
            Log.d("SONG ID", String.valueOf(a.getSongId()));
        }
        syntonesWebAPI.saveGeneratedPlaylist(playlist).enqueue(new Callback<GeneratePlaylistResponse>() {
            @Override
            public void onResponse(Call<GeneratePlaylistResponse> call, Response<GeneratePlaylistResponse> response) {
                Intent intent = new Intent(GenerateBySongsActivity.this, PlayListActivity.class);
                startActivity(intent);
            }


            @Override
            public void onFailure(Call<GeneratePlaylistResponse> call, Throwable t) {

            }
        });
    }

    public void cancelGeneratedPlaylist() {

        Intent intent = new Intent(GenerateBySongsActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
                Log.d("CONNECTION YL", "TRUE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
                SyntonesTimerTask.getInstance().isPlaying(GenerateBySongsActivity.this, "Generate Playlist");

            } else {
                Log.d("CONNECTION YL", "FALSE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
                SyntonesTimerTask.getInstance().stopCounter();
                SyntonesTimerTask.getInstance().stopPlayerCounter();
                Intent intent = new Intent(GenerateBySongsActivity.this, YourLibraryActivity.class);
                startActivity(intent);
            }

        } catch (Exception e) {

        }
    }

}
