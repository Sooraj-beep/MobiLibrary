package com.example.mobilibrary.DatabaseController;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telecom.Call;
import android.util.Base64;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobilibrary.Activity.LogIn;
import com.example.mobilibrary.Activity.ProfileActivity;
import com.example.mobilibrary.Activity.SignUp;
import com.example.mobilibrary.Callback;
import com.example.mobilibrary.DatabaseController.User;
import com.example.mobilibrary.MainActivity;
import com.google.android.gms.common.api.Batch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.google.firebase.firestore.DocumentChange.Type.MODIFIED;
import static com.google.firebase.firestore.DocumentChange.Type.REMOVED;
/**
* This class handles the database related tasks that are requested from other activities
 */

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    static final String UsersCol = "Users";
    //Firestore database object
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Context context;

    public DatabaseHelper(Context context) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        this.context = context;
    }

    /**
     * https://stackoverflow.com/questions/52861391/firestore-checking-if-username-already-exists
     * Checks to see if a user signing up is already registered in our Firestore Cloud database.
     *
     * @param username new user username
     * @param password new user password
     * @param name     new user full name
     * @param email    new user email
     * @param phoneNo  new user phone number
     */
    public void regCheck(final String username, final String password, final String name, final String email, final String phoneNo) {
        /**
        * Tries to fetch a document with the matching
        * If the document does not exist, creates the account with parameters passed in
         */
        db.collection(UsersCol)
                .document(username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                Toast.makeText(context, "Username already exists. Please try again!", Toast.LENGTH_SHORT).show();
                            } else {
                                registerUser(username, password, name, email, phoneNo);
                            }

                        } else {
                            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * https://stackoverflow.com/questions/50087616/firebase-transaction-read-and-update-multiple-documents
     * Registers a user with Firebase User Authentication, and adds their public information to the Firestore database
     *
     * @param username new user's username
     * @param password new user's password
     * @param name     new user's full name
     * @param email    new user's email
     * @param phoneNo  new user's phone number
     */
    private void registerUser(final String username, final String password, final String name, final String email, final String phoneNo) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = mAuth.getCurrentUser();
                            // Create User object to send to the Firestore database
                            final User newUser = new User(username, email, name, phoneNo);
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            user.updateProfile(profileUpdate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Added username as user display name!");
                                            DocumentReference userRef = db.collection("Users").document(username);
                                            userRef.set(newUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "User Profile successfully created for " + username);
                                                            Toast.makeText(context, ("Account created for " + username), Toast.LENGTH_SHORT).show();
                                                            //go back to login screen so the user can log in
                                                            context.startActivity(new Intent(context, LogIn.class));
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "User Profile could not be added!" + e.toString());
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "Username could not be added as user display name!");
                                        }
                                    });

                        } else {
                            Toast.makeText(context, "Account already exists!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Logs in a currently existing user
     *
     * @param email    user email from Log In
     * @param password user password from Log In
     */
    public void validateUser(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Authentication Succeeded.", Toast.LENGTH_SHORT).show();
                            //log in to homepage
                            context.startActivity(new Intent(context, MainActivity.class));
                        } else {
                            Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            //go to log in screen again to prompt a new attempt
                            context.startActivity(new Intent(context, LogIn.class));
                        }
                    }
                });


    }

    /**
     * Gets profile to be looked at in the profile activity
     * https://stackoverflow.com/questions/48499310/how-to-return-a-documentsnapshot-as-a-result-of-a-method
     *
     * @param username username of the profile to be looked for
     * @param callback callback to profile
     */
    public void getUserProfile(final String username, final Callback callback) {
        db.collection("Users").document(username).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phoneNo");
                        User userProfile = new User(username, email, name, phone);
                        callback.onCallback(userProfile);
                    }
                });
    }

    /**
     * Getters
     */
    public FirebaseUser getUser() {
        return user;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseFirestore getDb() { return db; }

    /**
     * Signs user out of session.
     */
    public void signOut() {
        mAuth.signOut();
    }

    /**
     * Re-authenticates current user before allowing update of primary email.
     *
     * @param email    current email (before change)
     * @param password current user's password
     * @param callback callback to re-auth fragment
     */
    public void reAuthUser(final String email, String password, final Callback callback) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated!");
                            getUserProfile(user.getDisplayName(), new Callback() {
                                @Override
                                public void onCallback(User user) {
                                    callback.onCallback(user);
                                }
                            });
                        } else {
                            Toast.makeText(context, "Incorrect email or password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Updates the contact of the currently logged in user
     * https://stackoverflow.com/questions/55129887/problem-converting-documentsnapshot-to-custom-object
     *
     * @param username username of the current user
     * @param newEmail email to be updated
     * @param newPhone phone to be updated
     * @param name     name of the current user
     * @param callback callback to profile
     */
    public void updateUser(final String username, final String newEmail, final String newPhone, final String name, final Callback callback) {
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        user.updateProfile(profileUpdate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        DocumentReference docRef = db.collection("Users").document(username);
                        WriteBatch batch = db.batch();
                        batch.update(docRef, "email", newEmail);
                        batch.update(docRef, "phoneNo", newPhone);
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                user.updateEmail(newEmail)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                User updatedUser = new User(username, newEmail, name, newPhone);
                                                callback.onCallback(updatedUser);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "User new email update failed!");
                                            }
                                        });
                            }
                        });
                    }
                });
    }

    /**
     * Deletes a user from both the Firebase User Auth side and the Firestore side.
     * @param username username of the user to be deleted
     */
    public void deleteUser(String username) {
        db.collection("Users").document(username)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        user.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "User deleted!");
                                        context.startActivity(new Intent(context, SignUp.class));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting user", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

}
