package com.example.kante.live_alone.StartAppActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kante.live_alone.HomeActivities.HomeFeed;
import com.example.kante.live_alone.PostActivities.Posting;
import com.example.kante.live_alone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Demonstrate Firebase Authentication without a password, using a link sent to an
 * email address.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PasswordlessSignIn";
    private static final String KEY_PENDING_EMAIL = "key_pending_email";

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
//    private Button mSendLinkButton;
//    private Button mSignInButton;
//    private Button mSignOutButton;

    private EditText mEmailField;
    private TextView mStatusText;

    private String mPendingEmail;
    private String mEmailLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

//        mSendLinkButton = findViewById(R.id.passwordlessSendEmailButton);
//        mSignInButton = findViewById(R.id.passwordlessSignInButton);
//        mSignOutButton = findViewById(R.id.signOutButton);

//        mEmailField = findViewById(R.id.fieldEmail);
//        mStatusText = findViewById(R.id.status);

//        mSendLinkButton.setOnClickListener(this);
//        mSignInButton.setOnClickListener(this);
//        mSignOutButton.setOnClickListener(this);

        // Restore the "pending" email address
//        if (savedInstanceState != null) {
//            mPendingEmail = savedInstanceState.getString(KEY_PENDING_EMAIL, null);
//            mEmailField.setText(mPendingEmail);
//        }

        // Check if the Intent that started the Activity contains an email sign-in link.
        checkIntent(getIntent());

        Button goHomeFeed = findViewById(R.id.btn_goEnterdetailed);
        goHomeFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                mAuth.getCurrentUser().reload()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                updateUI(mAuth.getCurrentUser());
                                hideProgressDialog();
                            }
                        });
            }
        });
//
//        Button goPosting = findViewById(R.id.btn_goPosting);
//        goPosting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                btn_goPosting();
//            }
//        });

    }

    public void btn_gologin(){
        Intent intent = new Intent(this, FirebaseUIActivity.class);
        startActivity(intent);
        finish();
    }

    public void btn_gohome(){
        Intent intent = new Intent(this, HomeFeed.class);
        startActivity(intent);
        finish();
    }
    public void btn_goPosting(){
        Intent intent = new Intent(this, Posting.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(@Nullable FirebaseUser user) {
        if (user != null) {
            super.onStart();
//            mStatusText.setText(getString(R.string.passwordless_status_fmt,
//                    user.getEmail(), user.isEmailVerified()));

//            findViewById(R.id.mainFields).setVisibility(View.VISIBLE);
//            findViewById(R.id.passwordlessButtons).setVisibility(View.VISIBLE);
//            findViewById(R.id.signedInButtons).setVisibility(View.GONE);
            if(user.isEmailVerified()){
                goHomeFeed();
            }else {
                Toast.makeText(MainActivity.this, "아직 인증이 되지 않았습니다!!", Toast.LENGTH_SHORT).show();
            }
        } else {
//            findViewById(R.id.mainFields).setVisibility(View.VISIBLE);
//            findViewById(R.id.passwordlessButtons).setVisibility(View.VISIBLE);
//            findViewById(R.id.signedInButtons).setVisibility(View.GONE);
        }
    }


    private void goHomeFeed(){
        Intent intent = new Intent(this, HomeFeed.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString(KEY_PENDING_EMAIL, mPendingEmail);
//    }

    /**
     * Check to see if the Intent has an email link, and if so set up the UI accordingly.
     * This can be called from either onCreate or onNewIntent, depending on how the Activity
     * was launched.
     */
    private void checkIntent(@Nullable Intent intent) {
        if (intentHasEmailLink(intent)) {
//            mEmailLink = intent.getData().toString();

//            mStatusText.setText(R.string.status_link_found);
//            mSendLinkButton.setEnabled(false);
//            mSignInButton.setEnabled(true);

        } else {
//            mStatusText.setText(R.string.status_email_not_sent);
//            mSendLinkButton.setEnabled(true);
//            mSignInButton.setEnabled(false);
        }
    }

    /**
     * Determine if the given Intent contains an email sign-in link.
     */
    private boolean intentHasEmailLink(@Nullable Intent intent) {
        if (intent != null && intent.getData() != null) {
            String intentData = intent.getData().toString();
            if (mAuth.isSignInWithEmailLink(intentData)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Send an email sign-in link to the specified email.
     */
    private void sendSignInLink(String email) {
        ActionCodeSettings settings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(
                        getPackageName(),
                        false, /* install if not available? */
                        null   /* minimum app version */)
                .setHandleCodeInApp(true)
                .setUrl("https://auth.example.com/emailSignInLink")
                .build();

        hideKeyboard(mEmailField);
        showProgressDialog();

        final String email_new = email;
        mAuth.sendSignInLinkToEmail(email, settings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            Log.d(TAG, "Link sent");
                            showSnackbar("Sign-in link sent!");

                            mPendingEmail = email_new;
//                            mStatusText.setText(R.string.status_email_sent);
                        } else {
                            Exception e = task.getException();
                            Log.w(TAG, "Could not send link", e);
                            showSnackbar("Failed to send link.");

                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
//                                mEmailField.setError("Invalid email address.");
                            }
                        }
                    }
                });
    }

    /**
     * Sign in using an email address and a link, the link is passed to the Activity
     * from the dynamic link contained in the email.
     */
    private void signInWithEmailLink(String email, String link) {
        Log.d(TAG, "signInWithLink:" + link);

        hideKeyboard(mEmailField);
        showProgressDialog();

        mAuth.signInWithEmailLink(email, link)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        mPendingEmail = null;

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmailLink:success");

//                            mEmailField.setText(null);
                            updateUI(task.getResult().getUser());

                        } else {
                            Log.w(TAG, "signInWithEmailLink:failure", task.getException());
                            updateUI(null);

                            if (task.getException() instanceof FirebaseAuthActionCodeException) {
                                showSnackbar("Invalid or expired sign-in link.");
                            }
                        }
                    }
                });
    }

