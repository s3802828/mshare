package com.example.mshare;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mshare.models.Favorite;
import com.example.mshare.models.Genre;
import com.example.mshare.models.Song;
import com.example.mshare.models.Tokens;
import com.example.mshare.models.User;
import com.example.mshare.utilClasses.ApplicationStatus;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.util.Hex;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    protected FirebaseStorage storage = FirebaseStorage.getInstance("gs://androiddev-cbbc9.appspot.com/");
    private ImageView avatarView;
    private TextView userNameView, userEmailView;
    private Favorite favorite;
    private LinearLayout favSongLayout, favArtistLayout, favGenreLayout, uploadSongLayout;
    private EditText nameInput;
    private String userId;
    private File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        avatarView = findViewById(R.id.avatar);
        userNameView = findViewById(R.id.userName);
        userEmailView = findViewById(R.id.userEmail);
        nameInput = findViewById(R.id.userNameEdit);

        ImageButton editName = findViewById(R.id.editName_btn);
        ImageButton editFav = findViewById(R.id.editFav_btn);
        ImageButton addSong = findViewById(R.id.addSong_btn);
        Button logout = findViewById(R.id.logout);
        Intent intent = getIntent();
        userId = intent.getExtras().getString("userId");
        if(!userId.equals(firebaseAuth.getCurrentUser().getUid())){
            editName.setVisibility(View.GONE);
            editFav.setVisibility(View.GONE);
            logout.setVisibility(View.GONE);
            addSong.setVisibility(View.GONE);
        }
        db.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.getString("name");
                String userAvatar = documentSnapshot.getString("avatar");
                String userEmail = documentSnapshot.getString("email");

                userNameView.setText(userName);
                nameInput.setText(userName);
                Glide.with(ProfileActivity.this).load(userAvatar).into(avatarView);
                userEmailView.setText(userEmail);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

        favArtistLayout = findViewById(R.id.fav_artist_group);
        favSongLayout = findViewById(R.id.fav_song_group);
        favGenreLayout = findViewById(R.id.fav_genre_group);
        uploadSongLayout = findViewById(R.id.upload_group);

    }

    @Override
    protected void onResume() {
        super.onResume();
        favArtistLayout.removeAllViews();
        favSongLayout.removeAllViews();
        favGenreLayout.removeAllViews();
        uploadSongLayout.removeAllViews();
        db.collection("favorites").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        favorite = documentSnapshot.toObject(Favorite.class);
                        if(favorite != null){
                            LinearLayout.LayoutParams songLayoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            songLayoutParams.setMargins(0,0, 5, 5);
                            LinearLayout.LayoutParams artistLayoutParams = new LinearLayout.LayoutParams(
                                    0,
                                    200,
                                    1.0f
                            );
                            artistLayoutParams.setMargins(0,0, 2, 5);
                            for (Song song : favorite.getSongs()) {
                                TextView newView = new TextView(ProfileActivity.this);
                                newView.setText(song.getTitle() +" - " + song.getArtist());
                                newView.setPadding(10, 10, 10, 10);
                                newView.setTextColor(Color.BLACK);
                                newView.setTextSize(16);
                                newView.setGravity(Gravity.CENTER);
                                newView.setBackgroundResource(R.drawable.genre_border);
                                newView.setLayoutParams(songLayoutParams);
                                favSongLayout.addView(newView);
                            }
                            for (int i = 0; i < favorite.getArtists().size(); i += 2) {
                                LinearLayout newRowLayout = new LinearLayout(ProfileActivity.this);
                                newRowLayout.setOrientation(LinearLayout.HORIZONTAL);
                                newRowLayout.setLayoutParams(songLayoutParams);
                                newRowLayout.setWeightSum(2.0f);
                                for (int j = 0; j < 2; j++) {
                                    if(i+j == favorite.getArtists().size()) break;
                                    TextView newView = new TextView(ProfileActivity.this);
                                    newView.setText(favorite.getArtists().get(i+j));
                                    newView.setPadding(10, 10, 10, 10);
                                    newView.setTextColor(Color.BLACK);
                                    newView.setTextSize(16);
                                    newView.setGravity(Gravity.CENTER);
                                    newView.setBackgroundResource(R.drawable.artist_border);
                                    newView.setLayoutParams(artistLayoutParams);
                                    newRowLayout.addView(newView);
                                }
                                favArtistLayout.addView(newRowLayout);
                            }

                            for (int i = 0; i < favorite.getGenres().size(); i += 2) {
                                LinearLayout newRowLayout = new LinearLayout(ProfileActivity.this);
                                newRowLayout.setOrientation(LinearLayout.HORIZONTAL);
                                newRowLayout.setLayoutParams(songLayoutParams);
                                newRowLayout.setWeightSum(2.0f);
                                for (int j = 0; j < 2; j++) {
                                    if(i+j == favorite.getGenres().size()) break;
                                    TextView newView = new TextView(ProfileActivity.this);
                                    newView.setText(favorite.getGenres().get(i+j).getGenreName());
                                    newView.setTextColor(Color.WHITE);
                                    newView.setTextSize(16);
                                    newView.setGravity(Gravity.CENTER);
                                    newView.setTypeface(null, Typeface.BOLD);
                                    newView.setPadding(10, 10, 10, 10);
                                    newView.setBackgroundResource(R.drawable.genre_border);
                                    //newView.setBackgroundColor(Color.parseColor(favorite.getGenres().get(i+j).getGenreColor()));
                                    newView.setLayoutParams(artistLayoutParams);
                                    Drawable background = newView.getBackground().mutate();
                                    if (background instanceof ShapeDrawable) {
                                        ((ShapeDrawable)background).getPaint().setColor(Color.parseColor(favorite.getGenres().get(i+j).getGenreColor()));
                                    } else if (background instanceof GradientDrawable) {
                                        ((GradientDrawable)background).setColor(Color.parseColor(favorite.getGenres().get(i+j).getGenreColor()));
                                    } else if (background instanceof ColorDrawable) {
                                        ((ColorDrawable)background).setColor(Color.parseColor(favorite.getGenres().get(i+j).getGenreColor()));
                                    }
                                    newRowLayout.addView(newView);
                                }
                                favGenreLayout.addView(newRowLayout);
                            }
                        } else {
                            favorite = new Favorite();
                            favorite.setArtists(new ArrayList<>());
                            favorite.setSongs(new ArrayList<>());
                            favorite.setGenres(new ArrayList<>());
                        }
                    }
                });

        db.collection("songs")
                    .whereEqualTo("uploader_id", firebaseAuth.getCurrentUser().getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                LinearLayout.LayoutParams songLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                for (DocumentSnapshot ds: queryDocumentSnapshots.getDocuments()) {
                    TextView newView = new TextView(ProfileActivity.this);
                    newView.setText(ds.getString("title") +" - " + ds.getString("artist"));
                    newView.setPadding(10, 10, 10, 10);
                    newView.setTextColor(Color.BLACK);
                    newView.setTextSize(16);
                    newView.setGravity(Gravity.CENTER);
                    newView.setBackgroundResource(R.drawable.genre_border);
                    newView.setLayoutParams(songLayoutParams);
                    uploadSongLayout.addView(newView);
                }
            }
        });

    }
    @SuppressLint("SetTextI18n")
    public void onEditName(View v){
        TextView name = findViewById(R.id.userName);
        ImageButton nameEditBtn = findViewById(R.id.editName_btn);
        String nameValue = nameInput.getText().toString();
        if(name.getVisibility() == View.VISIBLE){
            name.setVisibility(View.GONE);
            nameInput.setVisibility(View.VISIBLE);
            nameEditBtn.setImageResource(R.drawable.ic_check);
        } else {
            TextView invalidName = findViewById(R.id.invalidName);
            //Validate name
            if(nameValue.equals("")){
                invalidName.setText("Name is required");
                invalidName.setVisibility(View.VISIBLE);
            } else if (!nameValue.matches("^(?![ ]+$)[a-zA-Z .]*$")){
                invalidName.setText("Name must only contain letters and space");
                invalidName.setVisibility(View.VISIBLE);
            } else invalidName.setVisibility(View.GONE);
            if(invalidName.getVisibility() == View.GONE){
                if(name.getText().toString().equals(nameValue.trim())){
                    name.setVisibility(View.VISIBLE);
                    nameInput.setVisibility(View.GONE);
                    nameEditBtn.setImageResource(R.drawable.ic_edit_profile);
                } else {
                    db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                            .update("name", nameValue.trim())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                    name.setText(nameValue.trim());
                                    name.setVisibility(View.VISIBLE);
                                    nameInput.setVisibility(View.GONE);
                                    nameEditBtn.setImageResource(R.drawable.ic_edit_profile);
                                }
                            });
                }
            }
        }
    }

    public void onLogout(View v){
        db.collection("tokens").document(firebaseAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        Tokens tokensList = documentSnapshot.toObject(Tokens.class);
                        ArrayList<String> tokens = tokensList.getNames();
                        tokens.remove(ApplicationStatus.getCurrentToken());
                        tokensList.setNames(tokens);
                        db.collection("tokens")
                                .document(firebaseAuth.getCurrentUser().getUid()).set(tokensList)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(@NonNull Void unused) {
                                        firebaseAuth.signOut();
                                        LoginManager.getInstance().logOut();
                                        Intent intent1 = new Intent(ProfileActivity.this, LoginActivity.class);
                                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                |Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent1);
                                        finish();
                                    }
                                });
                    }
                });
    }

    public void editFav(View v){
        Intent intent = new Intent(this, EditFavoriteActivity.class);
        intent.putExtra("favorites", favorite);
        startActivity(intent);
    }

    public void showFav(View view) {
        RelativeLayout favLayout = findViewById(R.id.fav_group);
        ImageButton showButton = findViewById(R.id.showFav_btn);
        if(favLayout.getVisibility() == View.GONE){
            favLayout.setVisibility(View.VISIBLE);
            showButton.setImageResource(R.drawable.ic_up);
        } else {
            favLayout.setVisibility(View.GONE);
            showButton.setImageResource(R.drawable.ic_down);
        }
    }

    public void showArtist(View view) {
        ImageButton showButton = findViewById(R.id.showArtist_btn);
        if(favArtistLayout.getVisibility() == View.GONE){
            favArtistLayout.setVisibility(View.VISIBLE);
            showButton.setImageResource(R.drawable.ic_up);
        } else {
            favArtistLayout.setVisibility(View.GONE);
            showButton.setImageResource(R.drawable.ic_down);
        }
    }

    public void showSong(View view) {
        ImageButton showButton = findViewById(R.id.showSong_btn);
        if(favSongLayout.getVisibility() == View.GONE){
            favSongLayout.setVisibility(View.VISIBLE);
            showButton.setImageResource(R.drawable.ic_up);
        } else {
            favSongLayout.setVisibility(View.GONE);
            showButton.setImageResource(R.drawable.ic_down);
        }
    }

    public void showGenre(View view) {
        ImageButton showButton = findViewById(R.id.showGenre_btn);
        if(favGenreLayout.getVisibility() == View.GONE){
            favGenreLayout.setVisibility(View.VISIBLE);
            showButton.setImageResource(R.drawable.ic_up);
        } else {
            favGenreLayout.setVisibility(View.GONE);
            showButton.setImageResource(R.drawable.ic_down);
        }
    }
    public void showUpload(View view){
        ImageButton showButton = findViewById(R.id.showUpload_btn);
        if(uploadSongLayout.getVisibility() == View.GONE){
            uploadSongLayout.setVisibility(View.VISIBLE);
            showButton.setImageResource(R.drawable.ic_up);
        } else {
            uploadSongLayout.setVisibility(View.GONE);
            showButton.setImageResource(R.drawable.ic_down);
        }
    }

    public void onEditAvatar(View view) {
        ArrayList<String> imagePath = new ArrayList<>();
        ArrayList<String> imageName = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("CHOOSE IMAGES (FROM YOUR DEVICE STORAGE)")
                .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        File imageFolder = new File("sdcard/Pictures");
        files = new File[0];

        if (imageFolder.exists()){
            files = imageFolder.listFiles();
            if (files == null) {
                builder.setMessage("There currently no images in device storage");
            } else {
                for (File file:files){
//                    if (file == files[files.length-1]) {
//                        continue;
//                    } else {
                        imagePath.add(file.getAbsolutePath());
                        imageName.add(file.getName());
 //                   }
                }
                AvatarListAdapter adapter = new AvatarListAdapter(getApplicationContext(), imagePath);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this);
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage("Updating your avatar.....");
                        long bytes = files[which].length();
                        Uri fileUri = Uri.fromFile(files[which]);

                        String[] data = imageName.get(which).split("\\.");

                        if (!(data[data.length-1].equalsIgnoreCase("jpg")) && !(data[data.length-1].equalsIgnoreCase("png")) && !(data[data.length-1].equalsIgnoreCase("jpeg"))) {
                            Toast.makeText(ProfileActivity.this, "Must be an image file", Toast.LENGTH_SHORT).show();
                        } else if (bytes > (2 * 1024 * 1024)) {
                            Toast.makeText(ProfileActivity.this, "Image file size must be smaller than 2MB", Toast.LENGTH_SHORT).show();
                        } else {
                            StorageReference storageRef = storage.getReference();
                            StorageReference riversRef = storageRef.child("avatar/"+fileUri.getLastPathSegment());
                            UploadTask uploadTask = riversRef.putFile(fileUri);

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    progressDialog.show();
                                    // Continue with the task to get the download URL
                                    return riversRef.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Uri downloadUri = task.getResult();
                                        db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                                                .update("avatar", downloadUri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(@NonNull Void unused) {
                                                if(progressDialog.isShowing()) progressDialog.dismiss();
                                                Glide.with(ProfileActivity.this).load(downloadUri.toString()).into(avatarView);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
        builder.create().show();

    }

    public void addSong(View view) {
        Intent intent = new Intent(ProfileActivity.this, AddSongActivity.class);
        startActivity(intent);
    }


    private class AvatarListAdapter extends ArrayAdapter<String> {
        private final Context context;
        private ArrayList<String> avatars;

        public AvatarListAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, -1, objects);
            this.context = context;
            this.avatars = (ArrayList<String>) objects;
        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View rowLayout = inflater.inflate(R.layout.avatar_list_view, parent, false);
            ImageView avatarView = rowLayout.findViewById(R.id.editAvatar);
            Bitmap imageBitmap = BitmapFactory.decodeFile(avatars.get(position));
            avatarView.setImageBitmap(imageBitmap);
            return rowLayout;
        }
    }
}