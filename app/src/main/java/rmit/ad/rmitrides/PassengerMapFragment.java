package rmit.ad.rmitrides;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.WriteBatch;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * A simple {@link Fragment} subclass.
 */
public class PassengerMapFragment extends Fragment implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    private GoogleMap mMap;

    //Views
    private Toolbar toolbar;
    private MaterialSearchBar originBar;
    private MaterialSearchBar destinationBar;
    private View mapView;
    private FrameLayout topNav;
    private ImageView pickupIcon;
    private ImageView destinationIcon;
    private Button bookingButton;
    private Button advancedButton;
    private FrameLayout bookingMessage;
    private Button cancelButton;
    private ImageButton backButton;

    //Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Other properties
    private FusedLocationProviderClient client;
    private PlacesClient placesClient;  //Responsible for loading suggestions
    private List<AutocompletePrediction> predictionList;    //Save the suggestions
    private Location currentLocation;
    private LatLng pickupPoint = null;
    private LatLng destination = null;
    private LocationCallback locationCallback;
    private boolean setOriginBar = true;    //Determine if we need to pass current address to originBar
    private Marker pickupMarker;
    private Marker destinationMarker;
    //Needed to draw route
    private LatLngBounds.Builder builder;
    private Polyline route;

    private Booking booking;
    private String randomUUIDString;
    private boolean statusChange = true;

    public PassengerMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_passenger_map, container, false);

        //Request user to allow location access
        ActivityCompat.requestPermissions(getActivity(), new String[] {ACCESS_FINE_LOCATION}, 1);

        //Initialize views
        //toolbar = layout.findViewById(R.id.toolbar);
        originBar = layout.findViewById(R.id.originBar);
        destinationBar = layout.findViewById(R.id.destinationBar);
        topNav = layout.findViewById(R.id.top_nav);
        pickupIcon = layout.findViewById(R.id.pickup_icon);
        destinationIcon = layout.findViewById(R.id.destination_icon);
        bookingButton = layout.findViewById(R.id.book_button);
        advancedButton = layout.findViewById(R.id.advanced_button);
        bookingMessage = layout.findViewById(R.id.booking_message);
        cancelButton = layout.findViewById(R.id.cancel_button);
        backButton = layout.findViewById(R.id.back_button);

        //When user presses back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                ft.replace(R.id.fragment_container, new HomeFragment()).commit();
            }
        });

//        //Set up toolbar
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        toolbar.setTitle("");

//        //Allow toolbar to have action options
//        setHasOptionsMenu(true);
//        //Enable up button
//        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Check if we just got back from AdvancedBookingFragment
        booking = ((MainActivity)getActivity()).getBooking();
        if (booking == null) {
            Log.i("debugg", "null");
        }
        if (booking != null) {
            Toast.makeText(getContext(), "hello", Toast.LENGTH_SHORT).show();
            Log.i("oncreateview", booking.getStatus() + "status");
            originBar.enableSearch();
            originBar.setText(booking.getPickUpAddress());
            destinationBar.enableSearch();
            destinationBar.setText(booking.getDestinationAddress());
            //Set pickupPoint and destination
            pickupPoint = new LatLng(((MainActivity)getActivity()).getBooking().getPickUpInGeoPoint().getLatitude(), ((MainActivity)getActivity()).getBooking().getPickUpInGeoPoint().getLongitude());
            destination = new LatLng(((MainActivity)getActivity()).getBooking().getDestinationInGeoPoint().getLatitude(), ((MainActivity)getActivity()).getBooking().getDestinationInGeoPoint().getLongitude());
        }

        //Get the map
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.passenger_map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        //Initialize properties needed for places suggestions
        client = LocationServices.getFusedLocationProviderClient(getActivity());
        Places.initialize(getActivity(), "AIzaSyAzsPauXs_9PJa6vKkBnu8s_yrT_cECuNA");
        placesClient = Places.createClient(getContext());
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        //When user presses a button on the origin bar
        originBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //open or close a navigation drawer

                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    Log.i("debug", "clicked");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            originBar.clearSuggestions();
                            originBar.disableSearch();
                        }
                    }, 1000);
                    originBar.clearSuggestions();
                }
            }
        });

        //When origin bar content changes
        originBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("VN")
                        .setSessionToken(token)
                        .setQuery(charSequence.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionList.add(prediction.getFullText(null).toString());
                                }
                                originBar.updateLastSuggestions(suggestionList);
                                if (!originBar.isSuggestionsVisible()) {
                                    originBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("PassengerMapFragment", "Prediction fetching task unsuccessful");
                        }
                    }
                });

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("origin bar", "text changed");
//                //Fill the originBar with current location address
//                if (originBar.getText().equals("") && setOriginBar) {
//                    originBar.enableSearch();
//                    originBar.setText("RMIT, Nguyen Van Linh, Tan Phong, Quan 7, Ho Chi Minh, Viet Nam");
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            originBar.clearSuggestions();
//                        }
//                    }, 1000);
//                    originBar.clearSuggestions();
//                }

