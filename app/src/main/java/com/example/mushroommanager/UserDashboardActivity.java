package com.example.mushroommanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;
import com.opencsv.CSVReader;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDashboardActivity extends AppCompatActivity {


    ImageView profilePic;
    TextView username,wrongIDMessage,expertType;
    String userId;
    String userType="";

    Button logoutBtn;
    Button exploreBtn;
    Button createTicketBtn;
    Button ownTicketsBtn;
    Button nopeBtn;
    Button expertBtn;
    Button changeDataBtn;
    Button exploreBtn2;


    FirebaseAuth fAuth;
    FirebaseFirestore fStore;


    private EditText inputExpertID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      /*  LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.mushroom_expert_popup,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setView(view).create();
        dialog.show();*/



        profilePic = findViewById(R.id.profilePic);
        username = (TextView) findViewById(R.id.username);

        expertType = findViewById(R.id.expertType);
        logoutBtn=findViewById(R.id.logoutBtn);
        exploreBtn=findViewById(R.id.ticketsList);
        exploreBtn2= findViewById(R.id.ticketsList2);
        createTicketBtn=findViewById(R.id.createTicket);
        ownTicketsBtn=findViewById(R.id.ownTicketsList);
        changeDataBtn=findViewById(R.id.changeData);


        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();



        checkUser();

        changeDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this,PersonalData.class);
                startActivity(intent);
            }
        });

        ownTicketsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this,OwnTickets.class);
                startActivity(intent);
            }
        });

        exploreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this, ExploreTickets.class);
                startActivity(intent);

            }
        });
        exploreBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this, ExploreTickets.class);
                startActivity(intent);
            }
        });

        createTicketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this,CreateTicket.class);
                startActivity(intent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fAuth.signOut();
                Intent intent = new Intent(UserDashboardActivity.this,Landing.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

   private void checkUser() {
        userId = fAuth.getCurrentUser().getUid();

          DocumentReference documentReference = fStore.collection("Users").document(userId);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    System.out.println(value.getString("username"));
                    String welcome = "Hello " + value.getString("username");
                    String usertype = "Rank: " + "User";
                    username.setText(welcome);


                    if (value.getString("profileImage")!=""){
                        Picasso.get().load(value.getString("profileImage")).into(profilePic);
                    }
                    if (value.getString("userType").equals("user")){
                        exploreBtn2.setVisibility(View.GONE);
                        expertType.setText(usertype);
                        expertType.setTextColor(Color.parseColor("#464654"));

                        showPopup();
                    }else if (value.getString("userType").equals("expert")) {
                        exploreBtn2.setVisibility(View.GONE);
                        expertType.setText("Rank: Expert");
                        expertType.setTextColor(Color.parseColor("#3CB043"));
                    }else if(value.getString("userType").equals("guest")){
                        expertType.setText("Rank: Guest");
                        exploreBtn.setVisibility(View.GONE);
                        createTicketBtn.setVisibility(View.GONE);
                        ownTicketsBtn.setVisibility(View.GONE);
                        changeDataBtn.setVisibility(View.GONE);
                        expertType.setTextColor(Color.parseColor("#464654"));

                    }else{
                        exploreBtn2.setVisibility(View.GONE);
                        expertType.setText(usertype);
                        expertType.setText("Rank: Expert");
                        showPopup();
                    }
                }
            });
    }

    private void Date(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        String finalDate =formatter.format(date).toString().split(" ")[0];
        System.out.println(finalDate);
    }

    private String checkActivity(){

        String activityName;

        if(getIntent().getStringExtra("activityName")!=null) {
            activityName =  "Login";
        }else {
            activityName =  "";
        }
        return activityName;
    }

    private  void showPopup(){

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.mushroom_expert_popup);

        if(checkActivity().equals("Login")) {
            dialog.show();
        }else{
            dialog.dismiss();
        }
        nopeBtn = dialog.findViewById(R.id.nopeBtn);
        expertBtn = dialog.findViewById(R.id.expertBtn);
        inputExpertID=dialog.findViewById(R.id.inputExpertID);
        wrongIDMessage = dialog.findViewById(R.id.wrongIDMessage);
        userId = fAuth.getCurrentUser().getUid();



        nopeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               dialog.dismiss();
            }
        });

        expertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = inputExpertID.getText().toString().replace("\n","").replace("-","").replace(" ","");

                InputStream inputStream = getResources().openRawResource(R.raw.nyilvantartas);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line="";
                Set<String> IDs = new HashSet<>();
                String[] record = null;
                while (true){
                    try {
                        if (!((line = reader.readLine()) != null)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    IDs.add(line.replace("\n","").replace("-","").replace(" ",""));
                }

                IDs.forEach(System.out::println);
                System.out.println("id: " + id);

                System.out.println(IDs.contains(id));
                if (IDs.contains(id)){

                    DocumentReference documentReference = fStore.collection("Users").document(userId);
                    documentReference.update("userType","expert").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(UserDashboardActivity.this, "UserType Successfully changed!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(UserDashboardActivity.this, "Something went wrong! UserType has not changed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    expertType.setText("Rank: Expert");
                    expertType.setTextColor(Color.parseColor("#3CB043"));
                    System.out.println("You are an expert " + id);
                    Toast.makeText(UserDashboardActivity.this, "You are an expert!", Toast.LENGTH_SHORT).show();

                    dialog.dismiss();

                }else {
                    System.out.println("You are not an expert " + id);
                    wrongIDMessage.setText("Wrong ID! Try Again!");
                }
            }
        });

    }

}