package com.testing.testappforreview.recycler;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.testing.testappforreview.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class RecycleAdapter extends RecyclerView.Adapter<Holder> {

    Context c;
    ArrayList<RecycleModel> models;

    public RecycleAdapter(Context c, ArrayList<RecycleModel> models) {
        this.c = c;
        this.models = models;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, viewGroup,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        try {
            holder.appName.setText(models.get(position).getAppName());
            holder.messageTXT.setText(models.get(position).getMessageTXT());

            PackageManager pm = c.getPackageManager();
            ApplicationInfo ai = null;
            try {
                ai = pm.getApplicationInfo(models.get(position).getImg(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            holder.mImageView.setImageDrawable(pm.getApplicationIcon(ai));

            SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
            String time = formatTime.format(models.get(position).getMTime());
            SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yy");
            String date = formatDate.format(models.get(position).getMTime());
            holder.messageTime.setText(time);
            holder.messageDate.setText(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

}

