package com.aarti.mychat;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import static com.aarti.mychat.R.id.message_text;

public class MainActivity extends ListActivity {
    private Firebase mFirebaseRef;
    private EditText mMessageEdit;
    private FirebaseListAdapter<ChatMessage> mListAdapter;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://Mychat.firebaseio.com");
        mMessageEdit =(EditText) this.findViewById(message_text);

        mListAdapter = new FirebaseListAdapter<ChatMessage>(mFirebaseRef, ChatMessage.class,
                R.layout.message_layout, this) {
            @Override
            protected void populateView(View v, ChatMessage model) {
                ((TextView)v.findViewById(R.id.username_text_view)).setText(model.getName());
                ((TextView)v.findViewById(R.id.message_text_view)).setText(model.getMessage());
            }
        };
        this.setListAdapter(mListAdapter);

        mFirebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if(authData != null) {
                    mUsername = ((String)authData.getProviderData().get("email"));
                    findViewById(R.id.login).setVisibility(View.INVISIBLE);
                }
                else {
                    mUsername = null;
                    findViewById(R.id.login).setVisibility(View.VISIBLE);
                }
            }
        });


    }

    public void onSendButtonClick(View v) {
        String message = mMessageEdit.getText().toString();
        mFirebaseRef.push().setValue(new ChatMessage(mUsername, message));
        mMessageEdit.setText("");
    }

    public void onLoginButtonClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Enter your email address and password")
                .setTitle("Log in");

        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_signin, null));

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AlertDialog dlg = (AlertDialog) dialog;
                final String email = ((TextView)dlg.findViewById(R.id.email)).getText().toString();
                final String password =((TextView)dlg.findViewById(R.id.password)).getText().toString();

                mFirebaseRef.createUser(email, password, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        mFirebaseRef.authWithPassword(email, password, null);
                    }
                    @Override
                    public void onError(FirebaseError firebaseError) {
                        mFirebaseRef.authWithPassword(email, password, null);
                    }
                });


            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }




}

