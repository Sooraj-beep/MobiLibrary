package com.example.mobilibrary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilibrary.DatabaseController.RequestService;
import com.example.mobilibrary.DatabaseController.aRequest;

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
                .inflate(R.layout.books_rows, parent, false);

        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // get element from your dataset at this position
        // replace the contents of the view with that element
        holder.requester.setText(mRequests.get(position).getRequester());

        //clicks listener
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestService.acceptRequest(mRequests.get(position))
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(mContext, "Successfully accepted request from" + mRequests.get(position).getRequester(), Toast.LENGTH_SHORT).show();
                                List<String> deletedIDs = new ArrayList<>();
                                for (int i = 0; i < mRequests.size(); i++) {
                                    if (i != position)
                                        deletedIDs.add(mRequests.get(i).getID());
                                }
                                RequestService.declineOthers(deletedIDs);
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


    public void clearData(){
        mRequests.clear();
        notifyDataSetChanged();
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
