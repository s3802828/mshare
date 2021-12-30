package com.example.mshare.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mshare.databinding.ConversationContainerBinding;
import com.example.mshare.listener.ConversationListener;
import com.example.mshare.model.Message;
import com.example.mshare.model.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>{
    private final List<Message> messageList;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ConversationListener conversationListener;

    public ConversationAdapter(List<Message> messageList, ConversationListener conversationListener) {
        this.messageList = messageList;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ConversationContainerBinding
                        .inflate(LayoutInflater
                                .from(parent.getContext()),parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setConversationData(messageList.get(position));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final ConversationContainerBinding conversationContainerBinding;

        ConversationViewHolder(ConversationContainerBinding conversationContainerBinding) {
            super(conversationContainerBinding.getRoot());
            this.conversationContainerBinding = conversationContainerBinding;
        }

        void setConversationData(Message message) {
//            conversationContainerBinding.conversationAvatar
            conversationContainerBinding.conversationName.setText(message.conversationName);
            conversationContainerBinding.recentConversation.setText(message.content);
            conversationContainerBinding.getRoot().setOnClickListener(v -> {
                User user = new User();
                if(firebaseAuth.getUid().equals(message.senderId)
                        && !firebaseAuth.getUid().equals(message.receiverId)) {
                    user.id = message.receiverId;
                    //avatar
                } else {
                    user.id = message.senderId;
                    //avatar
                }
                conversationListener.onConversationClicked(user);
            });
        }
    }
}
