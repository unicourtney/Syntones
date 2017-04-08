package com.syntones.syntones_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.SortedList;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syntones.model.Artist;
import com.syntones.model.Genre;
import com.syntones.model.Song;
import com.syntones.remote.ScreenOnOffReceiver;
import com.syntones.remote.SyntonesTimerTask;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.GenreResponse;
import com.syntones.response.LogoutResponse;
import com.syntones.response.SearchResponse;
import com.syntones.response.SongListResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchActivity extends AppCompatActivity {

    private ImageButton SearchBtn;
    private SearchActivity sContext;
    private ListView SearchResultLv;
    private ArrayAdapter<String> arrayAdapater;
    private ArrayList<String> songs = new ArrayList<>();
    private EditText SearchEt;
    private ImageView SearchIv, LibraryIv;
    private RelativeLayout SearchResultRl;
    private Timer timer;
    private SyntonesTimerTask syntonesTimerTask = new SyntonesTimerTask();
    private boolean isRunning;
    private CountDownTimer countDownTimer;
    private SearchActivity searchActivity;
    private ScreenOnOffReceiver onoffReceiver = new ScreenOnOffReceiver("Search");
    private List<Genre> genreList;
    private TextView SearchingTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(onoffReceiver, filter);

        SearchBtn = (ImageButton) findViewById(R.id.btnSearchRes);
        SearchResultLv = (ListView) findViewById(R.id.lvSearchResult);
        SearchEt = (EditText) findViewById(R.id.etSearch);
        LibraryIv = (ImageView) findViewById(R.id.ivLibrary);
        SearchResultRl = (RelativeLayout) findViewById(R.id.rlSearchResult);
        SearchingTv = (TextView) findViewById(R.id.tvSearching);

        final String searchText = SearchEt.getText().toString();
        syntonesTimerTask = new SyntonesTimerTask();

        arrayAdapater = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songs);
        SearchResultLv.setAdapter(arrayAdapater);

        displayAllSongs();


        SharedPreferences sharedPrefGenre = getSharedPreferences("genreList", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefGenre.getString("GenreObject", "");
        Type type = new TypeToken<List<Genre>>() {
        }.getType();
        genreList = gson.fromJson(json, type);

        LibraryIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, YourLibraryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        SearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrayAdapater.clear();
                arrayAdapater.notifyDataSetChanged();
                SyntonesTimerTask.getInstance().isPlaying(SearchActivity.this, "Search");
                searchBtn();
            }
        });

        SharedPreferences sharedPrefPlayedSongInfo = getSharedPreferences("playedSongInfo", 0);
        final SharedPreferences.Editor editorPlayedSongInfo = sharedPrefPlayedSongInfo.edit();

        SharedPreferences sharedPrefSongInfo = getSharedPreferences("songInfo", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editorSongInfo = sharedPrefSongInfo.edit();

        editorPlayedSongInfo.clear();
        editorPlayedSongInfo.apply();
        editorSongInfo.clear();
        editorSongInfo.apply();

        SearchResultLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String song = String.valueOf(parent.getItemAtPosition(position));

                Log.e("STRING POS", song);

                SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
                syntonesWebAPI.getAllSongsFromDB().enqueue(new Callback<SongListResponse>() {
                    @Override
                    public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {

                        SongListResponse songListResponse = response.body();
                        List<Song> songList = songListResponse.getSongs();


                        String[] song_urls = new String[1];
                        String[] song_titles = new String[1];
                        String[] song_artists = new String[1];
                        String[] song_lyrics = new String[1];
                        String[] song_ids = new String[1];
                        String[] songs_genre = new String[1];
                        editorPlayedSongInfo.putInt("song_url_array" + "_size", song_urls.length);

                        String[] song_info = song.split("\\s(by)\\s");
                        String[] splitText2 = song_info[1].split("\\|");
                        editorSongInfo.putString("songTitle", song_info[0]);
                        editorSongInfo.putString("artistName", splitText2[0]);

                        editorSongInfo.apply();
                        int b = 0;

                        if (songList != null) {
                            for (Song a : songList) {

                                if (a.getArtist().getArtistName().equals(splitText2[0].trim())) {

                                    if (a.getSongTitle().equals(song_info[0].trim())) {

                                        song_urls[b] = String.valueOf(a.getFilePath());
                                        song_ids[b] = String.valueOf(a.getSongId());
                                        song_titles[b] = a.getSongTitle();
                                        song_artists[b] = a.getArtist().getArtistName();
                                        song_lyrics[b] = a.getLyrics();


                                        editorPlayedSongInfo.putString("song_url_array" + "_" + b, song_urls[b]);
                                        editorPlayedSongInfo.putString("song_id_array" + "_" + b, song_ids[b]);
                                        editorPlayedSongInfo.putString("song_titles_array" + "_" + b, song_titles[b]);
                                        editorPlayedSongInfo.putString("song_artists_array" + "_" + b, song_artists[b]);
                                        editorPlayedSongInfo.putString("song_lyrics_array" + "_" + b, song_lyrics[b]);
                                        editorPlayedSongInfo.putString("song_genre_array" + "_" + b, splitText2[0]);
                                        b++;

                                    }
                                }
                            }
                        }
                        editorPlayedSongInfo.apply();

                        SharedPreferences sharedPrefActivityInfo = getSharedPreferences("activityInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorActivityInfo = sharedPrefActivityInfo.edit();
                        editorActivityInfo.putString("activityState", "SearchActivity");
                        editorActivityInfo.apply();

                        Intent intent = new Intent(SearchActivity.this, PlayerActivity.class);
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(Call<SongListResponse> call, Throwable t) {

                    }
                });


            }
        });

        SearchResultRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayAdapater.clear();
                displayAllSongs();
                SearchEt.setText("");
                SyntonesTimerTask.getInstance().isPlaying(SearchActivity.this, "Search");

            }
        });
        SyntonesTimerTask.getInstance().isPlaying(SearchActivity.this, "Search");

        SearchResultLv.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.i("SCROLLING DOWN", "TRUE");

                    SyntonesTimerTask.getInstance().isPlaying(SearchActivity.this, "Search");
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.i("SCROLLING UP", "TRUE");

                    SyntonesTimerTask.getInstance().isPlaying(SearchActivity.this, "Search");
                }
                mLastFirstVisibleItem = firstVisibleItem;


            }
        });
