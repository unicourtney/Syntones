package com.syntones.syntones_mobile;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ser.std.ObjectArraySerializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syntones.model.Genre;
import com.syntones.model.PlayedSongsByTime;
import com.syntones.model.Playlist;
import com.syntones.model.SavedOfflineSongs;
import com.syntones.model.Song;
import com.syntones.model.TemporaryDB;
import com.syntones.model.ThreeItemSet;
import com.syntones.model.TwoItemSet;
import com.syntones.model.User;
import com.syntones.remote.DBHelper;
import com.syntones.remote.IpAddressSetting;
import com.syntones.remote.ScreenOnOffReceiver;
import com.syntones.remote.SyntonesTimerTask;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.ListenResponse;
import com.syntones.response.LogoutResponse;
import com.syntones.response.PlayedSongsByTimeResponse;
import com.syntones.response.SongListResponse;
import com.syntones.response.SongLyricsResponse;
import com.syntones.response.ThreeItemSetResponse;
import com.syntones.response.TwoItemSetResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private PlayerActivity sContext;
    private static MediaPlayer mediaPlayer;
    private ImageView BackIv, SeeMoreIv;
    private ListView BasketRecomLv;
    private Switch SaveOfflineSw;
    private String song1, song2, username, userID, userUUID, activityState, songPath;
    private File songFile;
    private Button PreviousBtn, PlayBtn, PauseBtn, NextBtn, AddtoPlaylistBtn;
    private TextView SongStartTv, SongEndTv, SongTitleTv, ArtistNameTv, SeeMoreTv, LyricsTv, YouMightLikeTv;
    private SeekBar SongBarSb;
    private Handler myHandler = new Handler();
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> songs = new ArrayList<>();
    private String[] nextId, nextUrl, nextSongTitle, nextArtistName;
    private int counter = 0, nextSize, position, startTime, endTime, count = 0, twoItemSetCount = 0, threeItemSetCount = 0;
    private long playlist_id;
    private static int THRESHOLD = 50;
    public boolean isPlaying, twoItemIsNull, threeItemIsNull, songPlaying;
    private static IpAddressSetting iPAddressSetting = new IpAddressSetting();
    private static String IPADDRESS = "http://" + iPAddressSetting.getiPAddress();
    private SyntonesTimerTask syntonesTimerTask = new SyntonesTimerTask();
    private RelativeLayout PlayerLr;
    private ScreenOnOffReceiver onoffReceiver = new ScreenOnOffReceiver("Player");
    private List<Genre> genreList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(onoffReceiver, filter);

        //Buttons
        PreviousBtn = (Button) findViewById(R.id.btnPrev);
        PlayBtn = (Button) findViewById(R.id.btnPlay);
        PauseBtn = (Button) findViewById(R.id.btnPause);
        NextBtn = (Button) findViewById(R.id.btnNext);
        AddtoPlaylistBtn = (Button) findViewById(R.id.btnAddToPlaylist);

        //TextViews
        ArtistNameTv = (TextView) findViewById(R.id.tvArtistName);
        SongStartTv = (TextView) findViewById(R.id.tvSongStart);
        SongEndTv = (TextView) findViewById(R.id.tvSongEnd);
        SongTitleTv = (TextView) findViewById(R.id.tvSongTitle);
        SeeMoreTv = (TextView) findViewById(R.id.tvSeeMore);
        LyricsTv = (TextView) findViewById(R.id.tvLyrics);
        YouMightLikeTv = (TextView) findViewById(R.id.tvYouMightLike);


        //ImageViews
        BackIv = (ImageView) findViewById(R.id.ivBack);
        SeeMoreIv = (ImageView) findViewById(R.id.ivSeeMore);

        //SeekBars
        SongBarSb = (SeekBar) findViewById(R.id.sbSongBar);

        //ListViews
        BasketRecomLv = (ListView) findViewById(R.id.lvBasketRecom);

        //Switch
        SaveOfflineSw = (Switch) findViewById(R.id.swSaveOffline);

        //RelativeLayout
        PlayerLr = (RelativeLayout) findViewById(R.id.rlPlayer);

        SyntonesTimerTask.getInstance().stopCounter();
        SharedPreferences sharedPrefGenre = getSharedPreferences("genreList", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefGenre.getString("GenreObject", "");
        Type type = new TypeToken<List<Genre>>() {
        }.getType();
        genreList = gson.fromJson(json, type);

        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        username = sharedPrefUserInfo.getString("username", "");
        userID = String.valueOf(sharedPrefUserInfo.getLong("userID", 0));
        userUUID = sharedPrefUserInfo.getString("userUUID", "");

        SharedPreferences sharedPrefCounter = getSharedPreferences("counter", Context.MODE_PRIVATE);


/*        if (sharedPrefCounter.getInt("counterValue", 0) != 0) {
            counter = sharedPrefCounter.getInt("counterValue", 0);
        }*/

        SharedPreferences sharedPrefActivityInfo = getSharedPreferences("activityInfo", Context.MODE_PRIVATE);
        activityState = sharedPrefActivityInfo.getString("activityState", "");
        SharedPreferences sharedPrefPlaylistInfo = getSharedPreferences("playlistInfo", Context.MODE_PRIVATE);
        if (activityState.equals("Playlist")) {
            final long playlist_id = Long.parseLong(sharedPrefPlaylistInfo.getString("playlistId", ""));


        } else if (activityState.equals("SearchActivity")) {
            PreviousBtn.setEnabled(false);
            NextBtn.setEnabled(false);
        }

/*        SharedPreferences.Editor editorPlaylistInfo = sharedPrefPlaylistInfo.edit();
        editorPlaylistInfo.clear();
        editorPlaylistInfo.apply();*/
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        SharedPreferences sharedPrefPlayedSongInfo = getSharedPreferences("playedSongInfo", 0);
        SharedPreferences.Editor editorPlayedSongInfo = sharedPrefPlayedSongInfo.edit();
        final int size = sharedPrefPlayedSongInfo.getInt("song_url_array" + "_size", 0);
        nextSize = size;
        final String[] songs_urls = new String[size];
        final String[] songs_titles = new String[size];
        final String[] songs_artists = new String[size];
        final String[] songs_lyrics = new String[size];
        final String[] songs_ids = new String[size];
        final String[] songs_genre = new String[size];

        SharedPreferences sharedPrefSongInfo = getSharedPreferences("songInfo", Context.MODE_PRIVATE);
        String currentSongTitlePosition = sharedPrefSongInfo.getString("songTitle", "");
        String currentArtistNamePosition = sharedPrefSongInfo.getString("artistName", "");
        SharedPreferences.Editor editorSongInfo = sharedPrefSongInfo.edit();

        Log.d("DLI NULL CYA UI", String.valueOf(size));

        Log.d("currentArtistName", currentArtistNamePosition);
        Log.d("currentSongTitle", currentSongTitlePosition);
        for (int a = 0; a < size; a++) {


            songs_urls[a] = sharedPrefPlayedSongInfo.getString("song_url_array" + "_" + a, null);
            songs_titles[a] = sharedPrefPlayedSongInfo.getString("song_titles_array" + "_" + a, null);
            songs_artists[a] = sharedPrefPlayedSongInfo.getString("song_artists_array" + "_" + a, null);
            songs_lyrics[a] = sharedPrefPlayedSongInfo.getString("song_lyrics_array" + "_" + a, null);
            songs_ids[a] = sharedPrefPlayedSongInfo.getString("song_id_array" + "_" + a, null);
            songs_genre[a] = sharedPrefPlayedSongInfo.getString("song_genre_array" + "_" + a, null);
            Log.d("SONGURL:", IPADDRESS + songs_urls[a]);
            Log.d("SONG TITLE", songs_titles[a]);
            Log.d("ARTIST NAME", songs_artists[a]);
            if (songs_titles[a].equals(currentSongTitlePosition) && songs_artists[a].equals(currentArtistNamePosition)) {
                counter = a;
                Log.d("POSITION: ", String.valueOf(a));

                editorSongInfo.clear();
                editorSongInfo.apply();
            }


        }
        final SharedPreferences sharedControllerPref = getSharedPreferences("playerControls", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editorControllerPref = sharedControllerPref.edit();
        if (mediaPlayer != null) {
            Log.d("PLAYING", "TRUE");
            isPlaying = true;
            if (sharedPrefCounter.getInt("counterValue", 0) != 0) {

                int counterValue = sharedPrefCounter.getInt("counterValue", 0);
                Log.e("COUNT", String.valueOf(counterValue));

                count = counterValue;

            }
            myHandler.removeCallbacks(UpdateSongTime);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            try {
                editorControllerPref.putString("isPaused", "notPaused");
                editorControllerPref.apply();


                if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {

                    Log.d("PLAY OFFLINE", "FALSE");
                    play(size, songs_ids, songs_urls, songs_titles, songs_artists, playlist_id, songs_lyrics);
                } else {
                    Log.d("PLAY OFFLINE", "TRUE");

                    playOffline(size, songs_ids, songs_urls, songs_titles, songs_artists, userID, songs_lyrics);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            isPlaying = false;
            mediaPlayer = new MediaPlayer();
        }


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        SongTitleTv.setText(songs_titles[counter]);
        ArtistNameTv.setText(songs_artists[counter]);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songs);

        BasketRecomLv.setAdapter(arrayAdapter);
        BasketRecomLv.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.i("SCROLLING DOWN", "TRUE");
                    syntonesTimerTask.stopPlayerCounter();
                    SyntonesTimerTask.getInstance().stopPlayerCounter();

                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.i("SCROLLING UP", "TRUE");
                    SyntonesTimerTask.getInstance().stopPlayerCounter();

                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });

        SongBarSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar SongBarSb, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }
        });


        editorControllerPref.clear();
        editorControllerPref.apply();

        PreviousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                    SyntonesTimerTask.getInstance().stopPlayerCounter();

                    editorControllerPref.putString("isPaused", "notPaused");
                    editorControllerPref.apply();
                    if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
                        Log.d("PLAY OFFLINE", "FALSE");

                        previous(size, songs_ids, songs_urls, songs_titles, songs_artists, playlist_id, songs_lyrics);
                    } else {
                        Log.d("PLAY OFFLINE", "TRUE");
                        String fileName = URLUtil.guessFileName(songs_urls[counter], null, MimeTypeMap.getFileExtensionFromUrl(songs_urls[counter]));
                        File downloadDir = new File(getFilesDir() + "/Syntones/savedSongs/", userID + "-" + fileName);
                        songFile = new File(downloadDir.getName());
                        convertSongToTxt(songFile.getName());

                        previousOffline(size, songs_ids, songs_urls, songs_titles, songs_artists, userID, songs_lyrics);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        PlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SyntonesTimerTask.getInstance().stopPlayerCounter();

                    if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
                        Log.d("PLAY OFFLINE", "FALSE");

                        play(size, songs_ids, songs_urls, songs_titles, songs_artists, playlist_id, songs_lyrics);
                    } else {
                        Log.d("PLAY OFFLINE", "TRUE");

                        playOffline(size, songs_ids, songs_urls, songs_titles, songs_artists, userID, songs_lyrics);
                    }
                    PlayBtn.setEnabled(false);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });


        PauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editorControllerPref.putString("isPaused", "paused");
                editorControllerPref.apply();

                pause();

                if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
                    Log.d("PLAY OFFLINE", "FALSE");
                    SyntonesTimerTask.getInstance().isPlaying(PlayerActivity.this, "Player");
                } else {
                    Log.d("PLAY OFFLINE", "TRUE");
//
                }


            }
        });

        NextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SyntonesTimerTask.getInstance().stopPlayerCounter();
                    editorControllerPref.putString("isPaused", "notPaused");
                    editorControllerPref.apply();
                    if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
                        Log.d("PLAY OFFLINE", "FALSE");

                        next(size, songs_ids, songs_urls, songs_titles, songs_artists, playlist_id, songs_lyrics);
                    } else {
                        String fileName = URLUtil.guessFileName(songs_urls[counter], null, MimeTypeMap.getFileExtensionFromUrl(songs_urls[counter]));
                        File downloadDir = new File(getFilesDir() + "/Syntones/savedSongs/", userID + "-" + fileName);
                        songFile = new File(downloadDir.getName());
                        convertSongToTxt(songFile.getName());

                        nextOffline(size, songs_ids, songs_urls, songs_titles, songs_artists, userID, songs_lyrics);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });


        AddtoPlaylistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addToPlaylist();
            }
        });


        BackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                backToPlaylist();
            }
        });

        SeeMoreTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlayerActivity.this, SeeMoreActivity.class);
                SyntonesTimerTask.getInstance().stopPlayerCounter();
                startActivity(intent);
            }
        });
        SaveOfflineSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isSongSaved(songs_ids[counter], userID) == false) {
                    if (SaveOfflineSw.isChecked()) {

                        saveSongsOffline(userID, songs_ids, songs_urls, songs_titles, songs_artists, songs_lyrics, songs_genre);
                    }
                }

            }
        });

        if (isSongSaved(songs_ids[counter], userID) == true) {
            SaveOfflineSw.setChecked(true);
            SaveOfflineSw.setEnabled(false);
        }

        PlayerLr.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
