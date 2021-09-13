package rmit.ad.rmitrides;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class DriverFragment_Adapter extends RecyclerView.Adapter {
    Context context, theContext;
    List<Booking> data;
    public DriverFragment_Adapter(Context context, List<Booking> list) {
        this.context = context;
        data = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_driver_item, parent, false);
        return new RequestsViewHolder(view);
    }

    Booking booking;
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        booking = data.get(position);
        final RequestsViewHolder theHolder = (RequestsViewHolder) holder;
        theHolder.pickUpLocationTxt.setText(booking.getPickUpAddress() );
        theHolder.destinationTxt.setText(booking.getDestinationAddress());
        FireBaseRef.FBUsersRef.document(booking.getBookerID()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                String  booker_name = document.getString("username");

                                theHolder.passengerTxt.setText(booker_name);
                            } else {
                                Log.d("LOGGER", "No such document");
                            }
                        } else {
                            Log.d("LOGGER", "get failed with ", task.getException());
                        }
                    }
                });
        theHolder.timeTxt.setText(booking.getPickUpTime());
        theHolder.fast_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get booking from firestore
                String path = data.get(position).reference.getPath();
                FireBaseRef.db.document(path).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                booking = task.getResult().toObject(Booking.class);

                            }
                        });
                //Update booking status
                booking.setAccepted(true);
                booking.setStatus("accepted");
                //Update driver's id to booking
                booking.setAcceptedDriver(FireBaseRef.mAuth.getCurrentUser().getUid());

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(FirebaseAuth.getInstance().getUid())
                        .update("currentBooking_AsDriver", data.get(position).reference.getId())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("Driver Adapter", "Updated driver user");
                                } else {
                                    Log.w("Driver Adapter", "Update failed");
                                }
                            }
                        });

                //Update booking object in MainActivity
                ((MainActivity)context).setBooking(booking);
                //Direct driver to the map
                FragmentTransaction ft = ((MainActivity)context).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new DriverOnRideFragment()).commit();

                FireBaseRef.db.document(path).set(booking); //Update booking to firestore
                Log.d("update","success");
            }
        });

//        theHolder.item.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int selectedPosition = position;
//                Toast.makeText(context, selectedPosition + " " + data.get(selectedPosition).getReference().getPath(), Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(context, Activity_AcceptRequest.class);
//                intent.putExtra("FirebaseRef", data.get(selectedPosition).reference.getPath());
//                context.startActivity(intent);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}

class RequestsViewHolder extends RecyclerView.ViewHolder{
    public TextView passengerTxt, timeTxt, pickUpLocationTxt, destinationTxt;
    public LinearLayout item;
    public Button fast_accept;

    public RequestsViewHolder(@NonNull View view) {
        super(view);
        item = view.findViewById(R.id.item);
        passengerTxt = view.findViewById(R.id.passengerTxt);
        timeTxt = view.findViewById(R.id.timeTxt);
        pickUpLocationTxt = view.findViewById(R.id.pickUpLocation);
        destinationTxt = view.findViewById(R.id.destinationLocation);
        fast_accept = view.findViewById(R.id.fast_accept);
    }
}
