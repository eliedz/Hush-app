package com.example.elied.hush;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DisplaySongFragment extends Fragment implements View.OnClickListener {

    Song mSong;
    private TextView mSongArtist;
    private TextView mSongTitle;
    private ImageButton mBack;
    private ImageButton mPause;
    private ImageButton mPlay;
    private ImageButton mPrev;
    private ImageButton mNext;
    private BroadcastReceiver br;
    private Context currContext;
    private PendingIntent pi;




    public DisplaySongFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments()!=null){
            mSong = (Song) getArguments().getSerializable("song"); //takes everything that's for the song
        }

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent i)
            {
                updateSong();
                syncButtons(i.getBooleanExtra("playing",true));
            }
        };

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_display_song, container, false);
        mSongArtist = (TextView)v.findViewById(R.id.song_artist);
        mSongTitle = (TextView) v.findViewById(R.id.song_title);
        mSongArtist.setText(mSong.getArtist());
        mSongTitle.setText(mSong.getTitle());

        mPause = (ImageButton)v.findViewById(R.id.pause);
        mPlay = (ImageButton) v.findViewById(R.id.play);
        mBack = (ImageButton) v.findViewById(R.id.hide_button);
        mPrev = (ImageButton) v.findViewById(R.id.prev);
        mNext = (ImageButton) v.findViewById(R.id.next);

        mPlay.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mPause.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPrev.setOnClickListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        currContext = context;
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause:
                Log.e("=========>","pause Clicked");
                togglePlay();
                break;
            case R.id.play:
                Log.e("=========>","play Clicked");
                togglePlay();
                break;
            case R.id.next:
                Log.e("======>","next Clicked");
                ((MainActivity)getActivity()).playNext();
                updateSong();
                syncButtons(true);
                break;
            case R.id.prev:
                Log.e("=======>","prev Clicked");
                ((MainActivity)getActivity()).playPrev();
                updateSong();
                syncButtons(true);
                break;
            case R.id.hide_button:
                getFragmentManager().popBackStackImmediate();
                break;
        }
    }

    public void togglePlay()
    {
        if(!((MainActivity)getActivity()).isPlaying()) {
            Log.e("============>","isPlaying false");
            syncButtons(true);
            ((MainActivity)getActivity()).start();
        } else {
            syncButtons(false);
            ((MainActivity)getActivity()).pause();
        }
    }

    public void syncButtons(boolean play){
        if(play){
            mPause.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.INVISIBLE);
        } else {
            mPause.setVisibility(View.INVISIBLE);
            mPlay.setVisibility(View.VISIBLE);
        }
    }

    public void updateSong(){
        mSongArtist.setText(((MainActivity) getActivity()).getMusicSrv().getSongArtist());
        mSongTitle.setText(((MainActivity) getActivity()).getMusicSrv().getSongTitle());
    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(currContext).registerReceiver(br, new IntentFilter("UPDATE_PLAYER"));
        Log.e("=====>","Fragment Resume");
    }

    @Override
    public void onPause(){
        Log.e("======>","Fragment Pause");
        LocalBroadcastManager.getInstance(currContext).unregisterReceiver(br);
        super.onPause();
    }

   // @Override
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
}
