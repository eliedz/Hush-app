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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        currentSong.setPosition(position);
        Button addToQueue = (Button) songLay.findViewById(R.id.add_to_queue);
        songView.setText(currentSong.getTitle());
        artistView.setText(currentSong.getArtist());
        songLay.setTag(position);

        addToQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)activity).getMusicSrv().addToQueueu(currentSong.getPosition());
                Toast.makeText(context, "Song Added to Shuffle Queue!", Toast.LENGTH_SHORT).show();
            }
        });

        songLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)activity).songPicked(v,currentSong);

            }
        });

        return songLay;
    }

}
