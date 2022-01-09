package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mshare.interfaces.APIService;
import com.example.mshare.models.Data;
import com.example.mshare.models.NotificationResponse;
import com.example.mshare.models.Sender;
import com.example.mshare.models.Song;
import com.example.mshare.models.Tokens;
import com.example.mshare.models.User;
import com.example.mshare.utilClasses.Client;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends AppCompatActivity {
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<User> users = new ArrayList<>();
    private final ArrayList<User> fullUsers = new ArrayList<>();
    private APIService apiService;
    private String roomId;
    private ListenerRegistration listener1;
    private UserListAdapter userListAdapter;
    private ListView userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        Intent intent = getIntent();
        roomId = intent.getExtras().getString("room_id");
        db.collection("users")
                .orderBy("onlineStatus", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot ds : task.getResult()) {
                    if (!ds.getId().equals(firebaseUser.getUid())) {
                        User newUser = new User();
                        newUser.setName(ds.getString("name"));
                        newUser.setAvatar(ds.getString("avatar"));
                        newUser.setId(ds.getId());
                        newUser.setOnlineStatus(ds.getString("onlineStatus"));
                        users.add(newUser);
                        fullUsers.add(newUser);
                        db.collection("users")
                                .whereEqualTo(FieldPath.documentId(), ds.getId())
                                .addSnapshotListener(eventListener1);
                    }
                }
                userList = findViewById(R.id.user_listView);
                userListAdapter = new UserListAdapter(UserListActivity.this, users, fullUsers);
                userListAdapter.notifyDataSetChanged();
                userList.setAdapter(userListAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
        listener1 = db.collection("rooms")
                .document(roomId)
                .collection("request_response")
                .whereEqualTo(FieldPath.documentId(),roomId)
                .addSnapshotListener(eventListener)
        ;


    }

    private void sendRequestNotification(String receiverId) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        db.collection("users").document(receiverId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                db.collection("tokens").document(receiverId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        Tokens tokens = documentSnapshot.toObject(Tokens.class);
                        for (String token : tokens.getNames()) {
                            assert currentUser != null;
                            Data data = new Data(currentUser.getUid(), null, null, roomId, receiverId);
                            Sender sender = new Sender(data, token);
                            apiService.sendNotification(sender)
                                    .enqueue(new Callback<NotificationResponse>() {
                                        @Override
                                        public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                                            if (response.code() == 200) {
                                                assert response.body() != null;
                                                if (response.body().getSuccess() != 1) {
                                                    Toast.makeText(UserListActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<NotificationResponse> call, Throwable t) {

                                        }
                                    });
                        }
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

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        String res = null;
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    res = documentChange.getDocument().getString("response");
                    if (res.equals("accept")) {
                        Intent intent = new Intent(UserListActivity.this, MediaPlayerActivity.class);
                        setResult(100, intent);
                        finish();
                    } else if (res.equals("decline"))
                        Toast.makeText(UserListActivity.this, "This user has declined your request", Toast.LENGTH_SHORT).show();
                    else if (res.equals("no_response"))
                        Toast.makeText(UserListActivity.this, "No response from this user", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    private final EventListener<QuerySnapshot> eventListener1 = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    String onlineStatus = documentChange.getDocument().getString("onlineStatus");
                    for (int i = 0; i < users.size(); i++) {
                        if(users.get(i).getId().equals(documentChange.getDocument().getId())){
                            users.get(i).setOnlineStatus(onlineStatus);
                        }
                    }
                    for (int i = 0; i < fullUsers.size(); i++){
                        if(fullUsers.get(i).getId().equals(documentChange.getDocument().getId())){
                            fullUsers.get(i).setOnlineStatus(onlineStatus);
                            userListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_main, menu);
        MenuItem searchViewItem = menu.findItem(R.id.searchView);
        SearchView searchUser = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchUser.setQueryHint("Enter User Name...");
        searchUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userListAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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

    private class UserListAdapter extends ArrayAdapter<User> {
        private final Context context;
        private ArrayList<User> users;
        private ArrayList<User> allUsers;

        public UserListAdapter(@NonNull Context context, @NonNull List<User> objects, List<User> fullUsers) {
            super(context, -1, objects);
            this.context = context;
            this.users = (ArrayList<User>) objects;
            this.allUsers = (ArrayList<User>) fullUsers;
        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View rowLayout = inflater.inflate(R.layout.user_list_view, parent, false);
            ImageView avatarView = rowLayout.findViewById(R.id.user_avatar);
            Glide.with(UserListActivity.this).load(users.get(position).getAvatar()).into(avatarView);

            TextView nameView = rowLayout.findViewById(R.id.userName);
            nameView.setText(users.get(position).getName());

            TextView onlineStatusView = rowLayout.findViewById(R.id.onlineStatus);
            if (users.get(position).getOnlineStatus().equals("Online")) {
                onlineStatusView.setText("Online");
                onlineStatusView.setTextColor(Color.GREEN);
            } else if (users.get(position).getOnlineStatus().equals("Offline")) {
                onlineStatusView.setText("Offline");
                onlineStatusView.setTextColor(Color.DKGRAY);
            } else {
                onlineStatusView.setText("Busy");
                onlineStatusView.setTextColor(Color.RED);
            }

            Button sendRequestButton = rowLayout.findViewById(R.id.send_request_btn);
            sendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    {
                        if (users.get(position).getOnlineStatus().equals("Online"))
                            sendRequestNotification(users.get(position).getId());
                        else Toast.makeText(UserListActivity.this,
                                "CANNOT SEND REQUEST: This user is currently offline or busy", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Button viewProfileButton = rowLayout.findViewById(R.id.view_profile_btn);
            viewProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserListActivity.this, ProfileActivity.class);
                    intent.putExtra("userId", users.get(position).getId());
                    startActivity(intent);
                }
            });
            return rowLayout;
        }

        @Override
        public Filter getFilter() {
            return searchFilter;
        }

        private Filter searchFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<User> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(allUsers);
                } else {
                    for (User user : allUsers) {
                        if (user.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredList.add(user);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                users.clear();
                users.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listener1.remove();
    }
}