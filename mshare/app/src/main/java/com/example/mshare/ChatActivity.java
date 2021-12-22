package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.mshare.adapter.MessageAdapter;
import com.example.mshare.databinding.ActivityChatBinding;
import com.example.mshare.model.Message;
import com.example.mshare.model.User;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//    FirebaseUser user = firebaseAuth.getCurrentUser();

    private ActivityChatBinding activityChatBinding;
    private User receiver;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
//    private PreferenceManager preferenceManager;
//    private String senderId;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityChatBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(activityChatBinding.getRoot());
        initialize();
        realTimeListenChat();
//        Toast.makeText(    ChatActivity
//                .this, "" + user.getDisplayName(), Toast.LENGTH_SHORT).show();
        activityChatBinding.sendMessageBtn.setOnClickListener(v -> sendMessage());
    }

    private void initialize() {
//        preferenceManager = new PreferenceManager(getApplicationContext());
        messageList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(messageList, firebaseAuth.getUid());
        activityChatBinding.chatAllMessagesRecyclerView.setAdapter(messageAdapter);
        database = FirebaseFirestore.getInstance();
        receiver = new User();
        if (firebaseAuth.getUid().equals("xK7FRsEspEbgWty8t0BVhwVAv1j1")) {
            receiver.id = "37Lxmhq87ZXg8RXR9XTYNG3hi1l1";
        } else {
            receiver.id = "xK7FRsEspEbgWty8t0BVhwVAv1j1";
        }
    }

    private String convertDateFormat(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }



    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("senderId",firebaseAuth.getUid());
        message.put("receiverId",receiver.id);
        message.put("content",activityChatBinding.messageContent.getText().toString());
        message.put("timestamp",new Date());
        database.collection("messages").add(message);
        activityChatBinding.messageContent.setText(null);
    }

    private void realTimeListenChat() {
        database.collection("messages")
                .whereEqualTo("senderId",firebaseAuth.getUid())
                .whereEqualTo("receiverId",receiver.id)
                .addSnapshotListener(eventListener);
        database.collection("messages")
                .whereEqualTo("senderId",receiver.id)
                .whereEqualTo("receiverId",firebaseAuth.getUid())
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) {
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
                    if(!message.content.isEmpty()) {
                        messageList.add(message);
                    }
                }
            }
            Collections.sort(messageList, (a, b) -> a.date.compareTo(b.date));
            if (count == 0) {
                messageAdapter.notifyDataSetChanged();
            } else {
                messageAdapter.notifyItemRangeInserted(messageList.size(),messageList.size());
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
        switch (item.getItemId()){
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
}