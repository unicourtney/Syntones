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
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syntones.model.Genre;
import com.syntones.model.Playlist;
import com.syntones.model.PlaylistSong;
import com.syntones.model.Song;
import com.syntones.model.User;

import com.syntones.remote.SyntonesTimerTask;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.LibraryResponse;
import com.syntones.response.LogoutResponse;
import com.syntones.response.PlaylistResponse;
import com.syntones.response.SearchResponse;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.R.attr.type;

public class AddSongsActivity extends AppCompatActivity {
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> songs_list = new ArrayList<>(), selected_list = new ArrayList<>(), song_list_info = new ArrayList<>();
    private ListView ListOfSongsLv;
    private String[] outputStrArr;
    private Button AddAllSongsBtn, CancelAddSongsBtn;
    private AddSongsActivity sContext;
    private HashMap<String, String> checked = new HashMap<String, String>();
    private SyntonesTimerTask syntonesTimerTask = new SyntonesTimerTask();
    private RelativeLayout AddSongsRl;
    private ScreenOnOffReceiver onoffReceiver = new ScreenOnOffReceiver("Add Songs");
    private List<Genre> genreList;
    private EditText SearchSongEt;
    private ImageButton SearchSongIb;
    private TextView SearchingTv;
    private String username;
    private List<PlaylistSong> playlistSongList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_songs);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        SharedPreferences sharedPrefGenre = getSharedPreferences("genreList", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefGenre.getString("GenreObject", "");
        Type type = new TypeToken<List<Genre>>() {
        }.getType();
        genreList = gson.fromJson(json, type);
        registerReceiver(onoffReceiver, filter);

        SharedPreferences sharedPrefEditSong = getSharedPreferences("editInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorEditSong = sharedPrefEditSong.edit();

        editorEditSong.putBoolean("editStatus", true);
        editorEditSong.apply();

        ListOfSongsLv = (ListView) findViewById(R.id.lvListOfSongs);
        AddAllSongsBtn = (Button) findViewById(R.id.btnAddAllSongs);
        CancelAddSongsBtn = (Button) findViewById(R.id.btnCancelAddSongs);
        SearchSongIb = (ImageButton) findViewById(R.id.ibSearchSong);
        AddSongsRl = (RelativeLayout) findViewById(R.id.rlAddSongs);
        SearchSongEt = (EditText) findViewById(R.id.etSearchSong);
        SearchingTv = (TextView) findViewById(R.id.tvSearching);


        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, songs_list);
        ListOfSongsLv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ListOfSongsLv.setAdapter(arrayAdapter);

//        insertSongList();

        SearchSongIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayAdapter.clear();
                arrayAdapter.notifyDataSetChanged();
                SyntonesTimerTask.getInstance().isPlaying(AddSongsActivity.this, "Add Songs");
                searchBtn();
            }
        });
        AddAllSongsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToPlaylist();
            }
        });

        CancelAddSongsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddSongsActivity.this, ViewPlayListActivity.class);

                startActivity(intent);
            }
        });

        SyntonesTimerTask.getInstance().isPlaying(AddSongsActivity.this, "Add Songs");
        AddSongsRl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                arrayAdapter.clear();
                arrayAdapter.notifyDataSetChanged();
                SearchSongEt.setText("");
                SyntonesTimerTask.getInstance().isPlaying(AddSongsActivity.this, "Add Songs");
                return true;
            }
        });
    }

