package com.example.mobilibrary;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilibrary.Activity.ProfileActivity;
import com.example.mobilibrary.DatabaseController.RequestService;
import com.example.mobilibrary.DatabaseController.aRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.MyViewHolder> {
    private ArrayList<aRequest> mRequests;
    private Context mContext;
    private RequestService requestService;

    public RequestAdapter(Context context, ArrayList<aRequest> requests) {
        this.mContext = context;
        this.mRequests = requests;
        this.requestService = RequestService.getInstance();
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RequestAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_request_list, parent, false);

        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // get element from your dataset at this position
        // replace the contents of the view with that element
        System.out.println(mRequests);
        holder.requester.setText(mRequests.get(position).getRequester());

        holder.requester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra("profile", holder.requester.getText().toString());
                mContext.startActivity(intent);
            }
        });

        //clicks listener
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               requestService.acceptRequest(mRequests.get(position))
                       .addOnCompleteListener(task -> {
                           if (task.isSuccessful()) {
                               Toast.makeText(mContext, "Successfully accepted request from" + mRequests.get(position).getRequester(), Toast.LENGTH_SHORT).show();
                               if (mRequests.size() > 0) {
                                   List<String> deletedIDs = new ArrayList<>();
                                   for (int i = 0; i < mRequests.size(); i++) {
                                       deletedIDs.add(mRequests.get(i).getID());
                                   }
                                   requestService.deleteAll(deletedIDs).addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void aVoid) {
                                           Log.e("BookDetailsFragment", "Successfully deletes al requests");
                                       }
                                   }).addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           Log.e("BookDatailsFragment", "Failed to delete requests: " + e.toString());
                                       }
                                   });
                               }
                               notifyDataSetChanged();
                           } else {
                               Toast.makeText(mContext, "Failed to accept the request", Toast.LENGTH_SHORT).show();
                           }

                       });
           }
       });


        holder.declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestService.decline(mRequests.get(position).getID())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(mContext, "Succesfully declined request", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                            } else {
                                Toast.makeText(mContext, "Failed to decline the request", Toast.LENGTH_SHORT).show();
                            }
                        });


            }
        });

    }



    @Override
    public int getItemCount() {
        return (mRequests == null) ? 0 : mRequests.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView requester;
        public Button acceptButton;
        public Button declineButton;
        public MyViewHolder(View itemView) {
            super(itemView);
            requester = (TextView)itemView.findViewById(R.id.textview_requester);
            acceptButton = (Button)itemView.findViewById(R.id.accept_button);
            declineButton = (Button)itemView.findViewById(R.id.decline_button);
        }
    }
}
