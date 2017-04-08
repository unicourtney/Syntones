package com.syntones.syntones_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.syntones.model.Genre;
import com.syntones.model.Product;
import com.syntones.model.SavedOfflineSongs;
import com.syntones.model.User;
import com.syntones.remote.DBHelper;
import com.syntones.remote.ScreenOnOffReceiver;
import com.syntones.remote.SyntonesTimerTask;
import com.syntones.remote.SyntonesWebAPI;
import com.syntones.response.GenreResponse;
import com.syntones.response.LogoutResponse;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Text;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YourLibraryActivity extends AppCompatActivity {

    private ImageView SearchIv, LibraryIv, ViewPlaylistsIv, ViewSavedSongsOfflineIv, UserIv;
    private TextView UsernameTv, SavedSongsOfflineTv, PlaylistTv;
    private Timer timer;
    private RelativeLayout RelativeL;
    private SyntonesTimerTask syntonesTimerTask;
    private boolean isRunning;
    private CountDownTimer countDownTimer;
    private YourLibraryActivity yourLibraryActivity;
    private ScreenOnOffReceiver onoffReceiver = new ScreenOnOffReceiver("YOUR LIB");
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_library);

        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String username = sharedPrefUserInfo.getString("username", "");
        userID = String.valueOf(sharedPrefUserInfo.getLong("userID", 0));

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(onoffReceiver, filter);

        try {
            deleteFiles();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ViewPlaylistsIv = (ImageView) findViewById(R.id.ivViewPlaylists);
        ViewSavedSongsOfflineIv = (ImageView) findViewById(R.id.ivViewSavedSongsOffline);
        UserIv = (ImageView) findViewById(R.id.ivUser);
        SearchIv = (ImageView) findViewById(R.id.ivSearch);
        LibraryIv = (ImageView) findViewById(R.id.ivLibrary);
        UsernameTv = (TextView) findViewById(R.id.tvUsername);
        SavedSongsOfflineTv = (TextView) findViewById(R.id.tvSavedSongsOffline);
        PlaylistTv = (TextView) findViewById(R.id.tvPlaylist);
        RelativeL = (RelativeLayout) findViewById(R.id.yourLibraryL);


        UsernameTv.setText(username);

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
            Log.d("CONNECTION YL", "TRUE");
            Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
            Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());

            ViewSavedSongsOfflineIv.setVisibility(View.INVISIBLE);
            SavedSongsOfflineTv.setVisibility(View.INVISIBLE);
            PlaylistTv.setVisibility(View.VISIBLE);
            ViewPlaylistsIv.setVisibility(View.VISIBLE);
            SearchIv.setEnabled(true);
            getGenre();
            SyntonesTimerTask.getInstance().isPlaying(YourLibraryActivity.this, "YOUR LIB");

        } else {
            Log.d("CONNECTION YL", "FALSE");
            Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
            Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
            ViewSavedSongsOfflineIv.setVisibility(View.VISIBLE);
            SavedSongsOfflineTv.setVisibility(View.VISIBLE);
            PlaylistTv.setVisibility(View.INVISIBLE);
            ViewPlaylistsIv.setVisibility(View.INVISIBLE);
            SearchIv.setEnabled(false);
            SyntonesTimerTask.getInstance().stopCounter();

        }


        SearchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(YourLibraryActivity.this, SearchActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);


            }
        });

        ViewPlaylistsIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YourLibraryActivity.this, PlayListActivity.class);

                startActivity(intent);
            }
        });

        ViewSavedSongsOfflineIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YourLibraryActivity.this, SavedSongsOfflineActivity.class);

                startActivity(intent);
            }
        });

        UsernameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(YourLibraryActivity.this, ProfileActivity.class);

                startActivity(intent);
            }
        });

        UserIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YourLibraryActivity.this, ProfileActivity.class);

                startActivity(intent);
            }
        });


        RelativeL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("TOUCH", "WOAH");
                SyntonesTimerTask.getInstance().stopCounter();

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    Log.e("MOVE", "WOAH");
                    SyntonesTimerTask.getInstance().stopCounter();

                }

                return true;
            }
        });
    }


    public void deleteFiles() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = dateFormat.format(calendar.getTime()), startDate;
        DBHelper db = new DBHelper(this);

        SharedPreferences sharedPrefUserInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        userID = String.valueOf(sharedPrefUserInfo.getLong("userID", 0));
        ArrayList<SavedOfflineSongs> savedOfflineSongsArrayList = db.getAllSavedOfflineSongsFromUser(userID);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");

        if(savedOfflineSongsArrayList!=null) {
            for (SavedOfflineSongs a : savedOfflineSongsArrayList) {

                startDate = a.getStartDate();
                DateTime dtStart = formatter.parseDateTime(startDate);
                DateTime dtOrgStart = new DateTime(dtStart);
                DateTime dtExpired = dtOrgStart.plusDays(5);
                DateTime dtCurrent = formatter.parseDateTime(currentDate);

                int days = Days.daysBetween(dtStart, dtCurrent).getDays();
                if (days >= 2) {

                    String fileName = URLUtil.guessFileName(a.getFilePath(), null, MimeTypeMap.getFileExtensionFromUrl(a.getFilePath()));
                    File extStore = Environment.getExternalStorageDirectory();
                    File mp3File = new File(extStore + getFilesDir().getPath() + "/Syntones/savedSongs/", userID + "-" + fileName);
                    mp3File.delete();
                    db.deleteSavedSongsFromUser(userID, a.getSongId());

                    Log.e("EXPIRED", startDate + " - " + dtExpired.toString(formatter));
                }


                Log.e("DATE EXPIRATION", startDate + " - " + dtExpired.toString(formatter));
            }
        }

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

    public void getGenre() {

        SyntonesWebAPI syntonesWebAPI = SyntonesWebAPI.Factory.getInstance(this);

        syntonesWebAPI.getGenre("Something").enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(Call<GenreResponse> call, Response<GenreResponse> response) {
                GenreResponse genreResponse = response.body();
                List<Genre> genre = genreResponse.getGenre();
                Genre genreObject = new Genre();


                if (genre != null) {
                    SharedPreferences sharedPrefGenre = getSharedPreferences("genreList", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorGenre = sharedPrefGenre.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(genre);
                    editorGenre.putString("GenreObject", json);
                    editorGenre.commit();
                }

            }

            @Override
            public void onFailure(Call<GenreResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onDestroy() {


        try {
            SyntonesWebAPI.Factory.getInstance(yourLibraryActivity).logout().enqueue(new Callback<LogoutResponse>() {
                @Override
                public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {

                }

                @Override
                public void onFailure(Call<LogoutResponse> call, Throwable t) {

                }
            });
            if (onoffReceiver != null)
                unregisterReceiver(onoffReceiver);
        } catch (Exception e) {

        }
        super.onDestroy();

    }

    @Override
    public void onResume(){
        super.onResume();
        try{
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
                Log.d("CONNECTION YL", "TRUE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());

                ViewSavedSongsOfflineIv.setVisibility(View.INVISIBLE);
                SavedSongsOfflineTv.setVisibility(View.INVISIBLE);
                PlaylistTv.setVisibility(View.VISIBLE);
                ViewPlaylistsIv.setVisibility(View.VISIBLE);
                SearchIv.setEnabled(true);
                getGenre();
                SyntonesTimerTask.getInstance().isPlaying(YourLibraryActivity.this, "YOUR LIB");

            } else {
                Log.d("CONNECTION YL", "FALSE");
                Log.d("CONNECTION YL", "WIFI " + wifiInfo.isConnected());
                Log.d("CONNECTION YL", "MOBILE " + mobileInfo.isConnected());
                ViewSavedSongsOfflineIv.setVisibility(View.VISIBLE);
                SavedSongsOfflineTv.setVisibility(View.VISIBLE);
                PlaylistTv.setVisibility(View.INVISIBLE);
                ViewPlaylistsIv.setVisibility(View.INVISIBLE);
                SearchIv.setEnabled(false);
                SyntonesTimerTask.getInstance().stopCounter();
                SyntonesTimerTask.getInstance().stopPlayerCounter();

            }

        }catch(Exception e){

        }
    }

}
