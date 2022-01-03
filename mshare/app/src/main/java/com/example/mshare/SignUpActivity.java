package com.example.mshare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {
    protected FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    TextView invalidEmail, toLogin;
    Button signUpButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText emailInput = findViewById(R.id.email_signup);
        EditText passwordInput = findViewById(R.id.password_signup);
        EditText nameInput = findViewById(R.id.name_signup);
        EditText confirmPasswordInput = findViewById(R.id.password_confirm_signup);

        toLogin = findViewById(R.id.toLogin);
        toLogin.setOnClickListener(v -> goToLogin(50));

        signUpButton = findViewById(R.id.btnSignUp);
        signUpButton.setOnClickListener(v -> {
            String emailValue = emailInput.getText().toString();
            String passwordValue = passwordInput.getText().toString();
            String nameValue = nameInput.getText().toString();
            String confirmPasswordValue = confirmPasswordInput.getText().toString();

            invalidEmail = findViewById(R.id.invalidEmail);
            TextView invalidPassword = findViewById(R.id.invalidPassword);
            TextView invalidName = findViewById(R.id.invalidName);
            TextView invalidPasswordConfirm = findViewById(R.id.invalidPasswordConfirm);

            //Validate name
            if(nameValue.equals("")){
                invalidName.setText("Name is required");
                invalidName.setVisibility(View.VISIBLE);
            } else if (!nameValue.matches("^(?![ ]+$)[a-zA-Z .]*$")){
                invalidName.setText("Name must only contain letters and space");
                invalidName.setVisibility(View.VISIBLE);
            } else invalidName.setVisibility(View.GONE);

            //Validate email
            if(emailValue.equals("")){
                invalidEmail.setText("Email is required");
                invalidEmail.setVisibility(View.VISIBLE);
            } else invalidEmail.setVisibility(View.GONE);

            //Validate password
            if(passwordValue.equals("")){ //Required
                invalidPassword.setText("Password is required");
                invalidPassword.setVisibility(View.VISIBLE);
            } else if (!(8 <= passwordValue.length() && passwordValue.length() <= 16)){// Password length
                invalidPassword.setText("Password must contain 8-16 characters");
                invalidPassword.setVisibility(View.VISIBLE);
            } else if (!passwordValue.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]*$")){ //Password format
                invalidPassword.setText("Password must only contain at least one letter, one number, and one special character");
                invalidPassword.setVisibility(View.VISIBLE);
            } else invalidPassword.setVisibility(View.GONE);

            //Validate confirm password
            if(confirmPasswordValue.equals("")){
                invalidPasswordConfirm.setText("Confirm password is required");
                invalidPasswordConfirm.setVisibility(View.VISIBLE);
            } else if(!confirmPasswordValue.equals(passwordValue)){
                invalidPasswordConfirm.setText("Confirm password does not match");
                invalidPasswordConfirm.setVisibility(View.VISIBLE);
            } else invalidPasswordConfirm.setVisibility(View.GONE);

            if(invalidName.getVisibility() == View.GONE && invalidEmail.getVisibility() == View.GONE
                    && invalidPassword.getVisibility() == View.GONE
                    && invalidPasswordConfirm.getVisibility() == View.GONE) {
                register(emailValue, passwordValue, nameValue);
            }
        });
    }

    private void goToLogin(int resultCode){
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        setResult(resultCode, intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    public void register(String email, String password, String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(Uri.parse("https://firebasestorage.googleapis.com/v0/b/androiddev-cbbc9.appspot.com/o/linux-avatar-by-qubodup-just-a-normal-tux-penguin-Z4TPDs-clipart.png?alt=media&token=4808957a-e786-4f03-b8c1-800d50d93b7d"))
                .build();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    assert user != null;
                    user.updateProfile(profileUpdates)
                            .addOnSuccessListener(unused -> {
                                user.sendEmailVerification();
                                Toast.makeText(SignUpActivity.this, "" +
                                        "Email Verification has been sent to you. Please verify to continue!", Toast.LENGTH_SHORT).show();
                                goToLogin(50);
                            })
                            .addOnFailureListener(Throwable::printStackTrace);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    if(e instanceof FirebaseAuthInvalidCredentialsException){
                        invalidEmail.setText("Invalid email format");
                        invalidEmail.setVisibility(View.VISIBLE);
                    }
                    if(e instanceof FirebaseAuthUserCollisionException){
                        invalidEmail.setText("This email is already existed");
                        invalidEmail.setVisibility(View.VISIBLE);
                    }
                });
    }

}