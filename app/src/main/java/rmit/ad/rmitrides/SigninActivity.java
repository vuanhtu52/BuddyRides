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
import android.widget.TextView;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;

public class SigninActivity extends AppCompatActivity {

    //Views
    private EditText emailText;
    private EditText passwordText;
    private Button signinButton;
    private Button signupButton;
    private Button facebookButton;
    private Button googleButton;
    private ImageButton phoneButton;
    private TextView forgotPasswordButton;
    private ProgressBar progressBar;

    //Firebase properties
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 1;

    //This watcher watches when input changes
    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //Disable button if any of the input is empty
            if(emailText.getText().toString().isEmpty() || passwordText.getText().toString().isEmpty()) {
                signinButton.setEnabled(false);
                signinButton.setAlpha(0.5f);
            } else {
                signinButton.setEnabled(true);
                signinButton.setAlpha(1f);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            startActivity(new Intent(SigninActivity.this,MainActivity.class));
        }

        //Initialize the views
        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);
        signinButton = findViewById(R.id.signin_button);
        signupButton = findViewById(R.id.signup_button);
        facebookButton = findViewById(R.id.facebook_button);
        googleButton = findViewById(R.id.google_button);
        phoneButton = findViewById(R.id.phone_button);
        forgotPasswordButton = findViewById(R.id.forgot_password);
        progressBar = findViewById(R.id.progress_bar);

        //Disable login button if input is empty
        emailText.addTextChangedListener(loginTextWatcher);
        passwordText.addTextChangedListener(loginTextWatcher);

        //Hide keyboard when user types outside text views
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

        //Sign in when user presses log-in button
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show progress bar and disable views
                progressBar.setVisibility(View.VISIBLE);
                disableViews();
                signinWithEmail();
            }
        });

        //Go to sign-up page when user presses sign-up button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        //Initialize Facebook login button
        mCallbackManager = CallbackManager.Factory.create();
        facebookButton = findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                disableViews();
                LoginManager.getInstance().logInWithReadPermissions(SigninActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("SigninActivity", "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d("SigninActivity", "facebook:onCancel");
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("SigninActivity", "facebook:onError" + error);
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
        googleButton = findViewById(R.id.google_button);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                disableViews();
                signInWithGoogle();
            }
        });

        //When user presses forgot password
        forgotPasswordButton = findViewById(R.id.forgot_password);
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SigninActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        //Sign in with phone
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Go to PhoneSignInActivity
                Intent intent = new Intent(SigninActivity.this, PhoneSigninActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
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
                Log.w("SigninActivity", "Google sign in failed", e);
                progressBar.setVisibility(View.INVISIBLE);
                enableViews();
                // ...
            }
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("SigninActivity", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SigninActivity", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                goToLoadingScreen();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SigninActivity", "signInWithCredential:failure", task.getException());
                            Toast.makeText(SigninActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                    }
                });
    }

    //Sign in with google account
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("SigninActivity", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SigninActivity", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                goToLoadingScreen();
                            }
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SigninActivity", "signInWithCredential:failure", task.getException());
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

    private void disableViews() {
        emailText.setEnabled(false);
        passwordText.setEnabled(false);
        signinButton.setEnabled(false);
        signinButton.setAlpha(0.5f);
        facebookButton.setEnabled(false);
        googleButton.setEnabled(false);
        phoneButton.setEnabled(false);
    }

    private void enableViews() {
        emailText.setEnabled(true);
        passwordText.setEnabled(true);
        signinButton.setEnabled(true);
        signinButton.setAlpha(1f);
        facebookButton.setEnabled(true);
        googleButton.setEnabled(true);
        phoneButton.setEnabled(true);
    }

    //Sign in with email
    public void signinWithEmail() {
        mAuth.signInWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //If signing in successfully, check if the email is verified
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    //If email is already verified, go to home screen
                    if (user.isEmailVerified()) {
                        User.setFireBaseUser(user);
                        goToLoadingScreen();
                    } else {    //If email is not verified, show error message
                        mAuth.signOut();
                        Toast.makeText(SigninActivity.this, "Cannot sign in", Toast.LENGTH_SHORT).show();
                    }
                    //Hide progress bar and enable views
                    progressBar.setVisibility(View.INVISIBLE);
                    enableViews();
                } else {    //Cannot sign in with email, try signing in with username
                    getUserInfo();
                }
            }
        });
    }

    public void goToLoadingScreen() {
        Intent intent = new Intent(SigninActivity.this, LoadingScreenActivity.class);
        startActivity(intent);
        finish();
    }

    //Get user info based on username
    public void getUserInfo() {
        final String[] email = {""};
        db.collection("users")
                .whereEqualTo("username", emailText.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {  //Found a user with correct username
                            //Check if no username is found
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(SigninActivity.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                            } else {
                                //Get the email associated with the username
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    email[0] = document.get("email").toString();
                                }
                                //Sign in with that email
                                signinWithUsername(email[0]);

                            }

                        } else {    //Cannot find username
                            Log.w("SigninActivity", "Cannot read data");
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                    }
                });

    }

    public void signinWithUsername(String email) {
        mAuth.signInWithEmailAndPassword(email, passwordText.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //If signing in successfully, check if the email is verified
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    //If email is already verified, go to home screen
                    if (user.isEmailVerified()) {
                        User.setFireBaseUser(user);
                        goToLoadingScreen();
                    } else {    //If email is not verified, show error message
                        mAuth.signOut();
                        Toast.makeText(SigninActivity.this, "Cannot sign in", Toast.LENGTH_SHORT).show();
                    }
                } else {    //Cannot sign in, show error message
                    Toast.makeText(SigninActivity.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