/*
                syntonesTimerTask.startCounter(PlayerActivity.this, "Player");*/
                return true;
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
                SyntonesTimerTask.getInstance().stopPlayerCounter();
                syntonesTimerTask.startPlayerCounter(context, tag);
                //Your logic comes here whatever you want perform when screen is in off state                                                   }

            } else {
                Log.e("Screen mode", " Screen is in on State");

                SyntonesTimerTask.getInstance().stopPlayerCounter();

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

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("STATE", "RESUME");
        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userUUID = sharedPrefUserInfo.getString("userUUID", "");
        Log.d("UUID RES", userUUID);

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
            Log.d("CONNECTION YL", "TRUE");
            Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
            Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
            SyntonesTimerTask.getInstance().isPlaying(PlayerActivity.this, "Player");

        } else {
            Log.d("CONNECTION YL", "FALSE");
            Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
            Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
            SyntonesTimerTask.getInstance().stopCounter();
            SyntonesTimerTask.getInstance().stopPlayerCounter();
        }


    }

    public void saveSongsOffline(final String userID, final String[] songs_ids, final String[] songs_urls, final String[] songs_titles, final String[] songs_artists, final String[] songs_lyrics, final String[] songs_genre) {
        SaveOfflineSw.setEnabled(false);
        DBHelper db = new DBHelper(PlayerActivity.this);
        SavedOfflineSongs savedOfflineSongs = new SavedOfflineSongs();
        Calendar calendar = Calendar.getInstance();
        DateFormat startDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        savedOfflineSongs.setUserName(userID);
        savedOfflineSongs.setSongId(songs_ids[counter]);
        savedOfflineSongs.setArtistName(songs_artists[counter]);
        savedOfflineSongs.setSongTitle(songs_titles[counter]);
        savedOfflineSongs.setLyrics(songs_lyrics[counter]);
        savedOfflineSongs.setFilePath(songs_urls[counter]);
        savedOfflineSongs.setStartDate(startDateFormat.format(calendar.getTime()));
        savedOfflineSongs.setGenre(songs_genre[counter]);

        db.insertSavedSong(savedOfflineSongs);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(IPADDRESS + songs_urls[counter]));
        final String fileName = URLUtil.guessFileName(songs_urls[counter], null, MimeTypeMap.getFileExtensionFromUrl(songs_urls[counter]));
        request.setTitle(fileName);
        request.setVisibleInDownloadsUi(false);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        File userSavedDir = new File(getFilesDir() + "/Syntones");
        if (!userSavedDir.exists()) {
            userSavedDir.mkdirs();
        } else {
            File savedSongsDir = new File(userSavedDir.getAbsolutePath() + "/savedSongs");
            if (!savedSongsDir.exists()) {
                savedSongsDir.mkdirs();
            }
        }


        request.setDestinationInExternalPublicDir(getFilesDir() + "/Syntones/savedSongs/", userID + "-" + fileName);
        DownloadManager downloadManager = (DownloadManager) PlayerActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);


        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                Log.d("COMPLETE", "DOWNLOAD IS DONE");
                convertSongToTxt(userID + "-" + fileName);
                Toast.makeText(PlayerActivity.this, "DONE", Toast.LENGTH_LONG).show();

            }
        };
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


    }


    public boolean isSongSaved(String song_id, String userID) {

        boolean isSaved = false;

        DBHelper db = new DBHelper(this);

        ArrayList<SavedOfflineSongs> savedOfflineSongsArrayList = db.getAllSavedOfflineSongsFromUser(userID);

        for (SavedOfflineSongs a : savedOfflineSongsArrayList) {

            if (song_id.equals(a.getSongId())) {
                isSaved = true;
                Log.d("IS SAVED", userID + a.getSongId());
            }

        }
        return isSaved;
    }

    public String convertSongToMp3(String songInfo) {

        File extStore = Environment.getExternalStorageDirectory();
        File downloadDir = new File(extStore + getFilesDir().getPath() + "/Syntones/savedSongs/");

        File listAllFiles[] = downloadDir.listFiles();

        if (listAllFiles != null && listAllFiles.length > 0) {
            for (File currentFile : listAllFiles) {
                if (currentFile.isDirectory()) {

                    Log.d("DIR", currentFile.toString());


                } else {
                    if (currentFile.getName().endsWith(".txt")) {

                        if (currentFile.getName().equals(songInfo.replace(".mp3", ".txt"))) {
                            Log.d("FILE", currentFile.getName().toString());
                            File getFileDir = currentFile.getAbsoluteFile();
                            File renameFile = new File(downloadDir + "", currentFile.getName().replace(".txt", ".mp3"));
                            getFileDir.renameTo(renameFile);
                            songPath = renameFile.getAbsolutePath();
                            Log.e("Path", renameFile.getAbsolutePath());
                        }
                    }
                }
            }
        }

        return songPath;
    }


    public void convertSongToTxt(String songInfo) {

        File extStore = Environment.getExternalStorageDirectory();
        File downloadDir = new File(extStore + getFilesDir().getPath() + "/Syntones/savedSongs/");

        Log.d("FILE-TXT", downloadDir.getName().toString());


        File listAllFiles[] = downloadDir.listFiles();

        if (listAllFiles != null && listAllFiles.length > 0) {

            for (File currentFile : listAllFiles) {
                if (currentFile.isDirectory()) {

                    Log.d("DIR", currentFile.toString());


                } else {
                    Log.d("FILE-TXT", currentFile.getName().toString());
                    if (currentFile.getName().endsWith(".mp3")) {

                        if (currentFile.getName().equals(songInfo)) {
                            Log.d("FILE-TXT", currentFile.getName().toString());
                            File getFileDir = currentFile.getAbsoluteFile();
                            File renameFile = new File(downloadDir + "", currentFile.getName().replace(".mp3", ".txt"));
                            getFileDir.renameTo(renameFile);
                            Log.e("Path", renameFile.getAbsolutePath());
                        }
                    }
                }
            }
        }

    }

    public void convertAllSongsToTxt() {
        File extStore = Environment.getExternalStorageDirectory();
        File downloadDir = new File(extStore + getFilesDir().getPath() + "/Syntones/savedSongs/");

        Log.d("FILE-TXT", downloadDir.getName().toString());


        File listAllFiles[] = downloadDir.listFiles();

        if (listAllFiles != null && listAllFiles.length > 0) {

            for (File currentFile : listAllFiles) {
                if (currentFile.isDirectory()) {

                    Log.d("DIR", currentFile.toString());


                } else {
                    Log.d("FILE-TXT", currentFile.getName().toString());
                    if (currentFile.getName().endsWith(".mp3")) {

                        Log.d("FILE-TXT", currentFile.getName().toString());
                        File getFileDir = currentFile.getAbsoluteFile();
                        File renameFile = new File(downloadDir + "", currentFile.getName().replace(".mp3", ".txt"));
                        getFileDir.renameTo(renameFile);
                        Log.e("Path", renameFile.getAbsolutePath());

                    }
                }
            }
        }
    }

    public void previousOffline(final int size, final String[] songs_ids, final String[] songs_urls, final String[] songs_titles, final String[] songs_artists, final String userID, final String[] songs_lyrics) throws IOException {
        if (counter > 0) {
            counter = counter - 1;

            if (counter == 0) {

                counter = 0;
            }

            String fileName = URLUtil.guessFileName(songs_urls[counter], null, MimeTypeMap.getFileExtensionFromUrl(songs_urls[counter]));
            File downloadDir = new File(getFilesDir() + "/Syntones/savedSongs/", userID + "-" + fileName);
            songFile = new File(downloadDir.getName());
            Log.d("FILE", downloadDir.getName());
            Log.d("NAME", fileName);

            if (downloadDir.getName().equals(userID + "-" + fileName)) {
                Log.d("FILE", downloadDir.toString());

                mediaPlayer.reset();
                mediaPlayer.setDataSource(convertSongToMp3(downloadDir.getName()));
                mediaPlayer.prepare();
                mediaPlayer.start();
            }

            SongTitleTv.setText(songs_titles[counter]);
            PlayBtn.setEnabled(true);
            PauseBtn.setEnabled(false);


            endTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();
            SongBarSb.setMax(endTime);

            SongEndTv.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
            );

            SongStartTv.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
            );

            SongBarSb.setProgress(startTime);
            myHandler.postDelayed(UpdateSongTime, 100);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        convertSongToTxt(songFile.getName());
                        nextOffline(size, songs_ids, songs_urls, songs_titles, songs_artists, userID, songs_lyrics);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            SongTitleTv.setText(songs_titles[counter]);
            ArtistNameTv.setText(songs_artists[counter]);
            PauseBtn.setEnabled(true);
            PlayBtn.setEnabled(false);
        }
    }


    public void playOffline(final int size, final String[] songs_ids, final String[] songs_urls, final String[] songs_titles, final String[] songs_artists, final String userID, final String[] songs_lyrics) throws IOException {

        SharedPreferences sharedControllerPref = getSharedPreferences("playerControls", Context.MODE_PRIVATE);
        String isPaused = sharedControllerPref.getString("isPaused", "");

        Log.d("IS PLAYING", String.valueOf(mediaPlayer.isPlaying()));
        if (mediaPlayer.isPlaying()) {
            myHandler.removeCallbacks(UpdateSongTime);
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Log.d("PAUSED STATE:", isPaused);

        if (isPaused.equals("paused")) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
            mediaPlayer.start();
        } else {
            LyricsTv.setText(songs_lyrics[counter].replace("\\n", ""));
            LyricsTv.setSelected(true);
            String fileName = URLUtil.guessFileName(songs_urls[counter], null, MimeTypeMap.getFileExtensionFromUrl(songs_urls[counter]));
            File downloadDir = new File(getFilesDir() + "/Syntones/savedSongs/", userID + "-" + fileName);
            songFile = new File(downloadDir.getName());
            Log.d("FILE", downloadDir.getName());
            Log.d("NEM", fileName.replace(".mp3", ".txt"));

            convertSongToTxt(songFile.getName());

            if (downloadDir.getName().equals(userID + "-" + fileName)) {
                Log.d("FILE", downloadDir.toString());
                if (downloadDir.toString() != null) {
                    mediaPlayer.setDataSource(convertSongToMp3(downloadDir.getName()));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }

            }
        }
        endTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();
        SongBarSb.setMax(endTime);

        SongEndTv.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
        );

        SongStartTv.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
        );

        SongBarSb.setProgress(startTime);
        myHandler.postDelayed(UpdateSongTime, 100);
        SongTitleTv.setText(songs_titles[counter]);
        ArtistNameTv.setText(songs_artists[counter]);


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    if (size != 1) {

                        if (counter != size - 1) {
                            convertSongToTxt(songFile.getName());
                            nextOffline(size, songs_ids, songs_urls, songs_titles, songs_artists, userID, songs_lyrics);
                        } else {
                            convertSongToTxt(songFile.getName());
                            mediaPlayer.stop();

                        }
                        convertSongToTxt(songFile.getName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        PlayBtn.setEnabled(false);
        PauseBtn.setEnabled(true);
    }

    public void nextOffline(final int size, final String[] songs_ids, final String[] songs_urls, final String[] songs_titles, final String[] songs_artists, final String userID, final String[] songs_lyrics) throws IOException {

        if (counter < size) {

            counter = counter + 1;

            if (counter == size) {

                counter = 0;
            }
            convertSongToTxt(songFile.getName());
            LyricsTv.setText(songs_lyrics[counter].replace("\\n", ""));
            LyricsTv.setSelected(true);
            String fileName = URLUtil.guessFileName(songs_urls[counter], null, MimeTypeMap.getFileExtensionFromUrl(songs_urls[counter]));
            File downloadDir = new File(getFilesDir() + "/Syntones/savedSongs/", userID + "-" + fileName);
            songFile = new File(downloadDir.getName());
            Log.d("FILE", downloadDir.getName());
            Log.d("NAME", fileName);
            if (downloadDir.getName().equals(userID + "-" + fileName)) {
                Log.d("FILE", downloadDir.toString());
                mediaPlayer.reset();
                mediaPlayer.setDataSource(convertSongToMp3(downloadDir.getName()));
                mediaPlayer.prepare();
                mediaPlayer.start();
                endTime = mediaPlayer.getDuration();
                startTime = mediaPlayer.getCurrentPosition();
                SongBarSb.setMax(endTime);
            }

        }

        SongEndTv.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
        );

        SongStartTv.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
        );

        SongBarSb.setProgress(startTime);
        myHandler.postDelayed(UpdateSongTime, 100);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {

                    if (size != 1) {

                        if (counter != size - 1) {
                            convertSongToTxt(songFile.getName());
                            nextOffline(size, songs_ids, songs_urls, songs_titles, songs_artists, userID, songs_lyrics);
                        } else {
                            mediaPlayer.stop();
                            convertAllSongsToTxt();

                        }
                        convertSongToTxt(songFile.getName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        SongTitleTv.setText(songs_titles[counter]);
        ArtistNameTv.setText(songs_artists[counter]);

        PlayBtn.setEnabled(false);
        PauseBtn.setEnabled(true);


    }


    public void previous(final int size, final String[] songs_ids, final String[] songs_urls, final String[] songs_titles, final String[] songs_artists, final long playlist_id, final String[] songs_lyrics) throws IOException, ParseException {

        if (counter > 0) {
            counter = counter - 1;

            if (counter == 0) {

                counter = 0;
            }
            Log.d("PREVIOUS:", IPADDRESS + songs_urls[counter]);
            SharedPreferences sharedPrefStorage = getSharedPreferences("storage", Context.MODE_PRIVATE);
            SharedPreferences.Editor editorStorage = sharedPrefStorage.edit();


            count++;
            if (count == 1) {
                Log.d("COUNT 1:", String.valueOf(counter));
                editorStorage.putString("song1", songs_ids[counter]);
                editorStorage.apply();
                song1 = sharedPrefStorage.getString("song1", "");


            } else if (count == 2) {
                Log.d("COUNT 2:", String.valueOf(counter));
                editorStorage.putString("song2", songs_ids[counter]);
                editorStorage.apply();
                song1 = sharedPrefStorage.getString("song1", "");
                song2 = sharedPrefStorage.getString("song2", "");
                getThreeItemSet(song1 + "," + song2);

            } else if (count == 3) {

                editorStorage.putString("song1", song2);
                Log.d("COUNT 3:", String.valueOf(counter));
                editorStorage.putString("song2", songs_ids[counter]);
                editorStorage.apply();
                song1 = sharedPrefStorage.getString("song1", "");
                song2 = sharedPrefStorage.getString("song2", "");
                getThreeItemSet(song1 + "," + song2);
                count = 1;

            }


            mediaPlayer.reset();

            SongTitleTv.setText(songs_titles[counter]);
            PlayBtn.setEnabled(true);
            PauseBtn.setEnabled(false);
            mediaPlayer.setDataSource(IPADDRESS + songs_urls[counter]);
            mediaPlayer.prepare();
            mediaPlayer.start();
            LyricsTv.setText(songs_lyrics[counter].replace("\\n", ""));
            LyricsTv.setSelected(true);
            saveToTemporaryDB(songs_ids[counter], userUUID);
            SyntonesTimerTask.getInstance().isPlaying(PlayerActivity.this, "Player");
            endTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();
            SongBarSb.setMax(endTime);

            SongEndTv.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
            );

            SongStartTv.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
            );

            SongBarSb.setProgress(startTime);
            myHandler.postDelayed(UpdateSongTime, 100);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {

                        next(size, songs_ids, songs_urls, songs_titles, songs_artists, playlist_id, songs_lyrics);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
            SongTitleTv.setText(songs_titles[counter]);
            ArtistNameTv.setText(songs_artists[counter]);
            PauseBtn.setEnabled(true);
            PlayBtn.setEnabled(false);
        }
    }

    public void play(final int size, final String[] songs_ids, final String[] songs_urls, final String[] songs_titles, final String[] songs_artists, final long playlist_id, final String[] songs_lyrics) throws IOException, ParseException {
        SharedPreferences sharedControllerPref = getSharedPreferences("playerControls", Context.MODE_PRIVATE);
        String isPaused = sharedControllerPref.getString("isPaused", "");

   /*     Log.d("IS PLAYING", String.valueOf(mediaPlayer.isPlaying()));*/
        Log.d("PAUSED STATE:", isPaused);

        if (isPaused.equals("paused")) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
            mediaPlayer.start();
            PauseBtn.setEnabled(true);

        } else {
            PauseBtn.setEnabled(true);
            Log.d("PLAY:", IPADDRESS + songs_urls[counter]);
            Log.d("PLAY POSITION:", String.valueOf(counter));
            LyricsTv.setText(songs_lyrics[counter].replace("\\n", ""));
            LyricsTv.setSelected(true);
            mediaPlayer.setDataSource(IPADDRESS + songs_urls[counter]);
            mediaPlayer.prepare();
            mediaPlayer.start();
            SyntonesTimerTask.getInstance().isPlaying(PlayerActivity.this, "Player");
            SharedPreferences sharedPrefStorage = getSharedPreferences("storage", Context.MODE_PRIVATE);
            SharedPreferences.Editor editorStorage = sharedPrefStorage.edit();

            SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

            SharedPreferences sharedPrefActivityInfo = getSharedPreferences("activityInfo", Context.MODE_PRIVATE);


            count++;
            if (arrayAdapter != null) {
                arrayAdapter.clear();
                arrayAdapter.notifyDataSetChanged();
            }

            if (count == 1) {
                editorStorage.putString("song1", songs_ids[counter]);
                editorStorage.apply();
                song1 = sharedPrefStorage.getString("song1", "");
                getTwoItemSet(songs_ids[counter]);

            } else if (count == 2) {
                editorStorage.putString("song2", songs_ids[counter]);
                editorStorage.apply();
                song1 = sharedPrefStorage.getString("song1", "");
                song2 = sharedPrefStorage.getString("song2", "");
                getThreeItemSet(song1 + "," + song2);

            } else if (count == 3) {

                editorStorage.putString("song1", sharedPrefStorage.getString("song2", ""));
                editorStorage.putString("song2", songs_ids[counter]);
                editorStorage.apply();
                song1 = sharedPrefStorage.getString("song1", "");
                song2 = sharedPrefStorage.getString("song2", "");
                count = 0;
                getThreeItemSet(song1 + "," + song2);
            }

            endTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();
            SongBarSb.setMax(endTime);

            SongEndTv.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
            );

            SongStartTv.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
            );

            SongBarSb.setProgress(startTime);
            myHandler.postDelayed(UpdateSongTime, 100);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        Log.d("PLAY SIZE:", String.valueOf(counter) + " - " + String.valueOf(nextSize));
                        if (size != 1) {

                            if (counter != size - 1) {

                                next(size, songs_ids, songs_urls, songs_titles, songs_artists, playlist_id, songs_lyrics);
                            } else {
                                mediaPlayer.stop();
                                SyntonesTimerTask.getInstance().isPlaying(PlayerActivity.this, "Player");


                            }

                        } else {
                            SyntonesTimerTask.getInstance().isPlaying(PlayerActivity.this, "Player");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });

            PlayBtn.setEnabled(false);
            PauseBtn.setEnabled(true);
            displayRecommendation();
            if (arrayAdapter != null) {
                arrayAdapter.notifyDataSetChanged();
                BasketRecomLv.setAdapter(arrayAdapter);
            }