/*    public void insertSongList() {

        SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

        syntonesWebAPI.getAllSongsFromDB().enqueue(new Callback<SongListResponse>() {
            @Override
            public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {
                SongListResponse songListResponse = response.body();
                List<Song> songList = songListResponse.getSongs();

                if (songList != null) {

                    for (Song a : songList) {
                        for (Genre b : genreList) {

                            if (a.getGenreId() == b.getId()) {

                                arrayAdapter.add(a.getSongTitle() + "\nby " + a.getArtist().getArtistName() + "\n| " + b.getGenre());
                                arrayAdapter.notifyDataSetChanged();
                                song_list_info.add(a.getSongId() + "," + a.getSongTitle() + "," + a.getArtist().getArtistName());
                            }
                        }

                    }

                }

                String songs_list = new Gson().toJson(song_list_info);
                SharedPreferences sharedPrefSongs = getSharedPreferences("ListOfSongs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorSongs = sharedPrefSongs.edit();
                editorSongs.putString("songs", songs_list);
                editorSongs.apply();

                addToPlaylist();
            }

            @Override
            public void onFailure(Call<SongListResponse> call, Throwable t) {

            }
        });

    }*/

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

    public void addToPlaylist() {
        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences sharedPrefPlaylistInfo = getSharedPreferences("playlistInfo", Context.MODE_PRIVATE);
        final String playlist_name = sharedPrefPlaylistInfo.getString("playlistName", "");
        username = sharedPrefUserInfo.getString("username", "");

        final User user = new User();

        user.setUsername(username);


        SparseBooleanArray sparseBooleanArray = ListOfSongsLv.getCheckedItemPositions();

        String itemsSelected = "";
        selected_list = new ArrayList<String>();
        Log.d("Total Number Selected: ", String.valueOf(ListOfSongsLv.getCheckedItemCount()));
        for (int i = 0; i < sparseBooleanArray.size(); i++) {
            int position = sparseBooleanArray.keyAt(i);
            itemsSelected += sparseBooleanArray.get(i) + ",";

            if (sparseBooleanArray.get(position)) {
//                        Log.d("ITEM", SampleLv.getItemAtPosition(position).toString());

                selected_list.add(ListOfSongsLv.getItemAtPosition(position).toString());
            }
        }

        syntonesWebAPI.getPlaylistFromDB(user).enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {


                PlaylistResponse playlistResponse = response.body();
                List<Playlist> playlists = playlistResponse.getPlaylists();

                String song_list = new Gson().toJson(song_list_info);
                SharedPreferences sharedPrefSongs = getSharedPreferences("ListOfSongs", Context.MODE_PRIVATE);
                String songs = sharedPrefSongs.getString("songs", null);


                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                final ArrayList<String> songsList = new Gson().fromJson(song_list, type);
                for (Playlist a : playlists) {

                    if (a.getPlaylistName().equals(playlist_name)) {


                        for (String b : selected_list) {


                            for (String c : songsList) {
                                String[] songInfo = c.split(","), selectedSongInfo = b.split("\\s(by)\\s");
                                String[] splitText2 = selectedSongInfo[1].split("\\|");

                                Long songId = Long.parseLong(songInfo[0]);
                                String artistName = songInfo[2], songTitle = songInfo[1], selectedSongTitle = selectedSongInfo[0].trim(), selectedArtist = splitText2[0].trim();


                                if (selectedArtist.equals(artistName) && selectedSongTitle.equals(songTitle)) {


                                    PlaylistSong playlistSong = new PlaylistSong();
                                    Log.e("ARTIST", selectedArtist);
                                    Log.e("TITLE", selectedSongTitle);
                                    Log.e("SONG ID", String.valueOf(songId));
                                    Log.e("PLAYLIST ID", String.valueOf(a.getPlaylistId()));
                                    playlistSong.setSongId(songId);
                                    playlistSong.setPlaylistId(a.getPlaylistId());
                                    User user = new User();
                                    user.setUsername(username);
                                    playlistSongList.add(playlistSong);


                                }

                            }


                        }

                        songExists(a.getPlaylistId(), user);
                    }
                }

                Log.e("Playlist Response: ", playlistResponse.getMessage().getMessage());
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {

            }
        });
    }

    public void songExists(final long playlistId, final User user) {

        SyntonesWebAPI.Factory.getInstance(sContext).checkIfSongExists(playlistSongList).enqueue(new Callback<LibraryResponse>() {
            @Override
            public void onResponse(Call<LibraryResponse> call, Response<LibraryResponse> response) {
                Log.e("CHECKING", "CALLED");
                LibraryResponse libraryResponse = response.body();
                List<Long> notExistingSongsList = libraryResponse.getNotExistingSongs();

                if (notExistingSongsList.size() != 0) {
                    for (Long b : notExistingSongsList) {

                        PlaylistSong playlistSong = new PlaylistSong();
                        playlistSong.setSongId(b);
                        playlistSong.setPlaylistId(playlistId);
                        playlistSong.setUser(user);

                        SyntonesWebAPI.Factory.getInstance(sContext).addToPlaylist(playlistSong).enqueue(new Callback<LibraryResponse>() {
                            @Override
                            public void onResponse(Call<LibraryResponse> call, Response<LibraryResponse> response) {
                                LibraryResponse libraryResponse = response.body();

                                Intent intent = new Intent(AddSongsActivity.this, ViewPlayListActivity.class);

                                startActivity(intent);
                                Log.e("Library Response: ", libraryResponse.getMessage().getMessage());
                            }

                            @Override
                            public void onFailure(Call<LibraryResponse> call, Throwable t) {

                            }
                        });
                    }
                } else {

                    Toast.makeText(AddSongsActivity.this, "Song(s) already exist in the playlist .",
                            Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<LibraryResponse> call, Throwable t) {

            }
        });

    }

    public void searchBtn() {

        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
        if (arrayAdapter.isEmpty()) {
            SearchingTv.setText("Searching . . .");
            SearchingTv.setVisibility(View.VISIBLE);
            ListOfSongsLv.setVisibility(View.INVISIBLE);
        }
        syntonesWebAPI.search(SearchSongEt.getText().toString()).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                SearchResponse searchResponse = response.body();
                List<Song> songListSearch = searchResponse.getSongs();
                SyntonesTimerTask.getInstance().stopCounter();

                if (songListSearch != null) {
                    for (Song a : songListSearch) {

                        for (Genre b : genreList) {
                            if (b.getId() == a.getGenreId()) {
                                arrayAdapter.add(a.getSongTitle() + "\nby " + a.getArtist().getArtistName() + "\n| " + b.getGenre());
                                song_list_info.add(a.getSongId() + "," + a.getSongTitle() + "," + a.getArtist().getArtistName());
                                arrayAdapter.notifyDataSetChanged();

                            }
                        }
                    }

                }

                if (searchResponse.getMessage().getMessage().equals("Not Found")) {
                    SearchingTv.setText("No results for " + SearchSongEt.getText().toString());
                    SearchingTv.setVisibility(View.VISIBLE);
                }

                if (!arrayAdapter.isEmpty()) {
                    SyntonesTimerTask.getInstance().isPlaying(AddSongsActivity.this, "Add Songs");
                    SearchingTv.setText("Searching . . .");
                    ListOfSongsLv.setVisibility(View.VISIBLE);
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
                SyntonesTimerTask.getInstance().isPlaying(AddSongsActivity.this, "Add Songs");

            } else {
                Log.d("CONNECTION YL", "FALSE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
                SyntonesTimerTask.getInstance().stopCounter();
                SyntonesTimerTask.getInstance().stopPlayerCounter();
                Intent intent = new Intent(AddSongsActivity.this, YourLibraryActivity.class);
                startActivity(intent);
            }

        } catch (Exception e) {

        }
    }
}
