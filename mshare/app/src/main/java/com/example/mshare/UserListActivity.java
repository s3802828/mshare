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

import com.example.mshare.interfaces.APIService;
import com.example.mshare.models.Data;
import com.example.mshare.models.NotificationResponse;
import com.example.mshare.models.Sender;
import com.example.mshare.utilClasses.Client;
import com.example.mshare.utilClasses.SetImageFromUri;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
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
                Data data = new Data(currentUser.getUid(), null, null, null, receiverId);
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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

    }

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
            avatarView.setTag(avatars.get(position));
            TextView nameView = rowLayout.findViewById(R.id.userName);
            new SetImageFromUri().execute(avatarView);
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

}