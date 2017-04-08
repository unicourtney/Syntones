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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syntones.model.Genre;
import com.syntones.model.Playlist;
import com.syntones.model.PlaylistSong;
import com.syntones.model.Song;
import com.syntones.model.User;
import com.syntones.remote.ScreenOnOffReceiver;
import com.syntones.remote.SyntonesTimerTask;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.LogoutResponse;
import com.syntones.response.PlaylistSongsResponse;
import com.syntones.response.RemovePlaylistResponse;
import com.syntones.response.SongListResponse;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewPlayListActivity extends AppCompatActivity {

    private TextView PlaylistNameTv;
    private ListView ViewPlaylistLv;
    private Button EditSongBtn, RemoveSongBtn, AddSongBtn;
    private ViewPlayListActivity sContext;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> songs = new ArrayList<>();
    private ImageView BackIv;
    private SyntonesTimerTask syntonesTimerTask = new SyntonesTimerTask();
    private RelativeLayout ViewPlaylistRl;
    private ScreenOnOffReceiver onoffReceiver = new ScreenOnOffReceiver("View Playlist");
    private List<Genre> genreList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_play_list);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(onoffReceiver, filter);

        PlaylistNameTv = (TextView) findViewById(R.id.tvPlaylistName);
        ViewPlaylistLv = (ListView) findViewById(R.id.lvViewPlaylist);

        EditSongBtn = (Button) findViewById(R.id.btnEditSong);
        RemoveSongBtn = (Button) findViewById(R.id.btnRemoveSong);
        AddSongBtn = (Button) findViewById(R.id.btnAddSong);

        BackIv = (ImageView) findViewById(R.id.ivBack);

        ViewPlaylistRl = (RelativeLayout) findViewById(R.id.rlViewPlaylist);

        SharedPreferences sharedPrefGenre = getSharedPreferences("genreList", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefGenre.getString("GenreObject", "");
        Type type = new TypeToken<List<Genre>>() {
        }.getType();
        genreList = gson.fromJson(json, type);

        SharedPreferences sharedPrefEditSong = getSharedPreferences("editInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorEditSong = sharedPrefEditSong.edit();
        boolean editStatus = sharedPrefEditSong.getBoolean("editStatus", false);

        SyntonesTimerTask.getInstance().isPlaying(this, "View Playlist");
        if (editStatus == true) {
            EditSongBtn.setText("Edit");
            editSong();
            editorEditSong.clear();
            editorEditSong.apply();
        }

        SharedPreferences sharedPrefPlaylistInfo = getSharedPreferences("playlistInfo", Context.MODE_PRIVATE);
        String playlist_name = sharedPrefPlaylistInfo.getString("playlistName", "");

        PlaylistNameTv.setText(playlist_name);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songs);
        ViewPlaylistLv.setAdapter(arrayAdapter);


        insertSongList();


        displayViewSongList();


        EditSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSong();
            }
        });

        RemoveSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSong(v);
            }
        });

        BackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewPlayListActivity.this, PlayListActivity.class);

                startActivity(intent);
            }
        });

        AddSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSong();
            }
        });


        ViewPlaylistRl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {



                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    Log.e("MOVE", "WOAH");


                }
                return true;
            }
        });

        ViewPlaylistLv.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.i("SCROLLING DOWN", "TRUE");


                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.i("SCROLLING UP", "TRUE");


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


    public void editSong() {

        if (EditSongBtn.getText().equals("Edit")) {
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, songs);
            ViewPlaylistLv.setAdapter(arrayAdapter);
            displaySongList();
            EditSongBtn.setText("Done");
            RemoveSongBtn.setVisibility(View.VISIBLE);
            AddSongBtn.setVisibility(View.VISIBLE);
        } else if (EditSongBtn.getText().equals("Done")) {
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songs);
            ViewPlaylistLv.setAdapter(arrayAdapter);
            displayViewSongList();
            EditSongBtn.setText("Edit");
            RemoveSongBtn.setVisibility(View.INVISIBLE);
            AddSongBtn.setVisibility(View.INVISIBLE);
            SharedPreferences sharedPrefEditSong = getSharedPreferences("editInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editorEditSong = sharedPrefEditSong.edit();
            editorEditSong.clear();
            editorEditSong.apply();
        }

    }

    public void displayViewSongList() {
        ViewPlaylistLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String song = String.valueOf(parent.getItemAtPosition(position));
                Log.e("VIEW SONG", song);
                SharedPreferences sharedPrefSongInfo = getSharedPreferences("songInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorSongInfo = sharedPrefSongInfo.edit();

                String[] song_info = song.split("\\s(by)\\s");

                String[] splitText2 = song_info[1].split("\\|");
                editorSongInfo.putString("songTitle", song_info[0].trim());
                editorSongInfo.putString("artistName", splitText2[0].trim());

                editorSongInfo.apply();

                SharedPreferences sharedPrefActivityInfo = getSharedPreferences("activityInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorActivityInfo = sharedPrefActivityInfo.edit();
                editorActivityInfo.putString("activityState", "Playlist");
                editorActivityInfo.apply();
                SharedPreferences sharedPrefEditSong = getSharedPreferences("editInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorEditSong = sharedPrefEditSong.edit();

                editorEditSong.putBoolean("editStatus", false);
                editorEditSong.apply();

                Intent intent = new Intent(ViewPlayListActivity.this, PlayerActivity.class);

                startActivity(intent);
                Toast.makeText(getBaseContext(), song, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displaySongList() {
        ViewPlaylistLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    public void addSong() {

        Intent intent = new Intent(ViewPlayListActivity.this, AddSongsActivity.class);

        startActivity(intent);

    }

    public void removeSong(View view) {
        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences sharedPrefPlaylistInfo = getSharedPreferences("playlistInfo", Context.MODE_PRIVATE);

        final String playlist_id = sharedPrefPlaylistInfo.getString("playlistId", "");
        String username = sharedPrefUserInfo.getString("username", "");
        final User user = new User();
        user.setUsername(username);
        Map<String, String> data = new HashMap<>();

        data.put("id", playlist_id);
        data.put("username", username);
        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
        int position = ViewPlaylistLv.getCheckedItemPosition();
        if (position > -1) {
            final String[] song = String.valueOf(ViewPlaylistLv.getItemAtPosition(position)).split("\\s(by)\\s");
            final String[] splitText2 = song[1].split("\\|");

            syntonesWebAPI.getSongsPlaylist(data).enqueue(new Callback<PlaylistSongsResponse>() {
                @Override
                public void onResponse(Call<PlaylistSongsResponse> call, Response<PlaylistSongsResponse> response) {
                    final PlaylistSongsResponse playlistSongsResponse = response.body();
                    final Playlist playlist = new Playlist();
                    playlist.setPlaylistId(Long.parseLong(playlist_id));
                    playlist.setSongs(playlistSongsResponse.getPlaylist().getSongs());

                    for (Song a : playlist.getSongs()) {
                        if (a.getSongTitle().equals(song[0].trim()) && a.getArtist().getArtistName().equals(splitText2[0].trim())) {

                            PlaylistSong playlistSong = new PlaylistSong();
                            playlistSong.setPlaylistId(Long.parseLong(playlist_id));
                            playlistSong.setSongId(a.getSongId());
                            playlistSong.setUser(user);
                            syntonesWebAPI.removeSongFromPlaylist(playlistSong).enqueue(new Callback<RemovePlaylistResponse>() {
                                @Override
                                public void onResponse(Call<RemovePlaylistResponse> call, Response<RemovePlaylistResponse> response) {

                                    RemovePlaylistResponse removePlaylistResponse = response.body();

                                    Log.e("Remove Playlist ", removePlaylistResponse.getMessage().getMessage());
                                }

                                @Override
                                public void onFailure(Call<RemovePlaylistResponse> call, Throwable t) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<PlaylistSongsResponse> call, Throwable t) {

                }
            });

            arrayAdapter.remove(songs.get(position));


            arrayAdapter.notifyDataSetChanged();
        }
    }

    public void insertSongList() {

        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences sharedPrefPlaylistInfo = getSharedPreferences("playlistInfo", Context.MODE_PRIVATE);

        final String playlist_id = sharedPrefPlaylistInfo.getString("playlistId", "");
        String username = sharedPrefUserInfo.getString("username", "");

        Map<String, String> data = new HashMap<>();

        data.put("id", playlist_id);
        data.put("username", username);

        final SharedPreferences sharedPrefPlayedSongInfo = getSharedPreferences("playedSongInfo", 0);
        final SharedPreferences.Editor editorPlayedSongInfo = sharedPrefPlayedSongInfo.edit();
        editorPlayedSongInfo.clear();
        editorPlayedSongInfo.apply();

        syntonesWebAPI.getSongsPlaylist(data).enqueue(new Callback<PlaylistSongsResponse>() {
            @Override
            public void onResponse(Call<PlaylistSongsResponse> call, Response<PlaylistSongsResponse> response) {

                final PlaylistSongsResponse playlistSongsResponse = response.body();
                final Playlist playlist = new Playlist();


                syntonesWebAPI.getAllSongsFromDB().enqueue(new Callback<SongListResponse>() {
                    @Override
                    public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {

                        SongListResponse songListResponse = response.body();

                        playlist.setPlaylistId(Long.parseLong(playlist_id));
                        playlist.setSongs(playlistSongsResponse.getPlaylist().getSongs());
                        String[] song_ids = new String[playlist.getSongs().size()];
                        String[] song_urls = new String[playlist.getSongs().size()];
                        String[] song_titles = new String[playlist.getSongs().size()];
                        String[] song_artists = new String[playlist.getSongs().size()];
                        String[] song_lyrics = new String[playlist.getSongs().size()];
                        String[] song_genre = new String[playlist.getSongs().size()];
                        editorPlayedSongInfo.putInt("song_url_array" + "_size", song_urls.length);

                        int b = 0;
                        for (Song a : playlist.getSongs()) {

                            for (Genre c : genreList) {

                                if (a.getGenreId() == c.getId()) {

                                    song_ids[b] = String.valueOf(a.getSongId());
                                    song_urls[b] = String.valueOf(a.getFilePath());
                                    song_titles[b] = a.getSongTitle();
                                    song_artists[b] = a.getArtist().getArtistName();
                                    song_lyrics[b] = a.getLyrics();
                                    song_genre[b] = c.getGenre();


                                    editorPlayedSongInfo.putString("song_id_array" + "_" + b, song_ids[b]);
                                    editorPlayedSongInfo.putString("song_url_array" + "_" + b, song_urls[b]);
                                    editorPlayedSongInfo.putString("song_titles_array" + "_" + b, song_titles[b]);
                                    editorPlayedSongInfo.putString("song_artists_array" + "_" + b, song_artists[b]);
                                    editorPlayedSongInfo.putString("song_lyrics_array" + "_" + b, song_lyrics[b]);
                                    editorPlayedSongInfo.putString("song_genre_array" + "_" + b, song_genre[b]);


                                    arrayAdapter.add(a.getSongTitle() + "\nby " + a.getArtist().getArtistName() + "\n| " + c.getGenre());
                                    arrayAdapter.notifyDataSetChanged();
                                    b++;
                                }
                            }

                        }
                        editorPlayedSongInfo.apply();
                        playlistSongsResponse.setPlaylist(playlist);

                        Log.e("Song List Response:", songListResponse.getMessage().getMessage());
                    }


                    @Override
                    public void onFailure(Call<SongListResponse> call, Throwable t) {


                        Log.e("Failed", String.valueOf(t.getMessage()));

                    }
                });


            }

            @Override
            public void onFailure(Call<PlaylistSongsResponse> call, Throwable t) {

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
                SyntonesTimerTask.getInstance().isPlaying(ViewPlayListActivity.this, "View Playlist");
                Log.d("CONNECTION YL", "TRUE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());


            } else {
                Log.d("CONNECTION YL", "FALSE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
                SyntonesTimerTask.getInstance().stopCounter();
                SyntonesTimerTask.getInstance().stopPlayerCounter();
                Intent intent = new Intent(ViewPlayListActivity.this, YourLibraryActivity.class);
                startActivity(intent);
            }

        } catch (Exception e) {

        }
    }
}
