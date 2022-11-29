package com.example.mushroommanager;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class TicketAdapter extends FirestoreRecyclerAdapter<TicketModel, TicketAdapter.TicketHolder> {


    public TicketAdapter(@NonNull FirestoreRecyclerOptions<TicketModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TicketHolder holder, int position, @NonNull TicketModel model) {

        holder.mushroomType.setText(model.getMushroomType());
        holder.dateCreated.setText(model.getDateCreated());
        holder.creatorName.setText(model.getCreatorName());
       // Picasso.get().load(model.getImage1()).resize(80,80).into(holder.imageView);
        String documentId = getSnapshots().getSnapshot(position).getId();
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Intent intent = new Intent(v.getContext(), OwnTicket.class);
                intent.putExtra("actualTicketID",documentId);
                v.getContext().startActivity(intent);
            }
        });

    }

    @NonNull
    @Override
    public TicketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tickets,
                parent,false);
        return new TicketHolder(v);
    }



    class TicketHolder extends RecyclerView.ViewHolder{

        TextView mushroomType;
        TextView creatorName;
        TextView dateCreated;
        CircleImageView imageView;
        CardView layout;


        public TicketHolder(@NonNull View itemView) {
            super(itemView);
            mushroomType=itemView.findViewById(R.id.mushroomType);
            creatorName=itemView.findViewById(R.id.creatorName);
            dateCreated=itemView.findViewById(R.id.dateCreated);
            imageView=itemView.findViewById(R.id.imageView);
            layout=itemView.findViewById(R.id.cardViewLayout);

        }
    }

}
