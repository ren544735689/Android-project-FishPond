package com.example.android_forest_app.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android_forest_app.R;
import com.example.android_forest_app.beans.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteListAdapter extends RecyclerView.Adapter<NoteViewHolder> implements View.OnClickListener {

    private final List<Note> notes = new ArrayList<>();

    public NoteListAdapter() {

    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void refresh(List<Note> newNotes) {
        notes.clear();
        if (newNotes != null) {
            notes.addAll(newNotes);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        //Implement your logic here
        Note note = notes.get(position);
        if(note.getState().equals("0"))
            return 1;
        else if(note.getState().equals("1"))
            return 2;
        else
            return 0;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int pos) {
        View itemView;

        if(pos == 1){//To do
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_todo, parent, false);
        }
        else {//done
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_done, parent, false);
        }

        itemView.setOnClickListener(this);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onClick(View view) {
        if(mOnItemClickListener != null)
            mOnItemClickListener.onItemClick(view, (int)view.getTag());
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int pos) {
        holder.itemView.setTag(pos);
        holder.bind(notes.get(pos));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
}