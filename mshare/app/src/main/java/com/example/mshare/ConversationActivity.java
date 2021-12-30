package com.example.mshare;

import static com.example.mshare.ChatActivity.convertDateFormat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.mshare.adapter.ConversationAdapter;
import com.example.mshare.databinding.ActivityConversationBinding;
import com.example.mshare.listener.ConversationListener;
import com.example.mshare.model.Message;
import com.example.mshare.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
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
    private Button button;


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
        conversationAdapter = new ConversationAdapter(conversationList, this);
        activityConversationBinding.conversationRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
        button = findViewById(R.id.gotoChat);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(ConversationActivity.this, ChatActivity.class);
            User user = new User();
            user.id = "tJYHI20FLMRhHG2IdYQDvM3stRI3";
            intent.putExtra("User", user);
            startActivity(intent);
        });
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
                    if (firebaseAuth.getUid().equals(senderId)) {
                        message.conversationName = documentChange.getDocument().getString("lastMessage_receiverId");
                        message.content = "You: " + documentChange.getDocument().getString("lastMessage");
                    } else if (firebaseAuth.getUid().equals(receiverId)) {
                        message.conversationName = documentChange.getDocument().getString("lastMessage_senderId");
                        message.content = documentChange.getDocument().getString("lastMessage");
                    }
                    message.date = documentChange.getDocument().getDate("timestamp");
                    conversationList.add(message);
                    System.out.println("ADDED");
                    System.out.println(conversationList.get(0).content);
                    System.out.println(conversationList.get(0).date);
                    System.out.println(conversationList.size());

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
                            System.out.println("Modified");
                            System.out.println(conversationList.get(0).content);
                            System.out.println(conversationList.get(0).date);
                            System.out.println(conversationList.size());
                            break;
                        }
                    }
                }
            }
            //Sort the list of conversation base on timestamp then update the conversation adapter
            Collections.sort(conversationList, Comparator.comparing(a -> a.date));
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