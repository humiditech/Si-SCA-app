package com.example.sisca_app.Adapters;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sisca_app.Models.ChatsModel;
import com.example.sisca_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    Context context;
    List<ChatsModel> chatsList;
    public static final int MESSAGE_RIGHT = 0; // For Me
    public static final int MESSAGE_LEFT = 1; // For Friend

    public ChatAdapter(Context context, List<ChatsModel> chatsList) {
        this.context = context;
        this.chatsList = chatsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MyViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatsModel chats = chatsList.get(position);

        holder.messageText.setText(chats.getMessage());
        if(holder.seenMessage != null && holder.deliveredMessage != null)
        {
            if(position == (getItemCount() - 1))
            {
                if(chats.isSeen())
                {
                    holder.seenMessage.setVisibility(View.VISIBLE);
                    holder.deliveredMessage.setVisibility(View.GONE);
                }
                else
                {
                    holder.seenMessage.setVisibility(View.GONE);
                    holder.deliveredMessage.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                holder.seenMessage.setVisibility(View.GONE);
                holder.deliveredMessage.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        ImageView seenMessage,deliveredMessage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            seenMessage = itemView.findViewById(R.id.message_seen);
            deliveredMessage = itemView.findViewById(R.id.message_delivered);

        }
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        if (chatsList.get(position).getSender().equals(user.getUid())) {
            return MESSAGE_RIGHT;
        } else {
            return MESSAGE_LEFT;
        }
    }
}

