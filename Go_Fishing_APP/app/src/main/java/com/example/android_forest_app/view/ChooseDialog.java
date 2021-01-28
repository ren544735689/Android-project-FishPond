package com.example.android_forest_app.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.example.android_forest_app.R;

public class ChooseDialog extends Dialog {
    private Context context;
    private Window window = null;

    private String title;
    private View dialogView;
    private ImageView starBurst;
    private ImageView time;
    private ImageView star;
    private starBurstOnClickListener starBurstClick;
    private timeOnClickListener timeClick;
    private starOnClickListener starClick;

    public ChooseDialog(Context context){
        super(context, R.style.Dialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choose);
        window = getWindow();
        window.setWindowAnimations(R.style.chooseDialogAnim);
        //按空白处取消动画
        setCanceledOnTouchOutside(true);

        starBurst = findViewById(R.id.starBurst);
        time = findViewById(R.id.time);
        star = findViewById(R.id.star);

        initClick();
    }

    private void initClick(){
        starBurst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                starBurstClick.onClick();
            }
        });
        time.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                timeClick.onClick();
            }
        });
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                starClick.onClick();
            }
        });
    }

    public interface starBurstOnClickListener{
        public void onClick();
    }
    public interface timeOnClickListener{
        public void onClick();
    }
    public interface starOnClickListener{
        public void onClick();
    }

    public void setStarBurstOnClickListener(starBurstOnClickListener starBurstOnClickListener){
        this.starBurstClick = starBurstOnClickListener;
    }
    public void setTimeOnClickListener(timeOnClickListener timeOnClickListener){
        this.timeClick = timeOnClickListener;
    }
    public void setStarClickListener(starOnClickListener starOnClickListener){
        this.starClick = starOnClickListener;
    }
}
