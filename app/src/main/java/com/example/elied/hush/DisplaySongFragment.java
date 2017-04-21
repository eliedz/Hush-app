package com.example.elied.hush;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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




    public DisplaySongFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments()!=null){
            mSong = (Song) getArguments().getSerializable("song"); //takes everything that's for the song
        }

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
                mSongArtist.setText(((MainActivity) getActivity()).getMusicSrv().getSongArtist());
                mSongTitle.setText(((MainActivity) getActivity()).getMusicSrv().getSongTitle());
                break;
            case R.id.prev:
                Log.e("=======>","prev Clicked");
                ((MainActivity)getActivity()).playPrev();
                mSongArtist.setText(((MainActivity) getActivity()).getMusicSrv().getSongArtist());
                mSongTitle.setText(((MainActivity) getActivity()).getMusicSrv().getSongTitle());
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
            mPause.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.GONE);
            // Change alignment to newly visible icon
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mNext.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_BOTTOM,R.id.pause);
            params.addRule(RelativeLayout.ALIGN_BASELINE,R.id.pause);
            mNext.setLayoutParams(params);
            ((MainActivity)getActivity()).start();
        } else {
            mPause.setVisibility(View.GONE);
            mPlay.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mNext.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_BOTTOM,R.id.play);
            params.addRule(RelativeLayout.ALIGN_BASELINE,R.id.play);
            mNext.setLayoutParams(params);
            ((MainActivity)getActivity()).pause();
        }
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
