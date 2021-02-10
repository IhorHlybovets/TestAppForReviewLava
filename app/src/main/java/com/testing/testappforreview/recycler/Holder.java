package com.testing.testappforreview.recycler;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.testing.testappforreview.R;

public class Holder extends RecyclerView.ViewHolder{

    ImageView mImageView;
    TextView appName, messageTXT, messageTime, messageDate;

    public Holder(@NonNull View itemView){
        super(itemView);
        this.mImageView = itemView.findViewById(R.id.appImage);
        this.appName = itemView.findViewById(R.id.appName);
        this.messageTXT = itemView.findViewById(R.id.messageTXT);
        this.messageTime = itemView.findViewById(R.id.messageTime);
        this.messageDate = itemView.findViewById(R.id.messageDate);
    }
}