//    private void onSendLinkClicked() {
//        String email = mEmailField.getText().toString();
//        if (TextUtils.isEmpty(email)) {
//            mEmailField.setError("Email must not be empty.");
//            return;
//        }
//
//        sendSignInLink(email);
//    }
//
//    private void onSignInClicked() {
//        String email = mEmailField.getText().toString();
//        if (TextUtils.isEmpty(email)) {
//            mEmailField.setError("Email must not be empty.");
//            return;
//        }
//
//        signInWithEmailLink(email, mEmailLink);
//    }

//    private void onSignOutClicked() {
//        mAuth.signOut();
//
//        updateUI(null);
//        mStatusText.setText(R.string.status_email_not_sent);
//    }

//    private void updateUI(@Nullable FirebaseUser user) {
//        if (user != null) {
//            mStatusText.setText(getString(R.string.passwordless_status_fmt,
//                    user.getEmail(), user.isEmailVerified()));
//
//            findViewById(R.id.mainFields).setVisibility(View.GONE);
//            findViewById(R.id.passwordlessButtons).setVisibility(View.GONE);
//            findViewById(R.id.signedInButtons).setVisibility(View.VISIBLE);
//        } else {
//            findViewById(R.id.mainFields).setVisibility(View.VISIBLE);
//            findViewById(R.id.passwordlessButtons).setVisibility(View.VISIBLE);
//            findViewById(R.id.signedInButtons).setVisibility(View.GONE);
//        }
//    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.passwordlessSendEmailButton:
//                onSendLinkClicked();
//                break;
//            case R.id.passwordlessSignInButton:
//                onSignInClicked();
//                break;
//            case R.id.signOutButton:
//                onSignOutClicked();
//                break;
//        }
    }
}