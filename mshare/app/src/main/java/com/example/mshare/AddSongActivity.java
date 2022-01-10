package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.services.s3.transfer.TransferManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
//import software.amazon.awssdk.transfer.s3.S3ClientConfiguration;
//import software.amazon.awssdk.transfer.s3.S3TransferManager;
//import software.amazon.awssdk.transfer.s3.CompletedDownload;
//import software.amazon.awssdk.transfer.s3.CompletedUpload;
//import software.amazon.awssdk.transfer.s3.Download;
//import software.amazon.awssdk.transfer.s3.Upload;
//import software.amazon.awssdk.transfer.s3.UploadRequest;

import java.io.File;
import java.util.ArrayList;

public class AddSongActivity extends AppCompatActivity {

    // Get a non-default Storage bucket
    private FirebaseStorage storage = FirebaseStorage.getInstance("gs://androiddev-cbbc9.appspot.com/");
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static final String MUSIC_PATH = "sdcard/Music";
    public static final String IMAGE_PATH = "sdcard/Pictures";
    private ListView songListView;
    private ListView imageListView;
    private Button returnButton;
    private Button confirmButton;
    private TextView activityHeader;
    private TextView chooseSongHeader;
    private TextView chooseImageHeader;
    private TextView labelTitle;
    private TextView labelArtist;
    private TextView labelCover;
    private TextView labelFile;
    private EditText editTitle;
    private EditText editArtist;
    private Button chooseSongButton;
    private Button chooseImageButton;
    private ImageView imagePreview;
    private TextView songPathPreview;
    private TextView noFileText;

    private ArrayList<String> songPath;
    private ArrayList<String> songName;
    private ArrayList<String> imagePath;
    private ArrayList<String> imageName;

    FirebaseFirestore db;
    ProgressDialog progressSongDialog;

    private Uri fileUri;
    private Uri imageUri;
    private String titleInput;
    private String artistInput;
    ;

//    private static S3Client s3;
//    private AmazonS3 s3Client;
//    private final String BUCKET_NAME = "msharezhangtreekhang";

//    private S3TransferManager s3TransferManager;

//    private AWSCredentials creds = new BasicAWSCredentials("AKIAW5EFHL2CZEVCBHGN","iZIMpN0jCDzkDcgSpXCejs8o3vmBYA6nHG0CkPE8");



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        db = FirebaseFirestore.getInstance();

        fileUri = null;
        imageUri = null;

        songListView = findViewById(R.id.music_file_list);
        imageListView = findViewById(R.id.image_file_list);
        returnButton = findViewById(R.id.buttonReturn);
        confirmButton = findViewById(R.id.buttonConfirmAdd);
        activityHeader = findViewById(R.id.headerActivity);
        chooseSongHeader = findViewById(R.id.headerSong);
        chooseImageHeader = findViewById(R.id.headerImage);
        labelTitle = findViewById(R.id.labelTitle);
        labelArtist = findViewById(R.id.labelArtist);
        labelCover = findViewById(R.id.labelCover);
        labelFile = findViewById(R.id.labelFile);
        editTitle = findViewById(R.id.editTitle);
        editArtist = findViewById(R.id.editArtist);
        chooseImageButton = findViewById(R.id.buttonCover);
        chooseSongButton = findViewById(R.id.buttonFile);
        imagePreview = findViewById(R.id.songImage);
        songPathPreview = findViewById(R.id.labelFilePath);
        noFileText = findViewById(R.id.textNoItem);




//        Region region = Region.US_EAST_1;
//        S3ClientConfiguration s3ClientConfiguration =
//                S3ClientConfiguration.builder()
//                        .region(region)
//                        .minimumPartSizeInBytes((long) (10 * 1024 * 1024))
//                        .targetThroughputInGbps(20.0)
//                        .build();
//
//        s3TransferManager = S3TransferManager.builder().s3ClientConfiguration(s3ClientConfiguration).build();

//        s3TransferManager = S3TransferManager.builder().build();

        // Initialize the Amazon Cognito credentials provider
//        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                getApplicationContext(),
//                "us-east-1:fc0329e6-35b5-455e-8245-21381f114d5f", // Identity pool ID
//                Regions.US_EAST_1 // Region
//        );
//
//        s3Client = new AmazonS3Client(credentialsProvider);


//        TransferManager manager = new TransferManager((AWSCredentials) credentials);

