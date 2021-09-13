package rmit.ad.rmitrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    //Store current user's information
    private User user;
    private Bitmap profileImage;
    private Bitmap backgroundImage;

    //Current booking
    private Booking booking ;

    //Firebase properties
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Button button = findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, CommunicationFragment.class);
//                startActivity(intent);
//            }
//        });

        //Get user information
        getUserInfo();

        //Download profile image
        StorageReference profileImageRef = storageRef.child(mAuth.getUid() + "/myprofile.jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        profileImageRef.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        profileImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyProfileFragment", "Error loading profile image");
                    }
                });

        //Download background image
        StorageReference backgroundImageRef = storageRef.child(mAuth.getUid() + "/mybackground.jpg");
        backgroundImageRef.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        backgroundImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyProfileFragment", "Error loading background image");
                    }
                });

        //Reference the nav bar and attach a listener to it
        BottomNavigationView navBar = findViewById(R.id.nav_bar);
        navBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Fragment selectedFragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_activities:
                        selectedFragment = new HistoryFragment();
                        break;
                    case R.id.nav_notifications:
                        selectedFragment = new NotificationsFragment();
                        break;
                    case R.id.nav_settings:
                        selectedFragment = new SettingsFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });

        //Go to home fragment when first launched
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    public void getUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        db.collection("users")
                .whereEqualTo("id", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {  //Found all users with matched id
                            //Check if user exists
                            if (task.getResult().isEmpty()) {
                                Log.w("MainActivity", "Error getting user information");
                            } else {
                                //Pass user info to user object in this class
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    User user = new User(documentSnapshot.getString("email"), documentSnapshot.getString("username"), documentSnapshot.getString("fullName"));
                                    user.setId(documentSnapshot.getString("id"));
                                    user.setBirthdate(documentSnapshot.getString("birthdate"));
                                    user.setGender(documentSnapshot.getString("gender"));
                                    setUserInfo(user);
                                }
                            }
                        } else {
                            Log.w("MainActivity", "Cannot read user information");
                        }
                    }
                });
    }

    public void setUserInfo(User user) {
        this.user = new User(user);
    }

    public User getUser() {
        return user;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }

    public Bitmap getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Bitmap backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public Booking getBooking() {
//        return booking;
        return FireBaseRef.mbooking;
    }

    public void setBooking(Booking booking) {
        //this.booking = booking;
        this.booking = new Booking();
        if (this.booking.getDescription() == null) {
            this.booking.setDescription("");
        }
        if (this.booking.getPickUpTime() == null) {
            this.booking.setPickUpTime("");
        }
        if (this.booking.getGenderDriver() == null) {
            this.booking.setGenderDriver("");
        }
        this.booking.setDescription(booking.getDescription());
        this.booking.setPickUpTime(booking.getPickUpTime());
        this.booking.setGenderDriver(booking.getGenderDriver());
        this.booking.setPickUpInGeoPoint(booking.getPickUpInGeoPoint());
        this.booking.setDestinationInGeoPoint(booking.getDestinationInGeoPoint());
        this.booking.setPickUpAddress(booking.getPickUpAddress());
        this.booking.setDestinationAddress(booking.getDestinationAddress());
        this.booking.setStatus(booking.getStatus());


        FireBaseRef.mbooking = new Booking();
        if (FireBaseRef.mbooking.getDescription() == null) {
            FireBaseRef.mbooking.setDescription("");
        }
        if (FireBaseRef.mbooking.getPickUpTime() == null) {
            FireBaseRef.mbooking.setPickUpTime("");
        }
        if (FireBaseRef.mbooking.getGenderDriver() == null) {
            FireBaseRef.mbooking.setGenderDriver("");
        }
        FireBaseRef.mbooking.setDescription(booking.getDescription());
        FireBaseRef.mbooking.setPickUpTime(booking.getPickUpTime());
        FireBaseRef.mbooking.setGenderDriver(booking.getGenderDriver());
        FireBaseRef.mbooking.setPickUpInGeoPoint(booking.getPickUpInGeoPoint());
        FireBaseRef.mbooking.setDestinationInGeoPoint(booking.getDestinationInGeoPoint());
        FireBaseRef.mbooking.setPickUpAddress(booking.getPickUpAddress());
        FireBaseRef.mbooking.setDestinationAddress(booking.getDestinationAddress());
        FireBaseRef.mbooking.setStatus(booking.getStatus());
    }

    public void deleteBooking() {
        booking = null;
    }
}
