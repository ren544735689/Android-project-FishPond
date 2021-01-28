package com.example.android_forest_app.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.android_forest_app.R;

public class PermissionDialog extends Dialog {
    private Context context;
    private TextView jumpButton;
    private jumpClickListener jumpListener;

    public PermissionDialog(Context context){
        super(context, R.style.Dialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_permission);
        setCanceledOnTouchOutside(true);

        jumpButton = findViewById(R.id.jump);
        jumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpListener.onClick();
            }
        });
    }

    public interface jumpClickListener{
        public void onClick();
    }

    public void setJumpClickListener(jumpClickListener jumpClickListener){
        this.jumpListener = jumpClickListener;
    }
}
