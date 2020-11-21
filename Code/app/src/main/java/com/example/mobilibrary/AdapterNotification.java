package com.example.mobilibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilibrary.DatabaseController.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.oned.ITFReader;

import java.util.ArrayList;
import java.util.Objects;


/* TYPES OF NOTIFICATIONS
1) Another user has requested your book -> lead to book details
2) User has declined your request
3) User has accepted your request -> lead to map with already-picked location to meet
4) Location the user/borrower has selected to meet with the user/borrower has been sent -> lead to map with already picked location
5) The borrower is ready to return back User's book -> lead to map with already-picked location
 */

public class AdapterNotification extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<ModelNotification> notificationsList;

    public AdapterNotification(Context context, ArrayList<ModelNotification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
    }

    @Override
    public int getItemViewType(int type) {
        return type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate view notifications_rows
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.notifications_rows, parent, false);
            return new typeOne(view);
        }
        if (viewType == 2) {
            view = LayoutInflater.from(context).inflate(R.layout.notifications_rows, parent, false);
            return new typeTwo(view);
        }
        if (viewType == 3) {
            view = LayoutInflater.from(context).inflate(R.layout.notifications_rows, parent, false);
            return new typeThree(view);
        }
        if (viewType == 4) {
            view = LayoutInflater.from(context).inflate(R.layout.notifications_rows, parent, false);
            return new typeFour(view);
        }
        if (viewType == 5){
            view = LayoutInflater.from(context).inflate(R.layout.notifications_rows, parent, false);
            return new typeFive(view);
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.notifications_rows, parent, false);
            return new typeOne(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //get type
        String type = notificationsList.get(position).getType();
        Integer iType = Integer.parseInt(type);

        if (iType == 1) {
            ((typeOne) holder).setOneDetails(notificationsList.get(position));
            ((typeOne) holder).notifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Notification type 1 Clicked");
                    System.out.println("Book Firestore ID: " + notificationsList.get(position).getBookFSID());

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
        if(iType == 2) {
            ((typeTwo) holder).setTwoDetails(notificationsList.get(position));
            //onClick will do nothing
        }

        //*Since type 3, 4, and 5 are the same could combine them? Depending on how the geolocation works.
        if (iType == 3) {
            ((typeThree) holder).setThreeDetails(notificationsList.get(position));
            ((typeThree) holder).notifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Notification type 3 Clicked");
                    //Lead to map with already picked location to meet
                }
            });
        }
        if (iType == 4) {
            ((typeFour) holder).setFourDetails(notificationsList.get(position));
            ((typeFour) holder).notifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Notification type 4 Clicked");
                    //Lead to map with already picked lozation to meet
                }
            });
        }
        else if (iType == 5) {
            ((typeFive) holder).setFiveDetails(notificationsList.get(position));
            ((typeFive) holder).notifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Notification type 5 Clicked");
                    //Lead to map with already picked location to meet
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }


    //Create viewholder classes for each type of notificiation
    class typeOne extends RecyclerView.ViewHolder { //someone has requested to borrow your book, arrow is visible, border is not there (color=background)

        FloatingActionButton profilePic;
        TextView userName;
        TextView notification;
        TextView arrow;
        CardView notifications;

        typeOne(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profile_pic);
            userName = itemView.findViewById(R.id.notifications_username);
            notification = itemView.findViewById(R.id.notifications_text);
            arrow = itemView.findViewById(R.id.arrow);
            notifications = itemView.findViewById(R.id.notifications_border);
            notifications.setCardBackgroundColor(Color.parseColor("#00000000")); //border is invisible by default
        }

        public void setOneDetails(ModelNotification modelNotification) {
            userName.setText(modelNotification.getUser());
            notification.setText(modelNotification.getNotification());
        }
    }

    //class typeTwo
    class typeTwo extends RecyclerView.ViewHolder { //User has declined your request, border is red, no arrow

        FloatingActionButton profilePic;
        TextView userName;
        TextView notification;
        CardView notifications;

        typeTwo(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profile_pic);
            userName = itemView.findViewById(R.id.notifications_username);
            notification = itemView.findViewById(R.id.notifications_text);
            notifications = itemView.findViewById(R.id.notifications_border);
            notifications.setCardBackgroundColor(Color.parseColor("#e6576a")); //border is red
        }

        public void setTwoDetails(ModelNotification modelNotification) {
            userName.setText(modelNotification.getUser());
            notification.setText(modelNotification.getNotification());
        }
    }

    //class typeThree
    class typeThree extends RecyclerView.ViewHolder { //User has accepted your request, border is green, show arrow

        FloatingActionButton profilePic;
        TextView userName;
        TextView notification;
        CardView notifications;
        TextView arrow;

        typeThree(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profile_pic);
            userName = itemView.findViewById(R.id.notifications_username);
            notification = itemView.findViewById(R.id.notifications_text);
            notifications = itemView.findViewById(R.id.notifications_border);
            arrow = itemView.findViewById(R.id.arrow);
            notifications.setCardBackgroundColor(Color.parseColor("#57e65c")); //border is green
        }

        public void setThreeDetails(ModelNotification modelNotification) {
            userName.setText(modelNotification.getUser());
            notification.setText(modelNotification.getNotification());
        }
    }

    //class typeFour
    class typeFour extends RecyclerView.ViewHolder { //The location the current user has selected to meet with the other has been sent

        FloatingActionButton profilePic;
        TextView userName;
        TextView notification;
        CardView notifications;
        TextView arrow;

        typeFour(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profile_pic);
            userName = itemView.findViewById(R.id.notifications_username);
            notification = itemView.findViewById(R.id.notifications_text);
            notifications = itemView.findViewById(R.id.notifications_border);
            arrow = itemView.findViewById(R.id.arrow);
            notifications.setCardBackgroundColor(Color.parseColor("#00000000")); //border is clear
        }

        public void setFourDetails(ModelNotification modelNotification) {
            userName.setText(modelNotification.getUser());
            notification.setText(modelNotification.getNotification());
        }
    }

    //class typeFive
    class typeFive extends RecyclerView.ViewHolder { //User has accepted your request, border is green, show arrow

        FloatingActionButton profilePic;
        TextView userName;
        TextView notification;
        CardView notifications;
        TextView arrow;

        typeFive(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profile_pic);
            userName = itemView.findViewById(R.id.notifications_username);
            notification = itemView.findViewById(R.id.notifications_text);
            notifications = itemView.findViewById(R.id.notifications_border);
            arrow = itemView.findViewById(R.id.arrow);
            notifications.setCardBackgroundColor(Color.parseColor("#00000000")); //border is clear
        }

        public void setFiveDetails(ModelNotification modelNotification) {
            userName.setText(modelNotification.getUser());
            notification.setText(modelNotification.getNotification());
        }
    }
}
