package com.syntones.syntones_mobile;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class PlayListActivity extends AppCompatActivity {

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
