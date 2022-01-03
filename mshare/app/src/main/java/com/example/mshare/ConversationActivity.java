package com.example.mshare;

import static com.example.mshare.ChatActivity.convertDateFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.example.mshare.adapters.ConversationAdapter;
import com.example.mshare.databinding.ActivityConversationBinding;

import com.example.mshare.interfaces.ConversationListener;
import com.example.mshare.models.Message;
import com.example.mshare.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConversationActivity extends AppCompatActivity implements ConversationListener {
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ActivityConversationBinding activityConversationBinding;
    private List<Message> conversationList;
    private ConversationAdapter conversationAdapter;
    private FirebaseFirestore database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityConversationBinding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(activityConversationBinding.getRoot());
        initialize();
        addListenToConversation();
    }

    private void initialize() {
        conversationList = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversationList, this, ConversationActivity.this);
        activityConversationBinding.conversationRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    //delete the opposite recent message
                    for(int i = 0; i < conversationList.size(); i++) {
                        String senderId = documentChange.getDocument().getString("lastMessage_senderId");
                        String receiverId = documentChange.getDocument().getString("lastMessage_receiverId");
                        if (conversationList.get(i).senderId.equals(receiverId) &&
                                conversationList.get(i).receiverId.equals(senderId)) {
                            conversationList.remove(i);
                            break;
                        }
                    }
                    Message message = new Message();
                    String senderId = documentChange.getDocument().getString("lastMessage_senderId");
                    String receiverId = documentChange.getDocument().getString("lastMessage_receiverId");
                    message.senderId = senderId;
                    message.receiverId = receiverId;
                    message.conversationAvatar = documentChange.getDocument().getString("lastMessage_receiverAvatar");

                    if (firebaseAuth.getUid().equals(senderId)) {
                        message.content = "You: " + documentChange.getDocument().getString("lastMessage");
                        message.conversationName = documentChange.getDocument().getString("lastMessage_receiverName");

                    } else if (firebaseAuth.getUid().equals(receiverId)) {
                        message.content = documentChange.getDocument().getString("lastMessage");
                        message.conversationName = documentChange.getDocument().getString("lastMessage_senderName");
                    }
                    message.date = documentChange.getDocument().getDate("timestamp");
                    conversationList.add(message);
                }
                else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    //find the old message in the list
                    for (int i = 0; i < conversationList.size(); i++) {
                        String senderId = documentChange.getDocument().getString("lastMessage_senderId");
                        String receiverId = documentChange.getDocument().getString("lastMessage_receiverId");
                        if (conversationList.get(i).senderId.equals(senderId)
                                && conversationList.get(i).receiverId.equals(receiverId)) {
                            conversationList.get(i).content = documentChange.getDocument().getString("lastMessage");
                            conversationList.get(i).timestamp = convertDateFormat(documentChange.getDocument().getDate("timestamp"));
                            break;
                        }
                    }
                }
            }
            //Sort the list of conversation base on timestamp then update the conversation adapter
            Collections.sort(conversationList, (a, b) -> b.date.compareTo(a.date));
            conversationAdapter.notifyDataSetChanged();
            activityConversationBinding.conversationRecyclerView.smoothScrollToPosition(0);
            activityConversationBinding.conversationRecyclerView.setVisibility(View.VISIBLE);

        }
    };

    private void addListenToConversation() {
        database.collection("conversation")
                .whereEqualTo("lastMessage_senderId", firebaseAuth.getUid())
                .addSnapshotListener(eventListener);
        database.collection("conversation")
                .whereEqualTo("lastMessage_receiverId", firebaseAuth.getUid())
                .addSnapshotListener(eventListener);
    }

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(ConversationActivity.this, ChatActivity.class);
        intent.putExtra("User", user);
        startActivity(intent);

    }
}