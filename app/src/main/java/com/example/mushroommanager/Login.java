package com.example.mushroommanager;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class Login extends AppCompatActivity {

    TextView btnToSignUp, expertType;

    EditText inputEmail, inputPassword;
    Button loginBtn;
    ProgressDialog mLoadingBar;
    private Button btnGoogle;

    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    GoogleSignInClient mSignInClient;

    private static final int REQ_ONE_TAP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnToSignUp = findViewById(R.id.textSignUp);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener((v) -> {
            checkCredentials();
        });

        mAuth = FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        mLoadingBar = new ProgressDialog(Login.this);
        btnGoogle = findViewById(R.id.btnGoogle);




        btnToSignUp.setOnClickListener((v) -> {
            Intent intent=new Intent(Login.this, UserDashboardActivity.class);
            startActivity(intent);

        });

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mSignInClient = GoogleSignIn.getClient(this, gso);



        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


    }
    private void signIn(){
        Intent signInIntent = mSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQ_ONE_TAP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ONE_TAP) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {



        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(this, authResult -> {

                    DocumentReference docIdRef = db.collection("Users").document(mAuth.getUid());
                    docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()){
                                System.out.println("Already exist");

                            }else{
                                updateUserInfoGoogleSignIn(acct);

                            }
                        }
                    });
                    Intent intent = new Intent(Login.this,UserDashboardActivity.class);
                    intent.putExtra("activityName","Login");
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(this, e -> Toast.makeText(Login.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show());
    }

    private void checkCredentials() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (email.isEmpty() || !email.contains("@")) {
            showError(inputEmail, "Email is not valid!");
        } else if (password.isEmpty() || password.length() < 7) {
            showError(inputPassword, "Password must be 7 character");
        } else {
            mLoadingBar.setTitle("Login");
            mLoadingBar.setMessage("Please wait while check your credentials");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mLoadingBar.dismiss();
                        Intent intent = new Intent(Login.this, UserDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                        intent.putExtra("activityName","Login");

                        startActivity(intent);

                    } else {
                        Toast.makeText(Login.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        mLoadingBar.dismiss();
                    }
                }
            });
        }
    }
    private void updateUserInfoGoogleSignIn(GoogleSignInAccount acct){
        mLoadingBar.setMessage("Saving your info");

        long timestamp = System.currentTimeMillis();

       /* String personGivenName = acct.getGivenName();
        String personFamilyName = acct.getFamilyName();
        String personId = acct.getId();*/
        String personEmail = acct.getEmail();
        String personName = acct.getDisplayName();
        Uri personPhoto = acct.getPhotoUrl();
        System.out.println(personName);
        System.out.println(personEmail);

        String uid = mAuth.getUid();

        HashMap<String, Object> userData = new HashMap<>();



        userData.put("uid", uid);
        userData.put("email", personEmail);
        userData.put("username", personName);
        userData.put("profileImage",personPhoto);
        userData.put("userType","user");
        userData.put("timestamp",timestamp);


        db.collection("Users").document(uid).set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        mLoadingBar.dismiss();
                        Toast.makeText(Login.this, "Logging in...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        mLoadingBar.dismiss();
                    }
                });
    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();

    }
}