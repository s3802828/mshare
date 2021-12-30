package com.example.mshare;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.mshare.adapters.MessageAdapter;
import com.example.mshare.databinding.ActivityChatBinding;
import com.example.mshare.models.Message;
import com.example.mshare.models.User;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore database;
    private ActivityChatBinding activityChatBinding;
    private User receiver;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private AppCompatImageView backButton;

    //    private PreferenceManager preferenceManager;
//    private String senderId;
    private final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityChatBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(activityChatBinding.getRoot());
        initialize();
        realTimeListenChat();
        activityChatBinding.sendMessageBtn.setOnClickListener(v -> sendMessage());
    }

    private void initialize() {
        messageList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(messageList, firebaseAuth.getUid());
        activityChatBinding.chatAllMessagesRecyclerView.setAdapter(messageAdapter);
        database = FirebaseFirestore.getInstance();
        receiver = (User) getIntent().getSerializableExtra("User");
        backButton = findViewById(R.id.backBtn);
//        backButton.setOnClickListener(v -> onBackPressed());
    }

    public static String convertDateFormat(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }


    private void sendMessage() {
        DocumentReference documentReference =  database.collection("conversation")
                .document(generateConversationId(firebaseAuth.getUid(), receiver.getId()));
        String content = activityChatBinding.messageContent.getText().toString();
        //Check empty space message
        if(!content.trim().isEmpty()) {
            HashMap<String, Object> message = new HashMap<>();
            message.put("senderId", firebaseAuth.getUid());
            message.put("receiverId", receiver.getId());
            message.put("content", content);
            message.put("timestamp", new Date());

            //add message to Firestore
            documentReference.collection("messages").add(message);
            activityChatBinding.messageContent.setText(null);

            //update the last message fields in Conversation Collection
            HashMap<String, Object> lastMessage = new HashMap<>();
            lastMessage.put("lastMessage_senderId", firebaseAuth.getUid());
            lastMessage.put("lastMessage_senderName", firebaseAuth.getCurrentUser().getDisplayName());
            lastMessage.put("lastMessage_receiverId", receiver.getId());
            lastMessage.put("lastMessage_receiverName", receiver.getName());
            lastMessage.put("lastMessage", content);
            lastMessage.put("timestamp", new Date());
            documentReference.set(lastMessage);
        }
    }

    private void realTimeListenChat() {
        database.collection("conversation")
                .document(generateConversationId(firebaseAuth.getUid(), receiver.getId()))
                .collection("messages")
                .whereEqualTo("senderId", firebaseAuth.getUid())
                .whereEqualTo("receiverId", receiver.getId())
                .addSnapshotListener(eventListener);
        database.collection("conversation")
                .document(generateConversationId(firebaseAuth.getUid(), receiver.getId()))
                .collection("messages")
                .whereEqualTo("senderId", receiver.getId())
                .whereEqualTo("receiverId", firebaseAuth.getUid())
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = messageList.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Message message = new Message();
                    message.senderId = documentChange.getDocument().getString("senderId");
                    message.receiverId = documentChange.getDocument().getString("receiverId");
                    message.content = documentChange.getDocument().getString("content").trim();
                    message.timestamp = convertDateFormat(documentChange.getDocument().getDate("timestamp"));
                    message.date = documentChange.getDocument().getDate("timestamp");
                    if (!message.content.isEmpty()) {
                        messageList.add(message);
                    }
                }
            }
            Collections.sort(messageList, Comparator.comparing(a -> a.date));

            if (count == 0) {
                messageAdapter.notifyDataSetChanged();
            } else {
                messageAdapter.notifyItemRangeInserted(messageList.size(), messageList.size());
                activityChatBinding.chatAllMessagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            }
            activityChatBinding.chatAllMessagesRecyclerView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                firebaseAuth.signOut();
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
                setResult(200, intent);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private String generateConversationId(String a, String b) {
        if (a.compareTo(b) < 0) {
            return b + a;
        }
        return a + b;
    }

}