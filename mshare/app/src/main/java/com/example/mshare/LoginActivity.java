package com.example.mshare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginActivity extends AppCompatActivity {
    protected FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    TextView invalidEmail, invalidPassword;
    EditText emailInput, passwordInput;
    CallbackManager mCallbackManager;
    GoogleSignInClient mGoogleSignInClient;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.email_login);
        passwordInput = findViewById(R.id.password_login);

        Button loginButton = findViewById(R.id.btnLogin);
        invalidEmail = findViewById(R.id.invalidEmail);
        invalidPassword = findViewById(R.id.invalidPassword);

        loginButton.setOnClickListener(v -> {
            String emailValue = emailInput.getText().toString();
            String passwordValue = passwordInput.getText().toString();
            //Validate email
            if(emailValue.equals("")){
                invalidEmail.setText("Email is required");
                invalidEmail.setVisibility(View.VISIBLE);
            } else invalidEmail.setVisibility(View.GONE);

            //Validate password
            if(passwordValue.equals("")){ //Required
                invalidPassword.setText("Password is required");
                invalidPassword.setVisibility(View.VISIBLE);
            } else invalidPassword.setVisibility(View.GONE);
            if(invalidEmail.getVisibility() == View.GONE && invalidPassword.getVisibility() == View.GONE)
                signIn(emailValue, passwordValue);
        });

        LoginButton facebookLoginButton = findViewById(R.id.facebook_login_button);
        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Cancel", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(@NonNull FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.google_login_button);
        TextView signInButtonText = (TextView) signInButton.getChildAt(0);
        signInButtonText.setText("Continue with Google");
        signInButton.setOnClickListener(v -> signInWithGoogle());

    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            goToMain();
                        } else {
                            System.out.println(task.getResult());
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void goToSignUp(View v){
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivityForResult(intent, 100);
    }

    @SuppressLint("SetTextI18n")
    public void signIn(String email, String password){
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, authResult -> {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    assert user != null;
                    invalidEmail.setVisibility(View.GONE);
                    invalidPassword.setVisibility(View.GONE);
                    goToMain();
                })
                .addOnFailureListener(this, e -> {
                    if(e instanceof FirebaseAuthInvalidUserException){
                        invalidEmail.setText("User does not exist");
                        invalidEmail.setVisibility(View.VISIBLE);
                    } else if(e instanceof FirebaseAuthInvalidCredentialsException){
                        invalidPassword.setText("Invalid password");
                        invalidPassword.setVisibility(View.VISIBLE);
                    }
                });
    }
    private void goToMain(){
        Intent intent = new Intent(LoginActivity.this, ConversationActivity.class);
        emailInput.setText("");
        passwordInput.setText("");
        startActivityForResult(intent, 200);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            goToMain();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "FAIL", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == 100){
                goToMain();
            }
        }
        if(requestCode == 200){
            if(resultCode == 200){
                mGoogleSignInClient.signOut();
                Toast.makeText(LoginActivity.this, "You have successfully logged out!", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, "FAIL 1", Toast.LENGTH_SHORT).show();
            }
        }
    }
}