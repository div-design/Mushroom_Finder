package com.example.mushroommanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class OtherTicket extends AppCompatActivity {
    Button backBtn, ConfirmBtn, RejectBtn;

    TextView dateCreatedTicket,mushroomNameTicket,creatorNameTicket, rejectCount,confirmCount;
    ImageView mushroomImage1, mushroomImage2;
    FirebaseFirestore fStore;

    String userId, actualTicketID;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_ticket);


        mushroomNameTicket =findViewById(R.id.mushroomNameTicket);
        dateCreatedTicket = findViewById(R.id.dateCreatedTicket);
        creatorNameTicket = findViewById(R.id.creatorNameTicket);
        ConfirmBtn = findViewById(R.id.ConfirmBtn);
        RejectBtn = findViewById(R.id.RejectBtn);

        mushroomImage1 = findViewById(R.id.mushroomImage1);
        mushroomImage2 = findViewById(R.id.mushroomImage2);

        rejectCount = findViewById(R.id.rejectCount);
        confirmCount = findViewById(R.id.confirmCount);


        backBtn = findViewById(R.id.backBtn);

        fStore= FirebaseFirestore.getInstance();
        fAuth= FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        actualTicketID = getIntent().getStringExtra("actualTicketID");


        Fragment fragment = new MapMushroomSpotFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("actualTicketID",actualTicketID);
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.frameFinalSpotLayout,fragment).commit();







        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherTicket.this, ExploreTickets.class);
                startActivity(intent);
            }
        });


        DocumentReference documentReference = fStore.collection("Tickets").document(actualTicketID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {


                //mushroomNameTicket.setText(value.getString("mushroomType"));
                creatorNameTicket.setText(value.getString("creatorName"));
                dateCreatedTicket.setText(value.getString("dateCreated"));
                ArrayList<String> confirmList = ( ArrayList<String>) value.get("confirmList");
                ArrayList<String> rejectList = ( ArrayList<String>) value.get("refuseList");
                String confirmC ="(" + Integer.toString(confirmList.size()) + ")";
                String rejectC ="(" + Integer.toString(rejectList.size()) + ")";
                rejectCount.setText(rejectC);
                confirmCount.setText(confirmC);
                checkConRej();

                ConfirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int conCount=0,rejCount=0;
                        if (confirmList.contains(userId)){
                            confirmList.remove(userId);
                            documentReference.update("confirmList", FieldValue.arrayRemove(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    System.out.println("Success");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Fail");
                                }
                            });

                        }else if( !confirmList.contains(userId)  &&   rejectList.contains(userId))
                        {

                            rejectList.remove(userId);
                            confirmList.add(userId);
                            documentReference.update("confirmList", FieldValue.arrayUnion(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    System.out.println("Success");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Fail");
                                }
                            });

                            documentReference.update("refuseList", FieldValue.arrayRemove(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    System.out.println("Success");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Fail");
                                }
                            });



                        }else
                        {
                            confirmList.add(userId);
                            documentReference.update("confirmList", FieldValue.arrayUnion(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    System.out.println("Success");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Fail");
                                }
                            });
                        }
                        //////




                        /////

                        if (confirmList.isEmpty()){
                            conCount = 0;
                        }else{
                            conCount = confirmList.size();
                        }


                        String rejectC ="(" + Integer.toString(rejCount) + ")";
                        String confirmC ="(" + Integer.toString(conCount) + ")";
                        rejectCount.setText(rejectC);
                        confirmCount.setText(confirmC);
                        if (conCount >= 3 ){
                            // mushroomNameTicket.setText(value.getString("mushroomType") + "(Confirmed)");
                            documentReference.update("ConRej","Con");
                            //  mushroomNameTicket.setTextColor(Color.parseColor("#3CB043"));
                        }else if(rejCount >= 3){
                            // mushroomNameTicket.setText(value.getString("mushroomType") + "(Rejected)");
                            documentReference.update("ConRej","Rej");
                            // mushroomNameTicket.setTextColor(Color.parseColor("#FD6D7E"));

                        }else{
                            documentReference.update("ConRej","Basic");
                            // mushroomNameTicket.setText(value.getString("mushroomType"));
                            // mushroomNameTicket.setTextColor(Color.parseColor("#3CB043"));

                        }

                        checkConRej();


                    }
                });

                RejectBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int conCount=0,rejCount=0;
                        if (rejectList.contains(userId)){
                            rejectList.remove(userId);
                            documentReference.update("refuseList", FieldValue.arrayRemove(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    System.out.println("Success");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Fail");
                                }
                            });

                        }else if( !rejectList.contains(userId)  &&   confirmList.contains(userId))
                        {

                            confirmList.remove(userId);
                            rejectList.add(userId);
                            documentReference.update("refuseList", FieldValue.arrayUnion(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    System.out.println("Success");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Fail");
                                }
                            });

                            documentReference.update("confirmList", FieldValue.arrayRemove(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    System.out.println("Success");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Fail");
                                }
                            });



                        }else
                        {
                            rejectList.add(userId);
                            documentReference.update("refuseList", FieldValue.arrayUnion(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    System.out.println("Success");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Fail");
                                }
                            });
                        }
                        //////




                        /////

                        if (rejectList.isEmpty()){
                            rejCount = 0;
                        }else{
                            rejCount = rejectList.size();
                        }


                        String rejectC ="(" + Integer.toString(rejCount) + ")";
                        String confirmC ="(" + Integer.toString(conCount) + ")";
                        rejectCount.setText(rejectC);
                        confirmCount.setText(confirmC);

                        if (conCount >= 3 ){
                            // mushroomNameTicket.setText(value.getString("mushroomType") + "(Confirmed)");
                            documentReference.update("ConRej","Con");
                            //  mushroomNameTicket.setTextColor(Color.parseColor("#3CB043"));
                        }
                        else if(rejCount >= 3){
                            // mushroomNameTicket.setText(value.getString("mushroomType") + "(Rejected)");
                            documentReference.update("ConRej","Rej");
                            // mushroomNameTicket.setTextColor(Color.parseColor("#FD6D7E"));

                        }else{
                            documentReference.update("ConRej","Basic");
                            // mushroomNameTicket.setText(value.getString("mushroomType"));
                            // mushroomNameTicket.setTextColor(Color.parseColor("#3CB043"));

                        }
                        checkConRej();



                    }
                });








                Picasso.get().load(R.drawable.gomba_search).resize(300,300).into(mushroomImage1);
                Picasso.get().load(R.drawable.gomba_search).resize(300,300).into(mushroomImage2);

                if (value.getString("image1")!="" )
                    Picasso.get().load(value.getString("image1")).resize(300,300).into(mushroomImage1);
                if (value.getString("image2")!="")
                    Picasso.get().load(value.getString("image2")).resize(300,300).into(mushroomImage2);
            }
        });



    }

    private void checkConRej() {
        DocumentReference documentReference = fStore.collection("Tickets").document(actualTicketID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.getString("ConRej")=="Con"){
                    mushroomNameTicket.setText(value.getString("mushroomType") + "(Confirmed)");
                     mushroomNameTicket.setTextColor(Color.parseColor("#3CB043"));

                }else if(value.getString("ConRej")=="Rej"){
                     mushroomNameTicket.setText(value.getString("mushroomType") + "(Rejected)");
                     mushroomNameTicket.setTextColor(Color.parseColor("#FD6D7E"));

                }else{
                     mushroomNameTicket.setText(value.getString("mushroomType"));
                     mushroomNameTicket.setTextColor(Color.parseColor("#464654"));
                }
            }
        });
    }


    @Override
    public void onBackPressed() {

        Intent intent = new Intent(OtherTicket.this, ExploreTickets.class);
        startActivity(intent);
    }
}