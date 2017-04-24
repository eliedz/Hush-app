package com.example.elied.hush;


import java.util.ArrayList;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.Random;

import android.app.PendingIntent;

public class MusicService extends IntentService implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private int queuePos;
    private LinkedList<SongListElement> songList;
    private ArrayList<Song> songs;

    public MediaPlayer getPlayer() {
        return player;
    }

    private MediaPlayer player;
    private final IBinder musicBind = new MusicBinder();
    private boolean shuffle=false;
    private Random rand;

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    private String songTitle="";
    private String songArtist="";
    private static final int NOTIFY_ID=1;
    private NotificationCompat.Builder notif;
    private PendingIntent pauseIntent, playIntent, middleIntent;
    private int middleDrawable;
    NotificationManager mNotificationManager;

    public boolean isPrepared() {
        return isPrepared;
    }

    private boolean isPrepared = false;

    private Activity boundActivity;

    public MusicService(){

        super("MusicService");


    }

    public void onCreate(){
        super.onCreate();
        songList = new LinkedList<SongListElement>();
        rand = new Random();
        queuePos = 0;
        player = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            onHandleIntent(intent);
        }
        return START_STICKY;
    }

    public void setShuffle(){
        shuffle = !shuffle;
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        //player.release();
        return false;
    }

    public void playSong(){
        isPrepared = false;
        player.reset();
        Song playSong = songs.get(songList.get(queuePos).getSongID());
        long currSong = playSong.getID();
        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        syncButtons(false);
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void start(){
        syncButtons(true);
        player.start();
    }

    public void playPrev(){
        if(shuffle){
            if(queuePos == 0){
                int newSong = rand.nextInt(songs.size());
                while(songList.contains(new SongListElement(newSong))){
                    newSong=rand.nextInt(songs.size());
                }
                songList.addFirst(new SongListElement(newSong));
            } else {
                queuePos--;
            }
        } else {
            songList.set(queuePos,new SongListElement((songList.get(queuePos).getSongID() == 0 ? songs.size()-1 : songList.get(queuePos).getSongID()-1 )));
        }
        playSong();
    }

    public void playNext(){
        if(shuffle){
            if(queuePos == songList.size()-1){
                int newSong = rand.nextInt(songs.size());
                while(songList.contains(new SongListElement(newSong))){
                    newSong=rand.nextInt(songs.size());
                }
                songList.addLast(new SongListElement(newSong));
            }
                queuePos++;
        } else {
            songList.set(queuePos,new SongListElement((songList.get(queuePos).getSongID() == songs.size()-1 ? 0 : songList.get(queuePos).getSongID()+1 )));
        }
        playSong();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        Intent progressbarIntent = new Intent("UPDATE_PLAYER");
        LocalBroadcastManager.getInstance(this).sendBroadcast(progressbarIntent);

        mp.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 5,notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousInt = new Intent(this,MusicService.class);
        previousInt.putExtra("action","prev");
        PendingIntent prevIntent = PendingIntent.getService(this,4,previousInt,PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextInt = new Intent(this,MusicService.class);
        nextInt.putExtra("action","next");
        PendingIntent nextIntent = PendingIntent.getService(this,3,nextInt,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseInt = new Intent(this,MusicService.class);
        pauseInt.putExtra("action","pause");
        pauseIntent = PendingIntent.getService(this,2,pauseInt,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playInt = new Intent(this,MusicService.class);
        playInt.putExtra("action","play");
        playIntent = PendingIntent.getService(this,1,playInt,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteInt = new Intent(this,MusicService.class);
        deleteInt.putExtra("action","delete");
        PendingIntent deleteIntent = PendingIntent.getService(this,6,deleteInt,PendingIntent.FLAG_UPDATE_CURRENT);

        middleIntent = pauseIntent;
        middleDrawable = R.drawable.ic_pause_notif;

        notif = new NotificationCompat.Builder(this)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendIntent)
                .setSmallIcon(R.drawable.ic_stat_h)
                .setLargeIcon(BitmapFactory.decodeResource(this.getApplicationContext().getResources(),
                        R.drawable.fallback_cover))
                .setTicker(songTitle)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentTitle(songTitle)
                .setContentText(songArtist)
                .addAction(R.drawable.ic_skip_previous_notif, "Previous", prevIntent )     // #0
                .addAction(middleDrawable, "Pause", middleIntent)           // #1
                .addAction(R.drawable.ic_skip_next_notif, "Next", nextIntent)            // #2
                .setDeleteIntent(deleteIntent);
        startForeground(NOTIFY_ID, notif.build());
    }

    public void setSong(int songIndex){
        songList = new LinkedList<SongListElement>();
        queuePos = 0;
        songList.push(new SongListElement(songIndex));
    }

    @Override
    public boolean onError(MediaPlayer media, int x, int y) {
        isPrepared = false;
        media.reset();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer media){
        if(player.getCurrentPosition()  == 0){
            Log.e("=======>","onCompletion");
            isPrepared=false;
            media.reset();
            playNext();
        }
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Intent notifIntent = new Intent("UPDATE_PLAYER");
        if( intent.getStringExtra("action") != null ) {
            if (intent.getStringExtra("action").equals("prev")) {
                Log.e("======>","playing Prev");
                notifIntent.putExtra("playing",true);
                //((MainActivity)boundActivity).notificationPlay();
                playPrev();
            } else {
                if (intent.getStringExtra("action").equals("next")) {
                    Log.e("======>","playing Next");
                    //((MainActivity)boundActivity).notificationPlay();
                    notifIntent.putExtra("playing",true);
                    playNext();
                } else {
                    if(intent.getStringExtra("action").equals("pause")) {
                        Log.e("======>", "pausing");
                        //((MainActivity)boundActivity).notificationPause();
                        notifIntent.putExtra("playing",false);
                        pausePlayer();
                    } else {
                        if(intent.getStringExtra("action").equals("play")) {
                            //((MainActivity)boundActivity).notificationPlay();
                            notifIntent.putExtra("playing", true);
                            start();
                        } else {
                            notifIntent.putExtra("playing",false);
                            stopSelf();
                        }
                    }
                }
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(notifIntent);
        } else {
            Log.e("=======>","action is null");
        }
    }

    public void setBoundActivity(Activity boundActivity){
        this.boundActivity = boundActivity;
    }

    public void syncButtons(boolean playing){
        if(playing){
            middleIntent = pauseIntent;
            middleDrawable = R.drawable.ic_pause_notif;
            notif.mActions.set(1,new NotificationCompat.Action(middleDrawable,"Pause",middleIntent));
            notif.setOngoing(true)
                    .setAutoCancel(false);
            startForeground(NOTIFY_ID, notif.build());
        } else {
            middleIntent = playIntent;
            middleDrawable = R.drawable.ic_play_notif;
            notif.mActions.set(1,new NotificationCompat.Action(middleDrawable,"Play",middleIntent));
            notif.setAutoCancel(true)
                    .setOngoing(false);
            stopForeground(false);
            mNotificationManager.notify(NOTIFY_ID, notif.build());
            //startForeground(NOTIFY_ID,notif.build());
        }
    }

    @Override
    public void onDestroy() {
        player.release();
        Log.e("======>","Service is Destroyed");
        mNotificationManager.cancel(NOTIFY_ID);
    }

}
