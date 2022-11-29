package com.example.mushroommanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    TextView AlertMessage;
    EditText inputNewPassword,inputConfirmNewPassword;
    Button doneChangePassword,cancelChangePassword;

    String userId;
    FirebaseAuth fAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        inputNewPassword = findViewById(R.id.inputNewPassword);
        inputConfirmNewPassword = findViewById(R.id.inputConfirmNewPassword);
        doneChangePassword = findViewById(R.id.doneChangePassword);
        cancelChangePassword = findViewById(R.id.cancelChangePassword);
        AlertMessage = findViewById(R.id.AlertMessage);

        fAuth = FirebaseAuth.getInstance();

        userId = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();

        doneChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newPass = inputNewPassword.getText().toString();
                String confNewPass = inputConfirmNewPassword.getText().toString();

                if (newPass.equals(confNewPass) && !newPass.equals("")){

                    user.updatePassword(newPass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ChangePassword.this, "Password Reset Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ChangePassword.this,PersonalData.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChangePassword.this, "Password Reset Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    AlertMessage.setText("Something went wrong. Please check the passwords!");
                }
            }
        });

        cancelChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangePassword.this,PersonalData.class);
                startActivity(intent);
            }
        });

    }
}