package com.example.elied.hush;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DisplaySongFragment extends Fragment implements View.OnClickListener {

    Song mSong;
    private TextView mSongArtist;
    private TextView mSongTitle;
    private ImageButton mBack;
    private Button mPause;
    private Button mPlay;
    private Button mPrev;




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

        mPause = (Button)v.findViewById(R.id.pause);
        mPlay = (Button) v.findViewById(R.id.play);
        mBack = (ImageButton) v.findViewById(R.id.hide_button);

        mPlay.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mPause.setOnClickListener(this);

        return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause:
            case R.id.play:
                togglePlay();
                break;
            case R.id.hide_button:
                getFragmentManager().popBackStackImmediate();
                break;
        }
    }

    public void togglePlay()
    {
        if(((MainActivity)getActivity()).isPlaying()) {
            mPause.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.GONE);
            ((MainActivity)getActivity()).pause();
        } else {
            mPause.setVisibility(View.GONE);
            mPlay.setVisibility(View.VISIBLE);
            ((MainActivity)getActivity()).start();
        }
    }
}