//                //Check if we just got back from AdvancedBookingFragment
//                if (getArguments() != null) {
//                    Toast.makeText(getContext(), "hello", Toast.LENGTH_SHORT).show();
//                    booking = ((MainActivity)getActivity()).getBooking();
//                    originBar.enableSearch();
//                    originBar.setText(booking.getPickUpAddress());
//                    destinationBar.enableSearch();
//                    destinationBar.setText(booking.getDestinationAddress());
//                }

//                if (booking != null) {
//                    originBar.enableSearch();
//                    originBar.setText(booking.getPickUpAddress());
//                }

                //Set originBar's text after returning from AdvancedBookingFragment
                if (booking != null && setOriginBar) {
//                    originBar.enableSearch();
//                    originBar.setText(booking.getPickUpAddress());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            originBar.enableSearch();
                            originBar.setText(booking.getPickUpAddress());
                            originBar.clearSuggestions();
                        }
                    }, 1000);
                    setOriginBar = false;
                }

                //Clear the marker and pickupPoint when the origin bar is empty
                if (originBar.getText().equals("")) {
                    if (booking == null) {
                        pickupPoint = null;
                    }

                    if (pickupMarker != null) {
                        pickupMarker.remove();
                    }
                }

                //Show booking and advanced button when both bars are filled
                if (!originBar.getText().equals("") && !destinationBar.getText().equals("")) {
                    bookingButton.setVisibility(View.VISIBLE);
                    advancedButton.setVisibility(View.VISIBLE);
                    if (booking != null) {
                        if (booking.getStatus().equals("pending")) {
                            bookingButton.setVisibility(View.INVISIBLE);
                            advancedButton.setVisibility(View.INVISIBLE);
                        }
                    }
                } else {
                    bookingButton.setVisibility(View.INVISIBLE);
                    advancedButton.setVisibility(View.INVISIBLE);
                }

            }
        });

        //When user clicks on a suggestion of originBar
        originBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = originBar.getLastSuggestions().get(position).toString();
                originBar.setText(suggestion);
                //Erase the suggestions
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        originBar.clearSuggestions();
                    }
                }, 1000);
                originBar.clearSuggestions();
                //Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(originBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("PassengerMapFragment", "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            pickupPoint = latLngOfPlace;    //Update pickupPoint
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, 15));
                            //Move pickupMarker to new location
                            pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupPoint).title("Pick-up Point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            //If destination is not set, move camera to pickup point. Otherwise, show route
                            if (destination == null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, 15));
                            } else {
                                giveDirection(pickupPoint, destination);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("PassengerMapFragment", "Place not found " + e.getMessage());
                            Log.i("PassengerMapFragment", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        //When user presses a button on the destination bar
        destinationBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //open or close a navigation drawer

                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            destinationBar.clearSuggestions();
                            destinationBar.disableSearch();
                        }
                    }, 1000);
                    destinationBar.clearSuggestions();
                }
            }
        });

        //When destination bar content changes
        destinationBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("VN")
                        .setSessionToken(token)
                        .setQuery(charSequence.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionList.add(prediction.getFullText(null).toString());
                                }
                                destinationBar.updateLastSuggestions(suggestionList);
                                if (!destinationBar.isSuggestionsVisible()) {
                                    destinationBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("PassengerMapFragment", "Prediction fetching task unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Clear the marker and destination when the destination bar is empty
                if (destinationBar.getText().equals("")) {
                    destination = null;
                    if (destinationMarker != null) {
                        destinationMarker.remove();
                    }
                }

                //Show booking and advanced button when both bars are filled
                if (!originBar.getText().equals("") && !destinationBar.getText().equals("")) {
                    bookingButton.setVisibility(View.VISIBLE);
                    advancedButton.setVisibility(View.VISIBLE);
                } else {
                    bookingButton.setVisibility(View.INVISIBLE);
                    advancedButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        //When user clicks on a suggestion of destination bar
        destinationBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = destinationBar.getLastSuggestions().get(position).toString();
                destinationBar.setText(suggestion);
                //Erase the suggestions
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        destinationBar.clearSuggestions();
                    }
                }, 1000);
                destinationBar.clearSuggestions();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(destinationBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            destination = latLngOfPlace;    //Update destination
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, 15));
                            //Move destinationMarker to new location
                            destinationMarker = mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
                            //giveDirection(pickupPoint, destination);
                            //If pickup point is not set, move camera to destination. Otherwise, show route
                            if (pickupPoint == null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, 15));
                            } else {
                                giveDirection(pickupPoint, destination);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("PassengerMapFragment", "Place not found " + e.getMessage());
                            Log.i("PassengerMapFragment", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        //When user clicks booking button
        bookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //LatLng pickUpLatLng = new LatLng(pickupPoint.latitude, pickupPoint.longitude);
                //LatLng destinationLatLng = new LatLng(destination.latitude, destination.longitude);
                Calendar calendar = Calendar.getInstance();
                long pickUpTime = calendar.getTimeInMillis() + 900000;
                calendar.setTimeInMillis(pickUpTime);

                if (pickupPoint == null || destination == null) {
                    return;
                }



                //Update booking on Firestore
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //Booking booking = new Booking(HelperClass.LatLng2GeoPoint(pickupPoint), HelperClass.LatLng2GeoPoint(destination), calendar.getTime().toString(), "", "", originBar.getText(), destinationBar.getText(), false, false, user.getUid(), "", "");
                //booking.toFirebase();
                UUID uuid = UUID.randomUUID();

                if (booking == null) {
                    booking = new Booking();
                    booking.setPickUpInGeoPoint(new GeoPoint(pickupPoint.latitude, pickupPoint.longitude));
                    booking.setDestinationInGeoPoint(new GeoPoint(destination.latitude, destination.longitude));
                    booking.setPickUpAddress(originBar.getText());
                    booking.setDestinationAddress(destinationBar.getText());
                    booking.setPickUpTime(calendar.getTime().toString());
                }

                randomUUIDString = uuid.toString();
                booking.setBookerID(FireBaseRef.mAuth.getCurrentUser().getUid());
                booking.setAccepted(false);
                booking.setStatus("pending");
                ((MainActivity)getActivity()).setBooking(booking);  //Update booking object in MainActivity
                //Create a booking on firestore
                db.collection("booking")
                        .document(randomUUIDString).set(booking)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    final DocumentReference docRef = db.collection("booking").document(randomUUIDString);
                                    docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                            @Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w("PassengerMapFragment", "Listen failed.", e);
                                                return;
                                            }

                                            if (snapshot != null && snapshot.exists()) {
                                                Log.d("PassengerMapFragment", "Current data: " + snapshot.getData());
                                                if (snapshot.getString("status").equals("accepted") && statusChange) {
                                                    // TODO: 2020-01-09 make notification works when driver accepts
                                                    //Show notification informing driver has arrived
                                                    Intent intent = new Intent(getActivity(), DriverAcceptedService.class);

                                                    getActivity().startService(intent);

                                                    //Direct passenger to next screen
                                                    statusChange = false;
                                                    Log.d("PassengerMapFragment", "Current data: " + snapshot.getString("status"));
                                                    ((MainActivity)getActivity()).getBooking().setStatus("accepted");
                                                    ((MainActivity)getActivity()).getBooking().setAcceptedDriver(snapshot.getString("acceptedDriver"));
                                                    Log.d("PassengerMapFragment", "acceptedDriver" + snapshot.getString("acceptedDriver"));
                                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                                                    ft.replace(R.id.fragment_container, new PassengerOnRideFragment()).commit();

                                                }
                                            } else {
                                                Log.d("PassengerMapFragment", "Current data: null");
                                            }
                                        }
                                    });
                                }
                            }
                        });

                //Update user information in MainActivity and Firestore
                User currentUser = new User(((MainActivity)getActivity()).getUser());
                currentUser.setRole("passenger");
                currentUser.setCurrentBooking_AsPassenger(randomUUIDString);
                ((MainActivity)getActivity()).setUserInfo(currentUser); //Update user in MainActivity
                db.collection("users")
                        .document(currentUser.getId())
                        .set(currentUser)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("PassengerMapFragment", "User information updated");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("PassengerMapFragment", "User information update failed");
                            }
                        });

                //Set a listener to booking

                bookingButton.setVisibility(View.INVISIBLE);
                advancedButton.setVisibility(View.INVISIBLE);

                //Show booking message
                bookingMessage.setVisibility(View.VISIBLE);
                bookingMessage.bringToFront();
            }
        });

        //When user presses cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookingMessage.setVisibility(View.INVISIBLE);
                bookingButton.setVisibility(View.VISIBLE);
                advancedButton.setVisibility(View.VISIBLE);
