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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;


public class HistoryFragment_Adapter extends RecyclerView.Adapter {

    Context context;
    List<Booking> data;
    String role;

    public HistoryFragment_Adapter(Context context, List<Booking> data, String role) {
        this.context = context;
        this.data = data;
        this.role = role;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history, parent, false);
        return new HistoryViewHolder(view);
    }

    Booking booking;
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        booking = data.get(position);
        final HistoryViewHolder theHolder = (HistoryViewHolder) holder;
        //theHolder.pickUpLocationTxt.setText(booking.getPickUpAddress() );
        theHolder.destinationTxt.setText(booking.getDestinationAddress());
        theHolder.timeTxt.setText(booking.getPickUpTime());
        if(role.equals("passenger"))
        {
            FireBaseRef.FBUsersRef.document(booking.getBookerID()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {
                                    String  booker_name = document.getString("fullName");

                                    theHolder.passengerTxt.setText(booker_name);
                                } else {
                                    Log.d("LOGGER", "No such document");
                                }
                            } else {
                                Log.d("LOGGER", "get failed with ", task.getException());
                            }
                        }
                    });
        } else {
            FireBaseRef.FBUsersRef.document(booking.getAcceptedDriver()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {
                                    String  booker_name = document.getString("fullName");

                                    theHolder.passengerTxt.setText(booker_name);
                                } else {
                                    Log.d("LOGGER", "No such document");
                                }
                            } else {
                                Log.d("LOGGER", "get failed with ", task.getException());
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


}
class HistoryViewHolder extends RecyclerView.ViewHolder {
    public TextView passengerTxt, timeTxt, pickUpLocationTxt, destinationTxt, descriptiontxt;

    public HistoryViewHolder(@NonNull View view) {
        super(view);
        passengerTxt = view.findViewById(R.id.passengerTxt_history);
        timeTxt = view.findViewById(R.id.timeTxt_history);
        descriptiontxt = view.findViewById(R.id.description_history);
        pickUpLocationTxt = view.findViewById(R.id.pickUpLocation_history);
        destinationTxt = view.findViewById(R.id.destinationLocation_history);
    }
}