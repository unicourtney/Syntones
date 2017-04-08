package com.syntones.remote;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syntones.model.Genre;
import com.syntones.model.Song;
import com.syntones.response.GenreResponse;
import com.syntones.syntones_mobile.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by CourtneyLove on 2/7/2017.
 */

public class CustomListAdapter extends ArrayAdapter<Song> {

    private final Activity context;
    private List<Song> song;
    private final Integer[] imgid;
    private int posImage;
    private String genre;


    public CustomListAdapter(Activity context, List<Song> song, Integer[] imgid) {
        super(context, R.layout.mylist, song);
        // TODO Auto-generated constructor stub


        this.context = context;
        this.song = song;
        this.imgid = imgid;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.mylist, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.textView1);

        SharedPreferences sharedPrefGenre = context.getSharedPreferences("genreList", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefGenre.getString("GenreObject", "");
        Type type = new TypeToken<List<Genre>>() {
        }.getType();
        List<Genre> genreList = gson.fromJson(json, type);

        for (Genre a : genreList) {
            if (song.get(position).getGenreId() == a.getId()) {
                genre = a.getGenre();
            }
        }

        switch (song.get(position).getMood()) {
            case "aggressive":
                posImage = 0;
                break;
            case "brooding":
                posImage = 1;
                break;
            case "cool":
                posImage = 2;
                break;
            case "defiant":
                posImage = 3;
                break;
            case "easygoing":
                posImage = 4;
                break;
            case "empowering":
                posImage = 5;
                break;
            case "fiery":
                posImage = 6;
                break;
            case "lively":
                posImage = 7;
                break;
            case "romantic":
                posImage = 8;
                break;
            case "rowdy":
                posImage = 9;
                break;
            case "sensual":
                posImage = 10;
                break;
            case "somber":
                posImage = 11;
                break;
            case "sophisticated":
                posImage = 12;
                break;
            case "urgent":
                posImage = 13;
                break;
        }


        txtTitle.setText(song.get(position).getSongTitle() + " by " + song.get(position).getArtist().getArtistName());
        extratxt.setText(genre);
        imageView.setImageResource(imgid[posImage]);
        return rowView;

    }


}
