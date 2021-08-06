package com.example.sisca_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sisca_app.ChatActivity;
import com.example.sisca_app.Models.DoctorsModel;
import com.example.sisca_app.Models.UsersModel;
import com.example.sisca_app.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.MyHolder> {

    Context context;
    List<DoctorsModel> doctorsList;
    String friendId;

    public DoctorAdapter(Context context, List<DoctorsModel> doctorsList){
        this.context = context;
        this.doctorsList = doctorsList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_of_users,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorAdapter.MyHolder holder, int position) {
        DoctorsModel doctor = doctorsList.get(position);
        friendId = doctor.getUid();
        holder.nickname.setText(doctor.getnName());

        if(doctor.getImageURL().equals("default"))
        {
            holder.imageView.setImageResource(R.drawable.ic_baseline_person_24);
        }
        else
        {
            Glide.with(context).load(doctor.getImageURL()).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return doctorsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nickname;
        CircleImageView imageView;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            nickname = itemView.findViewById(R.id.username_userfrag);
            imageView = itemView.findViewById(R.id.image_user_userfrag);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            DoctorsModel doctor = doctorsList.get(getAdapterPosition());
            friendId = doctor.getUid();
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("friendId",friendId);
            context.startActivity(intent);
        }
    }
}
