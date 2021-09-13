package rmit.ad.rmitrides;


import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class DriverOnRideFragment extends Fragment implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private View mapView;
    private Location currentLocation;
    private LatLng pickupPoint = null;
    private LatLng destination = null;
    private Marker pickupMarker;
    private Marker destinationMarker;
    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;

    //Needed to draw route
    private LatLngBounds.Builder builder;
    private Polyline route;

    private Booking booking;
    private String bookingId = "";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FloatingActionButton chatButton;

    private Button cancel,finish;

    public DriverOnRideFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_driver_on_ride, container, false);

        //Initialize views
        chatButton = layout.findViewById(R.id.chat_button);
        cancel = layout.findViewById(R.id.btCancel);
        finish = layout.findViewById(R.id.btFinish);

        //Get the booking from firestore
        booking = ((MainActivity)getActivity()).getBooking();
        db.collection("booking")
                .whereEqualTo("acceptedDriver", FirebaseAuth.getInstance().getUid())
                .whereEqualTo("status", "accepted")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                booking = document.toObject(Booking.class);
                                bookingId = document.getId();   //Save booking id for later use
                                Log.d("booking id ", bookingId);


                                final DocumentReference docRef = db.collection("booking").document(bookingId);
                                docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w("DriverOnRideFragment", "Listen failed.", e);
                                            return;
                                        }

                                        if (documentSnapshot != null && documentSnapshot.exists()) {
                                            Log.d("DriverOnRideFragment", "Current data: " + documentSnapshot.getData());
                                            if (documentSnapshot.get("status").equals("cancelled")) {

//                                                ((MainActivity)getActivity()).deleteBooking();

                                                db.collection("users")
                                                        .document(FirebaseAuth.getInstance().getUid())
                                                        .update("currentBooking_AsDriver", null)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                ((MainActivity)getActivity()).deleteBooking();
                                                                delete();
                                                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                                                                ft.replace(R.id.fragment_container, new DriverFragment()).commit();
                                                            }
                                                        });

                                            } else if (documentSnapshot.get("status").equals("finished")) {
                                                //((MainActivity)getActivity()).deleteBooking();
                                                db.collection("users")
                                                        .document(FirebaseAuth.getInstance().getUid())
                                                        .update("currentBooking_AsDriver", null)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                ((MainActivity)getActivity()).deleteBooking();
                                                                delete();
                                                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                                                                ft.replace(R.id.fragment_container, new DriverFragment()).commit();
                                                            }
                                                        });

                                            }
                                        } else {
                                            Log.d("DriverOnRideFragment", "Current data: null");
                                        }
                                    }
                                });

                            }
                        }
                    }
                });

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                //CommunicationFragment fragment = new CommunicationFragment();
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                ft.replace(R.id.fragment_container, new MyMessageFragment()).commit();

            }
        });

        //When passenger wants to cancel the ride
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Confirm if passenger wants to cancel the trip
                new AlertDialog.Builder(getContext())
                        .setTitle("Cancel trip")
                        .setMessage("Would you like to cancel this trip?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.collection("users")
                                        .document(FirebaseAuth.getInstance().getUid())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    //Update booking's status to cancelled on firestore
                                                    User user = task.getResult().toObject(User.class);
                                                    db.collection("booking")
                                                            .document(user.getCurrentBooking_AsDriver())
                                                            .update("status", "cancelled")
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    //Set booking in MainActivity to null
                                                                    ((MainActivity)getActivity()).deleteBooking();

                                                                    //Set user's field
                                                                    db.collection("users")
                                                                            .document(FirebaseAuth.getInstance().getUid())
                                                                            .update("currentBooking_AsDriver", null)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    //Go back to PassengerMapFragment
                                                                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                                                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                                                                                    ft.replace(R.id.fragment_container, new DriverOnRideFragment()).commit();
                                                                                }
                                                                            });
                                                                }
                                                            });




                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });

        //When passenger wants to finish the ride
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Confirm if passenger wants to cancel the trip
                new AlertDialog.Builder(getContext())
                        .setTitle("Cancel trip")
                        .setMessage("Finish this trip?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.collection("users")
                                        .document(FirebaseAuth.getInstance().getUid())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    //Update booking's status to cancelled on firestore
                                                    User user = task.getResult().toObject(User.class);
                                                    db.collection("booking")
                                                            .document(user.getCurrentBooking_AsDriver())
                                                            .update("status", "finished");
                                                    //Set booking in MainActivity to null
                                                    ((MainActivity)getActivity()).deleteBooking();
                                                    //Set user's field
                                                    db.collection("users")
                                                            .document(FirebaseAuth.getInstance().getUid())
                                                            .update("currentBooking_AsPassenger", null);
                                                    //Go back to PassengerMapFragment
                                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                                                    ft.replace(R.id.fragment_container, new PassengerMapFragment()).commit();
                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });

        //Get the map
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.driver_on_ride_map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        client = LocationServices.getFusedLocationProviderClient(getActivity());
        Places.initialize(getActivity(), "AIzaSyAzsPauXs_9PJa6vKkBnu8s_yrT_cECuNA");

        return layout;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateDriverLocation();
                        }
                    }, 1000);
                }
            });

            if (booking != null) {
                if (booking.getStatus().equals("accepted")) {
                    pickupPoint = new LatLng(booking.getPickUpInGeoPoint().getLatitude(), booking.getPickUpInGeoPoint().getLongitude());
                    destination = new LatLng(booking.getDestinationInGeoPoint().getLatitude(), booking.getDestinationInGeoPoint().getLongitude());
                    giveDirection(pickupPoint, destination);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupPoint).title("Pick-up Point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    destinationMarker = mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
                }
            }

        }


