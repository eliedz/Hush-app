package com.example.elied.hush;

import java.util.ArrayList;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrackAdapter extends android.widget.BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    private Context context;
    private Activity activity;

    public TrackAdapter(Context c, Activity activity, ArrayList<Song> theSongs){
        songs=theSongs;
        songInf=LayoutInflater.from(c);
        context = c;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLay = (LinearLayout) songInf.inflate(R.layout.song,parent,false);
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
        final Song currentSong = songs.get(position);
        songView.setText(currentSong.getTitle());
        artistView.setText(currentSong.getArtist());
        songLay.setTag(position);

        songLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)activity).songPicked(v);
                DisplaySongFragment fr = new DisplaySongFragment(); // create new instance of fragment you want to open
                Bundle args = new Bundle(); // create new bundle to pass data to the fragment
                args.putSerializable("song", currentSong); // pass desired song into bundle
                fr.setArguments(args); // set data bundle as an argument for fragment
                FragmentManager fm = activity.getFragmentManager(); // get fragment manager
                FragmentTransaction fragmentTransaction = fm.beginTransaction(); // create a new transaction to a fragment
                fragmentTransaction.replace(R.id.fragment_place, fr); // allocate a frame layout for fragment to reside in
                fragmentTransaction.commit(); // apply changes
            }
        });
        return songLay;
    }

}
