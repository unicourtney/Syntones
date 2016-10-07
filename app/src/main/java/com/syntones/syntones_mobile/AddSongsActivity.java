package com.syntones.syntones_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.syntones.model.Playlist;
import com.syntones.model.PlaylistSong;
import com.syntones.model.Song;
import com.syntones.model.User;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.LibraryResponse;
import com.syntones.response.PlaylistResponse;
import com.syntones.response.SongListResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddSongsActivity extends AppCompatActivity {
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> songs_list = new ArrayList<>();
    private ArrayList<String> selected_list = new ArrayList<>();
    private ListView ListOfSongsLv;
    private String[] outputStrArr;
    private Button AddAllSongsBtn, CancelAddSongsBtn;
    private AddSongsActivity sContext;
    private HashMap<String, String> checked = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_songs);

        SharedPreferences sharedPrefEditSong = getSharedPreferences("editInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorEditSong = sharedPrefEditSong.edit();

        editorEditSong.putBoolean("editStatus", true);
        editorEditSong.apply();

        ListOfSongsLv = (ListView) findViewById(R.id.lvListOfSongs);
        AddAllSongsBtn = (Button) findViewById(R.id.btnAddAllSongs);
        CancelAddSongsBtn = (Button) findViewById(R.id.btnCancelAddSongs);


        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, songs_list);
        ListOfSongsLv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ListOfSongsLv.setAdapter(arrayAdapter);

        insertSongList();
        displaySongList();

        AddAllSongsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songsChecked();
            }
        });

        CancelAddSongsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddSongsActivity.this, ViewPlayListActivity.class);
                startActivity(intent);
            }
        });
    }

    public void insertSongList() {

        SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

        syntonesWebAPI.getAllSongsFromDB().enqueue(new Callback<SongListResponse>() {
            @Override
            public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {
                SongListResponse songListResponse = response.body();
                List<Song> songList = songListResponse.getSongs();

                for (Song a : songList) {
                    arrayAdapter.add(a.getSongTitle() + " by " + a.getArtist().getArtistName());
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<SongListResponse> call, Throwable t) {

            }
        });

    }

    public void displaySongList() {
        ListOfSongsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SparseBooleanArray checked = ListOfSongsLv.getCheckedItemPositions();

                for (int a = 0; a < checked.size(); a++) {
                    position = checked.keyAt(a);

                    if (checked.valueAt(a)) {

                        selected_list.add(arrayAdapter.getItem(position));
                    }


                }

        }
    }

    );
}

    public void songsChecked() {
        Set<String> hs = new HashSet<>();
        hs.addAll(selected_list);
        selected_list.clear();
        selected_list.addAll(hs);

        for (String a : selected_list) {
            Log.d("CHECKED", a);
        }
    }

/*    public void addToPlaylist() {
        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);
        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences sharedPrefPlaylistInfo = getSharedPreferences("playlistInfo", Context.MODE_PRIVATE);
        final String playlist_name = sharedPrefPlaylistInfo.getString("playlistName", "");
        String username = sharedPrefUserInfo.getString("username", "");

        User user = new User();

        user.setUsername(username);
        syntonesWebAPI.getPlaylistFromDB(user).enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {


                PlaylistResponse playlistResponse = response.body();
                List<Playlist> playlists = playlistResponse.getPlaylists();

                for (Playlist a : playlists) {

                    if (a.getPlaylistName().equals(playlist_name)) {

                        PlaylistSong playlistSong = new PlaylistSong();
                        playlistSong.setSongId(songId);
                        playlistSong.setPlaylistId(a.getPlaylistId());
                        playlistSong.setUser(user);

                        syntonesWebAPI.addToPlaylist(playlistSong);

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
                }

                Log.e("Playlist Response: ", playlistResponse.getMessage().getMessage());
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {

            }
        });
    }*/


}
