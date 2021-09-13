package rmit.ad.rmitrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    //Views
    private EditText emailText;
    private Button sendButton;
    private Toolbar toolbar;

    ///Firebase properties
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //This watcher watches when email text changes
    private TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //Disable send button if email field is empty
            if (emailText.getText().toString().isEmpty()) {
                sendButton.setEnabled(false);
                sendButton.setAlpha(0.5f);
            } else {
                sendButton.setEnabled(true);
                sendButton.setAlpha(1f);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        //Initialize the views
        emailText = findViewById(R.id.email);
        sendButton = findViewById(R.id.send_button);
        toolbar = findViewById(R.id.toolbar);

        //Set up toolbar
        setSupportActionBar(toolbar);

        //Enable up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sign in");

        //Disable send button if email is empty
        emailText.addTextChangedListener(inputWatcher);

        //Hide keyboard when user types outside text view
        emailText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        //Send reset email when user presses send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //Send reset password email to user
    public void sendEmail() {
        mAuth.sendPasswordResetEmail(emailText.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            emailText.setText("");
                            Toast.makeText(ResetPasswordActivity.this, "Check your email.", Toast.LENGTH_SHORT).show();
                        } else {
                            emailText.setError("Invalid email");
                        }
                    }
                });
    }

    //Gets called when a button on toolbar is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //When user presses back button
            case android.R.id.home:
                //Go back to sign in page
                Intent intent = new Intent(ResetPasswordActivity.this, SigninActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
