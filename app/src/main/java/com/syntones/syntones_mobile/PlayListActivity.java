package com.syntones.syntones_mobile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.syntones.model.Playlist;
import com.syntones.model.User;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.PlaylistResponse;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayListActivity extends AppCompatActivity {
    private PlayListActivity sContext;
    ArrayList<String> play_lists = new ArrayList<>();
    ArrayAdapter<String> arrayAdapater;
    EditText PlayListNameEt;
    ListView PlaylistsLv;
    Button RemoveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        PlaylistsLv = (ListView) findViewById(R.id.lvPlaylists);
        arrayAdapater = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, play_lists);
        PlaylistsLv.setAdapter(arrayAdapater);
        RemoveBtn = (Button) findViewById(R.id.btnRemove);

        PlaylistsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                play_lists.get(position);
            }
        });

        RemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePlayList();
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
            editBtn.setText("Done");
            addBtn.setVisibility(View.VISIBLE);
            removeBtn.setVisibility(View.VISIBLE);
        } else if (btnText.equals("Done")) {

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

                    playlist.setPlaylistName(play_list_name);
                    playlist.setUser(user);


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
                    arrayAdapater.add(play_list_name);
                    arrayAdapater.notifyDataSetChanged();

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

            arrayAdapater.remove(play_lists.get(position));
            arrayAdapater.notifyDataSetChanged();

        }
    }


    public void bottomBar(View view) {
        String btnText;

        btnText = ((Button) view).getText().toString();

        if (btnText.equals("Home")) {

            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);

        } else if (btnText.equals("Search")) {

            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (btnText.equals("Your Library")) {

            Intent intent = new Intent(this, YourLibraryActivity.class);
            startActivity(intent);
        }

    }
}
