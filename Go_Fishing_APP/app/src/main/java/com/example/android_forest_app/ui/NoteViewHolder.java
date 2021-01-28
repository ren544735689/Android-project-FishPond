package com.example.android_forest_app.ui;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android_forest_app.R;
import com.example.android_forest_app.beans.Note;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NoteViewHolder extends RecyclerView.ViewHolder{

    private TextView item_id;
    private ImageView item_image;
    private TextView item_content;
    private TextView item_time;
    private ImageView item_ball;
    private ImageView item_fish;

    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);
        item_time = itemView.findViewById(R.id.item_time);
        item_id = itemView.findViewById(R.id.item_id);
        item_image = itemView.findViewById(R.id.item_image);
        item_content = itemView.findViewById(R.id.item_content);
        item_fish = itemView.findViewById(R.id.item_fish);
        item_ball = itemView.findViewById(R.id.item_ball);
    }

    public void bind(final Note note){
        Log.d("data",note.getScheduled()+","+note.getCaption()+","+note.getDeadline()+","+note.getTime());
        item_id.setText((CharSequence) (note.getID()+""));

        switch (note.getState()) {
            case"1":
                String con = note.getScheduled()+"~"+note.getDeadline();
                item_time.setText(con);
                String tex = "You get one "+note.getTime()+"'s "+note.getCaption().substring(0,note.getCaption().length()-1);
                item_content.setText(tex);
                switch (note.getCaption()) {
                    case "starBurst1":
                        item_fish.setImageResource(R.drawable.starburst1);
                        break;
                    case "starBurst2":
                        item_fish.setImageResource(R.drawable.starburst2);
                        break;
                    case "starBurst3":
                        item_fish.setImageResource(R.drawable.starburst3);
                        break;
                    case "starBurst4":
                        item_fish.setImageResource(R.drawable.starburst4);
                        break;
                    case "starBurst5":
                        item_fish.setImageResource(R.drawable.starburst5);
                        break;
                    case "starBurst6":
                        item_fish.setImageResource(R.drawable.starburst6);
                        break;
                    case "starBurst7":
                        item_fish.setImageResource(R.drawable.starburst7);
                        break;
                    case "time1":
                        item_fish.setImageResource(R.drawable.time1);
                        break;
                    case "time2":
                        item_fish.setImageResource(R.drawable.time2);
                        break;
                    case "time3":
                        item_fish.setImageResource(R.drawable.time3);
                        break;
                    case "time4":
                        item_fish.setImageResource(R.drawable.time4);
                        break;
                    case "time5":
                        item_fish.setImageResource(R.drawable.time5);
                        break;
                    case "time6":
                        item_fish.setImageResource(R.drawable.time6);
                        break;
                    case "time7":
                        item_fish.setImageResource(R.drawable.time7);
                        break;
                    case "star1":
                        item_fish.setImageResource(R.drawable.star1);
                        break;
                    case "star2":
                        item_fish.setImageResource(R.drawable.star2);
                        break;
                    case "star3":
                        item_fish.setImageResource(R.drawable.star3);
                        break;
                    case "star4":
                        item_fish.setImageResource(R.drawable.star4);
                        break;
                    case "star5":
                        item_fish.setImageResource(R.drawable.star5);
                        break;
                    case "star6":
                        item_fish.setImageResource(R.drawable.star6);
                        break;
                    case "star7":
                        item_fish.setImageResource(R.drawable.star7);
                        break;
                }
                break;
            case "0":
                item_time.setText(note.getScheduled());
                item_content.setText("You lose one lovely fish roe！");
        }
    }
}
