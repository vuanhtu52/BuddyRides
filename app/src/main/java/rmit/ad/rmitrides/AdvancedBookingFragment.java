package rmit.ad.rmitrides;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdvancedBookingFragment extends Fragment {

    //Views
    private EditText description;
    private TimePicker timePicker;
    private TextView displayTime;
    private Button saveButton, backButton;
    private EditText gender;



    public AdvancedBookingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_advanced_booking, container, false);

        description = layout.findViewById(R.id.bookingDescription);
        timePicker = layout.findViewById(R.id.timePicker);
        displayTime = layout.findViewById(R.id.displayTime);
        saveButton = layout.findViewById(R.id.save_button);
        backButton = layout.findViewById(R.id.back_button);
        gender = layout.findViewById(R.id.gender);

       //When user presses save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Pass data back to PassengerMapFragment
//                Bundle bundle = new Bundle();
//                bundle.putString("description", description.getText().toString());
//                Calendar calendar = Calendar.getInstance();
//                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
//                calendar.set(Calendar.MINUTE, timePicker.getMinute());
//                calendar.set(Calendar.SECOND, 0);
//                bundle.putString("pickupTime", calendar.getTime().toString());
//                bundle.putString("gender", gender.getText().toString());
//                bundle.putDouble("pickupLat", getArguments().getDouble("pickupLat"));
//                bundle.putDouble("pickupLon", getArguments().getDouble("pickupLon"));
//                bundle.putDouble("destinationLat", getArguments().getDouble("destinationLat"));
//                bundle.putDouble("destinationLon", getArguments().getDouble("destinationLon"));
//                bundle.putString("pickupAddress", getArguments().getString("pickupAddress"));
//                bundle.putString("destinationAddress", getArguments().getString("destinationAddress"));

                //Update booking object in MainActivity
                Booking booking = new Booking();
                booking.setDescription(description.getText().toString());
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                calendar.set(Calendar.SECOND, 0);
                booking.setPickUpTime(calendar.getTime().toString());
                booking.setGenderDriver(gender.getText().toString());
                GeoPoint pickupPoint = new GeoPoint(getArguments().getDouble("pickupLat"), getArguments().getDouble("pickupLon"));
                booking.setPickUpInGeoPoint(pickupPoint);
                GeoPoint destination = new GeoPoint(getArguments().getDouble("destinationLat"), getArguments().getDouble("destinationLat"));
                booking.setDestinationInGeoPoint(destination);
                booking.setPickUpAddress(getArguments().getString("pickupAddress"));
                booking.setDestinationAddress(getArguments().getString("destinationAddress"));

                ((MainActivity)getActivity()).setBooking(booking);

                //Go back to PassengerMapFragment
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                PassengerMapFragment fragment = new PassengerMapFragment();
                //fragment.setArguments(bundle);
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                ft.replace(R.id.fragment_container, fragment).commit();
            }
        });

        return layout;
    }

}
