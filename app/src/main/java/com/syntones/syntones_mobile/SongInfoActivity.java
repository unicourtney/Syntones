package com.syntones.syntones_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.syntones.model.Song;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.SongListResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongInfoActivity extends AppCompatActivity {

    final String songs_urls[] = {"http://project-tango.org/Projects/TangoBand/Songs/files/01%20La%20Cumparsita.mp3", "http://project-tango.org/Projects/TangoBand/Songs/files/Silueta%20Portena.mp3"};


    private TextView SongTitleTv, ArtistNameTv;
    private MediaPlayer mediaPlayer;
    private ImageView PlayBtnIv, PauseBtnIv, PreviousBtnIv, NextBtnIv;
    private int counter, length;
    private Button ShowLyricsBtn;
    private SongInfoActivity sContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        PlayBtnIv = (ImageView) findViewById(R.id.ivPlayBtn);
        PauseBtnIv = (ImageView) findViewById(R.id.ivPauseBtn);
        PreviousBtnIv = (ImageView) findViewById(R.id.ivPreviousBtn);
        NextBtnIv = (ImageView) findViewById(R.id.ivNextBtn);
        SongTitleTv = (TextView) findViewById(R.id.tvSongTitle);
        ArtistNameTv = (TextView) findViewById(R.id.tvArtistName);
        ShowLyricsBtn = (Button) findViewById(R.id.btnShowLyrics);


        Bundle extras = getIntent().getExtras();
        String[] song_info = extras.get("SongInfo").toString().split("\\s(by)\\s");
        SongTitleTv.setText(song_info[0]);
        ArtistNameTv.setText(song_info[1]);

        PlayBtnIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playBtn();
            }
        });

        PauseBtnIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pauseBtn();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        PreviousBtnIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    previousBtn();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        NextBtnIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    nextBtn();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ShowLyricsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLyrics();
            }
        });

    }


    public void playBtn() {
        try {

            if (counter == 0) {
                SongTitleTv.setText("La Cumparista");
            } else {
                SongTitleTv.setText("Silueta Portina");
            }


            mediaPlayer.setDataSource(songs_urls[counter]);

            mediaPlayer.prepare();
            mediaPlayer.start();


            PlayBtnIv.setVisibility(View.INVISIBLE);
            PauseBtnIv.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void pauseBtn() throws IOException {
        if (counter == 0) {
            SongTitleTv.setText("La Cumparista");
        } else {
            SongTitleTv.setText("Silueta Portina");
        }

        length = mediaPlayer.getCurrentPosition();
        mediaPlayer.pause();
        mediaPlayer.setDataSource(songs_urls[counter]);
        PlayBtnIv.setVisibility(View.VISIBLE);
        PauseBtnIv.setVisibility(View.INVISIBLE);
    }

    public void previousBtn() throws IOException {

        if (counter > 0) {
            counter = counter - 1;

            mediaPlayer.reset();
            if (counter == 0) {
                SongTitleTv.setText("La Cumparista");
            } else {
                SongTitleTv.setText("Silueta Portina");
            }
            mediaPlayer.setDataSource(songs_urls[counter]);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
    }

    public void nextBtn() throws IOException {
        if (counter < songs_urls.length) {

            counter = counter + 1;

            mediaPlayer.reset();
            if (counter == 0) {
                SongTitleTv.setText("La Cumparista");
            } else {
                SongTitleTv.setText("Silueta Portina");
            }
            mediaPlayer.setDataSource(songs_urls[counter]);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
    }

    public void showLyrics() {


        final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

        syntonesWebAPI.getAllSongsFromDB().enqueue(new Callback<SongListResponse>() {
            @Override
            public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {
                String song_lyrics;
                SongListResponse songListResponse = response.body();
                List<Song> songList = songListResponse.getSongs();
                for (Song s : songList) {

                    if (s.getSongTitle().equals(SongTitleTv.getText()) && s.getArtist().getArtistName().equals(ArtistNameTv.getText())) {

                        song_lyrics = s.getSongLyrics().toString();


                        SharedPreferences sharedPrefSongLyrics= getSharedPreferences("songLyrics", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorSongLyrics = sharedPrefSongLyrics.edit();

                        editorSongLyrics.putString("lyrics", song_lyrics);
                        editorSongLyrics.commit();
                        Toast.makeText(getBaseContext(), song_lyrics, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SongInfoActivity.this, LyricsActivity.class);
                        startActivity(intent);

                    }

                }

                Log.e("Song List Response:", songListResponse.getMessage().getMessage().toString());
            }


            @Override
            public void onFailure(Call<SongListResponse> call, Throwable t) {


                Log.e("Failed", String.valueOf(t.getMessage()));

            }
        });


    }

}
