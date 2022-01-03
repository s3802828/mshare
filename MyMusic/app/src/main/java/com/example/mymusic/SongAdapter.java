package com.example.mymusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> implements Filterable {

    Context context;
    ArrayList<Song> songArrayList;
    ArrayList<Song> songArrayListFull;
    ArrayList<String> selectedSongs = new ArrayList<>();

    public SongAdapter(Context context, ArrayList<Song> songArrayList, ArrayList<Song> songArrayListFull) {
        this.context = context;
        this.songArrayList = songArrayList;
        this.songArrayListFull = songArrayListFull;
//        this.songArrayListFull = new ArrayList<>(songArrayList);

    }

//    public void setSongArrayListFull() {
//        songArrayListFull.addAll(songArrayList);
//    }

    public String getSongList() {
        StringBuilder songsString = new StringBuilder();
        for (int i = 0; i < selectedSongs.size(); i++) {
            if (i == (selectedSongs.size() - 1)) {
                songsString.append(selectedSongs.get(i));
            } else {
                songsString.append(selectedSongs.get(i)).append(",");
            }
        }

        return songsString.toString();
    }

    @NonNull
    @Override
    public SongAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.song_item, viewGroup,false);
        return new SongViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {

        Song song = songArrayList.get(position);

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());
//        holder.songId.setText(song.getId());

        Glide.with(context).load(song.getCover()).into(holder.songCover);

        holder.songSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setChecked(true);
                    selectedSongs.add(song.getId());
                } else {
                    buttonView.setChecked(false);
                    selectedSongs.remove(song.getId());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder{

        TextView songTitle, songArtist;
        ImageView songCover;
        CheckBox songSelect;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.textSongTitle);
            songArtist = itemView.findViewById(R.id.textSongArtist);
            songCover = itemView.findViewById(R.id.imageCover);
            songSelect = itemView.findViewById(R.id.checkBoxSong);
//            songId = itemView.findViewById(R.id.textSongId);
        }
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Song> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                System.out.println("Add all");
                filteredList.addAll(songArrayListFull);
            } else {
                String filteredPattern = constraint.toString().toLowerCase().trim();

                System.out.println(songArrayListFull.toString());

                for (Song song: songArrayListFull) {
                    if (song.getTitle().toLowerCase().contains(filteredPattern)) {
                        System.out.println(song.getTitle()+" Matched");
                        filteredList.add(song);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            songArrayList.clear();
            songArrayList.addAll((List) results.values);
            System.out.println("Added result");
            notifyDataSetChanged();
        }
    };
}