/*            arrayAdapter.notifyDataSetChanged();
            BasketRecomLv.setAdapter(arrayAdapter);*/

            saveToTemporaryDB(songs_ids[counter], userUUID);


            String activityState = sharedPrefActivityInfo.getString("activityState", "");
            if (activityState.equals("Playlist")) {
                saveToRecentlyPlayedPlaylist(username);
            }


        }


    }


    public void pause() {
        mediaPlayer.pause();
        SyntonesTimerTask.getInstance().isPlaying(PlayerActivity.this, "Player");
        endTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();
        SongBarSb.setMax(endTime);

        SongEndTv.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
        );

        SongStartTv.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));
        PlayBtn.setEnabled(true);
        PauseBtn.setEnabled(false);
    }


    public void next(final int size, final String[] songs_ids, final String[] songs_urls, final String[] songs_titles, final String[] songs_artists, final long playlist_id, final String[] songs_lyrics) throws IOException, ParseException {

        SharedPreferences sharedControllerPref = getSharedPreferences("playerControls", Context.MODE_PRIVATE);
        String isPaused = sharedControllerPref.getString("isPaused", "");

        SharedPreferences sharedPrefStorage = getSharedPreferences("storage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorStorage = sharedPrefStorage.edit();
        Log.d("PAUSE", isPaused);
        Log.d("POSITION NEXT: ", String.valueOf(counter));
        if (counter < size - 1) {

            if (counter == size - 1) {

                counter = 0;

                next(size, songs_ids, songs_urls, songs_titles, songs_artists, playlist_id, songs_lyrics);
            }
            mediaPlayer.reset();
            counter = counter + 1;
            LyricsTv.setText(songs_lyrics[counter].replace("\\n", ""));
            LyricsTv.setSelected(true);

            mediaPlayer.reset();
            Log.d("NEXT:", IPADDRESS + songs_urls[counter]);
            mediaPlayer.setDataSource(IPADDRESS + songs_urls[counter]);
            mediaPlayer.prepare();
            mediaPlayer.start();

            SyntonesTimerTask.getInstance().isPlaying(PlayerActivity.this, "Player");
            endTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();
            SongBarSb.setMax(endTime);


            count++;
            arrayAdapter.clear();
            arrayAdapter.notifyDataSetChanged();
            if (count == 1) {
                editorStorage.putString("song1", songs_ids[counter]);
                editorStorage.apply();
                song1 = sharedPrefStorage.getString("song1", "");

            } else if (count == 2) {
                editorStorage.putString("song2", songs_ids[counter]);
                editorStorage.apply();
                song1 = sharedPrefStorage.getString("song1", "");
                song2 = sharedPrefStorage.getString("song2", "");
                getThreeItemSet(song1 + "," + song2);

            } else if (count == 3) {

                editorStorage.putString("song1", song2);
                editorStorage.putString("song2", songs_ids[counter]);
                editorStorage.apply();
                song1 = sharedPrefStorage.getString("song1", "");
                song2 = sharedPrefStorage.getString("song2", "");
                count = 1;
                getThreeItemSet(song1 + "," + song2);
            }

            BasketRecomLv.setAdapter(arrayAdapter);
            displayRecommendation();
            if (arrayAdapter != null) {
                arrayAdapter.notifyDataSetChanged();
                BasketRecomLv.setAdapter(arrayAdapter);
            }
            saveToTemporaryDB(songs_ids[counter], userUUID);


            saveToRecentlyPlayedPlaylist(username);

            SongEndTv.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
            );

            SongStartTv.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
            );

            SongBarSb.setProgress(startTime);
            myHandler.postDelayed(UpdateSongTime, 100);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {

                        if (size != 1) {

                            if (counter != size - 1) {

                                next(size, songs_ids, songs_urls, songs_titles, songs_artists, playlist_id, songs_lyrics);
                            } else {
                                mediaPlayer.stop();
                                SyntonesTimerTask.getInstance().isPlaying(PlayerActivity.this, "Player");


                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
            SongTitleTv.setText(songs_titles[counter]);
            ArtistNameTv.setText(songs_artists[counter]);

            PlayBtn.setEnabled(false);
            PauseBtn.setEnabled(true);
        }


    }


    private void release() {
        if (mediaPlayer == null) {
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }


    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            SongStartTv.setText(String.format("%d:%d",

                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            SongBarSb.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
        }
    };


    public void addToPlaylist() {

        SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

        syntonesWebAPI.getAllSongsFromDB().enqueue(new Callback<SongListResponse>() {
            @Override
            public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {
                SongListResponse songListResponse = response.body();
                List<Song> songList = songListResponse.getSongs();
                for (Song a : songList) {

                    if (a.getSongTitle().equals(SongTitleTv.getText().toString()) && a.getArtist().getArtistName().equals(ArtistNameTv.getText().toString())) {

                        Intent intent = new Intent(PlayerActivity.this, PlayListActivity.class);
                        intent.putExtra("SongId", a.getSongId());
                        intent.putExtra("buttonStatus", "addToPlaylist");
                        startActivity(intent);

                        Log.e("Song List Response ", songListResponse.getMessage().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<SongListResponse> call, Throwable t) {

            }
        });

    }

    public void saveToTemporaryDB(String songs_urls, String userUUID) throws ParseException {


        TemporaryDB temporaryDB = new TemporaryDB();

        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userUUID = sharedPrefUserInfo.getString("userUUID", "");
        Log.d("UUID RES", userUUID);

        Date currdate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
        DateFormat timeFormat = new SimpleDateFormat("HH");
        Timestamp timeStamp = new Timestamp(currdate.getTime());
        String currentDateTimeString = dateFormat.format(timeStamp);
        Log.d("DATE", String.valueOf(timeStamp));

        String partOfDay = part_of_day(Integer.parseInt(timeFormat.format(currdate)));

        temporaryDB.setSong_id(Long.parseLong(songs_urls));
        temporaryDB.setSession_id(userUUID);
        temporaryDB.setDate(timeStamp);
        temporaryDB.setUser_id(Long.parseLong(userID));
        temporaryDB.setPart_of_day(partOfDay);


        SyntonesWebAPI.Factory.getInstance(sContext).listen(temporaryDB).enqueue(new Callback<ListenResponse>() {
            @Override
            public void onResponse(Call<ListenResponse> call, Response<ListenResponse> response) {

                ListenResponse listenResponse = response.body();


                Log.d("Listen Repsone", listenResponse.getMessage().getMessage());
            }

            @Override
            public void onFailure(Call<ListenResponse> call, Throwable t) {

            }
        });
    }

    public void saveToRecentlyPlayedPlaylist(String username) {
        if (playlist_id != 0) {

            Playlist playlist = new Playlist();
            User user = new User();

            user.setUsername(username);
            playlist.setPlaylistId(playlist_id);
            playlist.setUser(user);
            SyntonesWebAPI.Factory.getInstance(sContext).listenPlaylist(playlist).enqueue(new Callback<ListenResponse>() {
                @Override
                public void onResponse(Call<ListenResponse> call, Response<ListenResponse> response) {

                }

                @Override
                public void onFailure(Call<ListenResponse> call, Throwable t) {

                }
            });
        }
    }

    public void getTwoItemSet(String songId) {

        SyntonesWebAPI.Factory.getInstance(sContext).getTwoItemSet(songId).enqueue(new Callback<TwoItemSetResponse>() {
            @Override
            public void onResponse(Call<TwoItemSetResponse> call, Response<TwoItemSetResponse> response) {
                TwoItemSetResponse twoItemSetResponse = response.body();
                List<Song> songList = twoItemSetResponse.getSongList();
                Log.e("RESULT STAT", twoItemSetResponse.getMessage().getMessage());
                final List<String> twoItemSongList = new ArrayList<>();
                if (songList != null) {
                    YouMightLikeTv.setVisibility(View.VISIBLE);
                    for (Song a : songList) {


                        twoItemSongList.add(String.valueOf(a.getSongId()));


                        for (Genre b : genreList) {

                            if (a.getGenreId() == b.getId()) {
                                if (twoItemSetCount <= 4) {
                                    arrayAdapter.add(a.getSongTitle() + " by " + a.getArtist().getArtistName() + "\n| " + b.getGenre());

                                    twoItemSetCount++;
                                    Log.e("TWO ITEM COUNT", String.valueOf(twoItemSetCount));

                                }
                            }

                        }


                    }

                    if (twoItemSongList.size() > 5) {
                        SeeMoreTv.setVisibility(View.VISIBLE);
                        SeeMoreIv.setVisibility(View.VISIBLE);

                        String itemSetSongs = new Gson().toJson(twoItemSongList);
                        SharedPreferences sharedPrefItemSetSongs = getSharedPreferences("itemSets", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorItemSetSongs = sharedPrefItemSetSongs.edit();
                        editorItemSetSongs.putString("songs", itemSetSongs);
                        editorItemSetSongs.apply();

                        retrieveData();

                    } else if (twoItemSongList.size() < 6 || twoItemSongList.size() == 0) {
                        SeeMoreTv.setVisibility(View.INVISIBLE);
                        SeeMoreIv.setVisibility(View.INVISIBLE);

                    }
                } else {
                    arrayAdapter.clear();
                    arrayAdapter.notifyDataSetChanged();
                    YouMightLikeTv.setVisibility(View.INVISIBLE);
                    SeeMoreIv.setVisibility(View.INVISIBLE);
                    SeeMoreTv.setVisibility(View.INVISIBLE);
                }


            }

            @Override
            public void onFailure(Call<TwoItemSetResponse> call, Throwable t) {

            }
        });

    }

    public void getThreeItemSet(String songId) {

        SyntonesWebAPI.Factory.getInstance(sContext).getThreeItemSet(songId).enqueue(new Callback<ThreeItemSetResponse>() {
            @Override
            public void onResponse(Call<ThreeItemSetResponse> call, Response<ThreeItemSetResponse> response) {
                ThreeItemSetResponse threeItemSetResponse = response.body();
                List<Song> songList = threeItemSetResponse.getSongList();
                final List<String> threeItemSongList = new ArrayList<>();
                if (songList != null) {
                    YouMightLikeTv.setVisibility(View.VISIBLE);

                    for (Song a : songList) {
                        threeItemSongList.add(String.valueOf(a.getSongId()));

                        for (Genre b : genreList) {
                            if (a.getGenreId() == b.getId()) {
                                if (threeItemSetCount <= 4) {

                                    arrayAdapter.add(a.getSongTitle() + " by " + a.getArtist().getArtistName() + "\n| " + b.getGenre());
                                    threeItemSetCount++;


                                }
                                Log.e("THREE ITEM COUNT", String.valueOf(threeItemSetCount));
                            }
                        }
                    }
                    threeItemSetCount=0;
                    if (threeItemSongList.size() > 5) {

                        SeeMoreTv.setVisibility(View.VISIBLE);
                        SeeMoreIv.setVisibility(View.VISIBLE);
                        String itemSetSongs = new Gson().toJson(threeItemSongList);
                        SharedPreferences sharedPrefItemSetSongs = getSharedPreferences("itemSets", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorItemSetSongs = sharedPrefItemSetSongs.edit();
                        editorItemSetSongs.putString("songs", itemSetSongs);
                        editorItemSetSongs.apply();
                        retrieveData();

                    } else if (threeItemSongList.size() < 6 || threeItemSongList.size() == 0) {
                        SeeMoreTv.setVisibility(View.INVISIBLE);
                        SeeMoreIv.setVisibility(View.INVISIBLE);
                    }
                } else {
                    arrayAdapter.clear();
                    arrayAdapter.notifyDataSetChanged();
                    YouMightLikeTv.setVisibility(View.INVISIBLE);
                    SeeMoreIv.setVisibility(View.INVISIBLE);
                    SeeMoreTv.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onFailure(Call<ThreeItemSetResponse> call, Throwable t) {

            }
        });
    }

    public void retrieveData() {
        SharedPreferences sharedPrefItemSetSongs = getSharedPreferences("itemSets", Context.MODE_PRIVATE);
        String str = sharedPrefItemSetSongs.getString("songs", null);

        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> restoreData = new Gson().fromJson(str, type);

        for (String a : restoreData) {
            Log.d("RETRIEVED", a);
        }
    }


/*    public void showLyrics(String activityState, String[] song_lyrics, String[] song_id) {

        if (activityState.equals("Playlist") || activityState.equals("SavedOffline")) {

            LyricsTv.setText(song_lyrics[counter].replace("\\n", ""));
            LyricsTv.setSelected(true);

        } else {
            final SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

            long songId = Long.parseLong(song_id[counter]);
            syntonesWebAPI.getSongLyrics(songId).enqueue(new Callback<SongLyricsResponse>() {
                @Override
                public void onResponse(Call<SongLyricsResponse> call, Response<SongLyricsResponse> response) {
                    SongLyricsResponse songLyricsResponse = response.body();
                    String lyrics = songLyricsResponse.getLyrics();
                    LyricsTv.setText(lyrics);
                    LyricsTv.setSelected(true);
                }

                @Override
                public void onFailure(Call<SongLyricsResponse> call, Throwable t) {

                }
            });


        }

    }*/

    public void stopPlaying() {

        if (mediaPlayer != null) {
            myHandler.removeCallbacks(UpdateSongTime);
            mediaPlayer.stop();
        }

    }

    public boolean isPlaying() {
        boolean isPlaying = false;
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                Log.e("PLAYER STAT", "PLAYING");
                isPlaying = true;
            } else {
                Log.e("PLAYER STAT", "NOT PLAYING");
                isPlaying = false;
            }

        }

        return isPlaying;
    }


    public void backToPlaylist() {
        SharedPreferences sharedPrefActivityInfo = getSharedPreferences("activityInfo", Context.MODE_PRIVATE);
        String activityState = sharedPrefActivityInfo.getString("activityState", "");

        if (activityState.equals("SearchActivity")) {
//            myHandler.removeCallbacks(UpdateSongTime);
//            mediaPlayer.stop();

            Intent intent = new Intent(PlayerActivity.this, SearchActivity.class);

            startActivity(intent);
        } else if (activityState.equals("Playlist")) {
//            myHandler.removeCallbacks(UpdateSongTime);
//            mediaPlayer.stop();
            Intent intent = new Intent(PlayerActivity.this, ViewPlayListActivity.class);

            startActivity(intent);
        } else if (activityState.equals("SavedOffline")) {
            Intent intent = new Intent(PlayerActivity.this, SavedSongsOfflineActivity.class);

            startActivity(intent);
        }

    }

    public void displayRecommendation() {
        BasketRecomLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String song = String.valueOf(parent.getItemAtPosition(position));

                SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(sContext);

                syntonesWebAPI.getAllSongsFromDB().enqueue(new Callback<SongListResponse>() {
                    @Override
                    public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {

                        SongListResponse songListResponse = response.body();
                        List<Song> songList = songListResponse.getSongs();

                        SharedPreferences sharedPrefPlayedSongInfo = getSharedPreferences("playedSongInfo", 0);
                        final SharedPreferences.Editor editorPlayedSongInfo = sharedPrefPlayedSongInfo.edit();

                        SharedPreferences sharedPrefSongInfo = getSharedPreferences("songInfo", Context.MODE_PRIVATE);

                        final SharedPreferences.Editor editorSongInfo = sharedPrefSongInfo.edit();

                        editorPlayedSongInfo.clear();
                        editorPlayedSongInfo.apply();
                        editorSongInfo.clear();
                        editorSongInfo.apply();

                        String[] song_urls = new String[1];
                        String[] song_titles = new String[1];
                        String[] song_artists = new String[1];
                        String[] song_lyrics = new String[1];
                        String[] song_ids = new String[1];
                        editorPlayedSongInfo.putInt("song_url_array" + "_size", song_urls.length);

                        String[] song_info = song.split("\\s(by)\\s");
                        String[] splitText2 = song_info[1].split("\\|");
                        editorSongInfo.putString("songTitle", song_info[0].trim());
                        editorSongInfo.putString("artistName", splitText2[0].trim());

                        editorSongInfo.apply();

                        SharedPreferences sharedPrefCounter = getSharedPreferences("counter", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorCounter = sharedPrefCounter.edit();
                        editorCounter.putInt("counterValue", count);
                        editorCounter.apply();
                        int b = 0;
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
                                    b++;

                                }
                            }
                        }
                        editorPlayedSongInfo.apply();

                        SharedPreferences sharedPrefActivityInfo = getSharedPreferences("activityInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorActivityInfo = sharedPrefActivityInfo.edit();
                        editorActivityInfo.putString("activityState", activityState);
                        editorActivityInfo.apply();

                        Intent intent = new Intent(PlayerActivity.this, PlayerActivity.class);

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
    public void onCompletion(MediaPlayer mp) {

    }

    public String part_of_day(int current_time) {
        String part_of_day = "";


        if (current_time >= 00 && current_time <= 04) {

            part_of_day = "midnight";


        } else if (current_time >= 05 && current_time <= 11) {

            part_of_day = "morning";


        } else if (current_time == 12) {

            part_of_day = "noon";


        } else if (current_time >= 13 && current_time <= 17) {

            part_of_day = "afternoon";


        } else if (current_time >= 18 && current_time <= 23) {

            part_of_day = "night";

        }

        return part_of_day;
    }


}