//                FireBaseRef.FBUsersRef.document(FireBaseRef.mAuth.getCurrentUser().getUid()).get()
//                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                final String booking_id = task.getResult().getString("currentBooking_AsPassenger");
//                                FireBaseRef.FBBookingRef.document(booking_id).get()
//                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                                Booking booking = task.getResult().toObject(Booking.class);
//                                                booking.setStatus("cancel");
//                                                FireBaseRef.FBBookingRef.document(booking_id).set(booking);
//                                            }
//                                        });
//                            }
//                        });

                WriteBatch batch = FireBaseRef.db.batch();

                batch.update(FireBaseRef.FBUsersRef.document(FireBaseRef.mAuth.getCurrentUser().getUid()), "currentBooking_AsPassenger", "");

                //Delete the booking when user presses cancel
                db.collection("booking").document(randomUUIDString)
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("PassengerMapFragment", "Deleted booking");
                                } else {
                                    Log.w("PassengerMapFragment", "Error deleting booking");
                                }
                            }
                        });

                batch.commit();

                ((MainActivity)getActivity()).deleteBooking();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new PassengerMapFragment()).commit();

            }

        });

        //When user presses advanced button
        advancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Pass data to AdvancedBookingFragment
                Bundle bundle = new Bundle();
                bundle.putDouble("pickupLat", pickupPoint.latitude);
                bundle.putDouble("pickupLon", pickupPoint.longitude);
                bundle.putDouble("destinationLat", destination.latitude);
                bundle.putDouble("destinationLon", destination.longitude);
                bundle.putString("pickupAddress", originBar.getText());
                bundle.putString("destinationAddress", destinationBar.getText());
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                AdvancedBookingFragment fragment = new AdvancedBookingFragment();
                fragment.setArguments(bundle);
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                ft.replace(R.id.fragment_container, fragment).commit();
            }
        });

        return layout;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("debugg", "on map ready");

        //toolbar.bringToFront();
        topNav.bringToFront();
        pickupIcon.bringToFront();
        destinationIcon.bringToFront();