/*
        SearchResultRl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("TOUCH", "WOAH");
                syntonesTimerTask.stopCounter();
                syntonesTimerTask.startCounter(SearchActivity.this, "Search");

                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    syntonesTimerTask.stopCounter();
                    syntonesTimerTask.startCounter(SearchActivity.this, "Search");

                }

                return true;
            }
        });
*/


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

    public void displayAllSongs() {

        SearchingTv.setText("Searching . . .");
        SearchResultLv.setVisibility(View.VISIBLE);
        SearchingTv.setVisibility(View.INVISIBLE);

        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);


        syntonesWebAPI.getAllSongsFromDB().enqueue(new Callback<SongListResponse>() {
            @Override
            public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {

                SongListResponse songListResponse = response.body();
                List<Song> songList = songListResponse.getSongs();

                if (songList.size() > 0) {
                    Collections.sort(songList, new Comparator<Song>() {
                        @Override
                        public int compare(final Song object1, final Song object2) {
                            return object1.getSongTitle().compareTo(object2.getSongTitle());
                        }
                    });
                }

                if (songList != null) {
                    for (Song s : songList) {

                        for (Genre a : genreList) {

                            if (a.getId() == s.getGenreId()) {

                                arrayAdapater.add(s.getSongTitle() + "\nby " + s.getArtist().getArtistName() + "\n| " + a.getGenre());
                                arrayAdapater.notifyDataSetChanged();
                            }
                        }


                    }
                }

                Log.e("Song List Response:", songListResponse.getMessage().getMessage());
            }


            @Override
            public void onFailure(Call<SongListResponse> call, Throwable t) {


                Log.e("Failed", String.valueOf(t.getMessage()));

            }
        });
    }

    public void searchBtn() {

        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
        if (arrayAdapater.isEmpty()) {
            SearchingTv.setText("Searching . . .");
            SearchingTv.setVisibility(View.VISIBLE);
            SearchResultLv.setVisibility(View.INVISIBLE);
        }
        syntonesWebAPI.search(SearchEt.getText().toString()).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                SearchResponse searchResponse = response.body();
                List<Song> songListSearch = searchResponse.getSongs();
                SyntonesTimerTask.getInstance().stopCounter();

                if (songListSearch != null) {
                    for (Song a : songListSearch) {
                        Log.e("ARTIST", a.getArtist().getArtistName() + " - " + a.getGenreId());
                        for (Genre b : genreList) {

                            if (b.getId() == a.getGenreId()) {

                                arrayAdapater.add(a.getSongTitle() + "\nby " + a.getArtist().getArtistName() + "\n| " + b.getGenre());
                                arrayAdapater.notifyDataSetChanged();

                            }
                        }
                    }

                }

                if (searchResponse.getMessage().getMessage().equals("Not Found")) {
                    SearchingTv.setText("No results for " + SearchEt.getText().toString());
                    SearchingTv.setVisibility(View.VISIBLE);
                }

                if (!arrayAdapater.isEmpty()) {
                    SyntonesTimerTask.getInstance().isPlaying(SearchActivity.this, "Search");
                    SearchingTv.setText("Searching . . .");
                    SearchResultLv.setVisibility(View.VISIBLE);
                    SearchingTv.setVisibility(View.INVISIBLE);

                }


                Log.e("Search Response", searchResponse.getMessage().getMessage());
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {

            }
        });

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
                SyntonesTimerTask.getInstance().isPlaying(SearchActivity.this, "Search");

            } else {
                Log.d("CONNECTION YL", "FALSE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
                SyntonesTimerTask.getInstance().stopCounter();
                SyntonesTimerTask.getInstance().stopPlayerCounter();
                Intent intent = new Intent(SearchActivity.this, YourLibraryActivity.class);
                startActivity(intent);
            }

        } catch (Exception e) {

        }
    }
}
