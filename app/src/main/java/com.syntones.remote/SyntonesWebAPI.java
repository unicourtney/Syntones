package com.syntones.remote;

/**
 * Created by Courtney Love on 9/2/2016.
 */

import android.content.Context;

import com.syntones.model.PlayedSongs;
import com.syntones.model.PlayedSongsByTime;
import com.syntones.model.Playlist;
import com.syntones.model.PlaylistSong;
import com.syntones.model.Tag;
import com.syntones.model.User;
import com.syntones.response.ArtistResponse;
import com.syntones.response.GeneratePlaylistResponse;
import com.syntones.response.GenreResponse;
import com.syntones.response.LibraryResponse;
import com.syntones.response.ListenResponse;
import com.syntones.response.LoginResponse;
import com.syntones.response.LogoutResponse;
import com.syntones.response.PlayedSongsResponse;
import com.syntones.response.PlayedSongsByTimeResponse;
import com.syntones.response.PlaylistResponse;
import com.syntones.response.PlaylistSongsResponse;
import com.syntones.response.RemovePlaylistResponse;
import com.syntones.response.SearchResponse;
import com.syntones.response.SongListResponse;
import com.syntones.model.TemporaryDB;
import com.syntones.response.SongLyricsResponse;
import com.syntones.response.TagsResponse;
import com.syntones.response.ThreeItemSetResponse;
import com.syntones.response.TwoItemSetResponse;


import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SyntonesWebAPI {

    IpAddressSetting iPAddresSetting = new IpAddressSetting();


    String ENDPOINT = "http://" + iPAddresSetting.getiPAddress() + ":8084/syntones-web/";

    @POST("register")
    Call<User> createUser(@Body User user);

    @POST("login")
    Call<LoginResponse> logInUser(@Body User user);

    @POST("savePlaylist")
    Call<PlaylistResponse> createPlaylist(@Body Playlist playlist);

    @POST("playlist")
    Call<PlaylistResponse> getPlaylistFromDB(@Body User user);

    @POST("removePlaylist")
    Call<RemovePlaylistResponse> removePlaylist(@Body Playlist playlist);

    @GET("songlist")
    Call<SongListResponse> getAllSongsFromDB();

    @POST("addToPlaylist")
    Call<LibraryResponse> addToPlaylist(@Body PlaylistSong playlistSong);

    @POST("playlistSong")
    Call<PlaylistSongsResponse> getSongsPlaylist(@Body Map<String, String> data);

    @POST("removeToPlaylist")
    Call<RemovePlaylistResponse> removeSongFromPlaylist(@Body PlaylistSong playlistSong);

    @POST("listen")
    Call<ListenResponse> listen(@Body TemporaryDB temporaryDB);

    @POST("listenPlaylist")
    Call<ListenResponse> listenPlaylist(@Body Playlist playlist);

    @POST("getTwoItemSet")
    Call<TwoItemSetResponse> getTwoItemSet(@Body String songId);

    @POST("getThreeItemSet")
    Call<ThreeItemSetResponse> getThreeItemSet(@Body String songId);

    @POST("logoutProcess")
    Call<LogoutResponse> logout();

    @POST("saveGeneratedPlaylist")
    Call<GeneratePlaylistResponse> saveGeneratedPlaylist(@Body Playlist playlist);

    @POST("getPlayedSongs")
    Call<PlayedSongsResponse> getPlayedSongs();

    @POST("search")
    Call<SearchResponse> search(@Body String search);

    @POST("getPlayedSongsByTime")
    Call<PlayedSongsByTimeResponse> getPlayedSongsByTime();

    @POST("generatePlaylist")
    Call<GeneratePlaylistResponse> generatePlaylist(@Body User user);

    @POST("getLyrics")
    Call<SongLyricsResponse> getSongLyrics(@Body long songId);

    @POST("getGenre")
    Call<GenreResponse> getGenre(@Body String something);

    @POST("checkIfSongExists")
    Call<LibraryResponse> checkIfSongExists(@Body List<PlaylistSong> playlistSong);

    class Factory {

        private static SyntonesWebAPI service;

        public static SyntonesWebAPI getInstance(Context context) {

            if (service == null) {

                Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(ENDPOINT).build();

                service = retrofit.create(SyntonesWebAPI.class);

                return service;
            } else {

                return service;
            }

        }
    }


}