//        //Get data passed from AdvancedBookingFragment
//        if (getArguments() != null) {
//            Toast.makeText(getContext(), "hello", Toast.LENGTH_SHORT).show();
//            pickupPoint = new LatLng(getArguments().getDouble("pickupLat"), getArguments().getDouble("pickupLon"));
//            destination = new LatLng(getArguments().getDouble("destinationLat"), getArguments().getDouble("destinationLon"));
//            originBar.enableSearch();
//            destinationBar.enableSearch();
//            originBar.setText(getArguments().getString("pickupAddress"));
//            destinationBar.setText(getArguments().getString("destinationAddress"));
//        }

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        //Move the my location button to bottom right
        mapView = mapFragment.getView();
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            layoutParams.rightMargin = 64;
            layoutParams.bottomMargin = 250;
        }

        //Check if gps is enabled
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Move the camera to user's current location
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        //Check if there is a pending booking
        if (booking == null) {
            Log.i("debugg", "null");

        } else {
            Log.i("debugg", "not null");
            Log.i("debugg", booking.getStatus());
            if (booking.getStatus().equals("pending")) {
                bookingMessage.setVisibility(View.VISIBLE);
                bookingMessage.bringToFront();
                bookingButton.setVisibility(View.INVISIBLE);
                advancedButton.setVisibility(View.INVISIBLE);
                topNav.setVisibility(View.INVISIBLE);
                originBar.setVisibility(View.INVISIBLE);
                destinationBar.setVisibility(View.INVISIBLE);
                pickupIcon.setVisibility(View.INVISIBLE);
                destinationIcon.setVisibility(View.INVISIBLE);
            }
        }




        //Fill the originBar with the pick-up address
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.newInstance(placeFields);

        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful()){
                    FindCurrentPlaceResponse response = task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Log.i("PassengerMapFragment", String.format("Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                        originBar.setText(placeLikelihood.getPlace().getAddress());
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e("PassengerMapFragment", "Place not found: " + apiException.getStatusCode());
                    }
                }
            }
        });

        //Check if there is a pending booking
        if (booking != null) {
            Log.i("debugg", booking.getStatus());
            if (booking.getStatus().equals("pending")) {
                bookingMessage.setVisibility(View.VISIBLE);
                pickupPoint = new LatLng(booking.getPickUpInGeoPoint().getLatitude(), booking.getPickUpInGeoPoint().getLongitude());
                destination = new LatLng(booking.getDestinationInGeoPoint().getLatitude(), booking.getDestinationInGeoPoint().getLongitude());
                giveDirection(pickupPoint, destination);
                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupPoint).title("Pick-up Point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                destinationMarker = mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
            }
        }

