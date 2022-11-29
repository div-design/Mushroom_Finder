package com.example.mushroommanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ExploreTickets extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    CollectionReference ticketRef = db.collection("Tickets");

    String userId = fAuth.getCurrentUser().getUid();
    OtherTicketAdapter adapter;
    FloatingActionButton btnCreateTicket;

    Button AllMushrooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_tickets);
        btnCreateTicket = findViewById(R.id.btnCreateTicket);
        AllMushrooms = findViewById(R.id.AllMushrooms);

        AllMushrooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExploreTickets.this,AllMushrooms.class);
                startActivity(intent);
            }
        });


        btnCreateTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ExploreTickets.this,CreateTicket.class);

                startActivity(intent);
            }
        });
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = ticketRef.whereNotEqualTo("creatorID",userId);

        FirestoreRecyclerOptions<TicketModel> options = new FirestoreRecyclerOptions.Builder<TicketModel>()
                .setQuery(query, TicketModel.class)
                .build();

        adapter = new OtherTicketAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


    }
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(ExploreTickets.this, UserDashboardActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}