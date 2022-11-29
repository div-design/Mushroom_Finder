package com.example.mushroommanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Landing extends AppCompatActivity implements View.OnClickListener {
    private Button toLoginAsGuestBtn;
    private Button toLoginBtn;
    private Button toRegisterBtn;

    private FirebaseAuth mAuth;
    FirebaseFirestore db;

    ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        toLoginAsGuestBtn = findViewById(R.id.toLoginAsGuestBtn);
        toLoginBtn = findViewById(R.id.toLoginBtn);
        toRegisterBtn = findViewById(R.id.toRegisterBtn);
        mLoadingBar = new ProgressDialog(Landing.this);
        mAuth = FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        toLoginAsGuestBtn.setOnClickListener(this);
        toLoginBtn.setOnClickListener(this);
        toRegisterBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toLoginAsGuestBtn:
                checkCredentials();
                break;
            case R.id.toLoginBtn:
                Intent intent1 = new Intent(this,Login.class);
                startActivity(intent1);
                break;
            case R.id.toRegisterBtn:
                Intent intent2 = new Intent(this,Register.class);
                startActivity(intent2);
                break;
        }
    }

    private void checkCredentials() {
        String email = "mushroomvendeg@gmail.com";
        String password = "1234567";


            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mLoadingBar.dismiss();
                        Intent intent = new Intent(Landing.this, UserDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);

                    } else {
                        Toast.makeText(Landing.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        mLoadingBar.dismiss();
                    }
                }
            });
        }
    }
