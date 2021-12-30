package com.example.mshare.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mshare.databinding.MessageReceiveContainerBinding;
import com.example.mshare.databinding.MessageSentContainerBinding;
import com.example.mshare.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Message> messageList;
    private String senderId;
    public static final int VIEW_TYPE_SEND = 1;
    public static final int VIEW_TYPE_RECEIVE = 2;

    public MessageAdapter(List<Message> messageList, String senderId) {
        this.messageList = messageList;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SEND) {
            return new MessageSentViewHolder(
                    MessageSentContainerBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent,false));
        } else {
            return new MessageReceiveViewHolder(
                    MessageReceiveContainerBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SEND) {
            ((MessageSentViewHolder) holder).setMessageData(messageList.get(position));
        } else {
            ((MessageReceiveViewHolder) holder).setMessageData(messageList.get(position));

        }

    }
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).getSenderId().equals(senderId)) {
            return VIEW_TYPE_SEND;
        } else {
            return VIEW_TYPE_RECEIVE;
        }

    }

    class MessageSentViewHolder extends RecyclerView.ViewHolder {
        private MessageSentContainerBinding messageSentContainerBinding;

        MessageSentViewHolder(MessageSentContainerBinding messageSentContainerBinding) {
            super(messageSentContainerBinding.getRoot());
            this.messageSentContainerBinding = messageSentContainerBinding;
        }

        void setMessageData(Message message) {
            messageSentContainerBinding.messageText.setText(message.getContent());
            messageSentContainerBinding.messageTimestamp.setText(message.getDate());

        }
    }

    class MessageReceiveViewHolder extends RecyclerView.ViewHolder {
        private final MessageReceiveContainerBinding messageReceiveContainerBinding;

        MessageReceiveViewHolder(MessageReceiveContainerBinding messageReceiveContainerBinding) {
            super(messageReceiveContainerBinding.getRoot());
            this.messageReceiveContainerBinding = messageReceiveContainerBinding;
        }

        void setMessageData(Message message) {
            messageReceiveContainerBinding.messageText.setText(message.getContent());
            messageReceiveContainerBinding.messageTimestamp.setText(message.getDate());

        }
    }


}