//        LatLng pickupPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//        mMap.addMarker(new MarkerOptions().position(pickupPoint).title("Pick-up Point"));
    }

//    //Gets called when a button on the toolbar is pressed
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            //When user presses up button
//            case android.R.id.home:
//                //Go back to HomeFragment
//                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
//                ft.replace(R.id.fragment_container, new HomeFragment()).commit();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    private void getDeviceLocation() {
        client.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            currentLocation = task.getResult();
                            //If the fetched location is not null, move the camera to the current location
                            if (currentLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
//                                //Add marker at pick-up location
//                                pickupPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupPoint).title("Pick-up Point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            } else {
                                //If the fetched location is null, request and update new location
                                LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        currentLocation = locationResult.getLastLocation();
                                        pickupPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
                                        client.removeLocationUpdates(locationCallback);
                                        //Add marker at pick-up point
                                        pickupPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupPoint).title("Pick-up Point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    }
                                };
                                client.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        } else {
                            //Cannot get location
                            Toast.makeText(getContext(), "Unable to get last known location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Draw the route
    private void giveDirection(LatLng origin, LatLng dest) {
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);
        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        // Then draw the direction in the map
        downloadTask.execute(url);
    }
    private String getDirectionsUrl(LatLng origin, LatLng dest){
        // Starting point
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Setting mode
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + HelperClass.getApiKeyInManifest(getContext());

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    class ParseJSON extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject object;

            List<List<HashMap<String, String>>> routes = null;

            try {
                object = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();

                routes = parser.parse(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            //Create new LatLngBounds to cover all the points
            builder = new LatLngBounds.Builder();

            ArrayList points = new ArrayList();
            PolylineOptions lineOptions = new PolylineOptions();

            for (int i = 0; i < result.size(); i++) {

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);

                    //Add position into LatLngBounds
                    builder.include(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.GREEN);
                lineOptions.geodesic(true);
            }

            // Drawing polyline in the Google Map
            if (points.size() != 0) {
                if (route != null) route.remove();
                route = mMap.addPolyline(lineOptions);

                //Move camera, and put LatLngBounds into center of the map
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            }
        }
    }

}
