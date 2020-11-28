package com.example.mobilibrary;

import android.app.Activity;
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
import com.example.mobilibrary.DatabaseController.User;
import com.example.mobilibrary.DatabaseController.aRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * @author Nguyen, Jill;
 * This is a request adapter to display all the requests grabbed from Firestore,
 * to the selected book .
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.MyViewHolder> {
    private ArrayList<aRequest> mRequests;
    private Context mContext;
    private RequestService requestService;
    private User user;

    /**
     *
     * @param context
     * @param requests
     */
    public RequestAdapter(Context context, ArrayList<aRequest> requests) {
        this.mContext = context;
        this.mRequests = requests;
        this.requestService = RequestService.getInstance();

    }

    /**
     * Create new views (invoked by the layout manager)
     * @param parent ViewGrou[
     * @param viewType Int type
     * @return
     */
    @NonNull
    @Override
    public RequestAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_request_list, parent, false);

        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     * @param holder the viewHolder we bind the data on
     * @param position global position
     */
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

                                //go to map activity
                                Intent mapIntent = new Intent(mContext, requestMap.class);
                                mapIntent.putExtra("bookID", mRequests.get(position).getBookID());
                                mapIntent.putExtra("otherUser", mRequests.get(position).getRequester());
                                ((Activity) mContext).startActivityForResult(mapIntent,1);

                                //delete rest of requests
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

                //send declined notification
                String requestor = mRequests.get(position).getRequester();
                String fireStoreID = mRequests.get(position).getBookID();
                //get book title and owner
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("Books").document(fireStoreID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        String title = document.getString("Title");
                        String notification = "Has declined your request for: " + title;
                        String currUser = document.getString("Owner");

                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("otherUser", requestor);
                        hashMap.put("user", currUser);
                        hashMap.put("notification", notification);
                        hashMap.put("type", "2");
                        hashMap.put("bookFSID", fireStoreID);

                        final FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Users").document(requestor).collection("Notifications").add(hashMap);

                        //delete the notification
                        db.collection("Users").document(currUser).collection("Notifications")
                                .whereEqualTo("notification", "Has requested to borrow your book: " + title)
                                .whereEqualTo("user", requestor)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                            doc.getReference().delete();
                                        }
                                    }
                                });


                    }


                });
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

    /**
     * This method overrides the default one with the list's count
     * @return 0 if the list is null , or total number of requests in the data set held by the adapter
     */
    @Override
    public int getItemCount() {
        return (mRequests == null) ? 0 : mRequests.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

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
