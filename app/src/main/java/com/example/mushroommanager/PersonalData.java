package com.example.mushroommanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonalData extends AppCompatActivity {

    Button changePassword,cancelBtn,saveBtn;
    EditText inputUsername,inputEmail;

    String userId;
    FirebaseAuth fAuth;
    FirebaseUser user;
    FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

        changePassword = findViewById(R.id.changePassword);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
        inputUsername = findViewById(R.id.inputUsername);
        inputEmail = findViewById(R.id.inputEmail);

        fStore=FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = inputUsername.getText().toString();
                String email = inputEmail.getText().toString();

                if (username.equals("") && email.equals("")){
                    Intent intent = new Intent(PersonalData.this,UserDashboardActivity.class);
                    startActivity(intent);
                    Toast.makeText(PersonalData.this, "Nothing has changed", Toast.LENGTH_SHORT).show();
                    
                }else if (!username.equals("") && email.equals("")){

                    DocumentReference documentReference = fStore.collection("Users").document(userId);
                    documentReference.update("username",username).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Intent intent = new Intent(PersonalData.this,UserDashboardActivity.class);
                            startActivity(intent);
                            Toast.makeText(PersonalData.this, "Username Successfully changed!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Intent intent = new Intent(PersonalData.this,UserDashboardActivity.class);
                            startActivity(intent);
                            Toast.makeText(PersonalData.this, "Something went wrong! Username has not changed!", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else if(username.equals("") && !email.equals("")){
                    user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Intent intent = new Intent(PersonalData.this,UserDashboardActivity.class);
                            startActivity(intent);
                            Toast.makeText(PersonalData.this, "Email Successfully changed!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Intent intent = new Intent(PersonalData.this,UserDashboardActivity.class);
                            startActivity(intent);
                            Toast.makeText(PersonalData.this, "Something went wrong! Email has not changed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    DocumentReference documentReference = fStore.collection("Users").document(userId);
                    documentReference.update("username",username).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(PersonalData.this, "Username Successfully changed!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Intent intent = new Intent(PersonalData.this,UserDashboardActivity.class);
                            startActivity(intent);
                            Toast.makeText(PersonalData.this, "Something went wrong! Username has not changed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(PersonalData.this, "Email Successfully changed!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Intent intent = new Intent(PersonalData.this,UserDashboardActivity.class);
                            startActivity(intent);
                            Toast.makeText(PersonalData.this, "Something went wrong! Email has not changed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent intent = new Intent(PersonalData.this,UserDashboardActivity.class);
                    startActivity(intent);
                    Toast.makeText(PersonalData.this, "Email and Username Successfully changed!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalData.this,UserDashboardActivity.class);
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalData.this,ChangePassword.class);
                startActivity(intent);
            }
        });
    }
}