package rmit.ad.rmitrides;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class FireBaseRef {
    static public FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static CollectionReference FBChatGroupRef = db.collection("chatGroups");
    public static CollectionReference FBChatRef = db.collection("chats");
    public static CollectionReference FBUsersRef = db.collection("users");
    public static CollectionReference FBBookingRef = db.collection("booking");
    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static Booking mbooking ;

    public void signinWithUsername(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    Log.i("login","result: "+user.getEmail());
                    //If email is already verified, go to home screen
                    if (user.isEmailVerified()) {
                        User.setFireBaseUser(user);
                    } else {    //If email is not verified, show error message
                        mAuth.signOut();
                    }
                }else {
                    Log.i("login","fail: ");
                }
            }
        });


    }



}
