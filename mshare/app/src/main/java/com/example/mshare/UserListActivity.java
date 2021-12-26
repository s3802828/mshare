package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<String> userNames = new ArrayList<>();
    private final ArrayList<String> avatars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot ds:task.getResult()) {
                    if(!ds.getId().equals(firebaseUser.getUid())){
                        userNames.add(ds.getString("name"));
                        avatars.add(ds.getString("avatar"));
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
            return rowLayout;
        }
    }

}