//        //Listen when passenger cancels the trip
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                final DocumentReference docRef = db.collection("booking").document(bookingId);
//                docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                        if (e != null) {
//                            Log.w("DriverOnRideFragment", "Listen failed.", e);
//                            return;
//                        }
//
//                        if (documentSnapshot != null && documentSnapshot.exists()) {
//                            Log.d("DriverOnRideFragment", "Current data: " + documentSnapshot.getData());
//                            if (documentSnapshot.get("status").equals("cancelled")) {
//                                getActivity()
//                                ((MainActivity)getActivity()).deleteBooking();
//
//                                db.collection("users")
//                                        .document(FirebaseAuth.getInstance().getUid())
//                                        .update("currentBooking_AsDriver", null);
//                                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
//                                ft.replace(R.id.fragment_container, new DriverFragment()).commit();
//                            } else if (documentSnapshot.get("status").equals("finished")) {
//                                //((MainActivity)getActivity()).deleteBooking();
//                                db.collection("users")
//                                        .document(FirebaseAuth.getInstance().getUid())
//                                        .update("currentBooking_AsDriver", null);
//                                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
//                                ft.replace(R.id.fragment_container, new DriverFragment()).commit();
//                            }
//                        } else {
//                            Log.d("DriverOnRideFragment", "Current data: null");
//                        }
//                    }
//                });
//            }
//        }, 2000);



    }

    private void getDeviceLocation() {
        Toast.makeText(getContext(), "hello", Toast.LENGTH_SHORT).show();
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
                                        Log.i("Location update", "hello");
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

    private void updateDriverLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
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
                Log.i("Location update", currentLocation.toString());


                //Update driver location
                //DEBUGGING
                Log.i("driver location", booking.getDriverLocation().toString());
                //booking.setDriverLocation(new GeoPoint(booking.getDriverLocation().getLatitude() + 1, booking.getDriverLocation().getLongitude() + 1));
                booking.setDriverLocation(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                //Update driver location on firestore
                db.collection("booking")
                        .document(bookingId)
                        .update("driverLocation", booking.getDriverLocation())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i("hello", booking.getDriverLocation().toString());
                                } else {

                                }
                            }
                        });
            }
        };
        client.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    //Draw the route
    private void giveDirection(LatLng origin, LatLng dest) {
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);
        DriverOnRideFragment.DownloadTask downloadTask = new DriverOnRideFragment.DownloadTask();

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
            DriverOnRideFragment.ParserTask parserTask = new DriverOnRideFragment.ParserTask();
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

    //Go back to previous screen
    private void delete() {
        ((MainActivity)getActivity()).deleteBooking();
    }
}
