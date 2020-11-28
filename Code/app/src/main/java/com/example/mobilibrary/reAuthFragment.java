package com.example.mobilibrary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.DatabaseController.User;

/**
 * @author Jill;
 * This fragment, extending DialogFragment, pops up when the user tries to edit their own profile.
 * In order to change anything in FirebaseUser, the user must be recently authenticated, so this
 * ensures all users can properly edit their own profile. The dialog pops up when the user clicks
 * the edit button, and only changes the page to open editing if the user correctly re-authenticates.
 * If the user clicks away, the dialog goes away, and nothing happens on the page.
 */

public class reAuthFragment extends DialogFragment {

    private EditText email;
    private EditText password;
    private OnFragmentInteractionListener listener;
    private DatabaseHelper databaseHelper;

    public interface OnFragmentInteractionListener {
        void onOkPressed();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
            databaseHelper = new DatabaseHelper(context);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_re_auth, null);
        email = view.findViewById(R.id.old_email_text_view);
        password = view.findViewById(R.id.password_text_view);

        // Builds the dialog for the user to input re-auth data
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("Re-authentication")
                .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // To be overridden
                    }
                });

        return builder.create();
    }

    /**
     * Prevents dialog from closing if the entered information is incorrect
     */
    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(Color.BLACK);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String reAuthEmail = email.getText().toString();
                    String reAuthPass = password.getText().toString();
                    databaseHelper.reAuthUser(reAuthEmail, reAuthPass, new Callback() {
                        @Override
                        public void onCallback(User user) {
                            d.dismiss();
                            listener.onOkPressed();
                        }
                    });
                }
            });
        }
    }
}