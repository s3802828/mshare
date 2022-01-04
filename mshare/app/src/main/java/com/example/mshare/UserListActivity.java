package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mshare.interfaces.APIService;
import com.example.mshare.models.Data;
import com.example.mshare.models.NotificationResponse;
import com.example.mshare.models.Sender;
import com.example.mshare.utilClasses.Client;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends AppCompatActivity {
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<String> userNames = new ArrayList<>();
    private final ArrayList<String> userIds = new ArrayList<>();
    private final ArrayList<String> avatars = new ArrayList<>();
    private APIService apiService;
    private String roomId;
    private ListenerRegistration listener1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        Intent intent = getIntent();
        roomId = intent.getExtras().getString("room_id");
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot ds:task.getResult()) {
                    if(!ds.getId().equals(firebaseUser.getUid())){
                        userNames.add(ds.getString("name"));
                        avatars.add(ds.getString("avatar"));
                        userIds.add(ds.getId());
                    }
                }
                ListView userList = findViewById(R.id.user_listView);
                UserListAdapter userListAdapter = new UserListAdapter(UserListActivity.this, userNames);
                userListAdapter.notifyDataSetChanged();
                userList.setAdapter(userListAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });


    }
    private void sendRequestNotification(String receiverId){
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        db.collection("users").document(receiverId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String token = task.getResult().getString("token");
                assert currentUser != null;
                Data data = new Data(currentUser.getUid(), null, null, roomId, receiverId);
                Sender sender = new Sender(data, token);
                apiService.sendNotification(sender)
                        .enqueue(new Callback<NotificationResponse>() {
                            @Override
                            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                                if(response.code() == 200){
                                    assert response.body() != null;
                                    if(response.body().getSuccess() != 1){
                                        Toast.makeText(UserListActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<NotificationResponse> call, Throwable t) {

                            }
                        });
//
                listener1 = db.collection("rooms")
                        .document(roomId)
                        .collection("request_response")
                        .whereEqualTo(FieldPath.documentId(),roomId)
                        .addSnapshotListener(eventListener)
                        ;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        String res = null;
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    res = documentChange.getDocument().getString("response");
                    if(res.equals("accept")){
                        Intent intent = new Intent(UserListActivity.this, MediaPlayerActivity.class);
                        setResult(100, intent);
                        finish();
                    }
                    else if(res.equals("decline"))
                        Toast.makeText(UserListActivity.this, "This user has declined your request", Toast.LENGTH_SHORT).show();
                    else if(res.equals("no_response"))
                        Toast.makeText(UserListActivity.this, "No response from this user", Toast.LENGTH_SHORT).show();
                }
            }
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
                Intent intent = new Intent(UserListActivity.this, LoginActivity.class);
                setResult(200, intent);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class UserListAdapter extends ArrayAdapter<String>{
        private final Context context;
        private final ArrayList<String> userNames;
        public UserListAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, -1, objects);
            this.context = context;
            this.userNames = (ArrayList<String>) objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View rowLayout = inflater.inflate(R.layout.user_list_view, parent, false);
            ImageView avatarView = rowLayout.findViewById(R.id.user_avatar);
            Glide.with(UserListActivity.this).load(avatars.get(position)).into(avatarView);


            TextView nameView = rowLayout.findViewById(R.id.userName);
            nameView.setText(userNames.get(position));

            Button sendRequestButton = rowLayout.findViewById(R.id.send_request_btn);
            sendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestNotification(userIds.get(position));
                }
            });
            return rowLayout;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listener1.remove();
    }
}