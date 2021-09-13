package rmit.ad.rmitrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class SignupActivity extends AppCompatActivity {

    //Views
    private EditText emailText;
    private EditText passwordText;
    private EditText usernameText;
    private EditText fullnameText;
    private Button signupButton;
    private Button signinButton;
    private Button facebookButton;
    private Button googleButton;
    private ImageButton phoneButton;
    private ProgressBar progressBar;

    //Firebase properties
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 1;


    //This listener watches the change in the text views
    private TextWatcher signupWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //Disable button if any of the input is empty
            if (emailText.getText().toString().isEmpty() || passwordText.getText().toString().isEmpty() || usernameText.getText().toString().isEmpty() || fullnameText.getText().toString().isEmpty()) {
                signupButton.setEnabled(false);
                signupButton.setAlpha(0.5f);
            } else {
                signupButton.setEnabled(true);
                signupButton.setAlpha(1f);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Initialize the views
        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);
        usernameText = findViewById(R.id.username);
        fullnameText = findViewById(R.id.fullname);
        signupButton = findViewById(R.id.signup_button);
        signinButton = findViewById(R.id.signin_button);
        facebookButton = findViewById(R.id.facebook_button);
        googleButton = findViewById(R.id.google_button);
        phoneButton = findViewById(R.id.phone_button);
        progressBar = findViewById(R.id.progress_bar);

        //Disable sign-up button if any field is empty
        emailText.addTextChangedListener(signupWatcher);
        passwordText.addTextChangedListener(signupWatcher);
        usernameText.addTextChangedListener(signupWatcher);
        fullnameText.addTextChangedListener(signupWatcher);

        //Hide keyboard when user types outside the text views
        emailText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        passwordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        usernameText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        fullnameText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        //Go to sign in page if user clicks on sign in button
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                startActivity(intent);
            }
        });

        //Create account when user presses sign-up button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show progress bar and disable the views
                progressBar.setVisibility(View.VISIBLE);
                disableViews();
                //Sign up
                createAccount();
            }
        });

        //Initialize Facebook login button
        mCallbackManager = CallbackManager.Factory.create();
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show progress bar and disable views
                progressBar.setVisibility(View.VISIBLE);
                disableViews();
                LoginManager.getInstance().logInWithReadPermissions(SignupActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("SignupActivity", "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d("SignupActivity", "facebook:onCancel");
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("SignupActivity", "facebook:onError" + error);
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                    }
                });
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                disableViews();
                signInWithGoogle();
            }
        });

        //Go to phone sign up page when user presses the phone button
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, PhoneSignupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("SignupActivity", "Google sign in failed", e);
                progressBar.setVisibility(View.INVISIBLE);
                enableViews();
                // ...
            }
        }
    }

    //Sign in with google account
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("SignupActivity", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignupActivity", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                goToLoadingScreen();
                            }
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignupActivity", "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.signup_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);

                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                        // ...
                    }
                });
    }

    //Hide the keyboard
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //Disable views when progress bar is running
    private void disableViews() {
        emailText.setEnabled(false);
        passwordText.setEnabled(false);
        usernameText.setEnabled(false);
        fullnameText.setEnabled(false);
        signupButton.setEnabled(false);
        signupButton.setAlpha(0.5f);
        facebookButton.setEnabled(false);
        googleButton.setEnabled(false);
        phoneButton.setEnabled(false);
    }

    //Enable the views after a process is finished
    private void enableViews() {
        emailText.setEnabled(true);
        passwordText.setEnabled(true);
        usernameText.setEnabled(true);
        fullnameText.setEnabled(true);
        signupButton.setEnabled(true);
        facebookButton.setEnabled(true);
        googleButton.setEnabled(true);
        phoneButton.setEnabled(true);
    }

    //Create account as user presses sign-up button
    public void createAccount() {
        if (!checkPassword(passwordText.getText().toString())) {
            passwordText.setError("Invalid password\nMust be 6-12 characters long\nContains letter and number \nNo whitespace");
            //Hide progress bar and enable the views again
            progressBar.setVisibility(View.INVISIBLE);
            enableViews();
        } else {
            //Create account
            mAuth.createUserWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //If account is created, send a verification email
                    if (task.isSuccessful()) {
                        //Add user to Firestore
                        addUser();
                        FirebaseUser user = mAuth.getCurrentUser();
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Email sent
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Check email", Toast.LENGTH_SHORT).show();
                                    emailText.setText("");
                                    passwordText.setText("");
                                    usernameText.setText("");
                                    fullnameText.setText("");
                                } else {    //Account created but cannot send verification email
                                    Toast.makeText(SignupActivity.this, "Cannot create account. Please try again later.", Toast.LENGTH_SHORT).show();
                                    emailText.setText("");
                                    passwordText.setText("");
                                    usernameText.setText("");
                                    fullnameText.setText("");
                                    mAuth.getCurrentUser().delete();
                                }
                                //Hide progress bar and enable the view again
                                progressBar.setVisibility(View.INVISIBLE);
                                enableViews();
                            }
                        });
                    } else {    //Cannot create account. Email address is invalid
                        emailText.setError("Email address is invalid");
                        //Hide progress bar and enable the view again
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                    }
                }
            });
        }
    }

    //Check if password is valid
    private boolean checkPassword(String password) {
        //Check password length
        if (password.length() < 6 || password.length() > 12) {
            return false;
        }
        //Check if password contains letter and number
        if (!password.matches(".*\\d.*") || !password.matches(".*[a-zA-Z].*")) {
            return false;
        }
        //Check if password contains whitespace
        if (password.contains(" ")) {
            return false;
        }
        return true;
    }

    //Add user to firestore so we can log in with their username (this is a workaround as firebase auth does not support username sign in)
    public void addUser() {
        User user = new User(emailText.getText().toString(), usernameText.getText().toString(), fullnameText.getText().toString());
        user.setId(mAuth.getUid());

        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                //.add(user)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("SignupActivity", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("SignupActivity", "Error writing document", e);
                    }
                });


    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("SignupActivity", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignupActivity", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                goToLoadingScreen();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignupActivity", "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                    }
                });
    }

    public void goToLoadingScreen() {
        Intent intent = new Intent(SignupActivity.this, LoadingScreenActivity.class);
        startActivity(intent);
        Toast.makeText(this, "Signed in sucessfully", Toast.LENGTH_SHORT).show();
    }
}
