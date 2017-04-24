package com.example.elied.hush;

import android.Manifest;
import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Debug;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ContentResolver;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import static android.R.attr.bitmap;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl, View.OnClickListener {

    private ArrayList<Song> songList;
    private ListView songView;
    private BroadcastReceiver br;

    public MusicService getMusicSrv() {
        return musicSrv;
    }

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=false;
    private Activity instance;
    private Song currSong;
    private boolean searchPerformed = true;

    public void setFragmentPresent(boolean fragmentPresent) {
        this.fragmentPresent = fragmentPresent;
    }

    private boolean fragmentPresent = false;
    private int songListID;
    private TextView widgetArtist,widgetTitle;
    private ImageButton widget_pause,widget_play;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        SQLiteDatabase mydatabase = openOrCreateDatabase("hushDB",MODE_PRIVATE,null);
        setContentView(R.layout.activity_main);
        instance = this;
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        Dexter.withActivity(this)
        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        .withListener(new PermissionListener() {
            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                getSongList();
                }
            @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
            @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
        }).check();

        TrackAdapter songAdt = new TrackAdapter(this,this, songList);
        songView.setAdapter(songAdt);
        SharedPreferences sp = getSharedPreferences("PLAYER_INFO", MODE_PRIVATE);
        String lastPlayedTitle = sp.getString("lastPlayedTitle",songList.get(0).getTitle());
        String lastPlayedArtist = sp.getString("lastPlayedArtist",songList.get(0).getArtist());
        final long songID = sp.getLong("lastPlayedID",0);
        for(int i = 0; i < songList.size();i++){
            if(songList.get(i).getID() == songID){
                songListID = i;
                currSong = songList.get(i);
                break;
            }
        }
        //currSong = songList.get(songList.indexOf(new Song(songID,lastPlayedTitle,lastPlayedArtist)));
        widgetTitle = (TextView) findViewById(R.id.song_title);
        widgetTitle.setText(lastPlayedTitle);
        widgetArtist = (TextView) findViewById(R.id.song_artist);
        widgetArtist.setText(lastPlayedArtist);
        widget_pause = (ImageButton) findViewById(R.id.pause);
        widget_play = (ImageButton) findViewById(R.id.play);
        widget_pause.setOnClickListener(this);
        widget_play.setOnClickListener(this);
        findViewById(R.id.small_widget).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicSrv.setSong(songListID);
                inflateFragment(currSong);
            }
            });
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent i)
            {
                updateSong();
                syncButtons();
            }
        };
        playIntent = new Intent(this, MusicService.class);
        startService(playIntent);
    }

    public void updateSong(){
        widgetTitle.setText(musicSrv.getSongTitle());
        widgetArtist.setText(musicSrv.getSongArtist());
    }

    public void syncButtons(){
        if(musicSrv.isPlaying()){
            widget_play.setVisibility(View.GONE);
            widget_pause.setVisibility(View.VISIBLE);
        } else {
            widget_pause.setVisibility(View.GONE);
            widget_play.setVisibility(View.VISIBLE);
        }
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
        fragmentTransaction.setCustomAnimations(R.anim.slide_in,R.anim.slide_out,R.anim.slide_in,R.anim.slide_out);
        fragmentTransaction.replace(R.id.fragment_place, fr); // allocate a frame layout for fragment to reside in
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit(); // apply changes
    }

    public void songPicked(int ID, Song currentSong){
        musicSrv.setSong(ID);
        if(playbackPaused){
            playbackPaused=false;
        }
        songListID = ID;
        currSong = currentSong;
        musicSrv.playSong();
        inflateFragment(currentSong);
        if(searchPerformed){
            Log.e("=====>","searchperformed is true");
        }
        getSongList();
        searchPerformed = false;
    }

    public void songPicked(View view, Song currentSong){
        songPicked(Integer.parseInt(view.getTag().toString()),currentSong);
    }

    public void addToPlaylist(View view){

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPerformed = true;
                getSongList(query);
                if (fragmentPresent) {
                    getFragmentManager().popBackStackImmediate();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.search),new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getSongList();
                return true;       // Return true to collapse action view
            }
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (fragmentPresent) {
                    getFragmentManager().popBackStackImmediate();
                }
                return true;      // Return true to expand action view
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getSongList(){

        getSongList(null);
    }

    public void getSongList(String searchQuery){
        if(!searchPerformed){
            Log.e("=======>","searchPerformed is false");
            return;
        }
        Log.e("======>","searchPerformed is true");
        songList.clear();
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
            // For Album Art
//            int albumIDColumn = musicCursor.getColumnIndex
//                    (MediaStore.Audio.Media.ALBUM_ID);
//            Uri sArtworkUri = Uri
//                    .parse("content://media/external/audio/albumart");
//            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumIDColumn);
//
//            try {
//                albumArt = MediaStore.Images.Media.getBitmap(
//                        musicResolver, albumArtUri);
//            } catch (FileNotFoundException exception) {
//                exception.printStackTrace();
//                albumArt = fallbackCover;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                Bitmap albumArt = null;

                if(searchQuery != null ){
                    if(org.apache.commons.lang3.StringUtils.containsIgnoreCase(thisTitle, searchQuery)) {
                        songList.add(new Song(thisId, thisTitle, thisArtist));
                    }
                } else {
                    songList.add(new Song(thisId, thisTitle, thisArtist));
                }
            }
            while (musicCursor.moveToNext());
        }
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("UPDATE_PLAYER"));
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
        } else {
            musicSrv.setBoundActivity(null);
        }
        unbindService(musicConnection);
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
        if(musicBound && musicSrv!= null) {
            return musicSrv.isPlaying();
        }
        return false;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause:
                Log.e("=========>", "pause Clicked");
                pause();
                break;
            case R.id.play:
                Log.e("=========>", "play Clicked");
                musicSrv.setSong(songListID);
                if(!musicSrv.isPrepared()) {
                    musicSrv.playSong();
                } else {
                    start();
                }
                break;
        }
    }
}
