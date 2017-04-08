package com.syntones.syntones_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syntones.model.Genre;
import com.syntones.model.PlayedSongsByTime;
import com.syntones.model.Song;
import com.syntones.remote.ScreenOnOffReceiver;
import com.syntones.remote.SyntonesTimerTask;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.LogoutResponse;
import com.syntones.response.PlayedSongsByTimeResponse;
import com.syntones.response.SongListResponse;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeeMoreActivity extends AppCompatActivity {

    private SeeMoreActivity sContext;
    private Map<String, Integer> toRankSongs = new HashMap<>();
    private ArrayAdapter<String> arrayAdapter;
    private ListView RankedSongsLv;
    private SyntonesTimerTask syntonesTimerTask = new SyntonesTimerTask();
    private RelativeLayout SeeMoreRl;
    private ScreenOnOffReceiver onoffReceiver = new ScreenOnOffReceiver("See More");
    private List<Genre> genreList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_more);

        SharedPreferences sharedPrefGenre = getSharedPreferences("genreList", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefGenre.getString("GenreObject", "");
        Type type = new TypeToken<List<Genre>>() {
        }.getType();
        genreList = gson.fromJson(json, type);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(onoffReceiver, filter);

        RankedSongsLv = (ListView) findViewById(R.id.lvRankedSongs);
        SeeMoreRl = (RelativeLayout) findViewById(R.id.rlSeeMore);

        String timeOfDay = getTimeofDay();
        getSongs(timeOfDay);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        RankedSongsLv.setAdapter(arrayAdapter);
        displayRankedSongs();
        SyntonesTimerTask.getInstance().isPlaying(SeeMoreActivity.this, "See More");
        SeeMoreRl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                SyntonesTimerTask.getInstance().isPlaying(SeeMoreActivity.this, "See More");
                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    SyntonesTimerTask.getInstance().isPlaying(SeeMoreActivity.this, "See More");

                }
                return true;
            }
        });

        RankedSongsLv.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.i("SCROLLING DOWN", "TRUE");

                    SyntonesTimerTask.getInstance().isPlaying(SeeMoreActivity.this, "See More");
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.i("SCROLLING UP", "TRUE");

                    SyntonesTimerTask.getInstance().isPlaying(SeeMoreActivity.this, "See More");
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });
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

    public String getTimeofDay() {
        String timeOfDay = null;
        Date currdate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
        DateFormat timeFormat = new SimpleDateFormat("HH");
        Timestamp timeStamp = new Timestamp(currdate.getTime());

        int time = Integer.parseInt(timeFormat.format(timeStamp));

        if (time >= 00 && time <= 04) {

            timeOfDay = "midnight";

        } else if (time >= 05 && time <= 11) {

            timeOfDay = "morning";

        } else if (time == 12) {

            timeOfDay = "noon";
        } else if (time >= 13 && time <= 17) {

            timeOfDay = "afternoon";

        } else if (time >= 18 && time <= 23) {

            timeOfDay = "evening";

        }

        return timeOfDay;

    }


    public void getSongs(final String timeOfDay) {


        SharedPreferences sharedPrefItemSetSongs = getSharedPreferences("itemSets", Context.MODE_PRIVATE);
        String songs = sharedPrefItemSetSongs.getString("songs", null);

        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        final ArrayList<String> songsList = new Gson().fromJson(songs, type);

        Collections.sort(songsList);

        SyntonesWebAPI.Factory.getInstance(sContext).getPlayedSongsByTime().enqueue(new Callback<PlayedSongsByTimeResponse>() {
            @Override
            public void onResponse(Call<PlayedSongsByTimeResponse> call, Response<PlayedSongsByTimeResponse> response) {
                PlayedSongsByTimeResponse playedSongsByTimeResponse = response.body();
                List<PlayedSongsByTime> playedSongsByTimeList = playedSongsByTimeResponse.getPlayedSongsByTimeList();
                int score = 0;

                for (String a : songsList) {

                    for (PlayedSongsByTime b : playedSongsByTimeList) {

                        if (b.getTrack_id().equals(a)) {

                            if (timeOfDay.equals("midnight")) {

                                score = b.getMidnight();
                            } else if (timeOfDay.equals("morning")) {
                                score = b.getMorning();
                            } else if (timeOfDay.equals("noon")) {
                                score = b.getNoon();
                            } else if (timeOfDay.equals("afternoon")) {
                                score = b.getAfternoon();
                            } else if (timeOfDay.equals("evening")) {
                                score = b.getEvening();
                            }

                            toRankSongs.put(b.getTrack_id(), score);
                        }
                    }
                }

                List<Map.Entry<String, Integer>> rankedSongsList = rankSongs(toRankSongs);

                getSongInfo(rankedSongsList);

            }


            @Override
            public void onFailure(Call<PlayedSongsByTimeResponse> call, Throwable t) {

            }
        });


    }

    public <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> rankSongs(Map<K, V> toRankSongs) {


        List<Map.Entry<K, V>> sortedEntries = new ArrayList<Map.Entry<K, V>>(toRankSongs.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );

        return sortedEntries;
    }

    public void getSongInfo(final List<Map.Entry<String, Integer>> rankedSongsList) {


        SyntonesWebAPI.Factory.getInstance(sContext).getAllSongsFromDB().enqueue(new Callback<SongListResponse>() {
            @Override
            public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {

                SongListResponse songListResponse = response.body();
                List<Song> songList = songListResponse.getSongs();

                for (Map.Entry<String, Integer> a : rankedSongsList) {


                    for (Song b : songList) {

                        String songId = String.valueOf(b.getSongId());
                        if (songId.equals(a.getKey())) {

                            Log.i("RANKED", "SONG ID: " + a.getKey() + " - " + a.getValue());


                            for (Genre c : genreList) {

                                if (c.getId() == b.getGenreId()) {
                                    arrayAdapter.add(b.getSongTitle() + "\nby " + b.getArtist().getArtistName() + "\n| " + c.getGenre());
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }


                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<SongListResponse> call, Throwable t) {

            }
        });

    }

    public void displayRankedSongs() {

        SharedPreferences sharedPrefPlayedSongInfo = getSharedPreferences("playedSongInfo", 0);
        final SharedPreferences.Editor editorPlayedSongInfo = sharedPrefPlayedSongInfo.edit();

        SharedPreferences sharedPrefSongInfo = getSharedPreferences("songInfo", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editorSongInfo = sharedPrefSongInfo.edit();

        editorPlayedSongInfo.clear();
        editorPlayedSongInfo.apply();
        editorSongInfo.clear();
        editorSongInfo.apply();

        RankedSongsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String song = String.valueOf(parent.getItemAtPosition(position));


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
                        String[] song_genre = new String[1];
                        editorPlayedSongInfo.putInt("song_url_array" + "_size", song_urls.length);

                        String[] song_info = song.split("\\s(by)\\s");
                        String[] splitText2 = song_info[1].split("\\|");
                        editorSongInfo.putString("songTitle", song_info[0].trim());
                        editorSongInfo.putString("artistName", splitText2[0].trim());

                        Log.d("ARTIST NAME:", song_info[1]);
                        editorSongInfo.apply();
                        int b = 0;
                        for (Song a : songList) {

                            if (a.getSongTitle().equals(song_info[0].trim()) && a.getArtist().getArtistName().equals(splitText2[0].trim())) {


                                song_urls[b] = String.valueOf(a.getFilePath());
                                song_ids[b] = String.valueOf(a.getSongId());
                                song_titles[b] = a.getSongTitle();
                                song_artists[b] = a.getArtist().getArtistName();
                                song_lyrics[b] = a.getLyrics();
                                song_genre[b] = splitText2[1].trim();


                                editorPlayedSongInfo.putString("song_url_array" + "_" + b, song_urls[b]);
                                editorPlayedSongInfo.putString("song_id_array" + "_" + b, song_ids[b]);
                                editorPlayedSongInfo.putString("song_titles_array" + "_" + b, song_titles[b]);
                                editorPlayedSongInfo.putString("song_artists_array" + "_" + b, song_artists[b]);
                                editorPlayedSongInfo.putString("song_lyrics_array" + "_" + b, song_lyrics[b]);
                                editorPlayedSongInfo.putString("song_genre_array" + "_" + b, song_genre[b]);
                                b++;


                            }
                        }

                        editorPlayedSongInfo.apply();

                        SharedPreferences sharedPrefActivityInfo = getSharedPreferences("activityInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorActivityInfo = sharedPrefActivityInfo.edit();
                        editorActivityInfo.putString("activityState", "SearchActivity");
                        editorActivityInfo.apply();

                        Intent intent = new Intent(SeeMoreActivity.this, PlayerActivity.class);
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(Call<SongListResponse> call, Throwable t) {

                    }
                });
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
                SyntonesTimerTask.getInstance().isPlaying(SeeMoreActivity.this, "See More");

            } else {
                Log.d("CONNECTION YL", "FALSE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
                SyntonesTimerTask.getInstance().stopCounter();
                SyntonesTimerTask.getInstance().stopPlayerCounter();
                Intent intent = new Intent(SeeMoreActivity.this, YourLibraryActivity.class);
                startActivity(intent);
            }

        } catch (Exception e) {

        }
    }

}
