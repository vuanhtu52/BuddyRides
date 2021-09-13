package rmit.ad.rmitrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PhoneSignupActivity extends AppCompatActivity {

    private ArrayList<CountryItem> countryList;
    private CountryAdapter adapter;
    private Spinner spinnerCountries;
    private EditText phoneText;
    private Button verifyButton;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    //Firebase properties
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Other properties
    private String currentCode; //Current phone code selected on the spinner

    //This watcher watches the phone input
    private TextWatcher phoneWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //Disable verify button if user has not entered 9 didits
            if (phoneText.length() != 9) {
                verifyButton.setEnabled(false);
                verifyButton.setAlpha(0.5f);
            } else {
                verifyButton.setEnabled(true);
                verifyButton.setAlpha(1f);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(PhoneSignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_signup);

        //Initialize the views
        spinnerCountries = findViewById(R.id.spinner);
        phoneText = findViewById(R.id.phone_number);
        verifyButton = findViewById(R.id.verify_button);
        progressBar = findViewById(R.id.progress_bar);
        toolbar = findViewById(R.id.toolbar);

        //Set up toolbar
        setSupportActionBar(toolbar);

        //Enable up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sign up");

        //Initialize the spinner
        initList();
        adapter = new CountryAdapter(this, countryList);
        spinnerCountries.setAdapter(adapter);
        //Adjust the dropdown position
        spinnerCountries.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                spinnerCountries.setDropDownVerticalOffset(spinnerCountries.getDropDownVerticalOffset() + spinnerCountries.getHeight());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    spinnerCountries.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    spinnerCountries.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                spinnerCountries.setDropDownHorizontalOffset(spinnerCountries.getPaddingLeft());
            }
        });
        //when user picks a country code on the spinner
        spinnerCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CountryItem clickedItem = (CountryItem) adapterView.getItemAtPosition(i);
                //Save the chosen code
                currentCode = clickedItem.getCountryName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Disable verify button if user has not entered 9 digits
        phoneText.addTextChangedListener(phoneWatcher);

        //Hide keyboard when user types outside keyboard
        phoneText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        //When user presses verify button
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verify phone number
                PhoneAuthProvider.getInstance().verifyPhoneNumber(currentCode + phoneText.getText().toString(), 60, TimeUnit.SECONDS, PhoneSignupActivity.this, mCallbacks);
                progressBar.setVisibility(View.VISIBLE);    //Show progress bar
                disableViews(); //Disable the views so user can't mess with when sending code
            }
        });
    }

    //Gets called when a button on toolbar is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //When user presses back button
            case android.R.id.home:
                //Go back to sign up page
                Intent intent = new Intent(PhoneSignupActivity.this, SignupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Sign in automatically when the phone number has been verified
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);  //Hide progress bar upon commpletion
                    enableViews();  //Enable the views again
                    //Go to home screen
                    Intent intent = new Intent(PhoneSignupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(PhoneSignupActivity.this, "Error signing in.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Initialize the list of countries for the spinner
    private void initList() {
        countryList = new ArrayList<>();
        countryList.add(new CountryItem("+84", R.drawable.vietnam_flag));
        countryList.add(new CountryItem("+1", R.drawable.usa_flag));
        countryList.add(new CountryItem("+61", R.drawable.australia_flag));
        countryList.add(new CountryItem("+86", R.drawable.china_flag));
        countryList.add(new CountryItem("+82", R.drawable.korea_flag));
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //Disable the views (except the back button) when sending verification code
    private void disableViews() {
        verifyButton.setEnabled(false);
        spinnerCountries.setEnabled(false);
        phoneText.setEnabled(false);
    }

    //Enable the views when the code has been sent
    private void enableViews() {
        verifyButton.setEnabled(true);
        spinnerCountries.setEnabled(true);
        phoneText.setEnabled(true);
    }
}
