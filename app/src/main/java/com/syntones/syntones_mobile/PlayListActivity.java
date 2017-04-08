package com.syntones.syntones_mobile;

import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.syntones.model.Playlist;
import com.syntones.model.PlaylistSong;
import com.syntones.model.User;
import com.syntones.remote.ScreenOnOffReceiver;
import com.syntones.remote.SyntonesTimerTask;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.LibraryResponse;
import com.syntones.response.LogoutResponse;
import com.syntones.response.PlaylistResponse;
import com.syntones.response.RemovePlaylistResponse;
import com.syntones.response.RemoveToPlaylistResponse;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayListActivity extends AppCompatActivity {
    private PlayListActivity sContext;
    private ArrayList<String> play_lists = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private EditText PlayListNameEt;
    private ListView PlaylistsLv;
    private Button RemoveBtn, EditBtn;
    private String buttonStatus;
    private ImageView BackIv;
    private SyntonesTimerTask syntonesTimerTask = new SyntonesTimerTask();
    private RelativeLayout PlaylistRl;
    private ScreenOnOffReceiver onoffReceiver = new ScreenOnOffReceiver("Playlist");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        SyntonesTimerTask.getInstance().isPlaying(PlayListActivity.this, "Playlist");
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(onoffReceiver, filter);

        PlaylistsLv = (ListView) findViewById(R.id.lvPlaylists);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, play_lists);
        PlaylistsLv.setAdapter(arrayAdapter);
        RemoveBtn = (Button) findViewById(R.id.btnRemove);
        EditBtn = (Button) findViewById(R.id.btnEdit);
        BackIv = (ImageView) findViewById(R.id.ivBack);
        PlaylistRl = (RelativeLayout) findViewById(R.id.rlPlaylist);

        insertPlaylist();
        displayViewPlaylistList();


        EditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editPlaylist(v);

            }
        });


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            buttonStatus = extras.get("buttonStatus").toString();

            if (buttonStatus.equals("addToPlaylist")) {
                EditBtn.setVisibility(View.INVISIBLE);
                String song_id = extras.get("SongId").toString();
                this.displayAddToPlaylistListView(song_id);

            }

        }


        RemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePlayList();
            }
        });

        BackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayListActivity.this, YourLibraryActivity.class);

                startActivity(intent);
            }
        });


        PlaylistRl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    Log.e("MOVE", "WOAH");


                }
                return true;
            }
        });

        PlaylistsLv.setOnScrollListener(new AbsListView.OnScrollListener() {

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

                    SyntonesTimerTask.getInstance().isPlaying(PlayListActivity.this, "Playlist");
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



    public void displayPlaylist() {

        PlaylistsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    public void displayAddToPlaylistListView(String song_id) {
        final long songId = Long.parseLong(song_id);

        PlaylistsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                play_lists.get(position);

                final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
                SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                final String playlist_name = String.valueOf(PlaylistsLv.getItemAtPosition(position));
                String username = sharedPrefUserInfo.getString("username", "");
                final User user = new User();
                user.setUsername(username);
                syntonesWebAPI.getPlaylistFromDB(user).enqueue(new Callback<PlaylistResponse>() {
                    @Override
                    public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {


                        PlaylistResponse playlistResponse = response.body();
                        List<Playlist> playlists = playlistResponse.getPlaylists();

                        for (final Playlist a : playlists) {
                            List<PlaylistSong> playlistSongList = new ArrayList<>();
                            if (a.getPlaylistName().equals(playlist_name)) {

                                PlaylistSong playlistSong = new PlaylistSong();
                                playlistSong.setSongId(songId);
                                playlistSong.setPlaylistId(a.getPlaylistId());
                                playlistSong.setUser(user);

                                playlistSongList.add(playlistSong);

                                SyntonesWebAPI.Factory.getInstance(sContext).checkIfSongExists(playlistSongList).enqueue(new Callback<LibraryResponse>() {
                                    @Override
                                    public void onResponse(Call<LibraryResponse> call, Response<LibraryResponse> response) {
                                        LibraryResponse libraryResponse = response.body();
                                        List<Long> notExistingSongsList = libraryResponse.getNotExistingSongs();
                                        if (notExistingSongsList.size() != 0) {
                                            for (Long b : notExistingSongsList) {

                                                PlaylistSong playlistSong = new PlaylistSong();
                                                playlistSong.setSongId(b);
                                                playlistSong.setPlaylistId(a.getPlaylistId());
                                                playlistSong.setUser(user);

                                                SyntonesWebAPI.Factory.getInstance(sContext).addToPlaylist(playlistSong).enqueue(new Callback<LibraryResponse>() {
                                                    @Override
                                                    public void onResponse(Call<LibraryResponse> call, Response<LibraryResponse> response) {
                                                        LibraryResponse libraryResponse = response.body();

                                                        Intent intent = new Intent(PlayListActivity.this, PlayerActivity.class);

                                                        startActivity(intent);
                                                        Log.e("Library Response: ", libraryResponse.getMessage().getMessage());
                                                    }

                                                    @Override
                                                    public void onFailure(Call<LibraryResponse> call, Throwable t) {

                                                    }
                                                });
                                            }
                                        }else{

                                            Toast.makeText(PlayListActivity.this, "Song(s) already exist in the playlist .",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<LibraryResponse> call, Throwable t) {

                                    }
                                });




                            }
                        }

                        Log.e("Playlist Response: ", playlistResponse.getMessage().getMessage());
                    }

                    @Override
                    public void onFailure(Call<PlaylistResponse> call, Throwable t) {

                    }
                });


            }
        });


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
    public void displayViewPlaylistList() {
        PlaylistsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                play_lists.get(position);

                final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
                SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                final String playlist_name = String.valueOf(PlaylistsLv.getItemAtPosition(position));
                String username = sharedPrefUserInfo.getString("username", "");
                final User user = new User();
                user.setUsername(username);
                syntonesWebAPI.getPlaylistFromDB(user).enqueue(new Callback<PlaylistResponse>() {
                    @Override
                    public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {


                        PlaylistResponse playlistResponse = response.body();
                        List<Playlist> playlists = playlistResponse.getPlaylists();

                        for (Playlist a : playlists) {

                            if (a.getPlaylistName().equals(playlist_name)) {
                                Intent intent = new Intent(PlayListActivity.this, ViewPlayListActivity.class);
                                SharedPreferences sharedPrefPlaylistInfo = getSharedPreferences("playlistInfo", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editorPlaylistInfo = sharedPrefPlaylistInfo.edit();
                                editorPlaylistInfo.putString("playlistName", playlist_name);
                                editorPlaylistInfo.putString("playlistId", String.valueOf(a.getPlaylistId()));
                                editorPlaylistInfo.apply();

                                startActivity(intent);
                            }
                        }


                        Log.e("Playlist Response: ", playlistResponse.getMessage().getMessage());
                    }

                    @Override
                    public void onFailure(Call<PlaylistResponse> call, Throwable t) {

                    }
                });


            }
        });
    }

    public void insertPlaylist() {

        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        String username = sharedPrefUserInfo.getString("username", "");
        User user = new User();
        user.setUsername(username);
        SharedPreferences sharedPrefPlaylistInfo = getSharedPreferences("playedSongInfo", 0);
        SharedPreferences sharedPrefPlaylistInfoOffline = getSharedPreferences("playedSongInfoOffline", 0);
        final SharedPreferences.Editor editorPlaylistInfo = sharedPrefPlaylistInfo.edit();
        final SharedPreferences.Editor editorPlaylistInfoOffline = sharedPrefPlaylistInfoOffline.edit();
        syntonesWebAPI.getPlaylistFromDB(user).enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {


                PlaylistResponse playlistResponse = response.body();
                List<Playlist> playlists = playlistResponse.getPlaylists();

                if (playlists != null) {
                    for (int a = 0; a < playlists.size(); a++) {


                        arrayAdapter.add(playlists.get(a).getPlaylistName());
                        arrayAdapter.notifyDataSetChanged();

                    }
                }
                editorPlaylistInfo.apply();
                editorPlaylistInfoOffline.apply();

             /*   Log.e("Playlist Response: ", playlistResponse.getMessage().getMessage());*/
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {

            }
        });

    }


    public void editPlaylist(View view) {

        String btnText;
        Button editBtn, addBtn, removeBtn;

        btnText = ((Button) view).getText().toString();
        editBtn = (Button) findViewById(R.id.btnEdit);
        addBtn = (Button) findViewById(R.id.btnAdd);
        removeBtn = (Button) findViewById(R.id.btnRemove);

        if (btnText.equals("Edit")) {

            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, play_lists);
            PlaylistsLv.setAdapter(arrayAdapter);
            editBtn.setText("Done");
            this.displayPlaylist();
            addBtn.setVisibility(View.VISIBLE);
            removeBtn.setVisibility(View.VISIBLE);
        } else if (btnText.equals("Done")) {
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, play_lists);
            PlaylistsLv.setAdapter(arrayAdapter);
            this.displayViewPlaylistList();
            editBtn.setText("Edit");
            addBtn.setVisibility(View.INVISIBLE);
            removeBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void popUpAdd(View view) {

        final Dialog dialog = new Dialog(PlayListActivity.this);
        dialog.setTitle("New Playlist");
        dialog.setContentView(R.layout.add_play_list_dialog);
        dialog.show();

        final EditText PlayListNameEt = (EditText) dialog.findViewById(R.id.etPlayListName);
        Button AddPlayListBtn = (Button) dialog.findViewById(R.id.btnAddPlaylist);
        Button CancelBtn = (Button) dialog.findViewById(R.id.btnCancel);


        AddPlayListBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String play_list_name = PlayListNameEt.getText().toString();

                if (!play_list_name.isEmpty() && play_list_name.length() > 0) {

                    SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

                    Playlist playlist = new Playlist();

                    SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

                    String username = sharedPrefUserInfo.getString("username", "");

                    User user = new User(username);


                    playlist.setUser(user);
                    playlist.setPlaylistName(play_list_name);

                    syntonesWebAPI.createPlaylist(playlist);

                    SyntonesWebAPI.Factory.getInstance(sContext).createPlaylist(playlist).enqueue(new Callback<PlaylistResponse>() {

                        @Override
                        public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {

                            PlaylistResponse playlistResponse = response.body();

                            Log.e("Playlist Response: ", String.valueOf(playlistResponse.getMessage().getMessage()));
                        }

                        @Override
                        public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                            Log.e("Failed", t.getMessage());
                        }
                    });
                    arrayAdapter.add(play_list_name);
                    arrayAdapter.notifyDataSetChanged();

                }
                dialog.cancel();
            }
        });
        CancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });


    }

    public void deletePlayList() {

        int position = PlaylistsLv.getCheckedItemPosition();
        if (position > -1) {
            final String playlist_name = String.valueOf(PlaylistsLv.getItemAtPosition(position));
            SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

            String username = sharedPrefUserInfo.getString("username", "");
            final User user = new User();
            user.setUsername(username);

            final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

            syntonesWebAPI.getPlaylistFromDB(user).enqueue(new Callback<PlaylistResponse>() {
                @Override
                public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {


                    PlaylistResponse playlistResponse = response.body();
                    List<Playlist> playlists = playlistResponse.getPlaylists();

                    for (Playlist a : playlists) {

                        if (a.getPlaylistName().equals(playlist_name)) {
                            Playlist playlist = new Playlist();
                            playlist.setPlaylistId(a.getPlaylistId());
                            playlist.setUser(user);


                            syntonesWebAPI.removePlaylist(playlist);

                            SyntonesWebAPI.Factory.getInstance(sContext).removePlaylist(playlist).enqueue(new Callback<RemovePlaylistResponse>() {
                                @Override
                                public void onResponse(Call<RemovePlaylistResponse> call, Response<RemovePlaylistResponse> response) {

                                }

                                @Override
                                public void onFailure(Call<RemovePlaylistResponse> call, Throwable t) {

                                }
                            });
                        }
                    }

                    Log.e("Playlist Response: ", playlistResponse.getMessage().getMessage());
                }

                @Override
                public void onFailure(Call<PlaylistResponse> call, Throwable t) {

                }
            });


            arrayAdapter.remove(play_lists.get(position));


            arrayAdapter.notifyDataSetChanged();

        }
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
                SyntonesTimerTask.getInstance().isPlaying(PlayListActivity.this, "Playlist");

            } else {
                Log.d("CONNECTION YL", "FALSE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
                SyntonesTimerTask.getInstance().stopCounter();
                SyntonesTimerTask.getInstance().stopPlayerCounter();
                Intent intent = new Intent(PlayListActivity.this, YourLibraryActivity.class);
                startActivity(intent);
            }

        } catch (Exception e) {

        }
    }

}
