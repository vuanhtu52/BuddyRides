package rmit.ad.rmitrides;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.sql.Driver;

import rmit.ad.rmitrides.Notification.AlarmFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private FrameLayout passengerButton;
    private FrameLayout driverButton;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_home, container, false);

        //Initialize the views
        passengerButton = layout.findViewById(R.id.passenger_button);
        driverButton = layout.findViewById(R.id.driver_button);

        //When user presses on the buttons
        passengerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FireBaseRef.FBUsersRef
                       .document(FireBaseRef.mAuth.getUid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    User user = task.getResult().toObject(User.class);
                                    if (user.getCurrentBooking_AsPassenger() != null) {
                                        FireBaseRef.FBBookingRef
                                                .document(user.getCurrentBooking_AsPassenger())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            ((MainActivity)getActivity()).setBooking(task.getResult().toObject(Booking.class));
                                                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                                                            ft.replace(R.id.fragment_container, new PassengerOnRideFragment()).commit();
                                                        }
                                                    }
                                                });

                                    } else {
                                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                                        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                                        ft.replace(R.id.fragment_container, new PassengerMapFragment()).commit();
                                    }
                                }
                            }
                        });

//                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
//                ft.replace(R.id.fragment_container, new PassengerMapFragment()).commit();
            }
        });
        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FireBaseRef.FBUsersRef
                        .document(FireBaseRef.mAuth.getUid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    User user = task.getResult().toObject(User.class);
                                    if (user.getCurrentBooking_AsDriver() != null) {
                                        FireBaseRef.FBBookingRef
                                                .document(user.getCurrentBooking_AsDriver())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            ((MainActivity)getActivity()).setBooking(task.getResult().toObject(Booking.class));
                                                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                                                            ft.replace(R.id.fragment_container, new DriverOnRideFragment()).commit();
                                                        }
                                                    }
                                                });

                                    } else {
                                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                                        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                                        ft.replace(R.id.fragment_container, new DriverFragment()).commit();
                                    }
                                }
                            }
                        });

//                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
//                ft.replace(R.id.fragment_container, new DriverFragment()).commit();
            }
        });

        return layout;
    }

}
