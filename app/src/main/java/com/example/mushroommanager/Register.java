package com.example.mushroommanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class Register extends AppCompatActivity {

    TextView goToLoginBtn;

    private EditText inputUsername, inputEmail, inputPassword, inputConfirmPasssword;
    Button registerBtn;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoadingBar;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        goToLoginBtn= findViewById(R.id.textToLogin);
        inputUsername=findViewById(R.id.inputUsername);
        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        inputConfirmPasssword=findViewById(R.id.inputConfirmPassword);
        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        mLoadingBar = new ProgressDialog(Register.this);

        registerBtn=findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCredentials();
            }
        });



        goToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this,Login.class));
            }
        });
    }

    private void checkCredentials() {
        String username = inputUsername.getText().toString();
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPasssword.getText().toString();

        if(username.isEmpty() || username.length()<7){
            showError(inputUsername,"Your username is not valid!");
        }
        else if (email.isEmpty() || !email.contains("@")){
            showError(inputEmail,"Email is not valid!");
        }else if (password.isEmpty() || password.length()<7){
            showError(inputPassword,"Password must be 7 character");
        }else if (confirmPassword.isEmpty() || !confirmPassword.equals(password)){
            showError(inputConfirmPasssword,"Password not match");
        }else{
            mLoadingBar.setTitle("Registration");
            mLoadingBar.setMessage("Please wait, white check your credentials");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();


            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        mLoadingBar.dismiss();
                        Toast.makeText(Register.this, "Succesfully Registration", Toast.LENGTH_SHORT).show();

                        updateUserInfo();
                        Intent intent = new Intent(Register.this, UserDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }else{
                        mLoadingBar.dismiss();
                        Toast.makeText(Register.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
    }

    private void updateUserInfo(){
        mLoadingBar.setMessage("Saving your info");

        long timestamp = System.currentTimeMillis();



        String uid = mAuth.getUid();
        String email = inputEmail.getText().toString();
        String username = inputUsername.getText().toString();



        HashMap<String, Object> userData = new HashMap<>();


        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("username", username);
        userData.put("profileImage","");
        userData.put("userType","user");
        userData.put("timestamp",timestamp);

        db.collection("Users").document(uid).set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        mLoadingBar.dismiss();
                        Toast.makeText(Register.this, "Account creating...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mLoadingBar.dismiss();
                        Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();

    }
}