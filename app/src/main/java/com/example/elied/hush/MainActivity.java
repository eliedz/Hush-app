package com.example.elied.hush;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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

    public MusicService getMusicSrv() {
        return musicSrv;
    }

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=false;
    private Activity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
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
        playIntent = new Intent(this, MusicService.class);
        startService(playIntent);
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
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        Log.e("=========>", "OnStart");
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setBoundActivity(instance);
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicSrv = null;
            musicBound = false;
        }
    };

    public void inflateFragment(Song currentSong){
        DisplaySongFragment fr = new DisplaySongFragment(); // create new instance of fragment you want to open
        Bundle args = new Bundle(); // create new bundle to pass data to the fragment
        args.putSerializable("song", currentSong); // pass desired song into bundle
        fr.setArguments(args); // set data bundle as an argument for fragment
        FragmentManager fm = instance.getFragmentManager(); // get fragment manager
        FragmentTransaction fragmentTransaction = fm.beginTransaction(); // create a new transaction to a fragment
        fragmentTransaction.replace(R.id.fragment_place, fr); // allocate a frame layout for fragment to reside in
        fragmentTransaction.commit(); // apply changes
    }

    public void songPicked(View view, Song currentSong){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        if(playbackPaused){
            playbackPaused=false;
        }
        musicSrv.playSong();
        inflateFragment(currentSong);
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
                if(musicConnection != null){
                    unbindService(musicConnection);
                    musicSrv.setBoundActivity(null);
                }
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

    public void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            playbackPaused=false;
        }
    }

    public void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            playbackPaused=false;
        }
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
            paused=false;
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("=========>","onDestroy");
        if(musicSrv!= null && !musicSrv.isPlaying()){
            musicSrv.stopSelf();
        }
        unbindService(musicConnection);
        musicSrv.setBoundActivity(null);
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

    public void notificationPause(){
        if(instance.getFragmentManager().findFragmentById(R.id.fragment_place) != null) {
            ((DisplaySongFragment) instance.getFragmentManager().findFragmentById(R.id.fragment_place)).syncButtons(false);
        }
    }

    public void notificationPlay(){
        if(instance.getFragmentManager().findFragmentById(R.id.fragment_place) != null) {
            ((DisplaySongFragment) instance.getFragmentManager().findFragmentById(R.id.fragment_place)).syncButtons(true);
            ((DisplaySongFragment) instance.getFragmentManager().findFragmentById(R.id.fragment_place)).updateSong();
        }
    }




//    @Override
//    protected void onNewIntent(Intent intent) {
//        if( intent.getStringExtra("action") != null ) {
//            if (intent.getStringExtra("action").equals("prev")) {
//                playPrev();
//            } else {
//                if (intent.getStringExtra("action").equals("next")) {
//                    playNext();
//                } else {
//                        pause();
//                }
//            }
//            moveTaskToBack(true);
//        } else {
//            Log.e("=======>","action is null");
//            super.onNewIntent(intent);
//        }
//    }

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
