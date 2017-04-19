package com.example.elied.hush;

import android.Manifest;
import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ContentResolver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songList = new ArrayList<Song>();
        songView = (ListView)findViewById(R.id.song_list);
        Dexter.withActivity(this)
        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        .withListener(new PermissionListener() {
            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                getSongList();
                Collections.sort(songList, new Comparator<Song>(){
                    public int compare(Song a, Song b){
                        return a.getTitle().compareTo(b.getTitle());
                    }
                });}
            @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
            @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
        }).check();

        TrackAdapter songAdt = new TrackAdapter(this,this, songList);
        songView.setAdapter(songAdt);
        setController();
    }

    @Override
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()){
            super.onBackPressed();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        Log.e("=========>", "OnStart");
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        if(playbackPaused){
            setController();
            controller.show(0);
            playbackPaused=false;
        }
        musicSrv.playSong();
    }

    public void addToPlaylist(View view){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getSongList(){
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    public void setController(){
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    public void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    public void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    protected void onPause(){
        Log.e("========>","onPause");
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        Log.e("========>","onResume");
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("=========>","onDestroy");
        unbindService(musicConnection);
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    @Override
    public void start() {
        if(musicSrv != null){
            musicSrv.start();
        }
    }

    @Override
    public void pause() {
        if(musicSrv != null){
            //playbackPaused = true;
            musicSrv.pausePlayer();
        }
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPlaying())
        return musicSrv.getDur();
        else return 0;    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPlaying())
        return musicSrv.getPosn();
        else return 0;    }

    @Override
    public void seekTo(int pos) {
        if(musicSrv != null){
            musicSrv.seek(pos);
        }
    }

    @Override
    public boolean isPlaying() {
        if(musicBound && musicSrv!= null){
            return musicSrv.isPlaying();
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if( intent.getStringExtra("action") != null ) {
            if (intent.getStringExtra("action").equals("prev")) {
                playPrev();
            } else {
                if (intent.getStringExtra("action").equals("next")) {
                    playNext();
                } else {
                        pause();
                }
            }
            moveTaskToBack(true);
        } else {
            Log.e("=======>","action is null");
            super.onNewIntent(intent);
        }
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
