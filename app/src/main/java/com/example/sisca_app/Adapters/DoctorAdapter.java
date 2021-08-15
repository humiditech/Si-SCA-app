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
import com.example.sisca_app.Models.ChatsModel;
import com.example.sisca_app.Models.DoctorsModel;
import com.example.sisca_app.Models.UsersModel;
import com.example.sisca_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.MyHolder> {

    Context context;
    List<DoctorsModel> doctorsList;
    String friendId, theLastMessage;
    boolean isChat;
    FirebaseUser fUser;

    public DoctorAdapter(Context context, List<DoctorsModel> doctorsList, boolean isChat) {
        this.context = context;
        this.doctorsList = doctorsList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_of_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorAdapter.MyHolder holder, int position) {
        DoctorsModel doctor = doctorsList.get(position);
        friendId = doctor.getUid();
        holder.nickname.setText(doctor.getnName());

        if (doctor.getImageURL().equals("default")) {
            holder.imageView.setImageResource(R.drawable.ic_baseline_person_24);
        } else {
            Glide.with(context).load(doctor.getImageURL()).into(holder.imageView);
        }

        if (isChat) {
            if (doctor.getStatus().equals("online")) {
                holder.image_online.setVisibility(View.VISIBLE);
                holder.image_offline.setVisibility(View.GONE);
            } else {
                holder.image_online.setVisibility(View.GONE);
                holder.image_offline.setVisibility(View.VISIBLE);
            }
        } else {
            holder.image_online.setVisibility(View.GONE);
            holder.image_offline.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return doctorsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nickname;
        CircleImageView imageView, image_online, image_offline;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            nickname = itemView.findViewById(R.id.username_userfrag);
            imageView = itemView.findViewById(R.id.image_user_userfrag);
            image_online = itemView.findViewById(R.id.image_online);
            image_offline = itemView.findViewById(R.id.image_offline);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            DoctorsModel doctor = doctorsList.get(getAdapterPosition());
            friendId = doctor.getUid();
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("friendId", friendId);
            context.startActivity(intent);
        }
    }

    private void LastMessage(String friendId, TextView last_msg)
    {
        theLastMessage = "default";
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren())
                {
                    ChatsModel chats = ds.getValue(ChatsModel.class);
                    if(fUser.getUid() != null && chats != null)
                    {
                        if(chats.getSender().equals(friendId) && chats.getReceiver().equals(fUser.getUid()) || chats.getReceiver().equals(friendId) && chats.getSender().equals(fUser.getUid()) )
                        {
                            theLastMessage = chats.getMessage();
                        }
                    }
                }

                switch (theLastMessage)
                {
                    case "default":
                        last_msg.setText("No Message");
                        break;
                    default:
                        last_msg.setText(theLastMessage);
                }
                theLastMessage =    "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
