package rmit.ad.rmitrides;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Booking implements Serializable {

    @Exclude
    private LatLng pickUp;

    @Exclude
    private LatLng destination;

    private GeoPoint pickUpInGeoPoint;
    private GeoPoint destinationInGeoPoint;
    private GeoPoint driverLocation = new GeoPoint(0, 0);
    private String pickUpTime = "";
    private String description = "";
    private String genderDriver = "";
    private String pickUpAddress = "";
    private String destinationAddress = "";
    private boolean arrived;
    private boolean accepted;
    private String status = "";
    private String bookerID;
    private String acceptedDriver = "";
    private String chatRoom_id = "";

    public GeoPoint getDriverLocation() {
        return driverLocation;
    }

    public void setDriverLocation(GeoPoint driverLocation) {
        this.driverLocation = driverLocation;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public Booking setAccepted(boolean accepted) {
        this.accepted = accepted;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Exclude
    public DocumentReference reference;

    @Exclude
    public DocumentReference getReference() {
        return reference;
    }

    public void setReference(DocumentReference reference) {
        this.reference = reference;
    }

    public GeoPoint getPickUpInGeoPoint() {
        return pickUpInGeoPoint;
    }

    public Booking setPickUpInGeoPoint(GeoPoint pickUpInGeoPoint) {
        this.pickUpInGeoPoint = pickUpInGeoPoint;
        this.pickUp = HelperClass.GeoPoint2LatLng(this.pickUpInGeoPoint);
        return this;
    }

    public GeoPoint getDestinationInGeoPoint() {
        return destinationInGeoPoint;
    }

    public Booking setDestinationInGeoPoint(GeoPoint destinationInGeoPoint) {
        this.destinationInGeoPoint = destinationInGeoPoint;
        this.destination = HelperClass.GeoPoint2LatLng(this.destinationInGeoPoint);
        return this;
    }

    public Booking setAcceptedDriver(String acceptedDriver) {
        this.acceptedDriver = acceptedDriver;
        return this;
    }

    public String getAcceptedDriver() {
        return acceptedDriver;
    }

    public boolean isArrived() {
        return arrived;
    }

    public Booking setArrived(boolean arrived) {
        this.arrived = arrived;
        return this;
    }

    public Booking setPickUpAddress(String pickUpAddress) {
        this.pickUpAddress = pickUpAddress;
        return this;
    }

    public String getPickUpAddress() {
        return pickUpAddress;
    }

    public Booking setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
        return this;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }


    public String getBookerID() {
        return bookerID;
    }

    public Booking setBookerID(String bookerID) {
        this.bookerID = bookerID;
        return this;
    }

    @Exclude
    public LatLng getPickUp() {
        return pickUp;
    }

    public Booking setPickUp(LatLng pickUp) {
        this.pickUp = pickUp;
        this.pickUpInGeoPoint = HelperClass.LatLng2GeoPoint(this.pickUp);
        return this;
    }

    @Exclude
    public LatLng getDestination() {
        return destination;
    }

    public Booking setDestination(LatLng destination) {
        this.destination = destination;
        this.destinationInGeoPoint = HelperClass.LatLng2GeoPoint(this.destination);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Booking setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getGenderDriver() {
        return genderDriver;
    }

    public Booking setGenderDriver(String genderDriver) {
        this.genderDriver = genderDriver;
        return this;
    }

    public Booking() { }

    public Booking(GeoPoint pickUpInGeoPoint, GeoPoint destinationInGeoPoint, String pickUpTime, String description, String genderDriver, String pickUpAddress, String destinationAddress, boolean arrived, boolean accepted, String bookerID, String acceptedDriver, String chatRoom_id) {
        this.pickUpInGeoPoint = pickUpInGeoPoint;
        this.destinationInGeoPoint = destinationInGeoPoint;
        this.pickUpTime = pickUpTime;
        this.description = description;
        this.genderDriver = genderDriver;
        this.pickUpAddress = pickUpAddress;
        this.destinationAddress = destinationAddress;
        this.arrived = arrived;
        this.accepted = accepted;
        this.bookerID = bookerID;
        this.acceptedDriver = acceptedDriver;
        this.chatRoom_id = chatRoom_id;
    }

    public String getPickUpTime() {
        return pickUpTime;
    }

    public Booking setPickUpTime(String pickUpTime) {
        this.pickUpTime = pickUpTime;
        return this;
    }


    public String getChatRoom_id() {
        return chatRoom_id;
    }

    public void setChatRoom_id(String chatRoom_id) {
        this.chatRoom_id = chatRoom_id;
    }

    public Map<String,Object> toFirebase(){
        Map<String, Object> data = new HashMap<>();
        data.put("pickUpAddress", this.pickUpAddress);
        data.put("pickUpLocation", this.pickUpInGeoPoint);
        data.put("pickUpTime",this.pickUpTime);
        data.put("destinationLocation",this.destinationInGeoPoint);
        data.put("destinationAddress", this.destinationAddress);
        data.put("description",this.description);
        data.put("bookerID",this.bookerID);
        data.put("genderDriver",this.genderDriver);
        data.put("acceptedDriver", this.acceptedDriver);
        data.put("arrived", this.arrived);
        data.put("accepted", this.accepted);
        data.put("chat room_id", this.chatRoom_id);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("booking").add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("tag", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                });


        return data;
    }

}
