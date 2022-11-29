package com.example.mushroommanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

public class OwnTicket extends AppCompatActivity {

    Button deleteTicketBtn, backBtn;

    TextView dateCreatedTicket,mushroomNameTicket,creatorNameTicket;
    ImageView mushroomImage1, mushroomImage2;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        mushroomNameTicket =findViewById(R.id.mushroomNameTicket);
        dateCreatedTicket = findViewById(R.id.dateCreatedTicket);
        creatorNameTicket = findViewById(R.id.creatorNameTicket);

        mushroomImage1 = findViewById(R.id.mushroomImage1);
        mushroomImage2 = findViewById(R.id.mushroomImage2);

        deleteTicketBtn = findViewById(R.id.deleteTicketBtn);
        backBtn = findViewById(R.id.backBtn);

        fStore= FirebaseFirestore.getInstance();

        String actualTicketID = getIntent().getStringExtra("actualTicketID");


        Fragment fragment = new MapMushroomSpotFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("actualTicketID",actualTicketID);
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.frameFinalSpotLayout,fragment).commit();

        deleteTicketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fStore.collection("Tickets").document(actualTicketID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(OwnTicket.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OwnTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent(OwnTicket.this, OwnTickets.class);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OwnTicket.this, OwnTickets.class);
                startActivity(intent);
            }
        });


        DocumentReference documentReference = fStore.collection("Tickets").document(actualTicketID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                mushroomNameTicket.setText(value.getString("mushroomType"));
                creatorNameTicket.setText(value.getString("creatorName"));
                dateCreatedTicket.setText(value.getString("dateCreated"));
                Picasso.get().load(R.drawable.gomba_search).resize(300,300).into(mushroomImage1);
                Picasso.get().load(R.drawable.gomba_search).resize(300,300).into(mushroomImage2);

                if (value.getString("image1")!="" )
                    Picasso.get().load(value.getString("image1")).resize(300,300).into(mushroomImage1);
                if (value.getString("image2")!="")
                    Picasso.get().load(value.getString("image2")).resize(300,300).into(mushroomImage2);
            }
        });



    }
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(OwnTicket.this, OwnTickets.class);
        startActivity(intent);
    }
}

