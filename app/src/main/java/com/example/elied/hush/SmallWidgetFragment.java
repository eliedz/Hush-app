package com.example.elied.hush;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SmallWidgetFragment extends Fragment {
    Song mSong;
    private TextView mSongArtist;
    private TextView mSongTitle;


    public SmallWidgetFragment() {
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            mSong = (Song) getArguments().getSerializable("song"); //takes everything that's for the song
        }

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_small_widget, container, false);
        mSongArtist = (TextView) v.findViewById(R.id.song_artist);
        mSongTitle = (TextView) v.findViewById(R.id.song_title);
        mSongArtist.setText(mSong.getArtist());
        mSongTitle.setText(mSong.getTitle());
        return v;
    }
}
