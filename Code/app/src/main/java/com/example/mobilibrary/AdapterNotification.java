package com.example.mobilibrary;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilibrary.Activity.ProfileActivity;
import com.example.mobilibrary.DatabaseController.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

/**
 * @author ;
 * This class is to display all the notifications a user has.
 */

/* TYPES OF NOTIFICATIONS
1) Another user has requested your book -> lead to book details
2) User has declined your request
3) User has accepted your request -> lead to map with already-picked location to meet
4) Location the user/borrower has selected to meet with the user/borrower has been sent -> lead to map with already picked location
5) The borrower is ready to return back User's book -> lead to map with already-picked location
 */

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotification> {

    private Context context;
    private ArrayList<ModelNotification> notificationsList;

    public AdapterNotification(Context context, ArrayList<ModelNotification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
    }


    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate view notifications_rows
        View view = LayoutInflater.from(context).inflate(R.layout.notifications_rows, parent, false);
        return new HolderNotification(view, viewType);

    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotification holder, int position){
        //get and set data to views

        //set data
        ModelNotification model = notificationsList.get(position);
        holder.userName.setText(model.getUser());
        holder.notification.setText(model.getNotification());

        //get type, change views depending on the type
        Integer type = Integer.parseInt(model.getType());
        if (type == 1) {
            holder.notifications.setOnClickListener(new View.OnClickListener() { //if this notification is clicked, will lead to owners own book details
                @Override
                public void onClick(View v) {

                    //Lead to the book details (of your own book)
                    String fsID = notificationsList.get(position).getBookFSID();

                    //get User object of user of the clicked book
                    String bookOwner = notificationsList.get(position).getOtherUser();
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("Users").document(bookOwner);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            String username = Objects.requireNonNull(document.get("username")).toString();
                            String email = Objects.requireNonNull(document.get("email")).toString();
                            String name = Objects.requireNonNull(document.get("name")).toString();
                            String phoneNo = Objects.requireNonNull(document.get("phoneNo")).toString();

                            User user = new User(username, email, name, phoneNo);

                            initIntent(user);
                        }
                        public void initIntent(User user){
                            //get the book details of currently clicked item
                            final FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference docRef = db.collection("Books").document(fsID);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document = task.getResult();
                                    String title = Objects.requireNonNull(document.get("Title")).toString();
                                    String isbn = Objects.requireNonNull(document.get("ISBN")).toString();
                                    String author = Objects.requireNonNull(document.get("Author")).toString();
                                    String status = Objects.requireNonNull(document.get("Status")).toString();
                                    //String image = Objects.requireNonNull(document.get("imageID")).toString();
                                    String image;
                                    try {
                                        image = Objects.requireNonNull(document.get("imageID")).toString();
                                    }
                                    catch(Exception e) {
                                        image = "";
                                    }


                                    Book clickedBook = new Book(fsID, title, isbn, author, status, image, user);
                                    Intent viewBook = new Intent(context, BookDetailsFragment.class);
                                    viewBook.putExtra("view book", clickedBook);
                                    context.startActivity(viewBook);

                                }
                            });

                        }

                    });

                }
            });
        }
        if (type == 2) {
            holder.notifications.setCardBackgroundColor(Color.parseColor("#e6576a")); //red border
            holder.arrow.setVisibility(View.INVISIBLE);
        }
        if (type == 3) { //meetmap confirm leads to bookdetails, need bookfsid
            holder.notifications.setCardBackgroundColor(Color.parseColor("#57e65c")); //green border
            holder.notifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get the latitude and longitude of the book
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("Books").document(notificationsList.get(position).getBookFSID());
                    System.out.println("Got doc ref");
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                System.out.println(document);
                                if (document.exists()) {
                                    System.out.println("About to get latlang");
                                    //bookLatLng = (LatLng) document.getData().get("LatLang");

                                    HashMap<String, Object> newData = (HashMap<String, Object>) document.getData().get("LatLang");
                                    System.out.println("NEW DATA: " + newData);
                                    Object latitude = newData.get("latitude");
                                    Object longitude = newData.get("longitude");
                                    Double dLatitude = new Double(latitude.toString());
                                    Double dLongitude = new Double(longitude.toString());

                                    //leads to a map with the location of the book
                                    Intent mapIntent = new Intent(context, meetMap.class);
                                    Bundle b = new Bundle();
                                    b.putDouble("latitude", dLatitude);
                                    b.putDouble("longitude", dLongitude);
                                    b.putString("bookFSID", notificationsList.get(position).getBookFSID());
                                    b.putString("bookOwner", notificationsList.get(position).getUser());
                                    b.putInt("type", type);
                                    mapIntent.putExtras(b);
                                    ((Activity) context).startActivityForResult(mapIntent,1);

                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });

                }


            });

        }
        if (type == 4) { //meet map confirm leads back to notifications
            holder.notifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //leads to a map with the location of the book
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("Books").document(notificationsList.get(position).getBookFSID());
                    System.out.println("Got doc ref");
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                System.out.println(document);
                                if (document.exists()) {
                                    System.out.println("About to get latlang");
                                    //bookLatLng = (LatLng) document.getData().get("LatLang");

                                    HashMap<String, Object> newData = (HashMap<String, Object>) document.getData().get("LatLang");
                                    System.out.println("NEW DATA: " + newData);
                                    Object latitude = newData.get("latitude");
                                    Object longitude = newData.get("longitude");
                                    Double dLatitude = new Double(latitude.toString());
                                    Double dLongitude = new Double(longitude.toString());

                                    //leads to a map with the location of the book
                                    Intent mapIntent = new Intent(context, meetMap.class);
                                    Bundle b = new Bundle();
                                    b.putDouble("latitude", dLatitude);
                                    b.putDouble("longitude", dLongitude);
                                    mapIntent.putExtras(b);
                                    ((Activity) context).startActivityForResult(mapIntent,1);

                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                }
            });

        }
        if (type == 5) { //meetmap confirm leads to bookdetails, need bookfsid
            holder.notifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //leads to a map with the location of the book
                    //get the latitude and longitude of the book
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("Books").document(notificationsList.get(position).getBookFSID());
                    System.out.println("Got doc ref");
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                System.out.println(document);
                                if (document.exists()) {
                                    System.out.println("About to get latlang");
                                    //bookLatLng = (LatLng) document.getData().get("LatLang");

                                    HashMap<String, Object> newData = (HashMap<String, Object>) document.getData().get("LatLang");
                                    System.out.println("NEW DATA: " + newData);
                                    Object latitude = newData.get("latitude");
                                    Object longitude = newData.get("longitude");
                                    Double dLatitude = new Double(latitude.toString());
                                    Double dLongitude = new Double(longitude.toString());

                                    //leads to a map with the location of the book
                                    Intent mapIntent = new Intent(context, meetMap.class);
                                    Bundle b = new Bundle();
                                    b.putDouble("latitude", dLatitude);
                                    b.putDouble("longitude", dLongitude);
                                    b.putString("bookFSID", notificationsList.get(position).getBookFSID());
                                    b.putString("bookOwner", notificationsList.get(position).getUser());
                                    b.putInt("type", type);
                                    mapIntent.putExtras(b);
                                    ((Activity) context).startActivityForResult(mapIntent,1);

                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                }
            });
        }

        //onclick for username
        holder.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("profile", model.getUser());
                context.startActivity(intent);
            }
        });

        //long press to show delete notification option
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this notification?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete notification
                        final FirebaseFirestore db = FirebaseFirestore.getInstance();
                        System.out.println(model.getBookFSID());
                        System.out.println(model.getNotification());
                        System.out.println(model.getOtherUser());
                        System.out.println(model.getType());
                        System.out.println(model.getUser());
                        db.collection("Users").document(model.getOtherUser()).collection("Notifications")
                                .whereEqualTo("notification", model.getNotification())
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
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    /**
     * Create viewholder classes for each type of notificiation
     */
    class HolderNotification extends RecyclerView.ViewHolder {

        CircleImageView profilePic;
        TextView userName;
        TextView notification;
        TextView arrow;
        CardView notifications;

        public HolderNotification(@NonNull View itemView, int viewType) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profile_pic);
            userName = itemView.findViewById(R.id.notifications_username);
            notification = itemView.findViewById(R.id.notifications_text);
            arrow = itemView.findViewById(R.id.arrow);
            notifications = itemView.findViewById(R.id.notifications_border);
            notifications.setCardBackgroundColor(Color.parseColor("#00000000")); //border is invisible by default
        }
    }

}

