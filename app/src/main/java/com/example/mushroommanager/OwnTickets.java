package com.example.mushroommanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaRouter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class OwnTickets extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    CollectionReference ticketRef = db.collection("Tickets");

    String userId = fAuth.getCurrentUser().getUid();
    TicketAdapter adapter;
    FloatingActionButton btnCreateTicket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_tickets);

        btnCreateTicket = findViewById(R.id.btnCreateTicket);

        btnCreateTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(OwnTickets.this,CreateTicket.class);

                startActivity(intent);
            }
        });
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = ticketRef.whereEqualTo("creatorID",userId);

        FirestoreRecyclerOptions<TicketModel> options = new FirestoreRecyclerOptions.Builder<TicketModel>()
                .setQuery(query, TicketModel.class)
                .build();

        adapter = new TicketAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(OwnTickets.this, UserDashboardActivity.class);
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