        requestPermission();
        displayMusicFile();
        displayImageFile();

        setVisible(R.id.music_file_list, false);
        setVisible(R.id.image_file_list, false);
        setVisible(R.id.headerImage, false);
        setVisible(R.id.headerSong, false);
        setVisible(R.id.buttonReturn, false);
        setVisible(R.id.textNoItem, false);
    }

    @SuppressLint("SetTextI18n")
    private void displayImageFile() {

        imagePath = new ArrayList<>();
        imageName = new ArrayList<>();

        File imageFolder = new File(IMAGE_PATH);
        File[] files = new File[0];

        if (imageFolder.exists()){
            files = imageFolder.listFiles();
            if (files == null) {
                noFileText.setText("There currently no images in this folder");
                setVisible(R.id.textNoItem, true);
            } else {
                for (File file:files){
//                    if (file == files[files.length-1]) {
//                        continue;
//                    } else {
                        imagePath.add(file.getPath());
                        imageName.add(file.getName());
                    //}
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, imageName);

        imageListView.setAdapter(adapter);
        File[] finalFiles = files;
        imageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                long bytes = finalFiles[position].length();

                String[] data = imageName.get(position).split("\\.");

                if (!(data[data.length-1].equalsIgnoreCase("jpg")) && !(data[data.length-1].equalsIgnoreCase("png")) && !(data[data.length-1].equalsIgnoreCase("jpeg"))) {
                    Toast.makeText(AddSongActivity.this, "Must be an image file", Toast.LENGTH_SHORT).show();
                } else if (bytes > (2 * 1024 * 1024)) {
                    Toast.makeText(AddSongActivity.this, "Image file size must be smaller than 2MB", Toast.LENGTH_SHORT).show();
                } else {

                    imageUri = Uri.fromFile(finalFiles[position]);

                    Bitmap imageBitmap = BitmapFactory.decodeFile(finalFiles[position].getAbsolutePath());
                    imagePreview.setImageBitmap(imageBitmap);

                    setVisible(R.id.image_file_list, false);
                    setVisible(R.id.headerImage, false);

                    setVisible(R.id.buttonReturn, false);
                    setVisible(R.id.headerActivity, true);
                    setVisible(R.id.buttonConfirmAdd, true);
                    setVisible(R.id.linearLayoutInput, true);

                }





//                StorageReference storageRef = storage.getReference();
//
//                Uri file = Uri.fromFile(finalFiles[position]);
//                StorageReference riversRef = storageRef.child("song_cover/"+file.getLastPathSegment());
//                UploadTask uploadTask = riversRef.putFile(file);
//
//                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Toast.makeText(AddSongActivity.this, taskSnapshot.getMetadata()+"", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(AddSongActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                });

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void displayMusicFile() {
        songPath = new ArrayList<>();
        songName = new ArrayList<>();

        File musicFolder = new File(MUSIC_PATH);
        File[] files = new File[0];

        if (musicFolder.exists()){
            files = musicFolder.listFiles();
            if (files == null) {
                noFileText.setText("There currently no files in this folder");
                setVisible(R.id.textNoItem, true);
            } else {
                for (File file:files){
//                    if (file == files[files.length-1]) {
//                        continue;
//                    } else {
                        songPath.add(file.getPath());
                        songName.add(file.getName());
                        //                   }
                }
            }

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, songName);

        songListView.setAdapter(adapter);
        File[] finalFiles = files;
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                long bytes = finalFiles[position].length();

                String[] data = songName.get(position).split("\\.");

                if (!(data[data.length-1].equalsIgnoreCase("mp3"))) {
                    Toast.makeText(AddSongActivity.this, "Must be an mp3 file", Toast.LENGTH_SHORT).show();
                } else if (bytes > (5 * 1024 * 1024)) {
                    Toast.makeText(AddSongActivity.this, "Mp3 file size must be smaller than 1MB", Toast.LENGTH_SHORT).show();
                } else {

                    fileUri = Uri.fromFile(finalFiles[position]);

                    String value = songListView.getItemAtPosition(position).toString();

                    songPathPreview.setText(value);

                    setVisible(R.id.music_file_list, false);
                    setVisible(R.id.headerSong, false);

                    setVisible(R.id.buttonReturn, false);
                    setVisible(R.id.headerActivity, true);
                    setVisible(R.id.buttonConfirmAdd, true);
                    setVisible(R.id.linearLayoutInput, true);
                }

//                StorageReference storageRef = storage.getReference();
//
//                Uri file = Uri.fromFile(finalFiles[position]);
//                StorageReference riversRef = storageRef.child("song_url/"+file.getLastPathSegment());
//                UploadTask uploadTask = riversRef.putFile(file);
//
//                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Toast.makeText(AddSongActivity.this, taskSnapshot.getMetadata()+"", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(AddSongActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                });


//                String path = songPath.get(position);
//
//                s3Client.putObject(BUCKET_NAME, path, finalFiles[position]);

//                Upload upload =
//                        s3TransferManager.upload(b -> b.putObjectRequest(r -> r.bucket(BUCKET_NAME).key(KEY))
//                                .source(Paths.get(String.valueOf(file))));
//
//                CompletedUpload completedUpload = upload.completionFuture().join();
//
//                System.out.println("PutObjectResponse: " + completedUpload.response());

//                Toast.makeText(AddSongActivity.this, path, Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        },101);
    }

    private void setVisible(int id, boolean isVisible) {
        View aView = findViewById(id);
        if (isVisible) {
            aView.setVisibility(View.VISIBLE);
        } else {
            aView.setVisibility(View.INVISIBLE);
        }

    }

    public void onCancel(View view) {
        if (songListView.getVisibility() == View.VISIBLE) {
            setVisible(R.id.music_file_list, false);
            setVisible(R.id.headerSong, false);
        }

        if (imageListView.getVisibility() == View.VISIBLE) {
            setVisible(R.id.image_file_list, false);
            setVisible(R.id.headerImage, false);
        }

        setVisible(R.id.buttonReturn, false);
        setVisible(R.id.headerActivity, true);
        setVisible(R.id.buttonConfirmAdd, true);
        setVisible(R.id.linearLayoutInput, true);
    }

    public void onConfirmSubmit(View view) {
        if (fileUri == null || imageUri == null || editTitle.getText().toString().equals("")
                || editArtist.getText().toString().equals("")) {
            Toast.makeText(AddSongActivity.this, "Please fill all the inputs", Toast.LENGTH_SHORT).show();
        } else {

            progressSongDialog = new ProgressDialog(this);
            progressSongDialog.setCancelable(false);
            progressSongDialog.setMessage("Uploading your song...");
            progressSongDialog.show();

            StorageReference storageRef = storage.getReference();
            StorageReference riversRef = storageRef.child("song_url/"+fileUri.getLastPathSegment());
            UploadTask uploadTask = riversRef.putFile(fileUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return riversRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        Map<String, String> data = new HashMap<String, String>();

                        data.put("title", editTitle.getText()+"");
                        data.put("artist",editArtist.getText()+"");
                        data.put("url",downloadUri.toString());
                        data.put("cover","");
                        data.put("uploader_id", firebaseAuth.getCurrentUser().getUid());

                        db.collection("songs").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                String songId = documentReference.getId();

                                StorageReference imageRef = storageRef.child("song_cover/"+imageUri.getLastPathSegment());
                                UploadTask uploadImageTask = imageRef.putFile(imageUri);

                                Task<Uri> urlTask = uploadImageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }

                                        // Continue with the task to get the download URL
                                        return imageRef.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri downloadImageUri = task.getResult();

                                            DocumentReference imageRef = db.collection("songs").document(songId);

                                            imageRef.update("cover", downloadImageUri.toString())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                            if (progressSongDialog.isShowing()) {
                                                                progressSongDialog.dismiss();
                                                                finish();
                                                            }


                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });



                                        } else {

                                        }
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddSongActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }

    public void chooseImage(View view) {
        setVisible(R.id.linearLayoutInput, false);
        setVisible(R.id.headerActivity, false);
        setVisible(R.id.buttonConfirmAdd, false);

        setVisible(R.id.image_file_list, true);
        setVisible(R.id.headerImage, true);
        setVisible(R.id.buttonReturn, true);

    }

    public void chooseFile(View view) {
        setVisible(R.id.linearLayoutInput, false);
        setVisible(R.id.headerActivity, false);
        setVisible(R.id.buttonConfirmAdd, false);

        setVisible(R.id.music_file_list, true);
        setVisible(R.id.headerSong, true);
        setVisible(R.id.buttonReturn, true);
    }
}